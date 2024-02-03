package com.sanatanshilpisanstha.data.entity.group


import com.google.gson.annotations.SerializedName
import com.sanatanshilpisanstha.data.entity.group.survey.Option

data class QuickPoll(
    @SerializedName("expiry_time")
    var expiryTime: String?,
    @SerializedName("group_id")
    var groupId: Int?,
    @SerializedName("options")
    var options: List<Option?>?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("visible_only_me")
    var visibleOnlyMe: Int?
)