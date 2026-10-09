package com.ramya.learnhub.ui.details

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramya.learnhub.R
import com.ramya.learnhub.databinding.ItemLessonBinding
import com.ramya.learnhub.domain.Lesson

class LessonAdapter(private val onComplete: (Lesson) -> Unit) :
    ListAdapter<Lesson, LessonAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: Lesson) = with(binding) {
            val context = root.context
            val done = lesson.isCompleted
            title.text = lesson.title
            status.setText(if (done) R.string.lesson_status_completed else R.string.lesson_status_pending)
            TextViewCompat.setTextAppearance(
                status,
                if (done) R.style.TextAppearance_LearnHub_StatusCompleted else R.style.TextAppearance_LearnHub_StatusPending
            )
            statusIcon.setImageResource(if (done) R.drawable.ic_check_circle else R.drawable.ic_pending)
            statusIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, if (done) R.color.success else R.color.pending)
            )
            statusIcon.contentDescription = context.getString(
                if (done) R.string.cd_lesson_completed else R.string.cd_lesson_pending
            )
            completeButton.isVisible = !done
            completeButton.setOnClickListener { onComplete(lesson) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(old: Lesson, new: Lesson) = old.id == new.id
        override fun areContentsTheSame(old: Lesson, new: Lesson) = old == new
    }
}
