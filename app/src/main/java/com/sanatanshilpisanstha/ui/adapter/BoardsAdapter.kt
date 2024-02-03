package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.data.entity.Boards
import com.sanatanshilpisanstha.databinding.UnitBoardsBinding
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities

class BoardsAdapter(
    val context: Context,
    var dataList: ArrayList<Boards>,
    val itemClick: ItemClick,
) :
    RecyclerView.Adapter<BoardsAdapter.SirenDetailViewHolder>() {

    inner class SirenDetailViewHolder(private val binding: UnitBoardsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: Boards) {

            binding.tvPersonName.text = data.username
            binding.tvDate.text =  Utilities.formatDate(
                "" + Constant.SERVER_DATE_FORMAT,
                "" + Constant.BOARD_CHAT_DATA_FORMAT,
                data.createdAt
            ).toString()
            binding.tvSubTitle.text = data.description
            binding.tvTitle.text = data.title

            if (data.coverImage?.isNotBlank() == true) {

                binding.ivImage.load(data.coverImage) {
                    crossfade(true)
                }
            } else {
                binding.ivImage.visibility = View.GONE
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
            UnitBoardsBinding.inflate(
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
        fun itemClick(id: String)
    }

    fun updateList(sirenList: ArrayList<Boards>) {
        this.dataList = sirenList
        notifyDataSetChanged()
    }
}
