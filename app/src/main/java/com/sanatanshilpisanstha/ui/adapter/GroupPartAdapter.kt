package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Directory
import com.sanatanshilpisanstha.data.entity.group.GroupMembersSelect
import com.sanatanshilpisanstha.databinding.UnitGroupPartBinding
import com.sanatanshilpisanstha.ui.group.JoinGroupParticipantsActivity


class GroupPartAdapter(val context: JoinGroupParticipantsActivity,var dataList: ArrayList<Directory>,val click: ContactClick,) :RecyclerView.Adapter<GroupPartAdapter.GroupMemberViewHolder>() {


    inner class GroupMemberViewHolder(private val binding: UnitGroupPartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setDetails(data: Directory) {
            binding.tvPersonName.text = data.name
            binding.ivProfile.load(data.profile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            binding.cbSelect.isChecked= context.arrayID.contains(data.id)
            binding.cbSelect.setOnClickListener {
                val isChecked = binding.cbSelect.isChecked
                click.contactDetailClick(data.id!!,isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        return GroupMemberViewHolder(
            UnitGroupPartBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.setDetails(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    interface ContactClick {
        fun contactDetailClick(id: String, boolean: Boolean)
    }

    fun updateList(list: ArrayList<Directory>) {
        this.dataList = list
        notifyDataSetChanged()
    }



}

