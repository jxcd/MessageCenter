package com.me.app.messagecenter.service

import android.content.ContentResolver
import android.net.Uri

/**
 * 定义从短信读取内容并解析的行为
 */
interface SmsReader {

    companion object {
        // 查询收件箱，可以替换为其他类型的 Uri
        val smsUri: Uri = Uri.parse("content://sms/inbox")
    }


    fun readFromSms(contentResolver: ContentResolver)
}