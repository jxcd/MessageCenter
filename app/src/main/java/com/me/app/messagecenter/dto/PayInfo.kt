package com.me.app.messagecenter.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class PayInfo(
    // 在哪里
    val place: String,
    // 什么时候
    val time: LocalDateTime,
    // 消费/收入了多少
    val money: BigDecimal,
    // 原始信息
    val information: String,
    // 信息来源
    val source: String,
)