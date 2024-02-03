package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MQA(
    @SerializedName("comments")
    var comments: Int?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: Int?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_forwarded")
    var isForwarded: Int?,
    @SerializedName("likes")
    var likes: Int?,
    @SerializedName("photo")
    var photo: String?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?
)