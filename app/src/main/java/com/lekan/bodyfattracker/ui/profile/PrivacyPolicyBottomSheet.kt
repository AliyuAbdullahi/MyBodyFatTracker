package com.lekan.bodyfattracker.ui.profile

import androidx.annotation.StringRes // Added
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R // Assuming R class is accessible for string resources
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyPolicyBottomSheet(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val currentDate = remember {
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_policy_title), // Changed
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_button_desc)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_policy_last_updated, currentDate), // Changed
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        PrivacyPolicySectionTitle(R.string.privacy_policy_intro_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_intro_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_info_collect_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_info_collect_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_how_use_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_how_use_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_storage_security_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_storage_security_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_sharing_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_sharing_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_rights_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_rights_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_children_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_children_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_changes_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_changes_content) // Changed

                        PrivacyPolicySectionTitle(R.string.privacy_policy_contact_title) // Changed
                        PrivacyPolicyTextContent(R.string.privacy_policy_contact_content) // Changed

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicySectionTitle(@StringRes titleResId: Int) { // Changed parameter to @StringRes
    Text(
        text = stringResource(id = titleResId), // Changed to use stringResource
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun PrivacyPolicyTextContent(@StringRes contentResId: Int) { // Changed parameter to @StringRes
    Text(
        text = stringResource(id = contentResId), // Changed to use stringResource
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Preview(showBackground = true, name = "Privacy Policy Bottom Sheet Preview")
@Composable
fun PrivacyPolicyBottomSheetPreview() {
    BodyFatTrackerTheme {
        Box(Modifier.fillMaxSize()) {
            PrivacyPolicyBottomSheet(onDismiss = {})
        }
    }
}
