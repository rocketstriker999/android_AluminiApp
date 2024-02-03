package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MPhotoLocation(
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
    @SerializedName("latitude")
    var latitude: Double?,
    @SerializedName("likes")
    var likes: Int?,
    @SerializedName("longitude")
    var longitude: Double?,
    @SerializedName("photo")
    var photo: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?
)