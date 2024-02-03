package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MSurvey(
    @SerializedName("close")
    var close: Int?,
    @SerializedName("comments")
    var comments: Int?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delete")
    var delete: Int?,
    @SerializedName("description")
    var description: String?,
    @SerializedName("edit_response")
    var editResponse: Int?,
    @SerializedName("expiry_date")
    var expiryDate: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("is_forwarded")
    var isForwarded: Int?,
    @SerializedName("likes")
    var likes: Int?,
    @SerializedName("location_with_response")
    var locationWithResponse: Int?,
    @SerializedName("multiple_response")
    var multipleResponse: Int?,
    @SerializedName("photo")
    var photo: String?,
    @SerializedName("questionCount")
    var questionCount: String?,
    @SerializedName("send_reminder")
    var sendReminder: Int?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("username")
    var username: String?,
    @SerializedName("visible_everyone")
    var visibleEveryone: Int?,
    @SerializedName("respond")
    var respond: MSurveyResponse?,
)