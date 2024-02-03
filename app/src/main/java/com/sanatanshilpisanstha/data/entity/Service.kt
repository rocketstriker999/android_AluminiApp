package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Service(
    @SerializedName("id")
    val id: String,

    @SerializedName("service")
    val service: String
)