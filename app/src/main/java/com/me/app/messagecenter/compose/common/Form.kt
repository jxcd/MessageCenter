package com.me.app.messagecenter.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InputText(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    width: Dp = 400.dp,
    height: Dp = TextFieldDefaults.MinHeight,
) {
    InputValue(
        label = label, value = value, enabled = enabled,
        onValueChange = { onValueChange(it) },
        trailingIcon = trailingIcon,
        width = width,
        height = height,
    )
}

/**
 * 文字输入框, 可以提交该变更
 */
@Composable
fun InputTextWithSubmit(
    label: String,
    canSubmit: (String) -> Boolean = { true },
    onSubmit: (String) -> Unit,
    defaultValue: String = "",
    enabled: Boolean = true,
    height: Dp = TextFieldDefaults.MinHeight,
) {
    var value by remember { mutableStateOf(defaultValue) }

    val trailingIcon = @Composable {
        if (canSubmit(value)) {
            IconButton(onClick = { onSubmit(value) }) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "submit",
                    // tint = ColorGreen,
                )
            }
        }
    }

    InputValue(
        label = label, value = value, enabled = enabled,
        onValueChange = { value = it },
        trailingIcon = trailingIcon,
        height = height,
    )
}

/**
 * 数字输入框
 */
@Composable
fun InputNumber(
    label: String,
    value: Int?,
    range: IntRange = 0..Int.MAX_VALUE,
    tips: String = "",
    onValueChange: (Int?) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    width: Dp = 400.dp,
    height: Dp = TextFieldDefaults.MinHeight,
) {
    val rangeCheck = { s: String ->
        val i = s.toIntOrNull()
        (s.isBlank()) || (i != null) && (i in range)
    }

    InputValue(
        label = label,
        value = value?.toString() ?: "",
        onValueChange = { onValueChange(it.toIntOrNull()) },
        rangeCheck = rangeCheck,
        tips = tips,
        trailingIcon = trailingIcon,
        enabled = enabled,
        width = width,
        height = height,
    )
}

/**
 * 数字输入框(浮点数)
 */
@Composable
fun InputDouble(
    label: String,
    value: Double?,
    min: Double = 0.0,
    max: Double = Double.MAX_VALUE,
    tips: String = "",
    onValueChange: (Double?) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val rangeCheck = { s: String ->
        val i = s.toDoubleOrNull()
        (s.isBlank()) || (i != null) && (i >= min) && (i <= max)
    }

    InputValue(
        label = label,
        value = value?.toString() ?: "",
        onValueChange = { onValueChange(it.toDoubleOrNull()) },
        rangeCheck = rangeCheck,
        tips = tips,
        trailingIcon = trailingIcon,
    )
}

/**
 * 值输入框
 */
@Composable
fun InputValue(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    // 值范围检测
    rangeCheck: (String) -> Boolean = { true },
    // 值范围提示
    tips: String = "",
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    width: Dp = TextFieldDefaults.MinWidth,
    height: Dp = TextFieldDefaults.MinHeight,
) {
    var error by remember { mutableStateOf(false) }

    val change: (String) -> Unit = {
        val valid = it.isBlank() || rangeCheck(it)
        error = !valid
        if (valid) {
            onValueChange(it)
        }
    }

    OutlinedTextField(
        value = value ?: "",
        onValueChange = change,
        label = { Text(label) },
        maxLines = 1,
        // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error,
        enabled = enabled,
        trailingIcon = trailingIcon,
        modifier = Modifier.requiredSize(width, height),
        // placeholder = { Text("请输入 $label${if (tips != "") ", $tips" else ""}") }
    )
}

/**
 * 下拉选择器
 */
@Composable
fun SelectOptions(
    label: String = "",
    value: String,
    options: Collection<String>,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    buttonModifier: Modifier = Modifier,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    listModifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box() {
        Button(
            onClick = { expanded = !expanded },
            modifier = buttonModifier,
            enabled = enabled,
            colors = buttonColors
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${if (label.isBlank()) "" else "$label: "}$value")
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "展开/收起",
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = listModifier
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        expanded = false
                        onValueChange(it)
                    },
                )
            }
        }
    }
}


