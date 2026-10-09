package com.ramya.learnhub.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramya.learnhub.R
import com.ramya.learnhub.databinding.ItemCourseBinding
import com.ramya.learnhub.domain.Course

class CourseAdapter(private val onCourseClick: (Course) -> Unit) :
    ListAdapter<Course, CourseAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) = with(binding) {
            val context = root.context
            title.text = course.title
            instructor.text = context.getString(R.string.course_instructor, course.instructor)
            lessons.text = context.resources.getQuantityString(
                R.plurals.course_lessons_count, course.totalLessons, course.totalLessons
            )
            progressText.text = context.getString(R.string.course_progress_percent, course.progress)
            progressBar.progress = course.progress
            root.setOnClickListener { onCourseClick(course) }
            continueButton.setOnClickListener { onCourseClick(course) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(old: Course, new: Course) = old.id == new.id
        override fun areContentsTheSame(old: Course, new: Course) = old == new
    }
}
