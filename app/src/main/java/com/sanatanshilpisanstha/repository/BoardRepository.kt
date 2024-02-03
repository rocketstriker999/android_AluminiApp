package com.sanatanshilpisanstha.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.enum.APIErrorCode
import com.sanatanshilpisanstha.data.enum.HTTPCode
import com.sanatanshilpisanstha.remote.APIManager
import com.sanatanshilpisanstha.remote.APIResponse
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.utility.Utilities
import org.json.JSONObject

class BoardRepository(val context: Context) {
    private var api: APIManager = APIManager.invoke(context)


    suspend fun starConversation(
        title: String,
        description: String,
        cover_image: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["title"] = title
            params["description"] = description
            params["cover_image"] = cover_image

            val response = try {
                api.startConversationAPi(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                response.body()?.asJsonObject?.let { jsonObject ->
                    val apiResponse: APIResponse =
                        Gson().fromJson(jsonObject, object : TypeToken<APIResponse>() {}.type)

                    when (response.code()) {
                        HTTPCode.SUCCESS.code -> {
                            if (jsonObject.has("success") && !jsonObject.get("success").isJsonNull) {
                                if (jsonObject.get("success").asBoolean) {
                                    var msg = "";
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }
                                    listener(
                                        APIResult.Success(
                                            msg, msg
                                        )
                                    )
                                } else {
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                jsonObject.get("message").asString
                                            )
                                        )
                                    } else {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                context.resources.getString(R.string.error_msg)
                                            )
                                        )
                                    }
                                }
                            } else {
                                listener(
                                    APIResult.Failure(
                                        APIErrorCode.NO_RESPONSE, apiResponse.message
                                    )
                                )
                            }
                        }

                        else -> {
                            listener(
                                APIResult.Failure(
                                    APIErrorCode.NO_RESPONSE, apiResponse.message
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                    return
                }
            } else {

                try {
                    val jObjError = JSONObject(response!!.errorBody()!!.string())
                    var msg = ""
                    msg = if (jObjError.has("message")) {
                        jObjError.get("message").toString()
                    } else {
                        context.getString(R.string.error_msg)
                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, msg
                        )
                    )

                } catch (e: java.lang.Exception) {
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                }
                return
            }
        } else {
            listener(
                APIResult.Failure(
                    APIErrorCode.NETWORK_ERROR, context.getString(R.string.network_error_msg)
                )
            )
            return
        }
    }


    suspend fun createEvent(
        title: String,
        description: String,
        coverImage: String,
        dateTime:String,
        duration:String,
        latitude:String,
        longitude:String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["title"] = title
            params["description"] = description
            params["cover_image"] = coverImage
            params["date_time"] = dateTime
            params["duration"] = duration
            params["latitude"] = latitude
            params["longitude"] = longitude

            val response = try {
                api.createEventAPI(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                response.body()?.asJsonObject?.let { jsonObject ->
                    val apiResponse: APIResponse =
                        Gson().fromJson(jsonObject, object : TypeToken<APIResponse>() {}.type)

                    when (response.code()) {
                        HTTPCode.SUCCESS.code -> {
                            if (jsonObject.has("success") && !jsonObject.get("success").isJsonNull) {
                                if (jsonObject.get("success").asBoolean) {
                                    var msg = "";
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }
                                    listener(
                                        APIResult.Success(
                                            msg, msg
                                        )
                                    )
                                } else {
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                jsonObject.get("message").asString
                                            )
                                        )
                                    } else {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                context.resources.getString(R.string.error_msg)
                                            )
                                        )
                                    }
                                }
                            } else {
                                listener(
                                    APIResult.Failure(
                                        APIErrorCode.NO_RESPONSE, apiResponse.message
                                    )
                                )
                            }
                        }

                        else -> {
                            listener(
                                APIResult.Failure(
                                    APIErrorCode.NO_RESPONSE, apiResponse.message
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                    return
                }
            } else {

                try {
                    val jObjError = JSONObject(response!!.errorBody()!!.string())
                    var msg = ""
                    msg = if (jObjError.has("message")) {
                        jObjError.get("message").toString()
                    } else {
                        context.getString(R.string.error_msg)
                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, msg
                        )
                    )

                } catch (e: java.lang.Exception) {
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                }
                return
            }
        } else {
            listener(
                APIResult.Failure(
                    APIErrorCode.NETWORK_ERROR, context.getString(R.string.network_error_msg)
                )
            )
            return
        }
    }

    suspend fun letsMeets(
        title: String,
        description: String,
        coverImage: String,
        meetDate:String,
        meetTime:String,
        cityId:String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["title"] = title
            params["description"] = description
            params["cover_image"] = coverImage
            params["meet_date"] = meetDate
            params["meet_time"] = meetTime
            params["city_id"] = cityId

            val response = try {
                api.letsMeetsAPI(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                response.body()?.asJsonObject?.let { jsonObject ->
                    val apiResponse: APIResponse =
                        Gson().fromJson(jsonObject, object : TypeToken<APIResponse>() {}.type)

                    when (response.code()) {
                        HTTPCode.SUCCESS.code -> {
                            if (jsonObject.has("success") && !jsonObject.get("success").isJsonNull) {
                                if (jsonObject.get("success").asBoolean) {
                                    var msg = "";
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }
                                    listener(
                                        APIResult.Success(
                                            msg, msg
                                        )
                                    )
                                } else {
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                jsonObject.get("message").asString
                                            )
                                        )
                                    } else {
                                        listener(
                                            APIResult.Failure(
                                                APIErrorCode.NO_RESPONSE,
                                                context.resources.getString(R.string.error_msg)
                                            )
                                        )
                                    }
                                }
                            } else {
                                listener(
                                    APIResult.Failure(
                                        APIErrorCode.NO_RESPONSE, apiResponse.message
                                    )
                                )
                            }
                        }

                        else -> {
                            listener(
                                APIResult.Failure(
                                    APIErrorCode.NO_RESPONSE, apiResponse.message
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                    return
                }
            } else {

                try {
                    val jObjError = JSONObject(response!!.errorBody()!!.string())
                    var msg = ""
                    msg = if (jObjError.has("message")) {
                        jObjError.get("message").toString()
                    } else {
                        context.getString(R.string.error_msg)
                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, msg
                        )
                    )

                } catch (e: java.lang.Exception) {
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                        )
                    )
                }
                return
            }
        } else {
            listener(
                APIResult.Failure(
                    APIErrorCode.NETWORK_ERROR, context.getString(R.string.network_error_msg)
                )
            )
            return
        }
    }
}