package com.mustafacanyucel.textcrypt.clickListener

import com.mustafacanyucel.textcrypt.model.RecentFileItem

class RecentFileItemDelete (val clickListener: (recentFileItem: RecentFileItem) -> Unit) {
    fun onClick(recentFileItem: RecentFileItem) = clickListener(recentFileItem)
}