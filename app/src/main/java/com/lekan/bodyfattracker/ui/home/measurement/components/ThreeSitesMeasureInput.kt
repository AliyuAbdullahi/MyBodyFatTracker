package com.lekan.bodyfattracker.ui.home.measurement.components // Adjust package as needed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.components.MeasurementInputRow // Assuming this is your updated component
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import com.lekan.bodyfattracker.ui.theme.Grey100
import com.lekan.bodyfattracker.ui.theme.Grey800
import com.lekan.bodyfattracker.ui.theme.PrimaryColor // Assuming you have this
import com.lekan.bodyfattracker.ui.theme.White

data class SkinfoldSiteData(
    @DrawableRes val imageResId: Int,
    val labelStringResId: Int,
    val currentValue: String,
    val focusRequester: FocusRequester
)

@Composable
fun ThreeSitesMeasureInput(
    age: String,
    onAgeChanged: (String) -> Unit,
    selectedGender: Gender = Gender.FEMALE,
    onGenderSelected: (Gender) -> Unit,
    chestSkinfold: String, // Represents "Chest" for Male, "Triceps" for Female
    onChestSkinfoldChanged: (String) -> Unit,
    abdomenSkinfold: String, // Represents "Abdomen" for Male, "Suprailiac" for Female
    onAbdomenSkinfoldChanged: (String) -> Unit,
    thighSkinfold: String, // "Thigh" for both
    onThighSkinfoldChanged: (String) -> Unit,
    onResetClicked: () -> Unit = {},
    onCalculateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus Requesters for sequential focus
    val ageFocus = remember { FocusRequester() }
    val site1Focus = remember { FocusRequester() }
    val site2Focus = remember { FocusRequester() }
    val site3Focus = remember { FocusRequester() }

    val isFormComplete = age.isNotBlank() &&
            // selectedGender != Gender.NONE && // Assuming Gender doesn't have a NONE state based on new default
            chestSkinfold.isNotBlank() &&
            abdomenSkinfold.isNotBlank() &&
            thighSkinfold.isNotBlank()

    val skinfoldSitesToDisplay =
        remember(selectedGender, chestSkinfold, abdomenSkinfold, thighSkinfold) {
            when (selectedGender) {
                Gender.MALE -> listOf(
                    SkinfoldSiteData(
                        R.drawable.man_front_chest, // Replace with actual drawable
                        R.string.chest_label_skinfold,
                        chestSkinfold,
                        site1Focus
                    ),
                    SkinfoldSiteData(
                        R.drawable.man_front_abs, // Replace with actual drawable
                        R.string.abdomen_label_skinfold,
                        abdomenSkinfold,
                        site2Focus
                    ),
                    SkinfoldSiteData(
                        R.drawable.thigh, // Replace with actual drawable
                        R.string.thigh_label_skinfold,
                        thighSkinfold,
                        site3Focus
                    )
                )

                Gender.FEMALE -> listOf(
                    SkinfoldSiteData(
                        R.drawable.woman_arm_triceps, // Replace with actual drawable
                        R.string.triceps_label_skinfold,
                        chestSkinfold, // Using chestSkinfold state for 1st female site
                        site1Focus
                    ),
                    SkinfoldSiteData(
                        R.drawable.woman_side_suprailiac, // Replace with actual drawable
                        R.string.suprailiac_label_skinfold,
                        abdomenSkinfold, // Using abdomenSkinfold state
                        site2Focus
                    ),
                    SkinfoldSiteData(
                        R.drawable.thigh, // Replace with actual drawable
                        R.string.thigh_label_skinfold,
                        thighSkinfold,
                        site3Focus
                    )
                )
                // else -> emptyList() // Handle case where gender might not be MALE or FEMALE if applicable
            }
        }

    val skinfoldChangeHandlers = remember(selectedGender) {
        // The order of these handlers must match the order of skinfoldSitesToDisplay for the selected gender
        listOf(onChestSkinfoldChanged, onAbdomenSkinfoldChanged, onThighSkinfoldChanged)
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Row with Reset Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Pushes title and icon apart
        ) {
            Text(
                text = stringResource(R.string.three_site_skinfold_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f) // Title takes available space
            )
            IconButton(onClick = onResetClicked) {
                Icon(
                    imageVector = Icons.Filled.Refresh, // Replace with your desired reset icon
                    contentDescription = stringResource(R.string.reset_form_button_description) // For accessibility
                )
            }
        }

        OutlinedTextField(
            value = age,
            onValueChange = onAgeChanged,
            label = { Text(stringResource(R.string.age_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (skinfoldSitesToDisplay.isNotEmpty()) {
                        // More direct way is to request focus on the first skinfold site
                        site1Focus.requestFocus()
                    } else {
                        focusManager.moveFocus(FocusDirection.Down) // Fallback if no sites (e.g. gender not set if that was possible)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(ageFocus)
                .padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        GenderSelector(
            selectedGender = selectedGender,
            onGenderSelected = {
                onGenderSelected(it)
                // Optionally move focus to the first skinfold site when gender is selected
                if (skinfoldSitesToDisplay.isNotEmpty()) {
                    site1Focus.requestFocus()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.skinfold_measurements_mm_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        skinfoldSitesToDisplay.forEachIndexed { index, siteData ->
            MeasurementInputRow(
                imageResId = siteData.imageResId, // Correct image is already selected
                imageContentDescription = stringResource(id = siteData.labelStringResId),
                label = stringResource(id = siteData.labelStringResId) + " (mm)",
                value = siteData.currentValue,
                onValueChanged = skinfoldChangeHandlers[index],
                onActionDone = {
                    if (index < skinfoldSitesToDisplay.size - 1) {
                        skinfoldSitesToDisplay[index + 1].focusRequester.requestFocus()
                    } else {
                        keyboardController?.hide()
                        if (isFormComplete) onCalculateClicked() // Auto-calculate if last and complete
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(siteData.focusRequester)
                    .padding(bottom = 16.dp),
                keyboardType = KeyboardType.Number
            )
        }

        // This Button should be inside the Column to scroll with the content
        // Or anchored at the bottom of the screen if that's the desired UI.
        // For simplicity with verticalScroll, placing it at the end of the Column.
        // If you want it anchored, you'd use a Scaffold or Box with alignment.
        Button(
            onClick = {
                keyboardController?.hide()
                onCalculateClicked()
            },
            enabled = isFormComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp) // Adjust padding as needed
                .height(50.dp)
        ) {
            Text(stringResource(R.string.calculate_button_label))
        }
    }
}

@Composable
fun GenderSelector(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        GenderTab(
            text = stringResource(R.string.female_gender_label),
            isSelected = selectedGender == Gender.FEMALE,
            onClick = { onGenderSelected(Gender.FEMALE) },
            isLeftTab = true,
            modifier = Modifier.weight(1f)
        )
        GenderTab(
            text = stringResource(R.string.male_gender_label),
            isSelected = selectedGender == Gender.MALE,
            isLeftTab = false,
            onClick = { onGenderSelected(Gender.MALE) },
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun GenderTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLeftTab: Boolean, // New parameter to determine corner rounding
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val backgroundColor = if (isSelected) PrimaryColor else Grey800
    val textColor = if (isSelected) White else Grey100

    // Determine the shape based on whether it's the left or right tab
    val tabShape: CornerBasedShape = if (isLeftTab) {
        RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius, topEnd = 0.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = cornerRadius, bottomEnd = cornerRadius)
    }

    Box(
        modifier = modifier
            .clip(tabShape) // Apply the specific rounded corners
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp), // Apply vertical padding to the Box
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// --- Previews for ThreeSitesMeasureInput ---

@Preview(showBackground = true, name = "3-Site Input - Female Selected")
@Composable
fun ThreeSitesMeasureInputPreviewFemale() {
    BodyFatTrackerTheme {
        var age by remember { mutableStateOf("28") }
        var gender by remember { mutableStateOf(Gender.FEMALE) }
        var site1Value by remember { mutableStateOf("10") } // Triceps
        var site2Value by remember { mutableStateOf("15") } // Suprailiac
        var site3Value by remember { mutableStateOf("20") } // Thigh

        Surface(modifier = Modifier.fillMaxSize()) {
            ThreeSitesMeasureInput(
                age = age,
                onAgeChanged = { age = it },
                selectedGender = gender,
                onGenderSelected = { gender = it },
                chestSkinfold = site1Value, // This state maps to Triceps for female
                onChestSkinfoldChanged = { site1Value = it },
                abdomenSkinfold = site2Value, // This state maps to Suprailiac for female
                onAbdomenSkinfoldChanged = { site2Value = it },
                thighSkinfold = site3Value,
                onThighSkinfoldChanged = { site3Value = it },
                onCalculateClicked = {
                    println("Calculate: Age=$age, Gender=$gender, Triceps=$site1Value, Suprailiac=$site2Value, Thigh=$site3Value")
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "3-Site Input - Male Selected - Empty Fields")
@Composable
fun ThreeSitesMeasureInputPreviewMaleEmpty() {
    BodyFatTrackerTheme {
        var age by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf(Gender.MALE) }
        var site1Value by remember { mutableStateOf("") } // Chest
        var site2Value by remember { mutableStateOf("") } // Abdomen
        var site3Value by remember { mutableStateOf("") } // Thigh

        Surface(modifier = Modifier.fillMaxSize()) {
            ThreeSitesMeasureInput(
                age = age,
                onAgeChanged = { age = it },
                selectedGender = gender,
                onGenderSelected = { gender = it },
                chestSkinfold = site1Value,
                onChestSkinfoldChanged = { site1Value = it },
                abdomenSkinfold = site2Value,
                onAbdomenSkinfoldChanged = { site2Value = it },
                thighSkinfold = site3Value,
                onThighSkinfoldChanged = { site3Value = it },
                onCalculateClicked = {
                    println("Calculate: Age=$age, Gender=$gender, Chest=$site1Value, Abs=$site2Value, Thigh=$site3Value")
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "3-Site Input - Female Selected - All Fields Filled")
@Composable
fun ThreeSitesMeasureInputPreviewFemaleFilled() {
    BodyFatTrackerTheme {
        var age by remember { mutableStateOf("35") }
        var gender by remember { mutableStateOf(Gender.FEMALE) }
        var site1Value by remember { mutableStateOf("12") } // Triceps
        var site2Value by remember { mutableStateOf("18") } // Suprailiac
        var site3Value by remember { mutableStateOf("22") } // Thigh

        Surface(modifier = Modifier.fillMaxSize()) {
            ThreeSitesMeasureInput(
                age = age,
                onAgeChanged = { age = it },
                selectedGender = gender,
                onGenderSelected = { gender = it },
                chestSkinfold = site1Value,
                onChestSkinfoldChanged = { site1Value = it },
                abdomenSkinfold = site2Value,
                onAbdomenSkinfoldChanged = { site2Value = it },
                thighSkinfold = site3Value,
                onThighSkinfoldChanged = { site3Value = it },
                onCalculateClicked = {
                    println("Calculate: Age=$age, Gender=$gender, Triceps=$site1Value, Suprailiac=$site2Value, Thigh=$site3Value")
                },
                // modifier = Modifier.padding(bottom = 56.dp) // Example if you had an anchored button
            )
        }
    }
}
