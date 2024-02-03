package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("heading")
    val heading: String,

    @SerializedName("banner")
    val banner: String
)