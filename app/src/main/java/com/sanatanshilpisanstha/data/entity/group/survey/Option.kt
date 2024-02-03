package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Option(
    @SerializedName("option")
    @Expose
    var option: String?,
    @SerializedName("photo")
    @Expose
    var photo: String?
)