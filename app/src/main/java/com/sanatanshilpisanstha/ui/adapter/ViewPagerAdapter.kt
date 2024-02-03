package com.sanatanshilpisanstha.ui.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.sanatanshilpisanstha.R
import java.net.URI
import java.util.*


class ViewPagerAdapter(val context: Context, private var imageList: List<Uri>) : PagerAdapter() {
    // on below line we are creating a method
    // as get count to return the size of the list.
    override fun getCount(): Int {
        return imageList.size
    }

    // on below line we are returning the object
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    // on below line we are initializing
    // our item and inflating our layout file
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = mLayoutInflater.inflate(R.layout.item_images, container, false)
        val imageView: ImageView = itemView.findViewById<View>(R.id.vpImageView) as ImageView
        imageView.load(imageList[position])
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    // on below line we are creating a destroy item method.
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // on below line we are removing view
        container.removeView(`object` as ConstraintLayout)
    }


    fun updateList(imageList: List<Uri>) {
        this.imageList = imageList;
        notifyDataSetChanged()
    }
}