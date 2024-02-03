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
import com.google.android.material.shape.RelativeCornerSize
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Branch
import com.sanatanshilpisanstha.data.entity.City
import com.sanatanshilpisanstha.data.entity.ContactModel
import com.sanatanshilpisanstha.data.entity.Degree
import java.util.*

class ContactAdapter(private var arContactlist: ArrayList<ContactModel>, var mContext: Context) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(), Filterable {

    private val arSearch: ArrayList<ContactModel>
    private var contactSelectionListener: ContactSelectionListener? = null
    init {
        arSearch = ArrayList()
        arSearch.addAll(arContactlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.contactName.text = arContactlist[position].name
        holder.contactNumber.text = arContactlist[position].number

        holder.contactListItem.setOnClickListener(View.OnClickListener {
            contactSelectionListener?.ContactSelection(arContactlist.get(position),position);
        })
    }

    override fun getItemCount(): Int {
        return arContactlist.size
    }

    fun ContactSelect(actDocList: ContactSelectionListener?) {
        try {
            contactSelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contactNumber: TextView
        var contactName: TextView
        var contactListItem :RelativeLayout

        init {
            contactNumber = itemView.findViewById(R.id.contactNumber)
            contactName = itemView.findViewById(R.id.contactName)
            contactListItem = itemView.findViewById(R.id.contactListItem);
        }
    }

    interface ContactSelectionListener {
        fun ContactSelection(arContactlist: ContactModel?, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arContactlist = if (charString.isEmpty()) {
                    arSearch
                } else {
                    val filteredList = ArrayList<ContactModel>()
                    for (i in arSearch.indices) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (arSearch[i].name!!.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(arSearch[i])
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arContactlist
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                arContactlist = filterResults.values as ArrayList<ContactModel>

                notifyDataSetChanged()
            }
        }
    }
}