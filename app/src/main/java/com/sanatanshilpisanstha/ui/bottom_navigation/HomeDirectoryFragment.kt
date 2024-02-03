package com.sanatanshilpisanstha.ui.bottom_navigation

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Directory
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.FragmentHomeDirectoryBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.ui.FillProfileActivity
import com.sanatanshilpisanstha.ui.adapter.DirectoryAdapter
import com.sanatanshilpisanstha.ui.directory.DirectoryChatActivity
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext


class HomeDirectoryFragment : Fragment(), DirectoryAdapter.ItemClick {

    val TAG = "HomeDirectoryFragment"
    private var _binding: FragmentHomeDirectoryBinding? = null
    private val binding get() = _binding!!
    lateinit var pd: ProgressDialog

    private lateinit var directoryAdapter: DirectoryAdapter
    private lateinit var preferenceManager: PreferenceManager
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var dashboardRepository: DashboardRepository
    private val directoryList: ArrayList<Directory> = arrayListOf()


    val start = 0
    val length = 20
    var search = ""
    var mMaxoffset = 0
    var lastPage = 0

    private lateinit var parentActivity: BottomNavActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity= context as BottomNavActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        init()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDirectoryBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    private fun init() {
        dashboardRepository = DashboardRepository(requireContext())

        pd = ProgressDialog(requireContext())
        pd.setMessage("loading")
        pd.setCancelable(false)
        preferenceManager = PreferenceManager(requireContext())
        binding.tvPersonName.text = "Directory"
        val subtitle = resources.getString(R.string.app_name) + " Virtual Secretariat"
        binding.tvSuTitle.text = subtitle

        directoryAdapter =DirectoryAdapter(requireContext(), arrayListOf(), this)
        binding.rvDirectory.adapter = directoryAdapter
        directoryList.clear()
        getDirectory(parentActivity.latitude!!, parentActivity.longitude!!, lastPage, length, search)


        Log.e("TAG=====>", parentActivity.latitude.toString())

        binding.ivProfile.setOnClickListener {
            val intent = Intent(requireContext(), FillProfileActivity::class.java)
            this.startActivity(intent)
        }

        binding.cardView2.setOnClickListener {
            binding.searchUser.isFocusableInTouchMode = true
            binding.searchUser.requestFocus()
            binding.searchUser.onActionViewExpanded()
        }

        binding.searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // on below line we are checking
                // if query exist or not.
                Log.e("query======>",query.toString())
                search=query!!
                directoryList.clear()
                getDirectory(parentActivity.latitude!!, parentActivity.longitude!!, start, length, search)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if(newText?.length==0){
                    search=""
                    directoryList.clear()
                    getDirectory(parentActivity.latitude!!, parentActivity.longitude!!, start, length, search)
                }
                return false
            }
        })


        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                // in this method we are incrementing page number,
                // making progress bar visible and calling get data method.
                lastPage = lastPage + length
                if (mMaxoffset.toInt() >= lastPage) {
                    Log.e("lastPage =====>", lastPage.toString());

                    if (Utilities.isNetworkAvailable(requireActivity())) {
                        getDirectory(parentActivity.latitude!!, parentActivity.longitude!!, lastPage, length, search)

                    } else {
                        Toast.makeText(
                            requireActivity(),
                            requireActivity().resources.getString(R.string.network_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClick(id: String, username: String) {
        val intent = Intent(context, DirectoryChatActivity::class.java)
        intent.putExtra("userId", id)
        intent.putExtra(Extra.GROUP_ID, id)
        intent.putExtra(Extra.DIRECTORY_USER_NAME, username)
        context?.startActivity(intent)
    }

    fun getDirectory(latitude: Double, longitude: Double, start: Int, length: Int, search: String) {

        scope.launch {
            dashboardRepository.getDirectory(
                latitude,
                longitude,
                start,
                length,
                search
            ) {
                when (it) {
                    is APIResult.Success -> {
                        if (pd != null && pd.isShowing) {
                            pd.cancel()
                        }
                        if (it.data.size > 0) {
                            directoryList.addAll(it.data)
                            mMaxoffset = lastPage + length
                        }
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                }
                directoryAdapter.updateList(directoryList)
            }
        }
    }

    /*fun searchUser(latitude: Double, longitude: Double, start: Int, length: Int, search: String) {

        scope.launch {
            dashboardRepository.getDirectory(
                latitude,
                longitude,
                start,
                length,
                search
            ) {

                when (it) {
                    is APIResult.Success -> {
                        if (pd != null && pd.isShowing) {
                            pd.cancel()
                        }
                        directoryList.clear()
                        if (it.data.size > 0) {
                            for (i in it.data.indices) {
                                val item = it.data[i]
                                directoryList.add(
                                    Directory(
                                        name = item.name,
                                        id = item.id,
                                        profile = item.profile,
                                        city = item.city,
                                        distance = item.distance
                                    )
                                )

                                directoryAdapter.updateList(directoryList)

                                binding.rvDirectory.adapter = directoryAdapter
                            }

                            mMaxoffset = lastPage + length
                        }
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
    }*/


}