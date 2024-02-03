package com.sanatanshilpisanstha.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.group.message.LikeCommentModel
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities
import java.util.*

class CommentAdapter(private var commentsList: ArrayList<LikeCommentModel.Data.Comment>, var mContext: Context) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

   
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.comment_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.Name.text = commentsList[position].name
        holder.createdDateTxt.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_DATA_FORMAT,
            commentsList[position].createdAt!!
        ).toString()


        // holder.Name.text = commentsList[position].name

        if(Utilities.IsValidUrl(commentsList[position].profilePic.toString())){
            holder.ivProfile.load(commentsList[position].profilePic) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }

        }
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Name: TextView
        var commentTxt :TextView
        var createdDateTxt :TextView
        var ivProfile: ShapeableImageView

        init {
            Name = itemView.findViewById(R.id.nameTxt)
            commentTxt = itemView.findViewById(R.id.commentTxt)
            createdDateTxt = itemView.findViewById(R.id.createdDateTxt)
            ivProfile = itemView.findViewById(R.id.ivProfile)
        }
    }


}