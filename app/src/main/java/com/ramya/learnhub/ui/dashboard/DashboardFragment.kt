package com.ramya.learnhub.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ramya.learnhub.R
import com.ramya.learnhub.databinding.FragmentDashboardBinding
import com.ramya.learnhub.ui.common.appContainer
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        viewModelFactory { initializer { DashboardViewModel(appContainer.courseRepository) } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = CourseAdapter { course ->
            val navController = findNavController()
            // Ignore a second tap once navigation has already left this screen.
            if (navController.currentDestination?.id == R.id.dashboardFragment) {
                navController.navigate(
                    R.id.action_dashboard_to_details,
                    bundleOf(CourseArgs.COURSE_ID to course.id)
                )
            }
        }
        binding.courseList.adapter = adapter
        binding.retryButton.setOnClickListener { viewModel.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(it, adapter) } }
                launch {
                    viewModel.messages.collect { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
                }
            }
        }
    }

    private fun render(state: DashboardUiState, adapter: CourseAdapter) = with(binding) {
        loading.isVisible = state is DashboardUiState.Loading
        courseList.isVisible = state is DashboardUiState.Success
        emptyGroup.isVisible = state is DashboardUiState.Empty
        errorGroup.isVisible = state is DashboardUiState.Error

        when (state) {
            is DashboardUiState.Success -> adapter.submitList(state.courses)
            is DashboardUiState.Error -> errorMessage.setText(state.message)
            else -> Unit
        }
    }

    override fun onDestroyView() {
        binding.courseList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
