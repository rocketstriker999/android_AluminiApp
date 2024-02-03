package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sanatanshilpisanstha.data.entity.group.survey.Question
import com.sanatanshilpisanstha.databinding.UnitSurveyQuestionBinding

class SurveyQuestionAdapter(
    val context: Context,
    var dataList: ArrayList<Question?>?,
    val itemClick: ItemClick,
) :
    RecyclerView.Adapter<SurveyQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: UnitSurveyQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSiren(data: Question) {
            val index = adapterPosition + 1
            binding.tvSr.text = "" + index


            if (data.text != null) {
                binding.tvQuestion.text = data.text!!.question
                binding.tvType.text = "Text"
                binding.tvOption.visibility = View.GONE
            } else if (data.image != null) {
                binding.tvQuestion.text = data.image!!.question
                binding.tvType.text = "Image"
                binding.tvOption.visibility = View.GONE
            } else if (data.phone != null) {
                binding.tvQuestion.text = data.phone!!.question
                binding.tvType.text = "Phone"
                binding.tvOption.visibility = View.GONE
            } else if (data.dropdown != null) {
                binding.tvQuestion.text = data.dropdown!!.question
                binding.tvType.text = "Drop Down"
                binding.tvOption.visibility = View.VISIBLE
            } else if (data.mutipleChoice != null) {
                binding.tvQuestion.text = data.mutipleChoice!!.question
                binding.tvType.text = "Drop Down"
                binding.tvOption.visibility = View.VISIBLE
                val choice = data.mutipleChoice!!.options?.size
                val choice1 = "$choice Choices"
                binding.tvOption.text = choice1
            } else if (data.date != null) {
                binding.tvQuestion.text = data.date!!.question
                binding.tvType.text = "Date"
                binding.tvOption.visibility = View.GONE
            } else if (data.numeric != null) {
                binding.tvQuestion.text = data.numeric!!.question
                binding.tvType.text = "Numeric"
                binding.tvOption.visibility = View.GONE
            } else if (data.image != null) {
                binding.tvQuestion.text = data.image!!.question
                binding.tvType.text = "Image"
                binding.tvOption.visibility = View.GONE
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UnitSurveyQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataList?.get(position)?.let { holder.setSiren(it) }
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    interface ItemClick {
        fun itemClick(id: String)
    }

    fun updateList(sirenList: ArrayList<Question?>?) {
        this.dataList = sirenList
        notifyDataSetChanged()
    }
}
