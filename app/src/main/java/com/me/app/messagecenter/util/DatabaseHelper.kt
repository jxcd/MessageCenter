package com.me.app.messagecenter.util

import androidx.room.Database
import androidx.room.RoomDatabase
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.dto.PayInfoDao

lateinit var db: AppDatabase

@Database(entities = [PayInfo::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payInfoDao(): PayInfoDao
}
