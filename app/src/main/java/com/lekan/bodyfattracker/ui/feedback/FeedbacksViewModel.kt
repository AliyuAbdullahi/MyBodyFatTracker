package com.lekan.bodyfattracker.ui.feedback

import com.lekan.bodyfattracker.model.UserFeedback
import com.lekan.bodyfattracker.ui.core.CoreViewModel

data class FeedbacksState(
    val feedbackList: List<UserFeedback> = emptyList(),
)

class FeedbacksViewModel : CoreViewModel<FeedbacksState>() {

    override fun initialize(): FeedbacksState = FeedbacksState()
}