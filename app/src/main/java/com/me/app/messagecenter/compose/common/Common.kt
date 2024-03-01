package com.me.app.messagecenter.compose.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.me.app.messagecenter.R

@Composable
fun SimpleDialog(
    title: String,
    text: String,
    icon: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        icon = icon,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.dialog_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
        onDismissRequest = onDismiss,
    )
}