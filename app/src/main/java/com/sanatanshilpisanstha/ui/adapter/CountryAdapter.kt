package com.sanatanshilpisanstha.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Country
import java.util.*

class CountryAdapter(private var arCountrylist: ArrayList<Country>, var mContext: Context) :
    RecyclerView.Adapter<CountryAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<Country>
    private var countrySelectionListener: CountrySelectionListener? = null

    init {
        arSearch = ArrayList()
        arSearch.addAll(arCountrylist)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.country_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.countryName.text = arCountrylist[position].country_name
        holder.CountryRelative.setOnClickListener {
            if (countrySelectionListener != null) {
                countrySelectionListener!!.CountrySelection(
                    arCountrylist[position],
                    holder.layoutPosition
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return arCountrylist.size
    }

    fun CountrySelect(actDocList: CountrySelectionListener?) {
        try {
            countrySelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var countryName: TextView
        var CountryRelative: RelativeLayout

        init {
            countryName = itemView.findViewById(R.id.countryName)
            CountryRelative = itemView.findViewById(R.id.CountryRelative)
        }
    }

    interface CountrySelectionListener {


        fun CountrySelection(arCountrylist: Country?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arCountrylist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<Country>()
                    for (i in arSearch.indices) {
                       // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (arSearch[i].country_name.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(arSearch[i])
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arCountrylist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arCountrylist = filterResults.values as ArrayList<Country>

                notifyDataSetChanged()
            }
        }
    }
}