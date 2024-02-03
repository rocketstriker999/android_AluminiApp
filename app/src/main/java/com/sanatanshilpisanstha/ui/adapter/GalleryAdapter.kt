package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.GalleryImage
import com.sanatanshilpisanstha.databinding.UnitGalleryBinding

class GalleryAdapter(
    val context: Context,
    var dataList: ArrayList<GalleryImage>,
    val click: GalleryClick,
) :
    RecyclerView.Adapter<GalleryAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: GalleryImage) {

            binding.ivProfile.load(data.photo) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
            }

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
            UnitGalleryBinding.inflate(
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
//        return 3
    }

    interface GalleryClick {
        fun galleryClick(id: String)
    }

    fun updateList(groupList: ArrayList<GalleryImage>) {
        this.dataList = groupList
        notifyDataSetChanged()
    }
}
