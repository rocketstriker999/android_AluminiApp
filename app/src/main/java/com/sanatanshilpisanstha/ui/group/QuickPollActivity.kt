package com.sanatanshilpisanstha.ui.group

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.group.QuickPoll
import com.sanatanshilpisanstha.data.entity.group.survey.Option
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.databinding.ActivityQuickPollBinding
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.adapter.MultiOptionAdapter
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class QuickPollActivity : AppCompatActivity(), View.OnClickListener, MultiOptionAdapter.ItemClick {


    private lateinit var binding: ActivityQuickPollBinding
    private lateinit var adapter: MultiOptionAdapter
    var a1: java.util.ArrayList<Option> = arrayListOf()
    val TAG = "QuickPollActivity"
    private val parentJob = Job()
    var quickPoll: QuickPoll? = null

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    var cal = Calendar.getInstance()

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var groupRepository: GroupRepository
    var groupId = 0
    var groupName=""
    var groupBanner =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityQuickPollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        init()
        binding.tvDate.text =
            SimpleDateFormat("yyyy-MM-dd hh-mm").format(System.currentTimeMillis())

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd hh:mm" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.tvDate.text = sdf.format(cal.time)
            }

        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun init() {
        val intent = intent
         groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        Log.i(TAG, "init: groupId : " + groupId)
        groupRepository = GroupRepository(this)
        a1.add(
            Option(
                "Option 1", ""
            )
        )
        a1.add(
            Option(
                "Option 2", ""
            )
        )
        adapter =
            MultiOptionAdapter(this, a1, this)
        binding.rvOption.adapter = adapter
        addListener()
    }

    private fun addListener() {
        binding.ivBack.setOnClickListener(this)
        binding.ivAddOption.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }

            binding.ivAddOption -> {
                a1.add(
                    Option(
                        "", ""
                    )
                )
                adapter.updateList(a1)
            }

            binding.btnNext -> {
                if (checkValidation()) {
                        setData()
                }
            }
        }
    }

    fun setData() {
        val cbVisibleOnlyMe = if (binding.cbOutAllow.isChecked) {
            1
        } else {
            0
        }
        quickPoll = QuickPoll(
            "" + binding.tvDate.text.toString(),
            groupId,
            a1,
            "" + binding.editTextTextPersonName4.text.toString(),
            cbVisibleOnlyMe,
        )
        postQuickPoll()
    }

    private fun checkValidation(): Boolean {
        if (binding.editTextTextPersonName4.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Question is Required!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun datePicker() {
//        2023-03-18 18:00

    }

    override fun itemClick(id: Int, data: String) {
        a1[id].option = data
    }

    fun postQuickPoll() {

        scope.launch {
            quickPoll?.let {
                groupRepository.postQuickPoll(
                    it
                ) {
                    when (it) {
                        is APIResult.Success -> {
                            Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                            val i = Intent(this@QuickPollActivity, ChatActivity::class.java)
                            i.putExtra(Extra.GROUP_ID, groupId)
                            i.putExtra(Extra.GROUP_NAME, groupName)
                            i.putExtra(Extra.GROUP_BANNER, groupBanner)
                            startActivity(i)
                            finishAffinity()

                        }

                        is APIResult.Failure -> {
                            binding.btnNext.text = "SEND"
                            binding.progressBar.visibility = View.GONE
                            Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        }

                        APIResult.InProgress -> {
                            binding.btnNext.text = ""
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        else -> {

                        }
                    }
                }
            }
        }
    }

}