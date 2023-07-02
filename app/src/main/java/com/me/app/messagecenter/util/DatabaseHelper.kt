package com.me.app.messagecenter.util

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.dto.PayInfoDao
import java.math.BigDecimal

lateinit var db: AppDatabase

@Database(entities = [PayInfo::class], version = 1, exportSchema = false)
//@TypeConverters(BigDecimalTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payInfoDao(): PayInfoDao
}

class BigDecimalTypeConverter {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.toBigDecimal()
    }
}