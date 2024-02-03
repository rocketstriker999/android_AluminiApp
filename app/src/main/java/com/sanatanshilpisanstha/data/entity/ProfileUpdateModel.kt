package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileUpdateModel {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {

        @SerializedName("profile_pic")
        var profilePic: String? = null
        override fun toString(): String {
            return "Data(profilePic=$profilePic)"
        }


    }

    override fun toString(): String {
        return "ProfileUpdateModel(success=$success, message=$message, data=$data)"
    }
}
