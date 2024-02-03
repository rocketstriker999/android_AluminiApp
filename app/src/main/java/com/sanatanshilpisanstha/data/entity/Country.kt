package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("country_name")
    val country_name: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("phone_code")
    val phone_code: String
)