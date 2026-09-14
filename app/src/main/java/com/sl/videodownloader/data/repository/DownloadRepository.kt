package com.sl.videodownloader.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.os.Build
import android.provider.MediaStore
import com.sl.videodownloader.data.database.DownloadDao
import com.sl.videodownloader.data.database.DownloadEntity
import com.sl.videodownloader.data.database.DownloadStatus
import com.sl.videodownloader.util.safeFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.io.IOException
import java.io.File

class DownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

class DownloadRepository(private val context: Context, private val dao: DownloadDao) {
    val downloads: Flow<List<DownloadEntity>> = dao.observeAll()

    suspend fun get(id: Long): DownloadEntity? = dao.getById(id)

    suspend fun enqueue(url: String): Long = withContext(Dispatchers.IO) {
        val guessedName = URL(url).path.substringAfterLast('/').ifBlank { "video.mp4" }.safeFileName()
        dao.insert(DownloadEntity(url = url, fileName = guessedName))
    }

    suspend fun download(item: DownloadEntity, onProgress: suspend (Long, Long) -> Unit) {
        withContext(Dispatchers.IO) {
            var lastError: Throwable? = null
            repeat(MAX_ATTEMPTS) { attempt ->
                try {
                    downloadOnce(item, onProgress)
                    return@withContext
                } catch (error: DownloadException) {
                    lastError = error
                    if (!error.message.orEmpty().startsWith("Temporary")) throw error
                    if (attempt < MAX_ATTEMPTS - 1) Thread.sleep(RETRY_DELAYS_MS[attempt])
                } catch (error: SocketTimeoutException) {
                    lastError = DownloadException("Temporary network timeout", error)
                    if (attempt < MAX_ATTEMPTS - 1) Thread.sleep(RETRY_DELAYS_MS[attempt])
                } catch (error: ConnectException) {
                    lastError = DownloadException("Temporary network connection failure", error)
                    if (attempt < MAX_ATTEMPTS - 1) Thread.sleep(RETRY_DELAYS_MS[attempt])
                }
            }
            throw (lastError ?: DownloadException("Network error"))
        }
    }

    private suspend fun downloadOnce(item: DownloadEntity, onProgress: suspend (Long, Long) -> Unit) {
        val connection = (URL(item.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", "video/*,application/octet-stream;q=0.9")
            setRequestProperty("User-Agent", "SLVideoDownloader/1.0")
        }
        try {
            connection.connect()
            when (val code = connection.responseCode) {
                in 200..299 -> Unit
                in 500..599 -> throw DownloadException("Temporary server error ($code)")
                401, 403 -> throw DownloadException("This file requires permission to download.")
                404 -> throw DownloadException("The video file was not found.")
                429 -> throw DownloadException("The server is busy. Try again later.")
                else -> throw DownloadException("The server rejected this download ($code).")
            }
            val contentType = connection.contentType?.substringBefore(';')?.lowercase()
            val hasVideoExtension = listOf(".mp4", ".webm", ".mov", ".m4v", ".ogv")
                .any { item.url.substringBefore('?').lowercase().endsWith(it) }
            if (contentType != null && !contentType.startsWith("video/") && contentType != "application/octet-stream" && !hasVideoExtension) {
                throw DownloadException("This URL does not point to a direct video file.")
            }
            val total = connection.contentLengthLong
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, item.fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, item.mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SL Downloader")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    } else {
                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                            "SL Downloader"
                        ).apply { mkdirs() }
                        put(MediaStore.Video.Media.DATA, File(directory, item.fileName).absolutePath)
                    }
                }
            val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Unable to create destination file")
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            onProgress(downloaded, total)
                        }
                    }
                } ?: error("Unable to open destination file")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.update(uri, ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }, null, null)
                }
                dao.update(item.copy(fileUri = uri.toString(), totalBytes = total, downloadedBytes = total, status = DownloadStatus.COMPLETED, completedAt = System.currentTimeMillis()))
            } catch (error: Throwable) {
                context.contentResolver.delete(uri, null, null)
                throw error
            }
        } catch (error: DownloadException) {
            dao.update(item.copy(status = DownloadStatus.FAILED, errorMessage = error.message))
            throw error
        } catch (error: IOException) {
            dao.update(item.copy(status = DownloadStatus.FAILED, errorMessage = "Network connection failed"))
            throw error
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private val RETRY_DELAYS_MS = longArrayOf(1_000, 3_000, 5_000)
    }
}
