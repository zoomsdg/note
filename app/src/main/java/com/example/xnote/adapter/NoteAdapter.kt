package com.example.xnote.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xnote.data.NoteSummary
import com.example.xnote.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (NoteSummary) -> Unit,
    private val onNoteLongClick: (NoteSummary) -> Unit = {},
    private val onSelectionChanged: (Set<String>) -> Unit = {}
) : ListAdapter<NoteSummary, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    private var isSelectionMode = false
    private val selectedNotes = mutableSetOf<String>()
    
    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedNotes.clear()
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedNotes.toSet())
    }
    
    fun toggleSelection(noteId: String) {
        if (selectedNotes.contains(noteId)) {
            selectedNotes.remove(noteId)
        } else {
            selectedNotes.add(noteId)
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedNotes.toSet())
    }
    
    fun selectAll() {
        selectedNotes.clear()
        currentList.forEach { selectedNotes.add(it.id) }
        notifyDataSetChanged()
        onSelectionChanged(selectedNotes.toSet())
    }
    
    fun deselectAll() {
        selectedNotes.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedNotes.toSet())
    }
    
    fun getSelectedNotes(): Set<String> = selectedNotes.toSet()
    
    fun hasSelectedNotes(): Boolean = selectedNotes.isNotEmpty()
    
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
                
                // 控制复选框显示
                cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
                cbSelect.isChecked = selectedNotes.contains(noteSummary.id)
                
                // 设置选中状态的背景效果
                root.isSelected = selectedNotes.contains(noteSummary.id)
                
                // 点击事件处理
                root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(noteSummary.id)
                    } else {
                        onNoteClick(noteSummary)
                    }
                }
                
                // 长按事件处理
                root.setOnLongClickListener {
                    if (!isSelectionMode) {
                        onNoteLongClick(noteSummary)
                        true
                    } else {
                        false
                    }
                }
                
                // 复选框点击事件
                cbSelect.setOnClickListener {
                    toggleSelection(noteSummary.id)
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