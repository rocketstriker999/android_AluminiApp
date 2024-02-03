package com.sanatanshilpisanstha.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.*
import com.sanatanshilpisanstha.data.entity.group.message.LikeCommentModel
import com.sanatanshilpisanstha.data.enum.APIErrorCode
import com.sanatanshilpisanstha.data.enum.HTTPCode
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.remote.APIManager
import com.sanatanshilpisanstha.remote.APIResponse
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.utility.Constant.RitanyaSansthaPackageName
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext


class AccountRepository(val context: Context) {

    private var preferenceManager: PreferenceManager = PreferenceManager(context)

    private var api: APIManager = APIManager.invoke(context)

    //Create a new Job
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private var packageName = context.packageName

    suspend fun login(
        phone: String, password: String, token: String, listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["phone"] = phone
            params["password"] = password
            params["fcm_token"] = token
            params["type"] = "A"


            val response = try {
                api.login(params)
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
                                    var user = User()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        user = Gson().fromJson(
                                            jsonObject.get("data").asJsonObject,
                                            object : TypeToken<User>() {}.type
                                        )

                                        preferenceManager.isUserLoggedIn = true
                                        preferenceManager.accessToken = user.token.toString()
                                        preferenceManager.personEmail = user.email.toString()
                                        preferenceManager.personName = user.name.toString()
                                        preferenceManager.personProfile = user.profilePic.toString()
                                        preferenceManager.personID = user.id?.toInt() ?: 0
                                        preferenceManager.personNumber = user.phone.toString()
                                        preferenceManager.personCountry = user.countryId.toString()
                                        preferenceManager.personCity = user.cityId.toString()
                                        preferenceManager.personLinkedinUrl =
                                            user.linkedin_url.toString()
                                        if (packageName.equals(RitanyaSansthaPackageName)) {
                                            preferenceManager.personDegree =
                                                user.degree_id.toString()
                                            preferenceManager.personBranch =
                                                user.branch_id.toString()
                                            preferenceManager.personGraduationYear =
                                                user.graduation_year.toString()
                                            preferenceManager.personYearEntity =
                                                user.year_of_entry.toString()
                                            preferenceManager.personDesignation =
                                                user.designation.toString()
                                            preferenceManager.personAboutMe =
                                                user.about_me.toString()
                                            preferenceManager.personRemarks =
                                                user.remarks.toString()
                                            preferenceManager.personInstitute =
                                                user.institute.toString()
                                            preferenceManager.personVerificationID =
                                                user.verification_id.toString()
                                        }


                                    }
                                    var msg = ""
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
                                    context.resources.getString(R.string.error_msg)
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
                            context.resources.getString(R.string.error_msg)
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
                            msg
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

    suspend fun logout(
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val response = try {
                api.logout()
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
                                    var msg = ""
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
                                    context.resources.getString(R.string.error_msg)
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
                            context.resources.getString(R.string.error_msg)
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
                            APIErrorCode.NO_RESPONSE,
                            msg
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

    suspend fun deleteAccount(
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val response = try {
                api.deleteAccount()
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
                                    var msg = ""
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
                                    context.resources.getString(R.string.error_msg)
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
                            context.resources.getString(R.string.error_msg)
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
                            APIErrorCode.NO_RESPONSE,
                            msg
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

    suspend fun resetPassword(
        phone: String, password: String, cPassword: String, listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["phone"] = phone
            params["password"] = password
            params["password_confirmation"] = cPassword


            val response = try {
                api.resetPassword(params)
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
                                    var user = User()
                                    var msg = ""
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


    suspend fun register(
        name: String,
        city_id: String,
        country_id: String,
        email: String,
        phone: String,
        password: String,
        fcm_token: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["name"] = name
            params["city_id"] = city_id
            params["country_id"] = country_id
            params["email"] = email
            params["phone"] = phone
            params["password"] = password
            params["fcm_token"] = fcm_token


            val response = try {
                api.register(params)
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
                                    var msg = ""
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
                    if (jObjError.has("data")) {
                        val dataobj = jObjError.getJSONObject("data")
                        when {
                            dataobj.has("phone") -> {
                                msg = dataobj.getJSONArray("phone")[0].toString()

                            }
                            dataobj.has("email") -> {
                                msg = dataobj.getJSONArray("email")[0].toString()
                            }
                            dataobj.has("password") -> {
                                msg = dataobj.getJSONArray("password")[0].toString()
                            }
                        }

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

    suspend fun register2(
        institute_id: String,
        name: String,
        city_id: String,
        country_id: String,
        email: String,
        country_code: String,
        phone: String,
        password: String,
        degree_id: String,
        branch_id: String,
        graduation_year: String,
        fcm_token: String,
        verification_id: String,
        ext:String,
        remarks: String,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["institute"] = institute_id
            params["name"] = name
            params["email"] = email
            params["country_id"] = country_id
            params["city_id"] = city_id
            params["degree_id"] = degree_id
            params["branch_id"] = branch_id
            params["graduation_year"] = graduation_year
            params["country_code"]= country_code
            params["phone"] = phone
            params["password"] = password
            params["fcm_token"] = fcm_token
            params["verification_id"] = verification_id
            params["ext"] = ext
            params["remarks"] = remarks

            val response = try {
                api.register2(params)
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
                                    var msg = ""
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
                    if (jObjError.has("data")) {
                        val dataobj = jObjError.getJSONObject("data")
                        when {
                            dataobj.has("phone") -> {
                                msg = dataobj.getJSONArray("phone")[0].toString()

                            }
                            dataobj.has("email") -> {
                                msg = dataobj.getJSONArray("email")[0].toString()
                            }
                            dataobj.has("password") -> {
                                msg = dataobj.getJSONArray("password")[0].toString()
                            }
                        }

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

    suspend fun editProfile(
        name: String,
        email: String,
        city_id: String,
        country_id: String,
        phone: String,
        address: String,
        verification_id: String,
        sec_verification_id: String,
        payment_qr: String,
        payment_link: String,
        service_ids: Array<String>,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["name"] = name
            params["city_id"] = city_id
            params["country_id"] = country_id
//            params["phone"] = phone
            params["verification_id"] = verification_id
            params["sec_verification_id"] = sec_verification_id
            params["address"] = address
            params["payment_qr"] = payment_qr
            params["payment_link"] = payment_link
            params["service_ids"] = service_ids.toString()
            params["email"] = email


            val response = try {
                api.updateProfile(params)
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
                                    var msg = ""
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
                    if (jObjError.has("data")) {
                        val dataobj = jObjError.getJSONObject("data")
                        when {
                            dataobj.has("phone") -> {
                                msg = dataobj.getJSONArray("phone")[0].toString()

                            }
                            dataobj.has("email") -> {
                                msg = dataobj.getJSONArray("email")[0].toString()
                            }
                            dataobj.has("password") -> {
                                msg = dataobj.getJSONArray("password")[0].toString()
                            }
                        }

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

    /* For Rityana */
    suspend fun updateProfile(
        name: String,
        email: String,
        city_id: String,
        country_id: String,
        institute: String,
        degree_id: String,
        branch_id: String,
        verification_id: String,
        year_of_entry: String,
        graduation_year: String,
        linkedin_url: String,
        designation: String,
        about_me: String,
        ext: String,
        pincode:String,
        address:String,
        remarks:String,
        //service_ids: Array<String>,
        listener: (APIResult<String>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["name"] = name
            params["email"] = email
            params["country_id"] = country_id
            params["city_id"] = city_id
            params["institute"] = institute
            params["degree_id"] = degree_id
            params["branch_id"] = branch_id
            params["branch_id"] = branch_id
            params["year_of_entry"] = year_of_entry
            params["graduation_year"] = graduation_year
            params["linkedin_url"] = linkedin_url
            params["designation"] = designation
            //params["service_ids"] = service_ids.toString()
            params["about_me"] = about_me
            params["verification_id"] = verification_id
            params["ext"] = ext
            params["pincode"] = pincode
            params["address"] = address
            params["remarks"] = remarks

            val response = try {
                api.updateProfile(params)
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
                                    Log.e("response==========>", response.body().toString())
                                    preferenceManager.personEmail = email
                                    preferenceManager.personName = name
                                    preferenceManager.personCountry = country_id
                                    preferenceManager.personCity = city_id
                                    preferenceManager.personLinkedinUrl = linkedin_url

                                    preferenceManager.personDegree = degree_id
                                    preferenceManager.personBranch = branch_id
                                    preferenceManager.personGraduationYear =
                                        graduation_year
                                    preferenceManager.personYearEntity = year_of_entry
                                    preferenceManager.personDesignation = designation
                                    preferenceManager.personAboutMe = about_me

                                    var msg = ""
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
                                                jsonObject.get("message").asString,
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
                    if (jObjError.has("data")) {
                        val dataobj = jObjError.getJSONObject("data")
                        when {
                            dataobj.has("phone") -> {
                                msg = dataobj.getJSONArray("phone")[0].toString()

                            }
                            dataobj.has("email") -> {
                                msg = dataobj.getJSONArray("email")[0].toString()
                            }
                            dataobj.has("password") -> {
                                msg = dataobj.getJSONArray("password")[0].toString()
                            }
                        }

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


    suspend fun getCountry(
        listener: (APIResult<ArrayList<Country>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getCountry()
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
                                    val countryList: ArrayList<Country> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val countryObject = jsonArray.get(i).asJsonObject

                                            countryList.add(
                                                Country(
                                                    "" + countryObject.get("country_name").asString,
                                                    "" + countryObject.get("id").asString,
                                                    "" + countryObject.get("phone_code").asString
                                                )
                                            )
                                        }
//                                        countryList = Gson().fromJson(
//                                            jsonObject.get("data").asString,
//                                            object : TypeToken<Country>() {}.type
//                                        )

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            countryList, msg
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

    suspend fun getCity(
        countryId: String, listener: (APIResult<ArrayList<City>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["country_id"] = countryId
            val response = try {
                api.getCity(params)
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
                                    var cityList: ArrayList<City> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val cityObject = jsonArray.get(i).asJsonObject

                                            cityList.add(
                                                City(
                                                    "" + cityObject.get("id").asString,
                                                    "" + cityObject.get("name").asString,
                                                )
                                            )
                                        }

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            cityList, msg
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

    suspend fun getInstitute(
        listener: (APIResult<ArrayList<Institute>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()

            val response = try {
                api.getInstitute()
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
                                    var instituteList: ArrayList<Institute> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        Log.e("jsonArray=======>",jsonArray.toString())
                                        for (i in 0 until jsonArray.size()) {
                                            val cityObject = jsonArray.get(i).asJsonObject

                                            instituteList.add(
                                                Institute(
                                                    "" + cityObject.get("id").asString,
                                                    "" + cityObject.get("name").asString,
                                                )
                                            )
                                        }

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            instituteList, msg
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

    suspend fun getDegree(
        listener: (APIResult<ArrayList<Degree>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()

            val response = try {
                api.getDegree()
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
                                    var degreeList: ArrayList<Degree> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val cityObject = jsonArray.get(i).asJsonObject

                                            degreeList.add(
                                                Degree(
                                                    "" + cityObject.get("id").asString,
                                                    "" + cityObject.get("name").asString,
                                                )
                                            )
                                        }

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            degreeList, msg
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


    suspend fun getBranch(
        listener: (APIResult<ArrayList<Branch>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()

            val response = try {
                api.getBranch()
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
                                    var branchList: ArrayList<Branch> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val cityObject = jsonArray.get(i).asJsonObject

                                            branchList.add(
                                                Branch(
                                                    "" + cityObject.get("id").asString,
                                                    "" + cityObject.get("code").asString,
                                                    "" + cityObject.get("name").asString,
                                                )
                                            )
                                        }

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            branchList, msg
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


    suspend fun getLikeComments(
        messageID: String, listener: (APIResult<LikeCommentModel>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["id"] = messageID
            val response = try {
                api.getLikeComment(params)
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
                                    val doctorListModel: LikeCommentModel = Gson().fromJson(
                                        jsonObject.toString(),
                                        LikeCommentModel::class.java
                                    )

                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            doctorListModel, msg
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

    suspend fun getService(
        listener: (APIResult<ArrayList<Service>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getService()
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
                                    var dataList: ArrayList<Service> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val dataObject = jsonArray.get(i).asJsonObject

                                            dataList.add(
                                                Service(
                                                    "" + dataObject.get("id").asString,
                                                    "" + dataObject.get("service").asString,
                                                )
                                            )
                                        }

                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            dataList, msg
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

    suspend fun getProfile(
       listener: (APIResult<ProfileModel>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val response = try {
                api.getProfile()
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
                                    val profileModel: ProfileModel = Gson().fromJson(
                                        jsonObject.toString(),
                                        ProfileModel::class.java
                                    )

                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            profileModel, msg
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

    suspend fun uploadProfilePic(profilePic: String, listener: (APIResult<ProfileUpdateModel>) -> Unit) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["profile_pic"] = profilePic
            val response = try {
                api.updateProfilePic(params)
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

                                    val profileUpdateModel: ProfileUpdateModel = Gson().fromJson(
                                        jsonObject.toString(),
                                        ProfileUpdateModel::class.java
                                    )


                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            profileUpdateModel, msg
                                        ))

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


    suspend fun getOtherUserProfile(
        UserID: String, listener: (APIResult<ProfileModel>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getUserProfile(UserID.toInt())
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
                                    val profileModel: ProfileModel = Gson().fromJson(
                                        jsonObject.toString(),
                                        ProfileModel::class.java
                                    )

                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            profileModel, msg
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

}