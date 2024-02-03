package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Degree(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "Degree(id='$id', name='$name')"
    }
}