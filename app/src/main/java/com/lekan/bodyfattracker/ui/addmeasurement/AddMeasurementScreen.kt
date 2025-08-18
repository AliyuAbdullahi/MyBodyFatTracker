package com.lekan.bodyfattracker.ui.addmeasurement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.model.MeasurementMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementScreen(
    viewModel: AddMeasurementViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState = viewModel.uiState
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    // Navigate back when save is successful
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.resetSaveStatus() // Reset flag before navigating
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Measurement") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.bodyFatPercentage,
                onValueChange = viewModel::onBodyFatPercentageChange,
                label = { Text("Body Fat Percentage (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.saveError?.contains("percentage", ignoreCase = true) == true
            )

            // Measurement Method Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.selectedMethod?.name ?: "Select Method",
                    onValueChange = { /* Read Only */ },
                    label = { Text("Measurement Method") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select Method") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { methodDropdownExpanded = true },
                    isError = uiState.saveError?.contains("method", ignoreCase = true) == true
                )

                DropdownMenu(
                    expanded = methodDropdownExpanded,
                    onDismissRequest = { methodDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MeasurementMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.name) },
                            onClick = {
                                viewModel.onMethodSelected(method)
                                methodDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.saveError != null) {
                Text(
                    text = uiState.saveError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = viewModel::saveMeasurement,
                enabled = !uiState.isSaving,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save Measurement")
            }
        }
    }
}

