package com.sanatanshilpisanstha.data.entity


import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class MapMarker(
    @SerializedName("city")
    var city: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("latitude")
    var latitude: Double?,
    @SerializedName("longitude")
    var longitude: Double?,
    @SerializedName("name")
    var name: String?
)