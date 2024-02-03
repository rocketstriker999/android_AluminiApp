package com.sanatanshilpisanstha.ui.group

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.databinding.ActivityJoinGroupCodeBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity
import com.sanatanshilpisanstha.utility.Extra.INTENT
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class JoinGroupCodeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityJoinGroupCodeBinding
    private lateinit var groupRepository: GroupRepository
    private val parentJob = Job()
    lateinit var pd: ProgressDialog

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityJoinGroupCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    fun init() {
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        groupRepository = GroupRepository(this)
        addlistner()
    }

    fun addlistner() {
        binding.button.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.button -> {
                if (binding.editTextTextPersonName3.text.toString().trim().isNotBlank()) {
                    postJoinGroup(binding.editTextTextPersonName3.text.toString().trim())
                } else {
                    Utilities.showErrorSnackBar(binding.cvRoot, "Code is required")

                }
            }
            binding.ivBack -> {
                finish();
            }
        }
    }

    fun postJoinGroup(search: String) {

        scope.launch {
            groupRepository.postJoinGroup(search) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        val intent = Intent(
                            this@JoinGroupCodeActivity,
                            BottomNavActivity::class.java
                        )
                        intent.putExtra(INTENT, "connect")
                        startActivity(intent)
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
}