package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Directory
import com.sanatanshilpisanstha.databinding.UnitDirectoryBinding

class DirectoryAdapter(
    val context: Context,
    var dataList: ArrayList<Directory>,
    val itemClick: ItemClick,
) :
    RecyclerView.Adapter<DirectoryAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitDirectoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: Directory) {

            binding.tvPersonName.text = data.name
            binding.tvDegree.text = data.city + " ."
            binding.ivProfile.load(data.profile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }

            binding.conLayoutDirectoryTabItem.setOnClickListener {
                itemClick.itemClick(data.id.toString(),data.name.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SirenDetailViewHolder {
        return SirenDetailViewHolder(
            UnitDirectoryBinding.inflate(
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

    interface ItemClick {
        fun itemClick(id: String,username: String)
    }

    fun updateList(sirenList: ArrayList<Directory>) {
        this.dataList = sirenList
        notifyDataSetChanged()
    }
}
