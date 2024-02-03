package com.sanatanshilpisanstha.data.entity.group


import com.google.gson.annotations.SerializedName

data class GroupMembersSelect(
    @SerializedName("city")
    var city: String?,
    @SerializedName("email")
    var email: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("phone")
    var phone: String?,
    @SerializedName("profile_pic")
    var profilePic: String?,
    @SerializedName("sec_verification_id")
    var secVerificationId: String?,
    @SerializedName("selected")
    var selected: Boolean = false,
    @SerializedName("updated_by")
    var updatedBy: Any?,
    @SerializedName("verification_id")
    var verificationId: String?
)