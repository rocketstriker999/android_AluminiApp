package com.sanatanshilpisanstha.screen

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.MapMarker
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.ui.directory.DirectoryChatActivity
import com.sanatanshilpisanstha.ui.group.ChatActivity
import com.sanatanshilpisanstha.utility.Extra
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MapsMarkerActivity : AppCompatActivity(), OnMapReadyCallback {





    private var mMap: GoogleMap? = null
    private lateinit var clusterManager: ClusterManager<CustomMapClusterItem>

    val start = 0
    val length = 500

    lateinit var pd: ProgressDialog
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    val list: ArrayList<MapMarker> = arrayListOf()
    private lateinit var dashboardRepository: DashboardRepository

    inner class CustomMapClusterItem(
        private val locationMarker: LatLng,
        private val locationTitle: String,
        private val locationSnippet: String,
        private val userId: Int
    ) : ClusterItem {

        fun getUserId():Int{
            return this.userId
        }

        override fun getPosition(): LatLng {
            return this.locationMarker;
        }

        override fun getTitle(): String? {
            return this.locationTitle;
        }

        override fun getSnippet(): String? {
            return this.locationSnippet;
        }

        override fun getZIndex(): Float? {
            return 0.0f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_marker)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        init()
    }

    fun init() {
        dashboardRepository = DashboardRepository(this)
        pd = ProgressDialog(this)
        pd.setMessage("Loading Locations")
        pd.setCancelable(false)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getMapMarker(start, length)
    }

    private fun getMapMarker(start: Int, length: Int) {
        scope.launch {
            dashboardRepository.getMapMarker(

                start,
                length,

                ) {

                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()

                        list.clear()
                        list.addAll(it.data)
                        setMarkers()
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

    private fun setMarkers() {


        clusterManager = ClusterManager(this, mMap!!)

        // Add cluster items (markers) to the cluster manager.
        for (location in list) {
            val offsetItem = CustomMapClusterItem(
                LatLng(location.latitude!!,location.longitude!!),
                location.name!!,
                location.city!!,
                location.id!!
            )
            clusterManager.addItem(offsetItem)
        }

        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(LatLng(
            intent.getDoubleExtra("INIT_LAT",0.0),
            intent.getDoubleExtra("INIT_LONG",0.0)
        )).zoom(6f).build()))

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap!!.setOnCameraIdleListener(clusterManager)
        mMap!!.setOnMarkerClickListener(clusterManager)

        //mMap!!.setOnInfoWindowClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content), "${it.title} From ${it.snippet}",
                10000
            ).setAction("Chat") { _ ->
                val intent = Intent(this, ChatActivity::class.java)
                println(it.getUserId())
                intent.putExtra(Extra.GROUP_ID, it.getUserId())
                intent.putExtra(Extra.GROUP_NAME, it.title)
                intent.putExtra(Extra.DIRECTORY_USER_NAME, it.title)
                startActivity(intent)
            }
            snackbar.show()
            false
        }


    }




}
