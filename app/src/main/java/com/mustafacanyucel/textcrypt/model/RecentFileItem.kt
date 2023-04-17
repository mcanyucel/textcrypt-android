package com.mustafacanyucel.textcrypt.model

import java.util.Date

data class RecentFileItem (val name: String, val uri: String, val date: Date, var isStarred: Boolean) {
    override fun toString(): String {
        return "$name|$uri|$date|$isStarred"
    }

    companion object {
        fun fromString(string: String): RecentFileItem {
            val split = string.split("|")
            return RecentFileItem(split[0], split[1], Date(split[2]), split[3].toBoolean())
        }
    }
}