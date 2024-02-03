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
import java.util.*

class CityAdapter(private var arCitylist: ArrayList<City>, var mContext: Context, countryID: String) :
    RecyclerView.Adapter<CityAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<City>
    private var citySelectionListener: CitySelectionListener? = null
    var countryId = ""
    init {
        arSearch = ArrayList()
        arSearch.addAll(arCitylist)
        countryId = countryID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.country_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.cityName.text = arCitylist[position].name
        holder.cityRelative.setOnClickListener {
            if (citySelectionListener != null) {
                citySelectionListener!!.CitySelection(
                    arCitylist[position], holder.layoutPosition
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return arCitylist.size
    }

    fun CitySelect(actDocList: CitySelectionListener?) {
        try {
            citySelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cityName: TextView
        var cityRelative: RelativeLayout

        init {
            cityName = itemView.findViewById(R.id.countryName)
            cityRelative = itemView.findViewById(R.id.CountryRelative)
        }
    }

    interface CitySelectionListener {
        fun CitySelection(arCitylist: City?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arCitylist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<City>()
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
                filterResults.values = arCitylist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arCitylist = filterResults.values as ArrayList<City>

                notifyDataSetChanged()
            }
        }
    }
}