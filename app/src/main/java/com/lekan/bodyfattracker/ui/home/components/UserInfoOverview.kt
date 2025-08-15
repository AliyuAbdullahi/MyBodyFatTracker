package com.lekan.bodyfattracker.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R
import androidx.compose.ui.unit.sp
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import com.lekan.bodyfattracker.ui.theme.Grey500
import com.lekan.bodyfattracker.ui.theme.Grey800
import com.lekan.bodyfattracker.ui.theme.PadGrey

@Composable
fun UserInfoOverview(
    modifier: Modifier = Modifier,
    bodyFat: String = "15%",
    labelColor: Color = Color.Green,
    circleColor: Color = Grey500,
    date: String = "2023-09-10 12:00:00"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp) // Add padding around the Column
            .clip(RoundedCornerShape(16.dp)) // Apply rounded corners
            .background(PadGrey) // Set background color
            .padding(16.dp) // Add padding inside the Column
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            Column {
                Text(text = stringResource(R.string.body_fat_label), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Column {
                    Text(text = date, fontSize = 12.sp, color = Color.Black.copy(alpha = 0.9f))
                }
            }
            CircleWithContent(
                modifier = Modifier.size(100.dp),
                circleBackgroundColor = circleColor,
            ) {
                Text(
                    text = bodyFat,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
           // Replace with actual body fat data
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BodyFatTrackerTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                UserInfoOverview()
            }
        }
    }
}
