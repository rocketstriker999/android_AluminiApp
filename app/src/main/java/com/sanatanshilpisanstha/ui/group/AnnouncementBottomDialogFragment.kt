package com.sanatanshilpisanstha.ui.group

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sanatanshilpisanstha.databinding.FragmentAnnouncementBottomDialogBinding
import com.sanatanshilpisanstha.databinding.FragmentGroupBottomDialogBinding


class AnnouncementBottomDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: FragmentAnnouncementBottomDialogBinding? = null
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
        binding.ivAudio.setOnClickListener(this)
        binding.ivCam.setOnClickListener(this)
        binding.ivDocument.setOnClickListener(this)
        binding.ivPhotos.setOnClickListener(this)
        binding.tvAudio.setOnClickListener(this)
        binding.tvCam.setOnClickListener(this)
        binding.tvDocument.setOnClickListener(this)
        binding.tvPhotos.setOnClickListener(this)

    }


    public fun newInstance(): AnnouncementBottomDialogFragment {
        return AnnouncementBottomDialogFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAnnouncementBottomDialogBinding.inflate(inflater, container, false)


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
            binding.ivAudio, binding.tvAudio -> {
                mListener?.onItemClick("ivAudio");
                dismiss()
            }
            binding.ivPhotos, binding.tvPhotos -> {
                mListener?.onItemClick("ivPhotos");
                dismiss()
            }
            binding.ivDocument, binding.tvDocument -> {
                mListener?.onItemClick("ivDocument");
                dismiss()
            }
            binding.ivCam, binding.tvCam -> {
                mListener?.onItemClick("ivCam");
                dismiss()
            }
        }
    }

}