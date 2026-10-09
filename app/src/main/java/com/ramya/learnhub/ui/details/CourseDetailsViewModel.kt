package com.ramya.learnhub.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramya.learnhub.data.repository.CourseRepository
import com.ramya.learnhub.domain.CourseDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val details: CourseDetails) : DetailsUiState
    data object NotFound : DetailsUiState
}

class CourseDetailsViewModel(
    private val repository: CourseRepository,
    private val courseId: Long
) : ViewModel() {

    val state: StateFlow<DetailsUiState> = repository.observeCourseDetails(courseId)
        .map { details -> if (details == null) DetailsUiState.NotFound else DetailsUiState.Success(details) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState.Loading)

    /** Safe to call repeatedly: the repository ignores already-completed lessons. */
    fun onCompleteLesson(lessonId: Long) {
        viewModelScope.launch { repository.completeLesson(courseId, lessonId) }
    }
}
