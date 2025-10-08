package com.example.task5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmissionsAdapter(
    private var items: List<Submission> = emptyList()
) : RecyclerView.Adapter<SubmissionsAdapter.SubmissionViewHolder>() {

    fun submitList(newList: List<Submission>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return SubmissionViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_meta)

        fun bind(s: Submission) {
            tvTitle.text = s.title ?: "Untitled"
            val status = s.status ?: "-"
            val lang = s.lang ?: "-"
            val tsText = s.timestamp?.let { formatTimestamp(it) } ?: ""
            val meta = buildString {
                append(lang)
                append(" • ")
                append(status)
                if (tsText.isNotEmpty()) {
                    append(" • ")
                    append(tsText)
                }
            }
            tvMeta.text = meta
        }

        private fun formatTimestamp(ts: Long): String {
            return try {
                val millis = if (ts > 10000000000L) ts else ts * 1000L
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                sdf.format(java.util.Date(millis))
            } catch (e: Exception) {
                ts.toString()
            }
        }
    }
}
