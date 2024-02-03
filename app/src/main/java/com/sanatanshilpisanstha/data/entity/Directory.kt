package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Directory(

    @SerializedName("name")
    val name: String?="",

    @SerializedName("id")
    val id: String?="0",

    @SerializedName("profile_pic")
    val profile: String?="",

    @SerializedName("city")
    val city: String?="",

    @SerializedName("distance")
    val distance: String?="",

    var selected: Boolean = false,


)
