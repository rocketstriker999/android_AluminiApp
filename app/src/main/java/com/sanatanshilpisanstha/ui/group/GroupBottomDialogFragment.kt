package com.sanatanshilpisanstha.ui.group

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sanatanshilpisanstha.databinding.FragmentGroupBottomDialogBinding


class GroupBottomDialogFragment(private val isFromGroup : Boolean) : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: FragmentGroupBottomDialogBinding? = null
    private val binding get() = _binding!!

    public val TAG = "GroupBottomDialogFragment"
    private var mListener: ItemClickListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init() {
        addListener()
        if(isFromGroup) {
            binding.horizontalScrollView.visibility= View.VISIBLE
        } else {
            binding.horizontalScrollView.visibility= View.GONE
        }
    }

    private fun addListener() {
        binding.clAnnouncement.setOnClickListener(this)
        binding.clJob.setOnClickListener(this)
        binding.clLetsMeet.setOnClickListener(this)
        binding.clPhotoWithLocation.setOnClickListener(this)
        binding.clQnA.setOnClickListener(this)
        binding.clQuickPoll.setOnClickListener(this)
        binding.clSurvey.setOnClickListener(this)
        binding.clAudio.setOnClickListener(this)
        binding.clGallery.setOnClickListener(this)
        binding.clVideo.setOnClickListener(this)
        binding.clContact.setOnClickListener(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGroupBottomDialogBinding.inflate(inflater, container, false)


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
            binding.clAudio -> {
                mListener?.onItemClick("clAudio");
                dismiss()
            }
            binding.clAnnouncement -> {
                mListener?.onItemClick("clAnnouncement");
                dismiss()
            }
            binding.clJob -> {
                mListener?.onItemClick("clJob");
                dismiss()
            }
            binding.clLetsMeet -> {
                mListener?.onItemClick("clLetsMeet");
                dismiss()
            }
            binding.clPhotoWithLocation -> {
                mListener?.onItemClick("clPhotoWithLocation");
                dismiss()
            }
            binding.clQnA -> {
                mListener?.onItemClick("clQnA");
                dismiss()
            }
            binding.clQuickPoll -> {
                mListener?.onItemClick("clQuickPoll");
                dismiss()
            }
            binding.clSurvey -> {
                mListener?.onItemClick("clSurvey");
                dismiss()
            }
            binding.clAudio -> {
                mListener?.onItemClick("clAudio");
                dismiss()
            }
            binding.clGallery -> {
                mListener?.onItemClick("clGallery");
                dismiss()
            }
            binding.clVideo -> {
                mListener?.onItemClick("clVideo");
                dismiss()
            }
            binding.clContact -> {
                mListener?.onItemClick("clContact");
                dismiss()
            }


        }
    }

}