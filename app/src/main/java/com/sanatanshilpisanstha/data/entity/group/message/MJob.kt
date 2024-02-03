package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class MJob(
    @SerializedName("assigned_to")
    @Expose
    var assignedTo: String?,
    @SerializedName("comments")
    @Expose
    var comments: Int?,
    @SerializedName("created_at")
    @Expose
    var createdAt: String?,
    @SerializedName("datetime")
    @Expose
    var datetime: String?,
    @SerializedName("delete")
    @Expose
    var delete: Int?,
    @SerializedName("description")
    @Expose
    var description: String?,
    @SerializedName("id")
    @Expose
    var id: Int?,
    @SerializedName("is_forwarded")
    @Expose
    var isForwarded: Int?,
    @SerializedName("likes")
    @Expose
    var likes: Int?,
    @SerializedName("photo")
    @Expose
    var photo: String?,
    @SerializedName("respond")
    @Expose
    var respond: MRespond?,
    @SerializedName("show_only_me")
    @Expose
    var showOnlyMe: Int?,
    @SerializedName("type")
    @Expose
    var type: String?,
    @SerializedName("user_id")
    @Expose
    var userId: Int?,
    @SerializedName("username")
    @Expose
    var username: String?
)