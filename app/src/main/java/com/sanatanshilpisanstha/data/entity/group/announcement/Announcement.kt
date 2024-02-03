package com.sanatanshilpisanstha.data.entity.group.announcement

import com.google.gson.annotations.SerializedName

data class Announcement(

    @SerializedName("attachment")
    var attachment: Attachment,

    @SerializedName("description")
    var description: String,

    @SerializedName("group_id")
    var group_id: Int,

    @SerializedName("title")
    var title: String
)