package com.lekan.bodyfattracker.ui.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.lekan.bodyfattracker.R

@Composable
fun FeedbackDialog(
    showDialog: Boolean,
    feedbackText: String,
    onFeedbackTextChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSendFeedback: () -> Unit,
    isSending: Boolean // To show loading state on the send button
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.leave_feedback)) },
            text = {
                Column {
                    Text(stringResource(R.string.we_d_love_to_hear_your_thoughts_or_suggestions))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = onFeedbackTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.type_your_feedback_here)) },
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onSendFeedback,
                    enabled = feedbackText.isNotBlank() && !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.send))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false) // Optional: prevent dismissal on outside click
        )
    }
}
