package com.sanatanshilpisanstha.data.entity.group

import com.google.gson.annotations.SerializedName

class MContactFile (
    @SerializedName("comments")
    var comments: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: String?,
    @SerializedName("contact")
    var contact: String?,
    @SerializedName("file")
    var `file`: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_forwarded")
    var isForwarded: String?,
    @SerializedName("likes")
    var likes: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?

)
