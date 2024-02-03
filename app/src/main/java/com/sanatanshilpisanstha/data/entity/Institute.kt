package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Institute(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "Institute(id='$id', name='$name')"
    }
}