package com.me.app.messagecenter.dto

import androidx.room.*
import kotlinx.coroutines.flow.Flow

private const val tableName = "pay_info"

@Entity(tableName = tableName)
data class PayInfo(
    // 在哪里
    val platform: String = "",
    val place: String = "",
    // 什么时候, 2007-12-03T10:15:30
    var time: String = "1970-01-01T00:00",
    // 消费/收入了多少
    val revenue: Boolean = false,
    val money: String = "",
    // 余额
    val balance: String = "",
    // 原始信息
    val information: String = "",
    // 信息来源
    val source: String = "",
    // 时间戳
    var timestamp: Long = 0L,
    // 忽略统计
    var ignoreStatistics: Boolean = false,
    // 备注
    var remark: String = "",
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}

@Dao
interface PayInfoDao {
    @Query("SELECT * FROM $tableName ORDER BY timestamp desc")
    fun flow(): Flow<List<PayInfo>>

    @Query("SELECT * FROM $tableName ORDER BY timestamp desc")
    suspend fun selectAll(): List<PayInfo>

    @Insert
    suspend fun insert(vararg payInfo: PayInfo)

    @Query("DELETE FROM $tableName")
    suspend fun deleteAll()

    @Query("UPDATE $tableName SET ignoreStatistics = :ignore WHERE id = :id")
    suspend fun toggleIgnoreStatistics(id: Int, ignore: Boolean)

    @Query("UPDATE $tableName SET remark = :remark WHERE id = :id")
    suspend fun updateRemark(id: Int, remark: String)
}
