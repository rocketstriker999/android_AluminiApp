package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MText(
    @SerializedName("comments")
    var comments: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_forwarded")
    var isForwarded: String?,
    @SerializedName("likes")
    var likes: String?,
    @SerializedName("message")
    var message: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?
)