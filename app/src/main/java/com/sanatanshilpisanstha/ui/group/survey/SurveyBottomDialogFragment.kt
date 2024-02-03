package com.sanatanshilpisanstha.ui.group.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sanatanshilpisanstha.databinding.FragmentGroupBottomDialogBinding
import com.sanatanshilpisanstha.databinding.FragmentSurveyBottomDialogBinding


class SurveyBottomDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: FragmentSurveyBottomDialogBinding? = null
    private val binding get() = _binding!!

    public val TAG = "GroupBottomDialogFragment"
    private var mListener: ItemClickListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init() {
        addListener()
    }

    private fun addListener() {
        binding.clMultipleChoice.setOnClickListener(this)
        binding.clDropDown.setOnClickListener(this)
        binding.clText.setOnClickListener(this)
        binding.clImage.setOnClickListener(this)
        binding.clNumeric.setOnClickListener(this)
        binding.clPhone.setOnClickListener(this)
        binding.clDate.setOnClickListener(this)
      
    }


    public fun newInstance(): SurveyBottomDialogFragment {
        return SurveyBottomDialogFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSurveyBottomDialogBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is ItemClickListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement ItemClickListener"
            )
        }

    }

    override fun onDetach() {
        super.onDetach()
    }

    interface ItemClickListener {
        fun onItemClick(item: String?)
    }

    override fun onClick(v: View?) {
        when (v) {
          
            binding.clMultipleChoice -> {
                mListener?.onItemClick("clMultipleChoice");
                dismiss()
            }
            binding.clDropDown -> {
                mListener?.onItemClick("clDropDown");
                dismiss()
            }
            binding.clText -> {
                mListener?.onItemClick("clText");
                dismiss()
            }
            binding.clImage -> {
                mListener?.onItemClick("clImage");
                dismiss()
            }
            binding.clNumeric -> {
                mListener?.onItemClick("clNumeric");
                dismiss()
            }
            binding.clPhone -> {
                mListener?.onItemClick("clPhone");
                dismiss()
            }
            binding.clDate -> {
                mListener?.onItemClick("clDate");
                dismiss()
            }



        }
    }

}