package com.sanatanshilpisanstha.data.entity.group.message


import com.google.gson.annotations.SerializedName

data class MSurveyResponse(
    @SerializedName("total")
    var total: String?,
    @SerializedName("total_my_resonse")
    var totalMyResonse: String?
)