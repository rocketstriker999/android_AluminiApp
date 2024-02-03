package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

class User(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("phone") var phone: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("profile_pic") var profilePic: String? = null,
    @SerializedName("city_id") var cityId: String? = null,
    @SerializedName("country_id") var countryId: Int? = null,
    @SerializedName("token") var token: String? = null,
    @SerializedName("graduation_year") var graduation_year: String? = null,
    @SerializedName("degree_id") var degree_id: String? = null,
    @SerializedName("branch_id") var branch_id: String? = null,
    @SerializedName("linkedin_url") var linkedin_url: String? = null,
    @SerializedName("year_of_entry") var year_of_entry: String? = null,
    @SerializedName("designation") var designation: String? = null,
    @SerializedName("about_me") var about_me: String? = null,
    @SerializedName("remarks") var remarks: String? = null,
    @SerializedName("institute") var institute: String? = null,
    @SerializedName("verification_id") var verification_id: String? = null,

    )