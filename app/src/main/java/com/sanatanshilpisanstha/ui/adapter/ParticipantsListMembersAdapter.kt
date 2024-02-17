package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.onItemClickListener
import com.sanatanshilpisanstha.data.entity.group.GroupMember
import com.sanatanshilpisanstha.data.entity.group.GroupMembersSelect
import com.sanatanshilpisanstha.databinding.UnitGroupPartBinding
import com.sanatanshilpisanstha.databinding.UnitParticipantsBinding

class ParticipantsListMembersAdapter(
    val context: Context,
    var dataList: ArrayList<GroupMember>,
    val onItemClickListener: onItemClickListener
) :
    RecyclerView.Adapter<ParticipantsListMembersAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: GroupMember) {

            binding.nameTxt.text = data.name

            binding.ivProfile.load(data.profile_pic) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }


            binding.relativelayout.setOnClickListener {
                data.id?.let { it1 -> onItemClickListener.onItemClick(it1) }
            }

            binding.imgMessage.setOnClickListener{

                data.id?.let { it1 -> data.name?.let { it2 ->
                    onItemClickListener.onimgMessageClick(it1,
                        it2
                    )
                } }

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SirenDetailViewHolder {
        return SirenDetailViewHolder(
            UnitParticipantsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SirenDetailViewHolder, position: Int) {
        holder.setSiren(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    fun updateList(sirenList: ArrayList<GroupMember>) {
        this.dataList = sirenList
        notifyDataSetChanged()
    }
}
