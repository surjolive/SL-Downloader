package com.sl.videodownloader.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class DownloadConverters {
    @TypeConverter fun fromStatus(status: DownloadStatus): String = status.name
    @TypeConverter fun toStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}

@Database(entities = [DownloadEntity::class], version = 1, exportSchema = false)
@TypeConverters(DownloadConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
