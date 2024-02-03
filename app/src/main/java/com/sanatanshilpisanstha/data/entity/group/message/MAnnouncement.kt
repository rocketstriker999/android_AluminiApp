package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MAnnouncement(
    @SerializedName("attachment")
    var attachment: List<Any?>?,
    @SerializedName("comments")
    var comments: Int?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: Int?,
    @SerializedName("description")
    var description: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_forwarded")
    var isForwarded: Int?,
    @SerializedName("likes")
    var likes: Int?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?
)