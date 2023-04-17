package com.mustafacanyucel.textcrypt.databindingConverter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.mustafacanyucel.textcrypt.R

class BindingAdapters {

    companion object {
        @JvmStatic
        @BindingAdapter("android:isStarred")
        fun setIsStarred(view: ImageView, isStarred: Boolean) {
            if (isStarred) {
                view.imageTintList = view.context.getColorStateList(R.color.yellow)
            } else {
                view.imageTintList = view.context.getColorStateList(R.color.gray)
            }
        }
    }
}