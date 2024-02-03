package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class MutipleChoice(
    @SerializedName("allow_more_ans")
    @Expose
    var allowMoreAns: String?,
    @SerializedName("image")
    @Expose
    var image: String?,
    @SerializedName("options")
    @Expose
    var options: List<Option?>?,
    @SerializedName("question")
    @Expose
    var question: String?,
    @SerializedName("question_optional")
    @Expose
    var questionOptional: String?
)