package com.sanatanshilpisanstha.utility

import com.sanatanshilpisanstha.BuildConfig


object AppURL {


    object API {

        var BASE_URL = BuildConfig.BASE_URL;

        var MESSAGES_ENDPOINT ="http://pusher-chat-demo.herokuapp.com"

        //  BASESTATION
        const val LOGIN = "login"
        const val RESET_PASSWORD  = "reset-password"
        const val REGISTER = "register"
        const val UPDATE_PROFILE = "update-profile"
        const val COUNTRY = "countries"
        const val CITY = "cities"
        const val BANNERS = "banners"
        const val PUBLIC_GROUP = "interested-group"
        const val GALLERIES = "galleries"
        const val SYSTEM_SETTING = "system-setting"
        const val SERVICE = "services"
        const val ANNOUNCEMENT = "announcement"
        const val SURVEY = "survey"
        const val POLL = "poll"
        const val CONNECT = "connect"
        const val MEMBERS = "members"
        const val JOIN_GROUP = "join-group"
        const val CREATE_GROUP = "create-group"
        const val DIRECTORY = "directory"
        const val BOARDS = "board/list"
        const val EXPLORE_MAP = "explore-map"
        const val QA = "qa"
        const val MEET = "meet"
        const val PHOTO_LOCATION = "photo-location"
        const val MESSAGE_LISTINHG = "message-listing"
        const val GROUP_MEMBER = "group-members"
        const val JOB = "job"
        const val POST_MESSAGE = "message"
        const val POST_DIRECTORY_MESSAGE = "chat"
        const val deleteMsg = "delete-chat-msg"
        const val ChatMessageLike ="chat-msg-like"
        const val ChatMessageComment ="chat-msg-comment"
        const val LikeComment ="msg-comment-list"
        const val Logout = "logout"
        const val deleteAccount = "delete-account"
        const val Degree = "degree"
        const val Branch = "branch"
        const val getProfileDetails = "get-profile"
        const val Institute = "institute"
        const val UpdateProfilePic= "update-profile-pic"
        const val startConversation = "board/conversation"
        const val event = "board/event"
        const val boardMeet = "board/meet"
        const val AgoraToken = "agora/token"
        const val ChatListing = "chat-listing"
        const val updateLatLng = "update-latlng"

    }
}