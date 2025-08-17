package com.lekan.bodyfattracker.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R

@Composable
fun EmptyProfileView(onCreateProfileClicked: () -> Unit, onAboutClickedAppClicked: () -> Unit) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_profile_yet_message),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Companion.Center
        )
        Spacer(modifier = Modifier.Companion.height(24.dp))
        Button(onClick = onCreateProfileClicked) {
            Text(stringResource(R.string.create_profile_button))
        }
        AboutAppButton(onAboutClickedAppClicked  = onAboutClickedAppClicked)
    }
}

