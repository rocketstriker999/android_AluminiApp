package com.sanatanshilpisanstha.data.entity.group

import com.google.gson.annotations.SerializedName
import com.sanatanshilpisanstha.data.entity.group.announcement.Attachment

data class GroupMember(

    @SerializedName("id")
    var id: Int?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("profile_pic")
    var profile_pic: String?,

)
