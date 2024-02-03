package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sanatanshilpisanstha.data.entity.group.survey.Option
import com.sanatanshilpisanstha.databinding.UnitAnsOptionBinding


class MultiOptionAdapter(
    val context: Context,
    var dataList: ArrayList<Option>,
    val itemClick: ItemClick,
) :
    RecyclerView.Adapter<MultiOptionAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: UnitAnsOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: Option) {
            val pos = adapterPosition + 1
            binding.etOption.setHint("option $pos")
            data.option = binding.etOption.text.toString()
            binding.etOption.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {

                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    itemClick.itemClick(adapterPosition, s.toString())
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UnitAnsOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setSiren(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    interface ItemClick {
        fun itemClick(id: Int, data: String)
    }

    fun updateList(list: ArrayList<Option>) {
        this.dataList = list
        notifyDataSetChanged()
    }

}
