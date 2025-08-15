package com.lekan.bodyfattracker.ui.components // Adjust package as needed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border // Import for Column border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Still useful for clipping content within the Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp // Not strictly needed in this snippet if not used directly
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import com.lekan.bodyfattracker.ui.theme.Grey700


@Composable
fun MeasurementInputRow(
    @DrawableRes imageResId: Int,
    imageContentDescription: String?,
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onActionDone: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFieldUnfocusedBorderColor = Grey700

    Row(
        // The modifier for the Row itself might not need fillMaxWidth()
        // if the parent Column controls width and padding
        modifier = modifier
            .fillMaxWidth() // Row should still fill width within its parent's padding
            // .padding(horizontal = 16.dp, vertical = 12.dp), // Padding moved to Column or applied differently
            .padding(vertical = 12.dp), // Keep vertical padding for Row content, horizontal from Column
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = imageContentDescription,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.FillBounds
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onActionDone()
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = textFieldUnfocusedBorderColor,
            )
        )
    }
}


@Composable
fun MeasurementInputRowWithInfo(
    @DrawableRes imageResId: Int,
    imageContentDescription: String?,
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onActionDone: () -> Unit,
    instructionalText: String,
    modifier: Modifier = Modifier, // This modifier will be applied to the Column
    keyboardType: KeyboardType = KeyboardType.Number
) {
    val columnBorderColor = Grey700 // Or your preferred border color

    Column(
        modifier = modifier // Apply the passed-in modifier first
            .fillMaxWidth() // Column takes full width
            .clip(RoundedCornerShape(16.dp)) // Clip the content to the rounded shape of the border
            .border(
                width = 1.dp,
                color = columnBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp) // Padding inside the border
    ) {
        MeasurementInputRow(
            imageResId = imageResId,
            imageContentDescription = imageContentDescription,
            label = label,
            value = value,
            onValueChanged = onValueChanged,
            onActionDone = onActionDone,
            keyboardType = keyboardType,
            // Modifier for MeasurementInputCore might not need explicit padding now,
            // as the Column provides it.
            // modifier = Modifier.fillMaxWidth() // It already fills width by default in its Row
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = instructionalText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
            // .padding(horizontal = 8.dp) // Padding for text might not be needed if Column's padding is sufficient
        )
    }
}

@Preview(showBackground = true, name = "Measurement Input Row With Info Preview")
@Composable
fun MeasurementInputRowWithInfoPreview() {
    BodyFatTrackerTheme {
        Surface(modifier = Modifier.padding(8.dp)) { // Add padding around Surface for better preview
            var textValue by remember { mutableStateOf("40") }
            MeasurementInputRowWithInfo(
                imageResId = R.drawable.man_back,
                imageContentDescription = "Neck icon",
                label = "Neck (cm)",
                value = textValue,
                onValueChanged = { textValue = it },
                onActionDone = {
                    println("Action Done in Preview: $textValue")
                },
                instructionalText = "Measure around the narrowest point of your neck, just below the Adam's apple. Keep the tape measure horizontal.",
                // modifier = Modifier.padding(16.dp) // Modifier now applied to Column, Surface gives outer padding
            )
        }
    }
}

@Preview(showBackground = true, name = "Measurement Input Row With Info - Empty")
@Composable
fun MeasurementInputRowWithInfoPreviewEmpty() {
    BodyFatTrackerTheme {
        Surface(modifier = Modifier.padding(8.dp)) { // Add padding around Surface for better preview
            var textValue by remember { mutableStateOf("") }
            MeasurementInputRowWithInfo(
                imageResId = R.drawable.ic_launcher_foreground,
                imageContentDescription = "Waist icon",
                label = "Waist (cm)",
                value = textValue,
                onValueChanged = { textValue = it },
                onActionDone = {
                    println("Action Done in Preview: $textValue")
                },
                instructionalText = "Measure around your natural waistline, which is the narrowest part of your torso, typically just above the belly button. Ensure the tape is snug but not compressing the skin.",
                // modifier = Modifier.padding(16.dp)
            )
        }
    }
}
