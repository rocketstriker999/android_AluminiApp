package com.sanatanshilpisanstha.ui.bottom_navigation

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.CreateBoardCallback
import com.sanatanshilpisanstha.data.entity.Boards
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.FragmentBoardBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.ui.FillProfileActivity
import com.sanatanshilpisanstha.ui.adapter.BoardsAdapter
import com.sanatanshilpisanstha.ui.createBoard.CreateBoardDialogFragment
import com.sanatanshilpisanstha.ui.group.GroupBottomDialogFragment
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class BoardsFragment : Fragment(), BoardsAdapter.ItemClick, View.OnClickListener {

    private var _binding: FragmentBoardBinding? = null

    val start = 0
    val length = 100
    var type = ""
    private lateinit var boardsAdapter: BoardsAdapter
    private lateinit var preferenceManager: PreferenceManager

    lateinit var pd: ProgressDialog
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    val boardsList: ArrayList<Boards> = arrayListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var dashboardRepository: DashboardRepository

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        init()
    }

    override fun onResume() {
        super.onResume()
        if (preferenceManager.personProfile.isNotBlank() && preferenceManager.personProfile.isNotEmpty()) {
            binding.ivProfile.load(preferenceManager.personProfile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                diskCacheKey(preferenceManager.personProfile)
                memoryCacheKey(preferenceManager.personProfile)
                error(R.drawable.logo)
            }
        }
    }


    private fun init() {
        dashboardRepository = DashboardRepository(requireContext())

        pd = ProgressDialog(requireContext())
        pd.setMessage("loading")
        pd.setCancelable(false)
        val a1: java.util.ArrayList<Boards> = arrayListOf()
        binding.tvPersonName.text = "Boards"
        val subtitle = resources.getString(R.string.app_name) + " Virtual Secretariat"
        binding.tvSuTitle.text = subtitle
        boardsAdapter =
            BoardsAdapter(requireContext(), arrayListOf(), this)
        binding.rvBoard.adapter = boardsAdapter

        addListner()
    }


    override fun itemClick(id: String) {
//        TODO("Not yet implemented")
    }

    fun getBoards(start: Int, length: Int, type: String) {

        scope.launch {
            dashboardRepository.getBoards(
                start,
                length,
                type
            ) {

                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        boardsList.clear()
                        boardsList.addAll(it.data)
                        boardsAdapter.updateList(boardsList)
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvAll -> {
                binding.tvAll.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.orange_text_bg)
                binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                binding.tvConversations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvConversations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                binding.tvEvents.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvEvents.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                binding.tvLetsMeet.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvLetsMeet.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                type = ""
                getBoards(start, length, type)
            }

            binding.tvConversations -> {
                binding.tvConversations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.orange_text_bg)
                binding.tvConversations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                binding.tvAll.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                binding.tvEvents.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvEvents.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                binding.tvLetsMeet.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvLetsMeet.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                type = "0"
                getBoards(start, length, type)
            }

            binding.tvEvents -> {
                binding.tvEvents.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.orange_text_bg)
                binding.tvEvents.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                binding.tvAll.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                binding.tvConversations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvConversations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                binding.tvLetsMeet.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvLetsMeet.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                type = "2"
                getBoards(start, length, type)
            }

            binding.tvLetsMeet -> {
                binding.tvLetsMeet.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.orange_text_bg)
                binding.tvLetsMeet.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                binding.tvAll.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                binding.tvConversations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvConversations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                binding.tvEvents.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
                binding.tvEvents.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text
                    )
                )
                type = "1"
                getBoards(start, length, type)
            }

            binding.flotCreateBoard -> {
                CreateBoardDialogFragment().show(childFragmentManager, "BoardDialog")
            }

            binding.ivProfile -> {
                val intent = Intent(requireContext(), FillProfileActivity::class.java)
                this.startActivity(intent)
            }

        }
    }

    private fun addListner() {
        binding.tvAll.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.orange_text_bg)
        binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tvConversations.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
        binding.tvConversations.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text
            )
        )
        binding.tvEvents.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
        binding.tvEvents.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text
            )
        )
        binding.tvLetsMeet.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.text_border)
        binding.tvLetsMeet.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text
            )
        )
        type = ""
        getBoards(start, length, type)
        binding.tvAll.setOnClickListener(this)
        binding.tvConversations.setOnClickListener(this)
        binding.tvEvents.setOnClickListener(this)
        binding.tvLetsMeet.setOnClickListener(this)
        binding.flotCreateBoard.setOnClickListener(this)
        binding.ivProfile.setOnClickListener(this)

    }

}