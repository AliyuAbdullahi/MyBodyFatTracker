import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme

@Composable
fun InfoRow(
    text: String,
    subtitle: String,
    circleColor: Color,
    onRowClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)) // Apply corner radius to the Row
            .background(MaterialTheme.colorScheme.surfaceVariant) // Example background color
            .clickable { onRowClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle to the left
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(circleColor)
        )

        Spacer(modifier = Modifier.width(16.dp)) // Space between circle and column

        // Column with text and button
        Column(
            modifier = Modifier.weight(1f), // Column takes remaining space
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp)) // Space between text and button
            Row {
//                Spacer(modifier = Modifier.weight(1F))
                Button(
                    onClick = {
                        // Important: Stop propagation to prevent Row's onClick from being triggered
                        // when the button is clicked.
                        onMoreInfoClick()
                    },
                    shape = RoundedCornerShape(12.dp), // Optional: Rounded corners for the button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.more_info))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInfoRow() {
    BodyFatTrackerTheme { // Ensure MaterialTheme is applied for previews
        InfoRow(
            text = "This is some important information that you should read.",
            circleColor = Color.Magenta,
            subtitle = "3.5% accuracy",
            onRowClick = { /* TODO: Handle row click */ },
            onMoreInfoClick = { /* TODO: Handle more info click */ }
        )
    }
}
