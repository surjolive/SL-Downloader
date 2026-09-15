package com.sl.videodownloader

import android.app.Application
import android.util.Log
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class SLDownloaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
            Aria2c.getInstance().init(this)
        } catch (error: YoutubeDLException) {
            Log.e(TAG, "Unable to initialize media download engine", error)
        }
    }

    companion object {
        private const val TAG = "SLDownloaderApp"
    }
}