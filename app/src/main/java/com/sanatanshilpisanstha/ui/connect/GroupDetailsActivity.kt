package com.sanatanshilpisanstha.ui.connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.onItemClickListener
import com.sanatanshilpisanstha.data.entity.group.GroupMember
import com.sanatanshilpisanstha.data.entity.group.GroupMembersSelect
import com.sanatanshilpisanstha.databinding.ActivityGroupDetailsBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.BaseActivity
import com.sanatanshilpisanstha.ui.adapter.GroupPartAdapter
import com.sanatanshilpisanstha.ui.adapter.ParticipantsListMembersAdapter
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GroupDetailsActivity : BaseActivity() , onItemClickListener {

    private lateinit var binding: ActivityGroupDetailsBinding
    private val TAG = "GroupDetailsActivity"
    private var groupId = 0
    private var groupName = ""
    private var groupBanner = ""
    private var memberList: ArrayList<GroupMember> = arrayListOf()
    private lateinit var participantsListMembersAdapter: ParticipantsListMembersAdapter
    private lateinit var groupRepository: GroupRepository
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        init()

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun init(){
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        groupRepository = GroupRepository(this)

        if(Utilities.IsValidUrl(groupBanner)){
            binding.ivGroup.load(groupBanner) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        }else{
            binding.ivGroup.load(Constant.ImageBannerURL +groupBanner) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        }
        getGroupMember()
    }



    private fun getGroupMember() {
        scope.launch {
            groupRepository.getGroupMember(groupId) {
                when (it) {
                    is APIResult.Success -> {
                        memberList = it.data
                        setAdepter()
                        dismissDialog()
                    }

                    is APIResult.Failure -> {
                        dismissDialog()
                    }

                    APIResult.InProgress -> {
                      showProgressDialog()
                    }
                }
            }
        }
    }
    private fun setAdepter () {
        participantsListMembersAdapter = ParticipantsListMembersAdapter(this,memberList,this)
        binding.rvParticipants.adapter = participantsListMembersAdapter
    }

    override fun onItemClick(id: Int) {
        val i = Intent(this, ContactInfoActivity::class.java)
        i.putExtra(Extra.USER_ID, id)
        startActivity(i)
    }

}