package com.ramya.learnhub.ui.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramya.learnhub.R
import com.ramya.learnhub.data.repository.CourseRepository
import com.ramya.learnhub.domain.Course
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val courses: List<Course>) : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(@StringRes val message: Int) : DashboardUiState
}

class DashboardViewModel(private val repository: CourseRepository) : ViewModel() {

    private sealed interface Refresh {
        data object Loading : Refresh
        data object Done : Refresh
        data class Failed(@StringRes val message: Int) : Refresh
    }

    private val refresh = MutableStateFlow<Refresh>(Refresh.Loading)
    private var refreshJob: Job? = null

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** One-off messages (e.g. refresh failed while cached courses are shown). */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    // The cache is the source of truth: cached courses always win over refresh status.
    val state: StateFlow<DashboardUiState> =
        combine(repository.observeCourses(), refresh) { courses, refresh ->
            when {
                courses.isNotEmpty() -> DashboardUiState.Success(courses)
                refresh is Refresh.Loading -> DashboardUiState.Loading
                refresh is Refresh.Failed -> DashboardUiState.Error(refresh.message)
                else -> DashboardUiState.Empty
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refresh.value = Refresh.Loading
        refreshJob = viewModelScope.launch {
            repository.refresh()
                .onSuccess { refresh.value = Refresh.Done }
                .onFailure { error ->
                    refresh.value = Refresh.Failed(error.toMessage())
                    if (repository.hasCachedCourses()) _messages.send(R.string.dashboard_showing_cached)
                }
        }
    }

    private fun Throwable.toMessage(): Int =
        if (this is IOException) R.string.error_no_connection else R.string.dashboard_error_load
}
