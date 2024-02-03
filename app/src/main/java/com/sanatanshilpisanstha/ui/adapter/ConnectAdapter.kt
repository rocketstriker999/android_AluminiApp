package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Connect
import com.sanatanshilpisanstha.databinding.UnitConnectBinding
import com.sanatanshilpisanstha.utility.Constant.ImageBannerURL
import com.sanatanshilpisanstha.utility.Utilities.IsValidUrl

class ConnectAdapter(
    val context: Context,
    var dataList: ArrayList<Connect>,
    val click: ContactClick,
) :
    RecyclerView.Adapter<ConnectAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitConnectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: Connect) {

            binding.tvPersonName.text = data.groupName
            binding.tvDegree.text = data.name + " : " + data.type
            binding.tvDate.text = data.id.toString()
            binding.clMain.setOnClickListener {
                click.contactDetailClick(adapterPosition)
            }

            if(IsValidUrl(data.groupBanner.toString())){
                binding.ivProfile.load(data.groupBanner) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
            }else{
                binding.ivProfile.load(ImageBannerURL+data.groupBanner) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
            }

//            binding.ivImage.load(data.image) {
//                crossfade(true)
//            }
//            if (data.type == 1) {
//                binding.imageViewSiren5.visibility = INVISIBLE
//                binding.textViewSiren10.visibility = INVISIBLE
//                binding.imageViewSiren6.visibility = INVISIBLE
//
//                binding.ivAddSiren.visibility = VISIBLE
//                binding.tvAddSiren.visibility = VISIBLE
//                binding.cvSiren.setOnClickListener {
//                    sirenClick.sirenAddClick()
//                }
//
//            } else if (data.type == 0) {
//                binding.cvSiren.setOnClickListener {
//                    sirenClick.sirenDetailClick(data.id)
//                }
//                binding.imageViewSiren5.visibility = VISIBLE
//                binding.textViewSiren10.visibility = VISIBLE
//                binding.imageViewSiren6.visibility = VISIBLE
//
//                binding.ivAddSiren.visibility = INVISIBLE
//                binding.tvAddSiren.visibility = INVISIBLE
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SirenDetailViewHolder {
        return SirenDetailViewHolder(
            UnitConnectBinding.inflate(
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

    interface ContactClick {
        fun contactDetailClick(id: Int)
    }

    fun updateList(sirenList: ArrayList<Connect>) {
        this.dataList = sirenList
        notifyDataSetChanged()
    }
}
