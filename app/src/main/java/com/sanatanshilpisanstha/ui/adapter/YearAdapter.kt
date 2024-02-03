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
import com.sanatanshilpisanstha.data.entity.Branch
import com.sanatanshilpisanstha.data.entity.City
import com.sanatanshilpisanstha.data.entity.Degree
import java.util.*

class YearAdapter(private var arYearlist: ArrayList<String>, var mContext: Context) :
    RecyclerView.Adapter<YearAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<String>
    private var yearSelectionListener: YearSelectionListener? = null
    init {
        arSearch = ArrayList()
        arSearch.addAll(arYearlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.country_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.branchName.text = arYearlist[position]
        holder.branchRelative.setOnClickListener {
            if (yearSelectionListener != null) {
                yearSelectionListener!!.YearSelection(
                    arYearlist[position], holder.layoutPosition
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return arYearlist.size
    }

    fun YearSelect(actDocList: YearSelectionListener?) {
        try {
            yearSelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var branchName: TextView
        var branchRelative: RelativeLayout

        init {
            branchName = itemView.findViewById(R.id.countryName)
            branchRelative = itemView.findViewById(R.id.CountryRelative)
        }
    }

    interface YearSelectionListener {
        fun YearSelection(arYearlist: String?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arYearlist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<String>()
                    for (i in arSearch.indices) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (arSearch[i].lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(arSearch[i])
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arYearlist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arYearlist = filterResults.values as ArrayList<String>

                notifyDataSetChanged()
            }
        }
    }
}