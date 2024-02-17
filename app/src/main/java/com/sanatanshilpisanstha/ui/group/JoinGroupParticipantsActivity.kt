package com.sanatanshilpisanstha.ui.group

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.Directory
import com.sanatanshilpisanstha.data.entity.group.GroupMembersSelect
import com.sanatanshilpisanstha.databinding.ActivityJoinGroupParticipantsBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.adapter.GroupPartAdapter
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class JoinGroupParticipantsActivity : AppCompatActivity(), View.OnClickListener,
    GroupPartAdapter.ContactClick {
    lateinit var pd: ProgressDialog

    private lateinit var groupAdapter: GroupPartAdapter
    val arrayID: ArrayList<String> = arrayListOf()
    private lateinit var binding: ActivityJoinGroupParticipantsBinding
    var groupName = ""
    var groupBanner = ""
    var groupDescription=""
    private lateinit var groupRepository: GroupRepository
    private val parentJob = Job()
    private lateinit var dashboardRepository: DashboardRepository

    private val directoryList: ArrayList<Directory> = arrayListOf()

    private lateinit var globalFusedLocationClient: FusedLocationProviderClient

    var latitude: Double? =null
    var longitude: Double? = null

    val start = 0
    val length = 20
    var search = ""
    var mMaxoffset = 0
    var lastPage = 0

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityJoinGroupParticipantsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //supportActionBar?.hide()
        //toolbar= binding.toolBar
        //setSupportActionBar(toolbar);
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title="Add Member"
        init()
    }

    fun init() {
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        groupRepository = GroupRepository(this)
        dashboardRepository = DashboardRepository(this)

        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        groupDescription= intent.getStringExtra("description").toString()
        addlistner()
        groupAdapter =GroupPartAdapter(this, arrayListOf(), this)
        binding.rvMember.adapter = groupAdapter
        globalFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocation(globalFusedLocationClient)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // below line is to get our inflater
        val inflater = menuInflater

        inflater.inflate(com.sanatanshilpisanstha.R.menu.search_menu, menu);

        val searchItem = menu.findItem(com.sanatanshilpisanstha.R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView?

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // on below line we are checking
                // if query exist or not.
                Log.e("query======>",query.toString())
                search=query!!
                directoryList.clear()
                getDirectory(latitude!!, longitude!!, start, length, search)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if(newText?.length==0){
                    search=""
                    directoryList.clear()
                    getDirectory(latitude!!, longitude!!, start, length, search)
                }
                return false
            }


        })

        return true
    }




    fun addlistner() {
        //binding.ivBack.setOnClickListener(this)
        binding.floatingActionButton2.setOnClickListener(this)

        binding.rvMember.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("-----", "end")
                    lastPage = lastPage + length
                    if (mMaxoffset >= lastPage) {
                        Log.e("lastPage =====>", lastPage.toString());

                        if (Utilities.isNetworkAvailable(this@JoinGroupParticipantsActivity)) {
                            getDirectory(latitude!!, longitude!!, lastPage, length, search)

                        } else {
                            Toast.makeText(
                                this@JoinGroupParticipantsActivity,
                                this@JoinGroupParticipantsActivity.resources.getString(R.string.network_error_msg),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    else{
                        println("Limit Reached for this search ${lastPage} ${length} ${search}")
                    }
                }

            }
        })

    }

    override fun onClick(v: View?) {
        when (v) {
            //binding.ivBack -> {
           //     finish()
           // }
            binding.floatingActionButton2 -> {
                for (it in directoryList) {
                    if (it.selected) {
                        it.id?.let { it1 -> arrayID.add(it1) }
                    }
                }
                if (arrayID.isEmpty()){
                    Toast.makeText(this@JoinGroupParticipantsActivity, "Add at least One Member", Toast.LENGTH_SHORT).show()
                }
                else
                    postCreateGroup()
            }
        }
    }

    override fun contactDetailClick(id: String, selectionStatus: Boolean) {

        arrayID.remove(id)
        if(selectionStatus){
            arrayID.add(id)
        }

        //directoryList[id].selected = selectionStatus
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
                groupAdapter.updateList(directoryList)
            }
        }
    }


/*

    fun getConnect() {

        scope.launch {
            groupRepository.getMembers(search) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        memberList.clear()
                        memberList.addAll(it.data)
                        groupAdapter.provideOriginalList(it.data)
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
*/

    fun getLocation(fusedLocationClient : FusedLocationProviderClient){

        //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val lastLocation = task.result
                latitude = (lastLocation).latitude
                longitude = (lastLocation).longitude
                Log.e("latitude=====>", latitude.toString())
                Log.e("longitude=====>", longitude.toString())
                directoryList.clear()
                getDirectory(latitude!!, longitude!!, lastPage, length, search)

            } else {
                Log.w("JOINGROUPPARTICIPANTS", "getLastLocation:exception", task.exception)
            }
        }
    }

    private fun postCreateGroup() {

        scope.launch {
            groupRepository.postCreateGroup("" + groupName, arrayID, "" + groupBanner) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        val intent = Intent(
                            this@JoinGroupParticipantsActivity,
                            BottomNavActivity::class.java
                        )
                        intent.putExtra(Extra.INTENT, "connect")
                        startActivity(intent)
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


}