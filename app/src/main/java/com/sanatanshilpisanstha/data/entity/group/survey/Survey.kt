package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Survey(
    @SerializedName("cover_image")
    @Expose
    var coverImage: String?,
    @SerializedName("description")
    @Expose
    var description: String?,
    @SerializedName("edit_response")
    @Expose
    var editResponse: Int?,
    @SerializedName("expiry_date")
    @Expose
    var expiryDate: String?,
    @SerializedName("group_id")
    @Expose
    var groupId: Int?,
    @SerializedName("location_with_response")
    @Expose
    var locationWithResponse: Int?,
    @SerializedName("multiple_response")
    @Expose
    var multipleResponse: Int?,
    @SerializedName("questions")
    @Expose
    var questions: ArrayList<Question?>?,
    @SerializedName("send_reminder")
    @Expose
    var sendReminder: Int?,
    @SerializedName("title")
    @Expose
    var title: String?,
    @SerializedName("visible_everyone")
    @Expose
    var visibleEveryone: Int?
)