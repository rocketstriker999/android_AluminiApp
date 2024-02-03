package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Branch(
    @SerializedName("id")
    val id: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "Degree(id='$id', code='$code, name='$name')"
    }
}