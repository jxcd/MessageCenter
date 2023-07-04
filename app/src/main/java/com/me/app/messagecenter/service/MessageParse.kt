package com.me.app.messagecenter.service

/**
 * 信息解析接口
 */
interface MessageParse<T> {

    fun parse(message: String): T?


}