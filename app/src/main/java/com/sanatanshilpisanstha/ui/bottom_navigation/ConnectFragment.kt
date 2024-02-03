package com.sanatanshilpisanstha.ui.bottom_navigation

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Connect
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.databinding.FragmentConnectBinding
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.ui.FillProfileActivity
import com.sanatanshilpisanstha.ui.adapter.ConnectAdapter
import com.sanatanshilpisanstha.ui.group.ChatActivity
import com.sanatanshilpisanstha.ui.group.JoinGroupActivity
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ConnectFragment : Fragment(), ConnectAdapter.ContactClick {

    private var _binding: FragmentConnectBinding? = null
    lateinit var pd: ProgressDialog
    private lateinit var connectAdapter: ConnectAdapter
    private lateinit var preferenceManager: PreferenceManager
    val connectList: ArrayList<Connect> = arrayListOf()

    // This property is only valid between onCreateView and`
    // onDestroyView.
    private val binding get() = _binding!!


    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var dashboardRepository: DashboardRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
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


    private fun init() {
        pd = ProgressDialog(requireContext())
        pd.setMessage("loading")
        pd.setCancelable(false)
        preferenceManager = PreferenceManager(requireContext())

        // imageList.add(SlideModel("String Url" or R.drawable)
        // imageList.add(SlideModel("String Url" or R.drawable, "title") You can add title
        dashboardRepository = DashboardRepository(requireContext())

        binding.tvPersonName.text = "Connect"
        val subtitle = resources.getString(R.string.app_name)+" Virtual Secretariat"
        binding.tvSuTitle.text = subtitle

        if(Utilities.IsValidUrl(preferenceManager.personProfile)) {
            binding.ivProfile.load(preferenceManager.personProfile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(CircleCropTransformation())
            }
        }else{
            binding.ivProfile.load(Constant.ImageBannerURL +preferenceManager.personProfile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(CircleCropTransformation())
        }}

        connectAdapter =
            ConnectAdapter(requireContext(), arrayListOf(), this)
        binding.rvConnect.adapter = connectAdapter

        getConnect()

        binding.ivProfile.setOnClickListener {
            val intent = Intent(requireContext(), FillProfileActivity::class.java)
            this.startActivity(intent)
        }

        binding.floatingActionButton.setOnClickListener {
            val i = Intent(requireContext(), JoinGroupActivity::class.java)
            startActivity(i)
        }

    }

    override fun contactDetailClick(id: Int) {
        val i = Intent(activity, ChatActivity::class.java)
        i.putExtra(Extra.GROUP_ID, connectList[id].id)
        i.putExtra(Extra.GROUP_NAME, connectList[id].groupName)
        i.putExtra(Extra.GROUP_BANNER, connectList[id].groupBanner)
        startActivity(i)
    }

    fun getConnect() {

        scope.launch {
            dashboardRepository.getConnect {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()

                        connectList.clear()
                        connectList.addAll(it.data)
                        if(connectList.size>0) {
                            connectAdapter.updateList(connectList)

                            binding.noGroup.visibility = View.GONE
                            binding.rvConnect.visibility = View.VISIBLE
                        }else{
                            binding.noGroup.visibility = View.VISIBLE
                            binding.rvConnect.visibility = View.GONE
                        }
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        binding.noGroup.visibility = View.VISIBLE
                        binding.rvConnect.visibility = View.GONE

                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                }
            }
        }
    }

}