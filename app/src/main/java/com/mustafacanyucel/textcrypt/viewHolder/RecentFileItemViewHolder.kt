package com.mustafacanyucel.textcrypt.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemClickListener
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemDelete
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemStar
import com.mustafacanyucel.textcrypt.databinding.ViewHolderRecentFileItemBinding
import com.mustafacanyucel.textcrypt.model.RecentFileItem

class RecentFileItemViewHolder private constructor(private val binding: ViewHolderRecentFileItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: RecentFileItem, starClickListener: RecentFileItemStar, deleteClickListener: RecentFileItemDelete, clickListener: RecentFileItemClickListener) {
        binding.recentFileItem = item
        binding.clickListenerStar = starClickListener
        binding.clickListenerDelete = deleteClickListener
        binding.clickListener = clickListener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): RecentFileItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ViewHolderRecentFileItemBinding.inflate(layoutInflater, parent, false)
            return RecentFileItemViewHolder(binding)
        }
    }
}