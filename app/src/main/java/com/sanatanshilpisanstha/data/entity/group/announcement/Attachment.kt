package com.sanatanshilpisanstha.data.entity.group.announcement

import com.google.gson.annotations.SerializedName

data class Attachment(
    @SerializedName("audio")
    var audio: String?,

    @SerializedName("document")
    var document: Document?,

    @SerializedName("photos")
    val photos: ArrayList<String>
)