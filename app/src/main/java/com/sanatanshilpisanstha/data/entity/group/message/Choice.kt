package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class Choice(
    @SerializedName("option")
    var option: String?,
    @SerializedName("photo")
    var photo: String?
)