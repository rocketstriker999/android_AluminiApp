package com.sanatanshilpisanstha.ui.bottom_navigation

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Banner
import com.sanatanshilpisanstha.data.entity.GalleryImage
import com.sanatanshilpisanstha.data.entity.PublicGroup
import com.sanatanshilpisanstha.data.entity.Setting
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.FragmentHomeBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.screen.MapsMarkerActivity
import com.sanatanshilpisanstha.ui.FillProfileActivity
import com.sanatanshilpisanstha.ui.adapter.GalleryAdapter
import com.sanatanshilpisanstha.ui.adapter.GroupAdapter
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext


class HomeFragment : Fragment(), OnClickListener, GroupAdapter.GroupClick,
    GalleryAdapter.GalleryClick {

    val TAG = "HomeFragment"

    private lateinit var preferenceManager: PreferenceManager
    lateinit var pd: ProgressDialog

    private lateinit var binding: FragmentHomeBinding
    private val parentJob = Job()
    val bannerList: ArrayList<Banner> = arrayListOf()
    val imageList = ArrayList<SlideModel>()
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var galleryAdapter: GalleryAdapter
    var publicGroupList: ArrayList<PublicGroup> = arrayListOf()
    var galleryImageList: ArrayList<GalleryImage> = arrayListOf()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    // This property is only valid between onCreateView anda
    // onDestroyView.

    private lateinit var dashboardRepository: DashboardRepository
    private var mContext: Context? = null
    private lateinit var groupRepository: GroupRepository

    private var fusedLocationClient: FusedLocationProviderClient? = null
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    lateinit var cityName : String
   lateinit var parentActivity : BottomNavActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
        parentActivity= context as BottomNavActivity


        if(parentActivity.latitude!=null && parentActivity.longitude!=null){
            val geocoder = Geocoder(parentActivity, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(parentActivity.latitude!!,parentActivity.longitude!!, 1)
            //val cityName: String = addresses!![0].getAddressLine(0)
            ///val stateName: String = addresses!![0].getAddressLine(1)
            //val countryName: String = addresses!![0].getAddressLine(2)
            cityName = addresses!![0].locality
            //println("CITY $cityName")
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        dashboardRepository = DashboardRepository(requireContext())
        groupRepository = GroupRepository(requireContext())

        init()
        getLastLocation()

    }



    fun init() {
        pd = ProgressDialog(requireContext())
        pd.setMessage("loading")
        pd.setCancelable(false)
        binding.tvPersonName.text = "Hi, " + preferenceManager.personName
        binding.tvPersonSub.text = resources.getString(R.string.app_name)

        if(::cityName.isInitialized)
            binding.textView10.append(cityName)

        binding.rvGallery.layoutManager =
            object : LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
            ) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    // force height of viewHolder here, this will override layout_height from xml
//                                    lp.width = (width / 1.15).toInt()
                    return true
                }
            }

        galleryAdapter =
            GalleryAdapter(requireContext(), galleryImageList, this@HomeFragment)
        binding.rvGallery.adapter = galleryAdapter
        addListener()
        groupAdapter =
            GroupAdapter(requireContext(), publicGroupList, this@HomeFragment)
        binding.rvGroup.adapter = groupAdapter
        getBanners()


    }

    override fun onResume() {
        super.onResume()
        binding.ivProfile.load(preferenceManager.personProfile) {
            crossfade(true)
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }
    }

    fun getBanners() {
//        pd.show()
        scope.launch {
            dashboardRepository.getBanners {
                when (it) {
                    is APIResult.Success -> {
                        bannerList.clear()
                        bannerList.addAll(it.data)
                        imageList.clear()
                        for (items in bannerList) {
                            imageList.add(
                                SlideModel(
                                    items.banner
                                )
                            )
                        }
                        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
                        getPublicGroup()

                    }

                    is APIResult.Failure -> {
//                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun getPublicGroup() {

        scope.launch {
            dashboardRepository.getPublicGroup {
                when (it) {
                    is APIResult.Success -> {
                        publicGroupList.clear()
                        publicGroupList.addAll(it.data)
                        groupAdapter.updateList(publicGroupList)
                        getGalleryImage()

                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun getGalleryImage() {

        scope.launch {
            dashboardRepository.getGalleryImage {
                when (it) {
                    is APIResult.Success -> {
                        galleryImageList.clear()
                        galleryImageList.addAll(it.data)
                        galleryAdapter.updateList(galleryImageList)
                        getSetting()
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun addListener() {
        binding.ivProfile.setOnClickListener(this)
        binding.clExplore.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivProfile -> {
                this.startActivity(Intent(requireContext(), FillProfileActivity::class.java))
            }
            binding.clExplore -> {
                val intentMap:Intent = Intent(requireContext(), MapsMarkerActivity::class.java)
                intentMap.putExtra("INIT_LAT",latitude)
                intentMap.putExtra("INIT_LONG",longitude)
                this.startActivity(intentMap)
            }
        }
    }

    override fun groupClick(id: String, position: Int) {
        postJoinGroup(id, position)
    }

    override fun galleryClick(id: String) {
//        TODO("Not yet implemented")
    }

    fun getSetting() {

        scope.launch {
            dashboardRepository.getSetting {
                when (it) {
                    is APIResult.Success -> {
                        val data = it.data
                        setContent(data)
                        pd.cancel()
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun setContent(setting: Setting) {
        if (!setting.dashboardImg.isNullOrBlank()) {
            binding.ivdashboard.visibility = View.VISIBLE
            binding.ivdashboard.load(setting.dashboardImg) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
            }
        } else {
            binding.ivdashboard.visibility = View.GONE
        }
//        firstSection
        if (!setting.firstSectionImg.isNullOrBlank()) {
            binding.ivC1Image.visibility = View.VISIBLE
            binding.ivC1Image.load(setting.firstSectionImg) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
            }
        } else {
            binding.ivC1Image.visibility = View.GONE
        }
        if (!setting.firstSectionHeading.isNullOrBlank()) {
            binding.ivC1Heading.visibility = View.VISIBLE
            binding.ivC1Heading.text = setting.firstSectionHeading
        } else {
            binding.ivC1Heading.visibility = View.GONE
        }
        if (!setting.firstSectionContent.isNullOrBlank()) {
            binding.ivC1Content.visibility = View.VISIBLE
            binding.ivC1Content.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(setting.firstSectionContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(setting.firstSectionContent)
            }
        } else {
            binding.ivC1Content.visibility = View.GONE
        }


        //secSection
        if (!setting.secSectionImg.isNullOrBlank()) {
            binding.ivC2Image.visibility = View.VISIBLE
            binding.ivC2Image.load(setting.secSectionImg) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
            }
        } else {
            binding.ivC2Image.visibility = View.GONE
        }
        if (!setting.secSectionHeading.isNullOrBlank()) {
            binding.ivC2Heading.visibility = View.VISIBLE
            binding.ivC2Heading.text = setting.secSectionHeading
        } else {
            binding.ivC2Heading.visibility = View.GONE
        }
        if (!setting.secSectionContent.isNullOrBlank()) {
            binding.ivC2Content.visibility = View.VISIBLE
            binding.ivC2Content.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(setting.secSectionContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(setting.secSectionContent)
            }
        } else {
            binding.ivC2Content.visibility = View.GONE
        }

//        thirdSection
        if (!setting.thirdSectionHeading.isNullOrBlank()) {
            binding.ivC3Heading.visibility = View.VISIBLE
            binding.ivC3Heading.text = setting.thirdSectionHeading
        } else {
            binding.ivC3Heading.visibility = View.GONE
        }
        if (!setting.thirdSectionContent.isNullOrBlank()) {
            binding.ivC3Content.visibility = View.VISIBLE
            binding.ivC3Content.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(setting.thirdSectionContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(setting.thirdSectionContent)
            }
        } else {
            binding.ivC3Content.visibility = View.GONE
        }

        if (!setting.fourthSectionHeading.isNullOrBlank()) {
            binding.ivC4Heading.visibility = View.VISIBLE
            binding.ivC4Heading.text = setting.fourthSectionHeading
        } else {
            binding.ivC4Heading.visibility = View.GONE
        }
        if (!setting.fourthSectionContent.isNullOrBlank()) {
            binding.ivC4Content.visibility = View.VISIBLE
            binding.ivC4Content.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(setting.fourthSectionContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(setting.fourthSectionContent)
            }
        } else {
            binding.ivC4Content.visibility = View.GONE
        }

        binding.textView5.text=setting.total_register_users
        binding.textView7.text=setting.total_users_in_city

        preferenceManager.pusherAppId = setting.pusher_app_id.toString()
        preferenceManager.pusherKey = setting.pusher_key.toString()
        preferenceManager.pusherSecret = setting.pusher_secret.toString()
        preferenceManager.pusherCluster = setting.pusher_cluster.toString()
        preferenceManager.agoraAppId = setting.agora_app_id.toString()
        preferenceManager.agoraAppSecret = setting.agora_app_certificate.toString()

    }

    fun postJoinGroup(group_join_Id: String, position: Int) {
        scope.launch {
            groupRepository.postJoinGroup(group_join_Id) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        groupAdapter.removeFromList(position)
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
        }
    }


    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val lastLocation = task.result
                Log.i(TAG, "getLastLocation: latitudeLabel  " + (lastLocation)!!.latitude)
                Log.i(TAG, "getLastLocation: longitudeLabel  " + (lastLocation).longitude)

                latitude = (lastLocation).latitude
                longitude = (lastLocation).longitude

                Log.e("latitude=====>", latitude.toString())
                Log.e("longitude=====>", longitude.toString())


                updateLatLng(latitude, longitude)
            } else {

                val mshg = "No location detected. Make sure location is enabled on the device."
                Log.w(TAG, "getLastLocation:exception", task.exception)
                Toast.makeText(this.mContext, mshg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateLatLng(latitude: Double, longitude: Double) {
        scope.launch {
            dashboardRepository.updateLatLng(
                latitude,
                longitude,
            ) {

                when (it) {
                    is APIResult.Success -> {
                        if(pd!=null && pd.isShowing) {
                            pd.cancel()
                        }
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }

                }
            }
        }
    }


}