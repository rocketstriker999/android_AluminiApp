package com.sanatanshilpisanstha.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.JobAction
import com.sanatanshilpisanstha.data.entity.group.*
import com.sanatanshilpisanstha.data.entity.group.announcement.Announcement
import com.sanatanshilpisanstha.data.entity.group.message.*
import com.sanatanshilpisanstha.data.entity.group.survey.Survey
import com.sanatanshilpisanstha.data.enum.APIErrorCode
import com.sanatanshilpisanstha.data.enum.HTTPCode
import com.sanatanshilpisanstha.data.enum.MessageCode
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.remote.APIManager
import com.sanatanshilpisanstha.remote.APIResponse
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.utility.Constant.CHAT_DATA_FORMAT
import com.sanatanshilpisanstha.utility.Constant.SERVER_DATE_FORMAT
import com.sanatanshilpisanstha.utility.Utilities
import com.sanatanshilpisanstha.utility.Utilities.formatDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.coroutines.CoroutineContext


class   GroupRepository(val context: Context) {

    private var preferenceManager: PreferenceManager = PreferenceManager(context)

    private var api: APIManager = APIManager.invoke(context)
    val TAG = "GroupRepository"

    //Create a new Job
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)


    suspend fun postAnnouncement(
        announcement: Announcement,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.postAnnouncement(announcement)
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
                                    APIErrorCode.NO_RESPONSE,
                                    "The phone or password you entered is invalid, Please try again!"
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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

    suspend fun getGroupMember(
        groupId: Int,
        listener: (APIResult<ArrayList<GroupMember>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_id"] = "$groupId"

            val response = try {
                api.getGroupMember(params)
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
                                    var groupMemberList: ArrayList<GroupMember> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {


                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val countryObject = jsonArray.get(i).asJsonObject

                                            groupMemberList.add(

                                                GroupMember(
                                                    if (countryObject.get("id") !== JsonNull.INSTANCE) countryObject.get(
                                                        "id"
                                                    ).asInt else null,
                                                    "" + if (countryObject.get("name") !== JsonNull.INSTANCE) countryObject.get(
                                                        "name"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("profile_pic") !== JsonNull.INSTANCE) countryObject.get(
                                                        "profile_pic"
                                                    ).asString else ""
                                                )
                                            )
                                        }
                                    }
                                    listener(
                                        APIResult.Success(
                                            groupMemberList, msg
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
                                    APIErrorCode.NO_RESPONSE,
                                    "The phone or password you entered is invalid, Please try again!"
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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


    suspend fun postSurvey(
        survey: Survey,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            var gson = Gson()
            var jsonString = gson.toJson(survey)
            Log.i(TAG, "postSurvey: " + jsonString)

            val bodyReq: RequestBody = jsonString
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val response = try {
                api.postSurvey(bodyReq)
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
                                    APIErrorCode.NO_RESPONSE,
                                    "The phone or password you entered is invalid, Please try again!"
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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


    suspend fun postQuickPoll(
        survey: QuickPoll,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val gson = Gson()
            val jsonString = gson.toJson(survey)
            Log.i(TAG, "postSurvey: $jsonString")

            val bodyReq: RequestBody = jsonString
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val response = try {
                api.postQuickPoll(bodyReq)
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
                                    APIErrorCode.NO_RESPONSE,
                                    "The phone or password you entered is invalid, Please try again!"
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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

    suspend fun postQA(
        group_id: String, title: String, photo: String, listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_id"] = group_id
            params["title"] = title
            params["photo"] = photo


            val response = try {
                api.postQA(params)
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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

    suspend fun postPhotoLocation(
        group_id: String,
        photo: String,
        latitude: String,
        longitude: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_id"] = group_id
            params["photo"] = photo
            params["latitude"] = latitude
            params["longitude"] = longitude


            val response = try {
                api.postPhotoLocation(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                response.body()?.asJsonObject?.let { jsonObject ->
                    val apiResponse: APIResponse =
                        Gson().fromJson(jsonObject, object : TypeToken<APIResponse>() {}.type)

                    when (response.code()) {
                        HTTPCode.SUCCESS.code -> {
                            Log.e("PhotoWithLocation1111========>",response.body().toString())
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

                    Log.e("PhotoWithLocation2222========>",jObjError.toString())
                    msg = if (jObjError.has("message")) {
                        jObjError.get("message").toString()
                    } else {
                        context.getString(R.string.error_msg)
                    }
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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


    suspend fun postMeet(
        group_id: String,
        description: String,
        cover_image: String,
        meet_date: String,
        meet_time: String,
        city_id: String,
        title: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_id"] = group_id
            params["description"] = description
            params["cover_image"] = cover_image
            params["meet_date"] = meet_date
            params["meet_time"] = meet_time
            params["city_id"] = city_id
            params["title"] = title


            val response = try {
                api.postMeet(params)
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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

    suspend fun postMessage(
        group_id: String,
        message: String,
        ext: String,
        type: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_id"] = group_id
            params["type"] = type
            params["ext"] = ext
            params["message"] = message

            Log.e("params====>",params.toString());


           val response = try {
                api.postMessage(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                Log.e("response====>",response.toString());

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
                                        Log.e("response====>",jsonObject.get("message").toString());

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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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


    suspend fun postMessageForOneTwoOne(
        user_id: String,
        message: String,
        ext: String,
        type: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["receiver_id"] = user_id
            params["type"] = type
            params["ext"] = ext
            params["message"] = message

            Log.e("params====>",params.toString());


            val response = try {
                api.postDirectoryMessage(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                Log.e("response====>",response.toString());

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
                                        Log.e("response====>",jsonObject.get("message").toString());

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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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

    suspend fun deleteMsg(
        user_id: String,
        messageID: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["id"] = user_id
            params["delete"] = messageID

            Log.e("params====>",params.toString());


            val response = try {
                api.deleteMsg(params)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                Log.e("response====>",response.toString());

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
                                        Log.e("response====>",jsonObject.get("message").toString());

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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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

    suspend fun likeMessage(
        message_id: Int,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["id"] = message_id.toString()
            val response = try {
                api.chatMessageLike(params)
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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

    suspend fun commentMessage(
        message_id: Int,
        message: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["id"] = message_id.toString()
            params["comment"] = message

            val response = try {
                api.chatMessageComment(params)
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

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


    suspend fun getMessageListing(
        group_id: String,
        start: String,
        length: String,
        search: String,
        fromDirectory : Boolean,
        listener: (APIResult<ArrayList<MMessage>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val params = HashMap<String, String>()

             if(fromDirectory) {
                 params["receiver_id"] = group_id
                 params["start"] = start
                 params["length"] = length
                 params["search"] = search
             } else {
                 params["group_id"] = group_id
                 params["start"] = start
                 params["length"] = length
                 params["search"] = search
             }


            val response = try {
                if(fromDirectory) {
                    api.getChatListing(params)
                } else {
                    api.getMessageListing(params)
                }
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
                                    val messageList: ArrayList<MMessage> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {


                                            val msgObject = jsonArray.get(i).asJsonObject
                                            i
                                            val date1 = msgObject.get("created_at").asString
                                            val date = formatDate(
                                                "" + SERVER_DATE_FORMAT,
                                                "" + CHAT_DATA_FORMAT,
                                                date1
                                            ).toString()

                                            val id = msgObject.get("user_id").asInt
                                            val like = msgObject.get("likes").asInt
                                            val comments = msgObject.get("comments").asInt
                                            val messageID = msgObject.get("id").asInt


                                            val mby = preferenceManager.personID != id
                                            if (msgObject.has("type")) {
                                                when (msgObject.get("type").asString) {
                                                    MessageCode.ANNOUNCEMENT.type -> {
                                                        val gson = Gson()
                                                        val mAnnouncement = gson.fromJson(
                                                            msgObject,
                                                            MAnnouncement::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                mAnnouncement,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.ANNOUNCEMENT.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.JOB.type -> {
                                                        val gson = Gson()
                                                        val mjob = gson.fromJson(
                                                            msgObject,
                                                            MJob::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                mjob,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.JOB.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.PHOTO_LOCATION.type -> {
                                                        val gson = Gson()
                                                        val mphotoLoc = gson.fromJson(
                                                            msgObject,
                                                            MPhotoLocation::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                mphotoLoc,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.PHOTO_LOCATION.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.QA.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MQA::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null, null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                data,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.QA.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.MESSAGE.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MText::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                data,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.MESSAGE.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.DOCUMENT.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MDocument::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                data,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.DOCUMENT.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.QUICK_POLL.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MQuickPoll::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                data,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.QUICK_POLL.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.SURVEY.type -> {
                                                        val jsonArray =
                                                            msgObject.getAsJsonArray("questions")
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MSurvey::class.java
                                                        )
                                                        data.questionCount =
                                                            jsonArray.size().toString()
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                data,
                                                                date,
                                                                "",
                                                                MessageCode.SURVEY.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }

                                                    MessageCode.VIDEO.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MVideoLocation::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,

                                                                data,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.VIDEO.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }
                                                    MessageCode.AUDIO.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MAudioFile::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                data,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.AUDIO.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }

                                                    MessageCode.CONTACT.type -> {
                                                        val gson = Gson()
                                                        val data = gson.fromJson(
                                                            msgObject,
                                                            MContactFile::class.java
                                                        )
                                                        messageList.add(
                                                            MMessage(
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                data,
                                                                null,
                                                                null,
                                                                null,
                                                                date,
                                                                "",
                                                                MessageCode.CONTACT.type,
                                                                mby,
                                                                like,
                                                                comments,
                                                                messageID
                                                            )
                                                        )
                                                    }


                                                }
                                            }


//                                            countryList.add(
//                                                Country(
//                                                    "" + countryObject.get("country_name").asString,
//                                                    "" + countryObject.get("id").asString,
//                                                    "" + countryObject.get("phone_code").asString
//                                                )
//                                            )
                                        }
//                                        countryList = Gson().fromJson(
//                                            jsonObject.get("data").asString,
//                                            object : TypeToken<Country>() {}.type
//                                        )

                                    }
                                    var msg = "";
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            messageList, msg
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
                listener(
                    APIResult.Failure(
                        APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                    )
                )

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




    suspend fun getMembers(
        search: String,
        listener: (APIResult<ArrayList<GroupMembersSelect>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["search"] = search
            params["member_status"] = "0"
            params["start"] = "0"
            params["length"] = "50"

            val response = try {
                api.getMembers(params)
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

                                    var connectList: ArrayList<GroupMembersSelect> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {


                                        val gson = GsonBuilder().create()
                                        val connectListType: Type =
                                            object :
                                                TypeToken<ArrayList<GroupMembersSelect?>?>() {}.type
                                        connectList = gson.fromJson(
                                            jsonObject.getAsJsonArray("data"),
                                            connectListType
                                        )


                                    }
                                    var msg = "";
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            connectList, msg
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
                listener(
                    APIResult.Failure(
                        APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                    )
                )

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

    suspend fun postJoinGroup(
        search: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["group_join_code"] = search

            val response = try {
                api.postJoinGroup(params)
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
                listener(
                    APIResult.Failure(
                        APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                    )
                )

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

    suspend fun postCreateGroup(
        group_name: String, group_members: ArrayList<String>, group_banner: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, Any>()
            params["group_name"] = group_name
            params["group_banner"] = group_banner
            params["group_members"] = group_members
            val cal = CreateGroup("" + group_banner, group_members.toList(), group_name)

            val response = try {
                api.postCreateGroup(cal)
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

//
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
                listener(
                    APIResult.Failure(
                        APIErrorCode.NO_RESPONSE, context.getString(R.string.error_msg)
                    )
                )

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


    suspend fun postJob(
        jobAction: JobAction,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.postJob(jobAction)
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
                                    APIErrorCode.NO_RESPONSE,
                                    "The phone or password you entered is invalid, Please try again!"
                                )
                            )
                        }
                    }

                    return
                } ?: run {
                    //Log.e("No Resp")
                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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
//                    if (jObjError.has("data")){
//                       msg =  jObjError.getJSONObject("data").getString("phone").toString()
//                    }

                    listener(
                        APIResult.Failure(
                            APIErrorCode.NO_RESPONSE,
                            "The phone or password you entered is invalid, Please try again!"
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