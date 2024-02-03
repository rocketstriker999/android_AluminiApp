package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileModel {
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

        @SerializedName("name")
        @Expose
        val name: String? = null

        @SerializedName("phone")
        @Expose
        val phone: String? = null

        @SerializedName("institute_name")
        @Expose
        val institute_name: String? = null

        @SerializedName("degree_name")
        @Expose
        val degree_name: String? = null

        @SerializedName("branch_name")
        @Expose
        val branch_name: String? = null

        @SerializedName("country_name")
        @Expose
        val country_name: String? = null

        @SerializedName("city_name")
        @Expose
        val city_name: String? = null

        @SerializedName("profile_pic")
        @Expose
        val profilePic: String? = null

        @SerializedName("verification_id")
        @Expose
        val verificationID: String? = null

        @SerializedName("sec_verification_id")
        @Expose
        val secVerificationID: String? = null

        @SerializedName("graduation_year")
        @Expose
        val graduationYear: String? = null

        @SerializedName("degree_id")
        @Expose
        val degreeID: Long? = null

        @SerializedName("branch_id")
        @Expose
        val branchID: Long? = null

        @SerializedName("city_id")
        @Expose
        val cityID: Long? = null

        @SerializedName("country_id")
        @Expose
        val countryID: Long? = null


        @SerializedName("pincode")
        @Expose
        val pincode: String? = null

        @SerializedName("address")
        @Expose
        val address: String? = null

        @SerializedName("service_ids")
        @Expose
        val serviceIDS: String? = null

        @SerializedName("designation")
        @Expose
        val designation: String? = null

        @SerializedName("current_company")
        @Expose
        val currentCompany: String? = null

        @SerializedName("linkedin_url")
        @Expose
        val linkedinURL: String? = null

        @SerializedName("about_me")
        @Expose
        val aboutMe: String? = null

        @SerializedName("payment_qr")
        @Expose
        val paymentQr: String? = null

        @SerializedName("payment_link")
        @Expose
        val paymentLink: String? = null

        @SerializedName("institute")
        @Expose
        val institute: String? = null

        @SerializedName("year_of_entry")
        @Expose
        val yearOfEntry: String? = null

        @SerializedName("email_verified_at")
        @Expose
        val emailVerifiedAt: String? = null

        @SerializedName("role")
        @Expose
        val role: String? = null

        @SerializedName("member_verify_by")
        @Expose
        val memberVerifyBy: Long? = null

        @SerializedName("member_status")
        @Expose
        val memberStatus: Long? = null

        @SerializedName("status")
        @Expose
        val status: Long? = null

        @SerializedName("fcm_token")
        @Expose
        val fcmToken: String? = null


        @SerializedName("is_block")
        @Expose
        val isBlock: Long? = null

        @SerializedName("deleted_at")
        @Expose
        val deletedAt: String? = null

        @SerializedName("is_email_verified")
        @Expose
        val isEmailVerified: Long? = null

        @SerializedName("remarks")
        @Expose
        val remarks: String? = null


    }
}