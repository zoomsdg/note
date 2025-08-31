package com.example.xnote.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xnote.data.NoteSummary
import com.example.xnote.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (NoteSummary) -> Unit
) : ListAdapter<NoteSummary, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return NoteViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        
        fun bind(noteSummary: NoteSummary) {
            binding.apply {
                tvTitle.text = noteSummary.title.ifEmpty { "无标题" }
                tvPreview.text = noteSummary.preview.ifEmpty { "空记事" }
                tvDate.text = dateFormat.format(Date(noteSummary.lastModified))
                tvBlockCount.text = "${noteSummary.blockCount} 项"
                
                root.setOnClickListener {
                    onNoteClick(noteSummary)
                }
            }
        }
    }
    
    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteSummary>() {
        override fun areItemsTheSame(oldItem: NoteSummary, newItem: NoteSummary): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: NoteSummary, newItem: NoteSummary): Boolean {
            return oldItem == newItem
        }
    }
}