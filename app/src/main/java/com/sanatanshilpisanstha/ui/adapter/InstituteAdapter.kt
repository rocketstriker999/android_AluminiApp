package com.sanatanshilpisanstha.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.City
import com.sanatanshilpisanstha.data.entity.Degree
import com.sanatanshilpisanstha.data.entity.Institute
import java.util.*

class InstituteAdapter(private var arInstitutelist: ArrayList<Institute>, var mContext: Context) :
    RecyclerView.Adapter<InstituteAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<Institute> = ArrayList()
    private var instituteSelectionListener: InstituteSelectionListener? = null
    init {
        arSearch.addAll(arInstitutelist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.country_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.degreeName.text = arInstitutelist[position].name
        holder.degreeRelative.setOnClickListener {
            if (instituteSelectionListener != null) {
                instituteSelectionListener!!.InstituteSelection(
                    arInstitutelist[position], holder.layoutPosition
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return arInstitutelist.size
    }

    fun InstituteSelect(actDocList: InstituteSelectionListener?) {
        try {
            instituteSelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var degreeName: TextView
        var degreeRelative: RelativeLayout

        init {
            degreeName = itemView.findViewById(R.id.countryName)
            degreeRelative = itemView.findViewById(R.id.CountryRelative)
        }
    }

    interface InstituteSelectionListener {
        fun InstituteSelection(arInstitutelist: Institute?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arInstitutelist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<Institute>()
                    for (i in arSearch.indices) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (arSearch[i].name.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(arSearch[i])
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arInstitutelist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arInstitutelist = filterResults.values as ArrayList<Institute>

                notifyDataSetChanged()
            }
        }
    }
}