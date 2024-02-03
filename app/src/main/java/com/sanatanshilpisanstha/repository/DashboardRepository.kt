package com.sanatanshilpisanstha.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Banner
import com.sanatanshilpisanstha.data.entity.Boards
import com.sanatanshilpisanstha.data.entity.Connect
import com.sanatanshilpisanstha.data.entity.Directory
import com.sanatanshilpisanstha.data.entity.GalleryImage
import com.sanatanshilpisanstha.data.entity.MapMarker
import com.sanatanshilpisanstha.data.entity.PublicGroup
import com.sanatanshilpisanstha.data.entity.Setting
import com.sanatanshilpisanstha.data.enum.APIErrorCode
import com.sanatanshilpisanstha.data.enum.HTTPCode
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.remote.APIManager
import com.sanatanshilpisanstha.remote.APIResponse
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.coroutines.CoroutineContext


class DashboardRepository(val context: Context) {

    private var preferenceManager: PreferenceManager = PreferenceManager(context)

    private var api: APIManager = APIManager.invoke(context)

    //Create a new Job
    private val parentJob = Job()
    var city = "";
    var id = 0;
    var latitude = 0.0;
    var longitude = 0.0;
    var name = "";

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)


    suspend fun getBanners(
        listener: (APIResult<ArrayList<Banner>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getBanners()
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
                                    val bannerlist: ArrayList<Banner> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val countryObject = jsonArray.get(i).asJsonObject

                                            bannerlist.add(
                                                Banner(
                                                    "" + if (countryObject.get("heading") !== JsonNull.INSTANCE) countryObject.get(
                                                        "heading"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("banner") !== JsonNull.INSTANCE) countryObject.get(
                                                        "banner"
                                                    ).asString else ""
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
                                            bannerlist, msg
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

    suspend fun getPublicGroup(
        listener: (APIResult<ArrayList<PublicGroup>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getPublicGroup()
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
                                    val grouplist: ArrayList<PublicGroup> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val countryObject = jsonArray.get(i).asJsonObject
                                            var aa =
                                                if (countryObject.get("total_members") !== JsonNull.INSTANCE) countryObject.get(
                                                    "total_members"
                                                ).asString else ""

                                            grouplist.add(
                                                PublicGroup(
                                                    "" + if (countryObject.get("id") !== JsonNull.INSTANCE) countryObject.get(
                                                        "id"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("group_join_code") !== JsonNull.INSTANCE) countryObject.get(
                                                        "group_join_code"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("group_name") !== JsonNull.INSTANCE) countryObject.get(
                                                        "group_name"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("group_banner") !== JsonNull.INSTANCE) countryObject.get(
                                                        "group_banner"
                                                    ).asString else "",
                                                    "" + if (countryObject.get("total_members") !== JsonNull.INSTANCE) countryObject.get(
                                                        "total_members"
                                                    ).asString else ""
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
                                            grouplist, msg
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

    suspend fun getConnect(
        listener: (APIResult<ArrayList<Connect>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getConnect()
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

                                    var connectList: ArrayList<Connect> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {


                                        val gson = GsonBuilder().create()
                                        val connectListType: Type =
                                            object : TypeToken<ArrayList<Connect?>?>() {}.type
                                        connectList = gson.fromJson(
                                            jsonObject.getAsJsonArray("data"),
                                            connectListType
                                        )


                                    }
                                    var msg = ""
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

    /*  suspend fun getDirectory(
          latitude: Double, longitude: Double, start: Int,length: Int,

          search: String,listener: (APIResult<String>) -> Unit
      ) {
          if (Utilities.isNetworkAvailable(context)) {
              listener(APIResult.InProgress)

              val params = HashMap<String, String>()
              params["latitude"] = latitude.toString()
              params["longitude"] = longitude.toString()
              params["start"] = start.toString()
              params["length"] = length.toString()
              params["search"] = search.toString()



              val response = try {
                  api.getDirectory(params)
              } catch (e: Exception) {
                  null
              }


              if (response != null && response.isSuccessful) {
                  Log.e("Success===>",response.body().toString())

                  response.body()?.asJsonObject?.let { jsonObject ->
                      val apiResponse: APIResponse =
                          Gson().fromJson(jsonObject, object : TypeToken<APIResponse>() {}.type)

                      when (response.code()) {
                          HTTPCode.SUCCESS.code -> {
                              Log.e("Success===>",jsonObject.asString)

                              if (jsonObject.has("success") && !jsonObject.get("success").isJsonNull) {
                                  if (jsonObject.get("success").asBoolean) {

                                      Log.e("Success===>",jsonObject.asString)

                                      var directoryList: ArrayList<Directory> = arrayListOf()
                                      if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {


                                          val gson = GsonBuilder().create()
                                          val connectListType: Type =
                                              object : TypeToken<ArrayList<Directory?>?>() {}.type
                                          directoryList = gson.fromJson(
                                              jsonObject.getAsJsonArray("data"),
                                              connectListType
                                          )

                                          Log.e("directoryList=====>",directoryList.toString())

                                      }
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
  */

    suspend fun getDirectory(latitude: Double, longitude: Double, start: Int, length: Int,search: String, listener: (APIResult<ArrayList<Directory>>) -> Unit) {

        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["latitude"] = latitude.toString()
            params["longitude"] = longitude.toString()
            params["start"] = start.toString()
            params["length"] = length.toString()
            params["search"] = search

            Log.e("params=====>",params.toString())
            val response = try {

                api.getDirectory(params)
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
                                    var directryList: ArrayList<Directory> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val gson = GsonBuilder().create()
                                        val connectListType: Type =
                                            object : TypeToken<ArrayList<Directory?>?>() {}.type
                                        directryList = gson.fromJson(
                                            jsonObject.getAsJsonArray("data"),
                                            connectListType
                                        )
                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            directryList, msg
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

    suspend fun getBoards(start: Int, length: Int,type: String, listener: (APIResult<ArrayList<Boards>>) -> Unit) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()

            params["start"] = start.toString()
            params["length"] = length.toString()
            if (type.isNotBlank()) {
                params["type"] = type
            }

            val response = try {

                api.getBoards(params)
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
                                    var list: ArrayList<Boards> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val gson = GsonBuilder().create()
                                        val connectListType: Type =
                                            object : TypeToken<ArrayList<Boards?>?>() {}.type
                                        list = gson.fromJson(
                                            jsonObject.getAsJsonArray("data"),
                                            connectListType
                                        )
                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            list, msg
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

    suspend fun getMapMarker(
        start: Int, length: Int,
        listener: (APIResult<ArrayList<MapMarker>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()

            params["start"] = start.toString()
            params["length"] = length.toString()
            val response = try {

                Log.e("params====>", params.toString())
                api.getMapMarker(params)
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
                                    val list: ArrayList<MapMarker> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val userJsonArray = jsonObject.getAsJsonObject("data")
                                            .getAsJsonArray("users")
                                        for (i in 0 until userJsonArray.size()) {
                                            val mapMarkerObject = userJsonArray.get(i).asJsonObject
                                            Log.e(
                                                "mapMarkerObject=====>",
                                                mapMarkerObject.toString()
                                            )
                                            if (mapMarkerObject != null && !mapMarkerObject.isEmpty) {

                                                city =
                                                    if (mapMarkerObject.get("city") !== JsonNull.INSTANCE) mapMarkerObject.get(
                                                        "city"
                                                    ).getAsString() else ""

                                                id =
                                                    if (mapMarkerObject.get("id") !== JsonNull.INSTANCE) mapMarkerObject.get(
                                                        "id"
                                                    ).asInt else 0

                                                latitude =
                                                    if (mapMarkerObject.get("latitude") !== JsonNull.INSTANCE) mapMarkerObject.get(
                                                        "latitude"
                                                    ).asDouble else 0.0
                                                longitude =
                                                    if (mapMarkerObject.get("longitude") !== JsonNull.INSTANCE) mapMarkerObject.get(
                                                        "longitude"
                                                    ).asDouble else 0.0

                                                name =
                                                    if (mapMarkerObject.get("name") !== JsonNull.INSTANCE) mapMarkerObject.get(
                                                        "name"
                                                    ).getAsString() else ""


                                                list.add(
                                                    MapMarker(
                                                        city,
                                                        id,
                                                        latitude,
                                                        longitude,
                                                        name,
                                                    ),

                                                    )
                                            }

                                        }

                                        var msg = ""
                                        if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                            msg = jsonObject.get("message").asString
                                        }
                                        listener(
                                            APIResult.Success(
                                                list, msg
                                            )
                                        )
                                    }


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


    suspend fun getGalleryImage(
        listener: (APIResult<ArrayList<GalleryImage>>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getGalleryImage()
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
                                    val datalist: ArrayList<GalleryImage> = arrayListOf()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        val jsonArray = jsonObject.getAsJsonArray("data")

                                        for (i in 0 until jsonArray.size()) {
                                            val countryObject = jsonArray.get(i).asJsonObject

                                            datalist.add(
                                                GalleryImage(
                                                    "" + if (countryObject.get("photo") !== JsonNull.INSTANCE) countryObject.get(
                                                        "photo"
                                                    ).asString else "",
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
                                            datalist, msg
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


    suspend fun getSetting(
        listener: (APIResult<Setting>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)


            val response = try {
                api.getSetting()
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
                                    var data = Setting()
                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                                        data = Gson().fromJson(
                                            jsonObject.get("data").asJsonObject,
                                            object : TypeToken<Setting>() {}.type
                                        )
                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            data, msg
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

    suspend fun getAgoraToken(
        userid: String,
        callAction: String,
        listener: (APIResult<JsonObject>) -> Unit
    ) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val jsonObject = JsonObject()
            jsonObject.addProperty("call_type", "chat")
            jsonObject.addProperty("chat_user_id", userid.toString())
            jsonObject.addProperty("group_id", "")
            jsonObject.addProperty("call_action", callAction)


            //val bodyReq: RequestBody = jsonObject.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            Log.e("jsonObject======>", jsonObject.toString())
            val response = try {
                api.getAgoraToken(jsonObject)
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

                                    if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {


                                    }
                                    var msg = ""
                                    if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                                        msg = jsonObject.get("message").asString
                                    }

                                    listener(
                                        APIResult.Success(
                                            jsonObject, msg
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
    suspend fun updateLatLng(latitude: Double, longitude: Double, listener: (APIResult<String>) -> Unit) {
        if (Utilities.isNetworkAvailable(context)) {
            listener(APIResult.InProgress)

            val params = HashMap<String, String>()
            params["latitude"] = latitude.toString()
            params["longitude"] = longitude.toString()
            Log.e("paramsLatLng====>",params.toString())
            val response = try {
                api.updateLatLng(params)
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


}