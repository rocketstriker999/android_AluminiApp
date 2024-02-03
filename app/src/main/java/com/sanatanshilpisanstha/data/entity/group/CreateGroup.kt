package com.sanatanshilpisanstha.data.entity.group


import com.google.gson.annotations.SerializedName

data class CreateGroup(
    @SerializedName("group_banner")
    var groupBanner: String?,
    @SerializedName("group_members")
    var groupMembers: List<String?>?,
    @SerializedName("group_name")
    var groupName: String?
)