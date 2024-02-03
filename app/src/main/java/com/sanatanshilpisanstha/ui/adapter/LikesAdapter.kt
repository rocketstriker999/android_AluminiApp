package com.sanatanshilpisanstha.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.group.message.LikeCommentModel
import com.sanatanshilpisanstha.utility.Utilities
import java.util.*

class LikesAdapter(private var likesList: ArrayList<LikeCommentModel.Data.Like>, var mContext: Context) :
    RecyclerView.Adapter<LikesAdapter.ViewHolder>() {

   
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.like_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.Name.text = likesList[position].name

        if(Utilities.IsValidUrl(likesList[position].profilePic.toString())){
            holder.ivProfile.load(likesList[position].profilePic) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }

        }
    }

    override fun getItemCount(): Int {
        return likesList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Name: TextView
        var ivProfile: ShapeableImageView

        init {
            Name = itemView.findViewById(R.id.nameTxt)
            ivProfile = itemView.findViewById(R.id.ivProfile)
        }
    }


}