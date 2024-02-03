package com.sanatanshilpisanstha.data.entity.group.message

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LikeCommentModel {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {
        @SerializedName("likes")
        @Expose
        var likes: List<Like>? = null

        @SerializedName("comments")
        @Expose
        var comments: List<Comment>? = null

        inner class Like {
            @SerializedName("id")
            @Expose
            var id: Int? = null

            @SerializedName("name")
            @Expose
            var name: String? = null

            @SerializedName("profile_pic")
            @Expose
            var profilePic: String? = null

            override fun toString(): String {
                return "Like(id=$id, name=$name, profilePic=$profilePic)"
            }


        }

        inner class Comment {
            @SerializedName("id")
            @Expose
            var id: Int? = null

            @SerializedName("name")
            @Expose
            var name: String? = null

            @SerializedName("profile_pic")
            @Expose
            var profilePic: String? = null

            @SerializedName("created_at")
            @Expose
            var createdAt: String? = null

            override fun toString(): String {
                return "Comment(id=$id, name=$name, profilePic=$profilePic, createdAt=$createdAt)"
            }


        }

        override fun toString(): String {
            return "Data(likes=$likes, comments=$comments)"
        }


    }

    override fun toString(): String {
        return "LikeCommentModel(success=$success, message=$message, data=$data)"
    }


}