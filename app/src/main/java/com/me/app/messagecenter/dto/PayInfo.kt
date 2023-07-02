package com.me.app.messagecenter.dto

import androidx.room.*
import java.math.BigDecimal
import java.time.LocalDateTime

private const val tableName = "pay_info"

@Entity(tableName = tableName)
data class PayInfo(
    // 在哪里
    val place: String = "",
    // 什么时候, 2007-12-03T10:15:30
    var time: String = "1970-01-01T00:00",
    // 消费/收入了多少
    val money: String = "",
    // 余额
    val balance: String = "",
    // 原始信息
    val information: String = "",
    // 信息来源
    val source: String = "",
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}

@Dao
interface PayInfoDao {
    @Query("SELECT * FROM $tableName")
    suspend fun selectAll(): List<PayInfo>

    @Insert
    suspend fun insert(vararg payInfo: PayInfo)

    @Delete
    suspend fun delete(payInfo: PayInfo)

    @Query("DELETE FROM $tableName")
    suspend fun deleteAll()

}