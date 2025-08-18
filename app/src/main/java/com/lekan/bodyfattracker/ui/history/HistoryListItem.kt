package com.lekan.bodyfattracker.ui.history

import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.WeightEntry

sealed interface HistoryListItem {
    val timestamp: Long // Common property for sorting

    data class MeasurementItem(val measurement: BodyFatMeasurement) : HistoryListItem {
        override val timestamp: Long = measurement.timeStamp
    }

    data class WeightItem(val entry: WeightEntry) : HistoryListItem {
        override val timestamp: Long = entry.timeStamp
    }
}