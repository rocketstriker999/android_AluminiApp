package com.sanatanshilpisanstha.remote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sanatanshilpisanstha.data.entity.JobAction
import com.sanatanshilpisanstha.data.entity.group.CreateGroup
import com.sanatanshilpisanstha.data.entity.group.announcement.Announcement
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.ui.LoginActivity
import com.sanatanshilpisanstha.utility.AppURL
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.Collections
import java.util.concurrent.TimeUnit

interface APIManager {

    companion object {

        /**
        authorized api key required to be sent in header of api call request
         */
        private const val API_KEY = "4a7PhUYdDxbT3W575b2EuAsK9RkwgcA3WDqPBXZutkLNMOuqeN"

        private fun getOkHttpClient(context: Context): OkHttpClient {
            var request: Request?
            var preferenceManager = PreferenceManager(context)
            val interceptor = HttpLoggingInterceptor()
            interceptor.apply {
                interceptor.level = HttpLoggingInterceptor.Level.BODY
            }

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)

            httpClient.protocols(Collections.singletonList(Protocol.HTTP_1_1) );

            httpClient.addInterceptor { chain ->
                val original = chain.request()

                val hasToken: Boolean = preferenceManager.isUserLoggedIn
//                Log.i("token " + userRepository.getAccessToken())
                when {
                    hasToken -> {
                        request = original.newBuilder()

                            .header("Accept", "application/json")
                            .header(
                                "Authorization",
                                "Bearer ${preferenceManager.accessToken.toString()}"
                            )
                            .method(original.method, original.body)
                            .build()
                    }
                    else -> {
                        request = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("acceptLanguage", "en")
                            .method(original.method, original.body)
                            .build()
                    }
                }

                val response = chain.proceed(request!!)
                val responseBody = response.body
                val contentType = responseBody?.contentType()
                val content = responseBody?.string()

                try {
                    val objResponse = Gson().fromJson(content, JsonElement::class.java).asJsonObject
                    if (response.code == 401) {
                        try {
                            val message = when {
                                objResponse.has("message") -> objResponse.get("message").asString
                                else -> ""
                            }

                            if (context is Activity) {
                                context.runOnUiThread {
                                    if (message.isNotBlank()) {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }
                            }

                            preferenceManager.clearSession()
                            context.startActivity(
                                Intent(context, LoginActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }

                response.newBuilder().body(ResponseBody.create(contentType, content.toString()))
                    .build()
            }

            return httpClient.build()
        }

        operator fun invoke(context: Context): APIManager {
            val retrofit = Retrofit.Builder()
                .baseUrl(AppURL.API.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    getOkHttpClient(
                        context
                    )
                )
                .build()

            return retrofit.create(APIManager::class.java)
        }
    }

    // login user.
    @FormUrlEncoded
    @POST(AppURL.API.REGISTER)
    suspend fun register(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.REGISTER)
    suspend fun register2(@FieldMap params: Map<String, String>): Response<JsonElement>


    @FormUrlEncoded
    @POST(AppURL.API.QA)
    suspend fun postQA(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.PHOTO_LOCATION)
    suspend fun postPhotoLocation(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.MEET)
    suspend fun postMeet(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.UPDATE_PROFILE)
    suspend fun updateProfile(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.LOGIN)
    suspend fun login(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.MESSAGE_LISTINHG)
    suspend fun getMessageListing(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.ChatListing)
    suspend fun getChatListing(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.POST_MESSAGE)
    suspend fun postMessage(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.POST_DIRECTORY_MESSAGE)
    suspend fun postDirectoryMessage(@FieldMap params: Map<String, String>): Response<JsonElement>


    @FormUrlEncoded
    @POST(AppURL.API.deleteMsg)
    suspend fun deleteMsg(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.RESET_PASSWORD)
    suspend fun resetPassword(@FieldMap params: Map<String, String>): Response<JsonElement>

    @GET(AppURL.API.COUNTRY)
    suspend fun getCountry(): Response<JsonElement>

    @POST(AppURL.API.BANNERS)
    suspend fun getBanners(): Response<JsonElement>

    @GET(AppURL.API.PUBLIC_GROUP)
    suspend fun getPublicGroup(): Response<JsonElement>

    @POST(AppURL.API.CONNECT)
    suspend fun getConnect(): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.MEMBERS)
    suspend fun getMembers(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.JOIN_GROUP)
    suspend fun postJoinGroup(@FieldMap params: Map<String, String>): Response<JsonElement>

    @POST(AppURL.API.CREATE_GROUP)
    suspend fun postCreateGroup(@Body requestData: CreateGroup): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.DIRECTORY)
    suspend fun getDirectory(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.BOARDS)
    suspend fun getBoards(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.EXPLORE_MAP)
    suspend fun getMapMarker(@FieldMap params: Map<String, String>): Response<JsonElement>

    @POST(AppURL.API.GALLERIES)
    suspend fun getGalleryImage(): Response<JsonElement>

    @POST(AppURL.API.ANNOUNCEMENT)
    suspend fun postAnnouncement(@Body requestData: Announcement): Response<JsonElement>

    @POST(AppURL.API.SURVEY)
    suspend fun postSurvey(@Body params: RequestBody): Response<JsonElement>

    @POST(AppURL.API.POLL)
    suspend fun postQuickPoll(@Body params: RequestBody): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.GROUP_MEMBER)
    suspend fun getGroupMember(@FieldMap params: Map<String, String>): Response<JsonElement>

    @POST(AppURL.API.JOB)
    suspend fun postJob(@Body jobAction: JobAction): Response<JsonElement>

    @GET(AppURL.API.SYSTEM_SETTING)
    suspend fun getSetting(): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.CITY)
    suspend fun getCity(@FieldMap params: Map<String, String>): Response<JsonElement>

    @GET(AppURL.API.Institute)
    suspend fun getInstitute(): Response<JsonElement>

    @GET(AppURL.API.Degree)
    suspend fun getDegree(): Response<JsonElement>

    @GET(AppURL.API.Branch)
    suspend fun getBranch(): Response<JsonElement>

    @GET(AppURL.API.SERVICE)
    suspend fun getService(): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.ChatMessageLike)
    suspend fun chatMessageLike(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.ChatMessageComment)
    suspend fun chatMessageComment(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.LikeComment)
    suspend fun getLikeComment(@FieldMap params: Map<String, String>): Response<JsonElement>

    @GET(AppURL.API.Logout)
    suspend fun logout(): Response<JsonElement>

    @GET(AppURL.API.deleteAccount)
    suspend fun deleteAccount(): Response<JsonElement>

    @GET(AppURL.API.getProfileDetails)
    suspend fun getProfile(): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.UpdateProfilePic)
    suspend fun updateProfilePic(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.startConversation)
    suspend fun startConversationAPi(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.event)
    suspend fun createEventAPI(@FieldMap params: Map<String, String>): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.boardMeet)
    suspend fun letsMeetsAPI(@FieldMap params: Map<String, String>): Response<JsonElement>

    @GET(AppURL.API.getProfileDetails +"/{UserID}")
    suspend fun getUserProfile(@Path("UserID") UserID: Int): Response<JsonElement>


    @POST(AppURL.API.AgoraToken)
    suspend fun getAgoraToken(@Body params: JsonObject): Response<JsonElement>

    @FormUrlEncoded
    @POST(AppURL.API.updateLatLng)
    suspend fun updateLatLng(@FieldMap params: Map<String, String>): Response<JsonElement>


}