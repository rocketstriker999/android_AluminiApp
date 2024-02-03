package com.sanatanshilpisanstha.data.entity

import com.google.gson.annotations.SerializedName

data class Setting(

    @SerializedName("id")
    var id: Int? = null,

    @SerializedName("logo")
    var logo: String? = null,

    @SerializedName("favicon")
    var favicon: String? = null,

    @SerializedName("app_name")
    var appName: String? = null,

    @SerializedName("primary_color")
    var primaryColor: String? = null,

    @SerializedName("secondary_color")
    var secondaryColor: String? = null,

    @SerializedName("tertiary_color")
    var tertiaryColor: String? = null,

    @SerializedName("quaternary_color")
    var quaternaryColor: String? = null,

    @SerializedName("quinary_color")
    var quinaryColor: String? = null,

    @SerializedName("senary_color")
    var senaryColor: String? = null,

    @SerializedName("footer_text")
    var footerText: String? = null,

    @SerializedName("footer_link")
    var footerLink: String? = null,

    @SerializedName("google_map_api_key")
    var googleMapApiKey: String? = null,

    @SerializedName("facebook")
    var facebook: String? = null,

    @SerializedName("instagram")
    var instagram: String? = null,

    @SerializedName("twitter")
    var twitter: String? = null,

    @SerializedName("linkedin")
    var linkedin: String? = null,

    @SerializedName("dashboard_img")
    var dashboardImg: String? = null,

    @SerializedName("first_section_heading")
    var firstSectionHeading: String? = null,

    @SerializedName("first_section_img")
    var firstSectionImg: String? = null,

    @SerializedName("first_section_content")
    var firstSectionContent: String? = null,

    @SerializedName("sec_section_heading")
    var secSectionHeading: String? = null,

    @SerializedName("sec_section_img")
    var secSectionImg: String? = null,

    @SerializedName("sec_section_content")
    var secSectionContent: String? = null,

    @SerializedName("third_section_heading")
    var thirdSectionHeading: String? = null,

    @SerializedName("third_section_content")
    var thirdSectionContent: String? = null,

    @SerializedName("fourth_section_heading")
    var fourthSectionHeading: String? = null,

    @SerializedName("fourth_section_content")
    var fourthSectionContent: String? = null,

    @SerializedName("pusher_app_id")
    var pusher_app_id: String? = null,

    @SerializedName("pusher_key")
    var pusher_key: String? = null,

    @SerializedName("pusher_secret")
    var pusher_secret: String? = null,

    @SerializedName("pusher_cluster")
    var pusher_cluster: String? = null,

    @SerializedName("agora_app_id")
    var agora_app_id: String? = null,

    @SerializedName("agora_app_certificate")
    var agora_app_certificate: String? = null,

    @SerializedName("total_register_users")
    var total_register_users: String? = null,

    @SerializedName("total_users_in_city")
    var total_users_in_city: String? = null
)


