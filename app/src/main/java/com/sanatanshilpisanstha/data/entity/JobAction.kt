package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class JobAction(

    @SerializedName("group_id")
    var group_id: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("photo")
    var photo: String,

    @SerializedName("datetime")
    var datetime: String,

    @SerializedName("assigned_to")
    var assigned_to: ArrayList<Int>,

    @SerializedName("show_only_me")
    var show_only_me: Int
)