package com.me.app.messagecenter.dto

import androidx.room.*
import java.time.LocalDate

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
    val timestamp: Long = 0L,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}

@Dao
interface PayInfoDao {
    @Query("SELECT * FROM $tableName ORDER BY timestamp desc")
    suspend fun selectAll(): List<PayInfo>

    @Insert
    suspend fun insert(vararg payInfo: PayInfo)

    @Delete
    suspend fun delete(payInfo: PayInfo)

    @Query("DELETE FROM $tableName")
    suspend fun deleteAll()

}

// 交通银行短信提醒
private val pattern =
    """(\d{2}月\d{2}日\d{2}:\d{2}).*?(.+?)(\d+\.\d+)元.*?余额为(\d+\.\d+)元""".toRegex()
private val platformKey = listOf("支付宝", "（特约）美团消费", "京东支付", "拼多多支付", "抖音支付")
private val revenueKey = listOf("转入", "退费", "退款", "退货")
val payInfoFromBmcSms: (String, Long) -> PayInfo? = { message, timestamp ->
    pattern.find(message)?.groupValues?.let {
        val where = it[2]
        val platform = platformKey.find { key -> where.contains(key) }.orEmpty()
        val revenue = revenueKey.find { key -> where.contains(key) } != null

        PayInfo(
            platform = platform,
            place = it[2],
            // time = LocalDateTime.parse("${LocalDate.now().year}年${it[1]}", formatter),
            time = "${LocalDate.now().year}-${it[1].replace("月", "-").replace("日", "T")}",
            revenue = revenue,
            money = it[3],
            balance = it[4],
            information = message,
            source = "SMS: 95559",
            timestamp = timestamp
        )
    }
}