package com.me.app.messagecenter.util

import androidx.activity.result.ActivityResultLauncher
import java.util.concurrent.ConcurrentHashMap

/**
 * 由于 launcher 只能在 ComponentActivity 中声明, 不方便调用方引用使用, 所以这里将其维护到map中
 * 维护 权限请求关键字 和 launcher 的关系, 方便在用的地方直接拉取效用
 */
val requestPermissionLauncher = ConcurrentHashMap<String, ActivityResultLauncher<String>>()

/**
 * 维护 权限请求关键字 和 callback 的关系, 方便在用的地方动态设置callback
 */
val requestPermissionCallback = ConcurrentHashMap<String, (Boolean) -> Unit>()