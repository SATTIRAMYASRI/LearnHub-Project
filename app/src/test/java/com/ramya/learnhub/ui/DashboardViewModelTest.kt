package com.ramya.learnhub.ui

import com.ramya.learnhub.MainDispatcherRule
import com.ramya.learnhub.R
import com.ramya.learnhub.data.repository.CourseRepository
import com.ramya.learnhub.fakes.FakeCourseDao
import com.ramya.learnhub.fakes.FakeRemote
import com.ramya.learnhub.fakes.FakeTransactionRunner
import com.ramya.learnhub.fakes.sampleRemoteCourse
import com.ramya.learnhub.ui.dashboard.DashboardUiState
import com.ramya.learnhub.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeCourseDao()

    private fun repository(remote: FakeRemote) = CourseRepository(dao, remote, FakeTransactionRunner())

    /** The state flow only runs while someone collects it, so tests collect in the background. */
    private fun kotlinx.coroutines.test.TestScope.stateOf(vm: DashboardViewModel): DashboardUiState {
        backgroundScope.launch { vm.state.collect {} }
        runCurrent() // let the collector start so the state is computed
        return vm.state.value
    }

    @Test
    fun `shows courses when loading succeeds`() = runTest {
        val vm = DashboardViewModel(repository(FakeRemote(Result.success(listOf(sampleRemoteCourse())))))

        val state = stateOf(vm)

        assertTrue(state is DashboardUiState.Success)
        assertEquals(1, (state as DashboardUiState.Success).courses.size)
    }

    @Test
    fun `shows empty state when the server returns no courses`() = runTest {
        val vm = DashboardViewModel(repository(FakeRemote(Result.success(emptyList()))))

        assertEquals(DashboardUiState.Empty, stateOf(vm))
    }

    @Test
    fun `shows error when loading fails and nothing is cached`() = runTest {
        val vm = DashboardViewModel(repository(FakeRemote(Result.failure(IOException("offline")))))

        assertEquals(DashboardUiState.Error(R.string.error_no_connection), stateOf(vm))
    }

    @Test
    fun `still shows cached courses when a refresh fails`() = runTest {
        val remote = FakeRemote(Result.success(listOf(sampleRemoteCourse())))
        val repo = repository(remote)
        repo.refresh() // first load worked, so courses are cached
        remote.result = Result.failure(IOException("offline")) // now the server is unreachable

        val state = stateOf(DashboardViewModel(repo))

        assertTrue(state is DashboardUiState.Success)
    }

    @Test
    fun `retry loads courses after an error`() = runTest {
        val remote = FakeRemote(Result.failure(IOException("offline")))
        val vm = DashboardViewModel(repository(remote))
        stateOf(vm)
        remote.result = Result.success(listOf(sampleRemoteCourse()))

        vm.refresh()
        testScheduler.runCurrent()

        assertTrue(vm.state.value is DashboardUiState.Success)
    }
}
