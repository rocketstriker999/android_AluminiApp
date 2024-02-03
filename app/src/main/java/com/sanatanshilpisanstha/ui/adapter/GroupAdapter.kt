package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.PublicGroup
import com.sanatanshilpisanstha.databinding.UnitGroupBinding
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities

class GroupAdapter(
    val context: Context,
    var publicGroupList: ArrayList<PublicGroup>,
    val groupClick: GroupClick,
) :
    RecyclerView.Adapter<GroupAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: PublicGroup) {

            binding.tvCount.text = data.total_members + " Members"
            binding.tvName.text = data.group_name

            if (Utilities.IsValidUrl(data.group_banner)) {
                binding.ivProfile.load(data.group_banner) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                    transformations(CircleCropTransformation())
                }
            } else {
                binding.ivProfile.load(Constant.ImageBannerURL + data.group_banner) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                    transformations(CircleCropTransformation())
                }
            }

            binding.btnJoin.setOnClickListener {
                groupClick.groupClick(data.group_join_code, adapterPosition)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SirenDetailViewHolder {
        return SirenDetailViewHolder(
            UnitGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SirenDetailViewHolder, position: Int) {
        holder.setSiren(publicGroupList[position])
    }

    override fun getItemCount(): Int {
        return publicGroupList.size
    }

    interface GroupClick {
        fun groupClick(id: String, position: Int)
    }

    fun updateList(publicGroupList: ArrayList<PublicGroup>) {
        this.publicGroupList = publicGroupList
        notifyDataSetChanged()
    }

    fun removeFromList(position: Int) {
        publicGroupList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, publicGroupList.size)
    }
}
