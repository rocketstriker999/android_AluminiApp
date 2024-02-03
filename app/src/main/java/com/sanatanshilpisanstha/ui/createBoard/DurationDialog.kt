package com.sanatanshilpisanstha.ui.createBoard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.SelectDurationListener
import com.sanatanshilpisanstha.databinding.FragmentSpinnerDialogBinding
import java.util.Objects

class DurationDialog(private val selectDurationListener: SelectDurationListener) : DialogFragment(), View.OnClickListener {

    private lateinit var spinnerDialogBinding: FragmentSpinnerDialogBinding
    private var selectedDays = ""
    private var selectedMin = ""
    private var selectedSeconds = ""

    companion object {
        var selectedDuration = ""
    }


    override fun onResume() {
        super.onResume()
        val params: WindowManager.LayoutParams? = dialog?.window?.attributes
        if (params != null) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        if (params != null) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        Objects.requireNonNull(dialog!!.window)?.attributes = params as WindowManager.LayoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        spinnerDialogBinding = FragmentSpinnerDialogBinding.inflate(inflater, container, false)

        initWidgets()

        return spinnerDialogBinding.root
    }


    private fun initWidgets() {
        spinnerDialogBinding.btnSpinnerDialogSelect.setOnClickListener(this)

        setDaysAdepter()
        setMinAdepter()
        setSecondAdepter()

    }

    override fun onClick(v: View?) {

        when (v) {
            spinnerDialogBinding.btnSpinnerDialogSelect -> {
                selectedDuration = "$selectedDays $selectedMin $selectedSeconds"
                selectDurationListener.onSelectedDuration(selectedDuration)
                dismiss()
            }
        }

    }

    private fun setDaysAdepter() {
        val dataArray: ArrayList<String> = ArrayList()
        dataArray.add("Please select days")
        for (i in 1..365) {
            dataArray.add(i.toString() + "Days")
        }

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dataArray
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.divider_color))
                } else {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                return view
            }
        }

        spinnerDialogBinding.spinnerDays.adapter = adapter

        spinnerDialogBinding.spinnerDays.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedDays = dataArray[position]
                }

            }
    }

    private fun setMinAdepter() {
        val dataArray: ArrayList<String> = ArrayList()
        dataArray.add("Please select minute")
        for (i in 1..60) {
            dataArray.add(i.toString() + "Minute")
        }

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dataArray
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.divider_color))
                } else {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                return view
            }
        }

        spinnerDialogBinding.spinnerMin.adapter = adapter

        spinnerDialogBinding.spinnerMin.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedMin = dataArray[position]
                }

            }
    }

    private fun setSecondAdepter() {
        val dataArray: ArrayList<String> = ArrayList()
        dataArray.add("Please select second")
        for (i in 1..60) {
            dataArray.add(i.toString() + "Second")
        }

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dataArray
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.divider_color))
                } else {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                return view
            }
        }

        spinnerDialogBinding.spinnerSeconds.adapter = adapter

        spinnerDialogBinding.spinnerSeconds.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSeconds = dataArray[position]
                }
            }
    }


}