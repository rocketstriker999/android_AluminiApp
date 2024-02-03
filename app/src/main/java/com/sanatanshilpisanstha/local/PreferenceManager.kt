package com.sanatanshilpisanstha.data.local

import android.content.Context
import android.content.SharedPreferences
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.ACCESS_TOKEN
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.AGORA_APP_CERTIFICATE
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.AGORA_APP_ID
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_ABOUT_ME
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_BRANCH
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_CITY
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_COUNTRY
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_DEGREE
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_DESIGNATION
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_EMAIL
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_GRADUATION_YEAR
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_ID
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_INSTITUTE
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_LinkedinUrl
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_NAME
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_NUMBER
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_PROFILE
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_REMARKS
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_VERIFICATION_ID
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PERSON_YEAR_OF_ENTITY
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PUSHER_APP_ID
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PUSHER_CLUSTER
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PUSHER_KEY
import com.sanatanshilpisanstha.data.local.PreferenceManager.Companion.Key.PUSHER_SECRET


class PreferenceManager(val context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    init {
        editor.apply()
    }

    companion object {
        private const val PREF_NAME = "Camereye"

        private object Key {
            const val ACCESS_TOKEN = "access_token"
            const val PERSON_NAME = "person_name"
            const val PERSON_EMAIL = "person_email"
            const val PERSON_PROFILE = "person_profile"
            const val PERSON_ID = "userID"
            const val PERSON_NUMBER = "person_number"
            const val PERSON_COUNTRY = "person_country"
            const val PERSON_CITY = "person_city"
            const val PERSON_BRANCH = "person_branch"
            const val PERSON_DEGREE = "person_degree"
            const val PERSON_GRADUATION_YEAR = "person_graduation_year"
            const val PERSON_LinkedinUrl = "person_linkedinUrl";
            const val PERSON_YEAR_OF_ENTITY = "person_yearOfEntity";
            const val PERSON_DESIGNATION  = "person_designation";
            const val PERSON_ABOUT_ME = "person_about_me";
            const val PERSON_REMARKS = "person_remark";
            const val PERSON_INSTITUTE = "person_institute";
            const val PERSON_VERIFICATION_ID = "person_verification_id";
            const val IS_USER_LOGGED_IN = "is_User_Logged_In"
            const val PUSHER_APP_ID = "PUSHER_APP_ID";
            const val PUSHER_KEY = "PUSHER_KEY";
            const val PUSHER_SECRET = "PUSHER_SECRET";
            const val PUSHER_CLUSTER = "PUSHER_CLUSTER";
            const val AGORA_APP_ID = "AGORA_APP_ID";
            const val AGORA_APP_CERTIFICATE = "AGORA_APP_CERTIFICATE";
        }
    }



    var isUserLoggedIn: Boolean
        get() = pref.getBoolean(Key.IS_USER_LOGGED_IN, false)
        set(isUserLoggedIn) {
            editor.putBoolean(Key.IS_USER_LOGGED_IN, isUserLoggedIn)
            editor.commit()
        }

    var accessToken: String
        get() = pref.getString(ACCESS_TOKEN,"")?:""
        set(accessToken){
            editor.putString(ACCESS_TOKEN,accessToken)
            editor.commit()
        }

    var personName: String
        get() = pref.getString(PERSON_NAME,"")?:""
        set(personName){
            editor.putString(PERSON_NAME,personName)
            editor.commit()
        }
    var personEmail: String
        get() = pref.getString(PERSON_EMAIL,"")?:""
        set(personEmail){
            editor.putString(PERSON_EMAIL,personEmail)
            editor.commit()
        }
    var personNumber: String
        get() = pref.getString(PERSON_NUMBER,"")?:""
        set(personNumber){
            editor.putString(PERSON_NUMBER,personNumber)
            editor.commit()
        }

    var personCountry: String
        get() = pref.getString(PERSON_COUNTRY,"")?:""
        set(personCountry){
            editor.putString(PERSON_COUNTRY,personCountry)
            editor.commit()
        }
    var personCity: String
        get() = pref.getString(PERSON_CITY,"")?:""
        set(personCity){
            editor.putString(PERSON_CITY,personCity)
            editor.commit()
        }


 var personProfile: String
        get() = pref.getString(PERSON_PROFILE,"")?:""
        set(personProfile){
            editor.putString(PERSON_PROFILE,personProfile)
            editor.commit()
        }

    var personID: Int
        get() = pref.getInt(PERSON_ID,0)?:0
        set(personID){
            editor.putInt(PERSON_ID,personID)
            editor.commit()
        }
    var personDegree: String
        get() = pref.getString(PERSON_DEGREE,"")?:""
        set(personCity){
            editor.putString(PERSON_DEGREE,personCity)
            editor.commit()
        }

    var personBranch: String
        get() = pref.getString(PERSON_BRANCH,"")?:""
        set(personCity){
            editor.putString(PERSON_BRANCH,personCity)
            editor.commit()
        }

    var personGraduationYear: String
        get() = pref.getString(PERSON_GRADUATION_YEAR,"")?:""
        set(personCity){
            editor.putString(PERSON_GRADUATION_YEAR,personCity)
            editor.commit()
        }

    var personLinkedinUrl: String
        get() = pref.getString(PERSON_LinkedinUrl,"")?:""
        set(personCity){
            editor.putString(PERSON_GRADUATION_YEAR,personCity)
            editor.commit()
        }
    var personYearEntity: String
        get() = pref.getString(PERSON_YEAR_OF_ENTITY,"")?:""
        set(personCity){
            editor.putString(PERSON_YEAR_OF_ENTITY,personCity)
            editor.commit()
        }

    var personDesignation: String
        get() = pref.getString(PERSON_DESIGNATION,"")?:""
        set(personCity){
            editor.putString(PERSON_DESIGNATION,personCity)
            editor.commit()
        }


    var personAboutMe: String
        get() = pref.getString(PERSON_ABOUT_ME,"")?:""
        set(personCity){
            editor.putString(PERSON_ABOUT_ME,personCity)
            editor.commit()
        }

    var personRemarks: String
        get() = pref.getString(PERSON_REMARKS,"")?:""
        set(personCity){
            editor.putString(PERSON_REMARKS,personCity)
            editor.commit()
        }

    var personInstitute: String
        get() = pref.getString(PERSON_INSTITUTE,"")?:""
        set(personInstitute){
            editor.putString(PERSON_INSTITUTE,personInstitute)
            editor.commit()
        }

    var personVerificationID: String
        get() = pref.getString(PERSON_VERIFICATION_ID,"")?:""
        set(personCity){
            editor.putString(PERSON_VERIFICATION_ID,personCity)
            editor.commit()
        }

    var pusherAppId: String
        get() = pref.getString(PUSHER_APP_ID,"")?:""
        set(personCity){
            editor.putString(PUSHER_APP_ID,personCity)
            editor.commit()
        }

    var pusherKey: String
        get() = pref.getString(PUSHER_KEY,"")?:""
        set(personCity){
            editor.putString(PUSHER_KEY,personCity)
            editor.commit()
        }

    var pusherSecret: String
        get() = pref.getString(PUSHER_SECRET,"")?:""
        set(personCity){
            editor.putString(PUSHER_SECRET,personCity)
            editor.commit()
        }

    var pusherCluster: String
        get() = pref.getString(PUSHER_CLUSTER,"")?:""
        set(personCity){
            editor.putString(PUSHER_CLUSTER,personCity)
            editor.commit()
        }

    var agoraAppId: String
        get() = pref.getString(AGORA_APP_ID,"")?:""
        set(personCity){
            editor.putString(AGORA_APP_ID,personCity)
            editor.commit()
        }

    var agoraAppSecret: String
        get() = pref.getString(AGORA_APP_CERTIFICATE,"")?:""
        set(personCity){
            editor.putString(AGORA_APP_CERTIFICATE,personCity)
            editor.commit()
        }




    fun clearSession() {
        editor.clear()
        editor.commit()
    }


}