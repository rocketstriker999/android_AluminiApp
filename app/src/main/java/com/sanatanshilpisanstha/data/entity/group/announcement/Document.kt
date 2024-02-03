package com.sanatanshilpisanstha.data.entity.group.announcement

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("ext")
    val ext: String,

    @SerializedName("file")
    val fileName: String
)