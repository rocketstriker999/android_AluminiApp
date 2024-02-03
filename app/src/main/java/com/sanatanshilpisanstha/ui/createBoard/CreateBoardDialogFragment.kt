package com.sanatanshilpisanstha.ui.createBoard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.CreateBoardCallback
import com.sanatanshilpisanstha.databinding.CreateBoardDialogFragmentBinding



class CreateBoardDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: CreateBoardDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private var myInterface: CreateBoardCallback? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            myInterface = context as CreateBoardCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement Communicator")
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateBoardDialogFragmentBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.rlBoardDialogStartConversations.setOnClickListener(this)
        binding.rlBoardDialogLetsMeet.setOnClickListener(this)
        binding.rlBoardOrganizeEvents.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rlBoardDialogStartConversations -> {
                val intent = Intent(context, StartConversationActivity::class.java)
                resultLauncher.launch(intent)
            }
            R.id.rlBoardDialogLetsMeet -> {
                val intent = Intent(context, LetsMeetsActivity::class.java)
                resultLauncher.launch(intent)
            }

            R.id.rlBoardOrganizeEvents -> {
                val intent = Intent(context, CreateEventActivity::class.java)
                resultLauncher.launch(intent)
            }

        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            myInterface?.onSuccessBoardCreated()
        }
    }

}