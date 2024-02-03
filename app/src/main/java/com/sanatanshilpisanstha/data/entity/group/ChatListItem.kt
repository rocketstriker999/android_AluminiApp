package com.sanatanshilpisanstha.data.entity.group

open class ChatListItem(
    val type: Int
) {
    companion object {
        const val TYPE_DATE = 0
        const val TYPE_GENERAL = 1
    }
}