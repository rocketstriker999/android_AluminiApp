package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class MRespond(
    @SerializedName("msg")
    @Expose
    var msg: String?
)