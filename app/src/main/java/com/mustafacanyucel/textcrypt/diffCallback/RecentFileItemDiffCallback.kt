package com.mustafacanyucel.textcrypt.diffCallback

import androidx.recyclerview.widget.DiffUtil
import com.mustafacanyucel.textcrypt.model.RecentFileItem

class RecentFileItemDiffCallback : DiffUtil.ItemCallback<RecentFileItem>() {
    override fun areItemsTheSame(oldItem: RecentFileItem, newItem: RecentFileItem): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: RecentFileItem, newItem: RecentFileItem): Boolean {
        return oldItem == newItem
    }
}