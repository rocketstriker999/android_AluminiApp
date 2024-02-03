package com.sanatanshilpisanstha.data.entity.group.survey


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Question(
    @SerializedName("date")
    @Expose
    var date: Date?,
    @SerializedName("dropdown")
    @Expose
    var dropdown: Dropdown?,
    @SerializedName("image")
    @Expose
    var image: Image?,
    @SerializedName("mutiple_choice")
    @Expose
    var mutipleChoice: MutipleChoice?,
    @SerializedName("numeric")
    @Expose
    var numeric: Numeric?,
    @SerializedName("phone")
    @Expose
    var phone: Phone?,
    @SerializedName("text")
    @Expose
    var text: Text?
)