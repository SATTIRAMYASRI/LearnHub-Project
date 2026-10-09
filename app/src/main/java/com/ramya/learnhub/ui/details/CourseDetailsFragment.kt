package com.ramya.learnhub.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.ramya.learnhub.R
import com.ramya.learnhub.databinding.FragmentCourseDetailsBinding
import com.ramya.learnhub.ui.common.appContainer
import com.ramya.learnhub.ui.dashboard.CourseArgs
import kotlinx.coroutines.launch

class CourseDetailsFragment : Fragment() {

    private var _binding: FragmentCourseDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseDetailsViewModel by viewModels {
        val courseId = requireArguments().getLong(CourseArgs.COURSE_ID)
        viewModelFactory { initializer { CourseDetailsViewModel(appContainer.courseRepository, courseId) } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCourseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = LessonAdapter { lesson -> viewModel.onCompleteLesson(lesson.id) }
        binding.lessonList.adapter = adapter
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it, adapter) }
            }
        }
    }

    private fun render(state: DetailsUiState, adapter: LessonAdapter) = with(binding) {
        loading.isVisible = state is DetailsUiState.Loading
        content.isVisible = state is DetailsUiState.Success
        notFound.isVisible = state is DetailsUiState.NotFound

        if (state is DetailsUiState.Success) {
            val course = state.details.course
            courseTitle.text = course.title
            progressText.text = getString(R.string.course_progress_percent, course.progress)
            progressBar.setProgressCompat(course.progress, true)
            adapter.submitList(state.details.lessons)
        }
    }

    override fun onDestroyView() {
        binding.lessonList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
