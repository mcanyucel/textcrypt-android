package com.mustafacanyucel.textcrypt.adapter


import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemClickListener
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemDelete
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemStar
import com.mustafacanyucel.textcrypt.diffCallback.RecentFileItemDiffCallback
import com.mustafacanyucel.textcrypt.model.RecentFileItem
import com.mustafacanyucel.textcrypt.viewHolder.RecentFileItemViewHolder

class RecentFileItemAdapter(private val starClickListener: RecentFileItemStar, private val deleteClickListener: RecentFileItemDelete, private val clickListener: RecentFileItemClickListener) :
    ListAdapter<RecentFileItem, RecentFileItemViewHolder>(
        RecentFileItemDiffCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFileItemViewHolder {
        return RecentFileItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecentFileItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, starClickListener, deleteClickListener, clickListener)
    }
}
