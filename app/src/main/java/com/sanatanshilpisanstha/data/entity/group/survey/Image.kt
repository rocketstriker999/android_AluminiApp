package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Image(
    @SerializedName("images")
    @Expose
    var images: ArrayList<String?>?,
    @SerializedName("question")
    @Expose
    var question: String?,
    @SerializedName("question_optional")
    @Expose
    var questionOptional: String?,
    @SerializedName("restric_single_image")
    @Expose
    var restricSingleImage: String?
)