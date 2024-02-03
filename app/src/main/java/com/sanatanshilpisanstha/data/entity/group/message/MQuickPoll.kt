package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MQuickPoll(
    @SerializedName("choice")
    var choice: List<Choice?>?,
    @SerializedName("close")
    var close: Int?,
    @SerializedName("comments")
    var comments: Int?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: Int?,
    @SerializedName("expiry_time")
    var expiryTime: String?,
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
    var username: String?,
    @SerializedName("visible_only_me")
    var visibleOnlyMe: Int?
)