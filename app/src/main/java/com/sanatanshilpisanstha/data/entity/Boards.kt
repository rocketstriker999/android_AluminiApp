package com.sanatanshilpisanstha.data.entity


import com.google.gson.annotations.SerializedName

data class Boards(
    @SerializedName("board_id")
    var boardId: Int?,
    @SerializedName("city")
    var city: String?,
    @SerializedName("cover_image")
    var coverImage: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("description")
    var description: String?,
    @SerializedName("group_id")
    var groupId: Int?,
    @SerializedName("interested")
    var interested: String?,
    @SerializedName("is_cancel")
    var isCancel: Int?,
    @SerializedName("meet_date")
    var meetDate: String?,
    @SerializedName("meet_time")
    var meetTime: String?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("type")
    var type: Int?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?
)