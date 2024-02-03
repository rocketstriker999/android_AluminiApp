package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "City(id='$id', name='$name')"
    }
}