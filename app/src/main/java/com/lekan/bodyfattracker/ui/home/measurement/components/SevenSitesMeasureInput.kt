package com.lekan.bodyfattracker.ui.home.measurement.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider // Added import
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard // Added import
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.components.MeasurementInputRow
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme

// SkinfoldSiteData remains the same as in ThreeSitesMeasureInput
// data class SkinfoldSiteData(...)

@Composable
fun SevenSitesMeasureInput(
    age: String,
    onAgeChanged: (String) -> Unit,
    selectedGender: Gender = Gender.FEMALE,
    onGenderSelected: (Gender) -> Unit,
    chestSkinfold: String,
    onChestSkinfoldChanged: (String) -> Unit,
    midaxillarySkinfold: String,
    onMidaxillarySkinfoldChanged: (String) -> Unit,
    tricepsSkinfold: String,
    onTricepsSkinfoldChanged: (String) -> Unit,
    subscapularSkinfold: String,
    onSubscapularSkinfoldChanged: (String) -> Unit,
    abdomenSkinfold: String,
    onAbdomenSkinfoldChanged: (String) -> Unit,
    suprailiacSkinfold: String,
    onSuprailiacSkinfoldChanged: (String) -> Unit,
    thighSkinfold: String,
    onThighSkinfoldChanged: (String) -> Unit,
    onResetClicked: () -> Unit = {},
    onCalculateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val ageFocus = remember { FocusRequester() }
    val site1Focus = remember { FocusRequester() }
    val site2Focus = remember { FocusRequester() }
    val site3Focus = remember { FocusRequester() }
    val site4Focus = remember { FocusRequester() }
    val site5Focus = remember { FocusRequester() }
    val site6Focus = remember { FocusRequester() }
    val site7Focus = remember { FocusRequester() }

    val isFormComplete = age.isNotBlank() &&
            chestSkinfold.isNotBlank() &&
            midaxillarySkinfold.isNotBlank() &&
            tricepsSkinfold.isNotBlank() &&
            subscapularSkinfold.isNotBlank() &&
            abdomenSkinfold.isNotBlank() &&
            suprailiacSkinfold.isNotBlank() &&
            thighSkinfold.isNotBlank()

    val skinfoldSitesToDisplay = remember(
        selectedGender, chestSkinfold, midaxillarySkinfold, tricepsSkinfold,
        subscapularSkinfold, abdomenSkinfold, suprailiacSkinfold, thighSkinfold
    ) {
        val maleSites = listOf(
            SkinfoldSiteData(R.drawable.man_front_chest, R.string.chest_label_skinfold, chestSkinfold, site1Focus),
            SkinfoldSiteData(R.drawable.man_side_axilla, R.string.midaxillary_label_skinfold, midaxillarySkinfold, site2Focus),
            SkinfoldSiteData(R.drawable.man_arm_triceps, R.string.triceps_label_skinfold, tricepsSkinfold, site3Focus),
            SkinfoldSiteData(R.drawable.man_back_subscapular, R.string.subscapular_label_skinfold, subscapularSkinfold, site4Focus),
            SkinfoldSiteData(R.drawable.man_front_abs, R.string.abdomen_label_skinfold, abdomenSkinfold, site5Focus),
            SkinfoldSiteData(R.drawable.man_side_suprailiac, R.string.suprailiac_label_skinfold, suprailiacSkinfold, site6Focus),
            SkinfoldSiteData(R.drawable.thigh, R.string.thigh_label_skinfold, thighSkinfold, site7Focus)
        )
        val femaleSites = listOf(
            SkinfoldSiteData(R.drawable.woman_front_chest, R.string.chest_label_skinfold, chestSkinfold, site1Focus),
            SkinfoldSiteData(R.drawable.woman_side_axilla, R.string.midaxillary_label_skinfold, midaxillarySkinfold, site2Focus),
            SkinfoldSiteData(R.drawable.woman_arm_triceps, R.string.triceps_label_skinfold, tricepsSkinfold, site3Focus),
            SkinfoldSiteData(R.drawable.woman_back_subscapular, R.string.subscapular_label_skinfold, subscapularSkinfold, site4Focus),
            SkinfoldSiteData(R.drawable.woman_front_abs, R.string.abdomen_label_skinfold, abdomenSkinfold, site5Focus),
            SkinfoldSiteData(R.drawable.woman_side_suprailiac, R.string.suprailiac_label_skinfold, suprailiacSkinfold, site6Focus),
            SkinfoldSiteData(R.drawable.thigh, R.string.thigh_label_skinfold, thighSkinfold, site7Focus)
        )
        when (selectedGender) {
            Gender.MALE -> maleSites
            Gender.FEMALE -> femaleSites
        }
    }

    val skinfoldChangeHandlers = listOf(
        onChestSkinfoldChanged, onMidaxillarySkinfoldChanged, onTricepsSkinfoldChanged,
        onSubscapularSkinfoldChanged, onAbdomenSkinfoldChanged, onSuprailiacSkinfoldChanged,
        onThighSkinfoldChanged
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.seven_site_skinfold_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp) // Keep this padding or adjust as needed
            )
            IconButton(onClick = onResetClicked) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.reset_form_button_description)
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
                onNext = { site1Focus.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(ageFocus)
            // .padding(bottom = 16.dp) // Removed specific bottom padding here
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Added Divider

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
                if (skinfoldSitesToDisplay.isNotEmpty()) {
                    site1Focus.requestFocus()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
            // .padding(bottom = 24.dp) // Removed specific bottom padding here
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Added Divider

        Text(
            text = stringResource(R.string.skinfold_measurements_mm_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedCard( // Added OutlinedCard
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // For spacing before the Calculate button
        ) {
            Column(modifier = Modifier.padding(16.dp)) { // Padding inside the card
                skinfoldSitesToDisplay.forEachIndexed { index, siteData ->
                    MeasurementInputRow(
                        imageResId = siteData.imageResId,
                        imageContentDescription = stringResource(id = siteData.labelStringResId),
                        label = stringResource(id = siteData.labelStringResId) + " (mm)",
                        value = siteData.currentValue,
                        onValueChanged = skinfoldChangeHandlers[index],
                        onActionDone = {
                            if (index < skinfoldSitesToDisplay.size - 1) {
                                skinfoldSitesToDisplay[index + 1].focusRequester.requestFocus()
                            } else {
                                keyboardController?.hide()
                                if (isFormComplete) onCalculateClicked()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(siteData.focusRequester)
                            // Adjust padding: only add bottom padding if not the last item in the card
                            .padding(bottom = if (index < skinfoldSitesToDisplay.size - 1) 16.dp else 0.dp),
                        keyboardType = KeyboardType.Number
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Keep or adjust based on overall spacing

        Button(
            onClick = {
                keyboardController?.hide()
                onCalculateClicked()
            },
            enabled = isFormComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(R.string.calculate_button_label))
        }
    }
}

// GenderSelector and GenderTab composables remain the same as in ThreeSitesMeasureInput
// @Composable fun GenderSelector(...) { ... }
// @Composable fun GenderTab(...) { ... }


// --- Previews for SevenSitesMeasureInput ---

@Preview(showBackground = true, name = "7-Site Input - Female Selected")
@Composable
fun SevenSitesMeasureInputPreviewFemale() {
    BodyFatTrackerTheme {
        var age by remember { mutableStateOf("30") }
        var gender by remember { mutableStateOf(Gender.FEMALE) }
        var chest by remember { mutableStateOf("10") }
        var midaxillary by remember { mutableStateOf("11") }
        var triceps by remember { mutableStateOf("12") }
        var subscapular by remember { mutableStateOf("13") }
        var abdomen by remember { mutableStateOf("14") }
        var suprailiac by remember { mutableStateOf("15") }
        var thigh by remember { mutableStateOf("16") }

        Surface(modifier = Modifier.fillMaxSize()) {
            SevenSitesMeasureInput(
                age = age, onAgeChanged = { age = it },
                selectedGender = gender, onGenderSelected = { gender = it },
                chestSkinfold = chest, onChestSkinfoldChanged = { chest = it },
                midaxillarySkinfold = midaxillary, onMidaxillarySkinfoldChanged = { midaxillary = it },
                tricepsSkinfold = triceps, onTricepsSkinfoldChanged = { triceps = it },
                subscapularSkinfold = subscapular, onSubscapularSkinfoldChanged = { subscapular = it },
                abdomenSkinfold = abdomen, onAbdomenSkinfoldChanged = { abdomen = it },
                suprailiacSkinfold = suprailiac, onSuprailiacSkinfoldChanged = { suprailiac = it },
                thighSkinfold = thigh, onThighSkinfoldChanged = { thigh = it },
                onCalculateClicked = { /* Log parameters */ }
            )
        }
    }
}

@Preview(showBackground = true, name = "7-Site Input - Male Selected - Empty")
@Composable
fun SevenSitesMeasureInputPreviewMaleEmpty() {
    BodyFatTrackerTheme {
        var age by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf(Gender.MALE) }
        var chest by remember { mutableStateOf("") }
        var midaxillary by remember { mutableStateOf("") }
        var triceps by remember { mutableStateOf("") }
        var subscapular by remember { mutableStateOf("") }
        var abdomen by remember { mutableStateOf("") }
        var suprailiac by remember { mutableStateOf("") }
        var thigh by remember { mutableStateOf("") }

        Surface(modifier = Modifier.fillMaxSize()) {
            SevenSitesMeasureInput(
                age = age, onAgeChanged = { age = it },
                selectedGender = gender, onGenderSelected = { gender = it },
                chestSkinfold = chest, onChestSkinfoldChanged = { chest = it },
                midaxillarySkinfold = midaxillary, onMidaxillarySkinfoldChanged = { midaxillary = it },
                tricepsSkinfold = triceps, onTricepsSkinfoldChanged = { triceps = it },
                subscapularSkinfold = subscapular, onSubscapularSkinfoldChanged = { subscapular = it },
                abdomenSkinfold = abdomen, onAbdomenSkinfoldChanged = { abdomen = it },
                suprailiacSkinfold = suprailiac, onSuprailiacSkinfoldChanged = { suprailiac = it },
                thighSkinfold = thigh, onThighSkinfoldChanged = { thigh = it },
                onCalculateClicked = { /* Log parameters */ }
            )
        }
    }
}
