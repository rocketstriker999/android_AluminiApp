package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Phone(
    @SerializedName("image")
    @Expose
    var image: String?,
    @SerializedName("question")
    @Expose
    var question: String?,
    @SerializedName("question_optional")
    @Expose
    var questionOptional: String?
)