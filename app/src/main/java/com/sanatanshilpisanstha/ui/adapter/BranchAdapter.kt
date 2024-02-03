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

class BranchAdapter(private var arBranchlist: ArrayList<Branch>, var mContext: Context) :
    RecyclerView.Adapter<BranchAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<Branch>
    private var branchSelectionListener: BranchSelectionListener? = null
    init {
        arSearch = ArrayList()
        arSearch.addAll(arBranchlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.country_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.branchName.text = arBranchlist[position].name
        holder.branchRelative.setOnClickListener {
            if (branchSelectionListener != null) {
                branchSelectionListener!!.BranchSelection(
                    arBranchlist[position], holder.layoutPosition
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return arBranchlist.size
    }

    fun BranchSelect(actDocList: BranchSelectionListener?) {
        try {
            branchSelectionListener = actDocList
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

    interface BranchSelectionListener {
        fun BranchSelection(arBranchlist: Branch?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arBranchlist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<Branch>()
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
                filterResults.values = arBranchlist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arBranchlist = filterResults.values as ArrayList<Branch>

                notifyDataSetChanged()
            }
        }
    }
}