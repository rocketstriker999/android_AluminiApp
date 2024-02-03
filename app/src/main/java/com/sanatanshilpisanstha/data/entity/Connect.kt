package com.sanatanshilpisanstha.data.entity


import com.google.gson.annotations.SerializedName

data class Connect(
    @SerializedName("group_banner")
    var groupBanner: String?,
    @SerializedName("group_join_code")
    var groupJoinCode: String?,
    @SerializedName("group_name")
    var groupName: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_admin")
    var isAdmin: Int?,
    @SerializedName("message")
    var message: String?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?
)