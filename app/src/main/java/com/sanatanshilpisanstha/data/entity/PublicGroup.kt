package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class PublicGroup(

    @SerializedName("id")
    val id: String,

    @SerializedName("group_join_code")
    val group_join_code: String,

    @SerializedName("group_name")
    val group_name: String,

    @SerializedName("group_banner")
    val group_banner: String,

    @SerializedName("total_members")
    val total_members: String
)
