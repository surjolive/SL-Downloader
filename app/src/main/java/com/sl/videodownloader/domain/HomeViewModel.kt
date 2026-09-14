package com.sl.videodownloader.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sl.videodownloader.data.database.DownloadEntity
import com.sl.videodownloader.data.database.DownloadStatus
import com.sl.videodownloader.data.repository.DownloadRepository
import com.sl.videodownloader.data.repository.DownloadException
import com.sl.videodownloader.util.UrlValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: DownloadRepository) : ViewModel() {
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _items = MutableStateFlow<List<DownloadEntity>>(emptyList())
    val items: StateFlow<List<DownloadEntity>> = _items.asStateFlow()

    init {
        repository.downloads.onEach { _items.value = it }
            .catch { _message.value = "Unable to read download history" }
            .launchIn(viewModelScope)
    }

    fun setUrl(value: String) { _url.value = value; _message.value = null }
    fun clearUrl() { setUrl("") }
    fun validate(): Boolean {
        val valid = UrlValidator.isAuthorizedMediaUrl(_url.value)
        if (!valid) _message.value = "Enter a valid HTTP or HTTPS direct video URL."
        return valid
    }
    fun addDownload() {
        if (!validate()) return
        viewModelScope.launch {
            runCatching {
                val id = repository.enqueue(_url.value.trim())
                val item = repository.get(id) ?: error("Unable to queue download")
                repository.download(item) { downloaded, total ->
                    val current = _items.value.firstOrNull { it.id == id } ?: return@download
                    val updated = current.copy(downloadedBytes = downloaded, totalBytes = total, status = DownloadStatus.DOWNLOADING)
                    _items.value = _items.value.map { if (it.id == id) updated else it }
                }
            }.onFailure { error ->
                _message.value = when (error) {
                    is DownloadException -> error.message ?: "Download failed."
                    else -> "Network connection failed. Check your connection and try again."
                }
            }
        }
    }
}
