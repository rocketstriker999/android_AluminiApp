package com.sanatanshilpisanstha.ui.group

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.JobAction
import com.sanatanshilpisanstha.data.entity.group.GroupMember
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.databinding.ActivityJobBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class  JobActivity : AppCompatActivity(), View.OnClickListener {
    var cameraPicker: CameraPicker? = null
    private lateinit var binding: ActivityJobBinding
    private lateinit var groupRepository: GroupRepository
    private var memberList: ArrayList<GroupMember> = arrayListOf()
    private var assigned: ArrayList<Int> = arrayListOf()
    var cal = Calendar.getInstance()
    private var jobAction: JobAction = JobAction("", "", "", "", arrayListOf(), 0)
    private val parentJob = Job()
    private val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    var groupName = ""
    var groupBanner = ""

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    var groupId = 0

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityJobBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        groupRepository = GroupRepository(this)
        init()
    }

    private fun init() {
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        getGroupMember()
        addListener()
        jobAction.group_id = groupId.toString()
    }

    private fun addListener() {
        binding.tvDate.setOnClickListener(this)
        binding.tvTime.setOnClickListener(this)
        binding.tvToday.setOnClickListener(this)
        binding.tvTomorrow.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivAddphoto.setOnClickListener(this)
        binding.ivPhotoClose.setOnClickListener(this)
    }

    private fun showDatePickerDialog() {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this, R.style.MyTimePickerDialogTheme, { _, selectedYear, selectedMonth, selectedDate ->

                val sdf = SimpleDateFormat(Constant.APP_DATE_FORMAT, Locale.ENGLISH)
                year = selectedYear
                month = selectedMonth
                day = selectedDate

                calendar.set(selectedYear, selectedMonth, selectedDate)

            }, year, month, day
        )
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.datePicker.maxDate = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 3)
        dpd.show()
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.tvDate -> {
                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, monthOfYear)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        binding.tvDate.text = formatter.format(cal.time)
                    }
                DatePickerDialog(
                    this,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            binding.tvToday -> {
                val date = Date()
                val currentDate = formatter.format(date)

                val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val currentTime = formatter.format(time)

                binding.tvDate.text = currentDate
                binding.tvTime.text = currentTime
            }
            binding.ivBack -> {
                finish()
            }
            binding.ivAddphoto -> {
                getGallery()
            }
            binding.ivPhotoClose -> {
                jobAction.photo = ""
                binding.ivPhoto.visibility = View.GONE
                binding.ivPhotoClose.visibility = View.GONE
            }
            binding.tvTime -> {
                val c = Calendar.getInstance()
                val mHour = c[Calendar.HOUR_OF_DAY]
                val mMinute = c[Calendar.MINUTE]

                // Launch Time Picker Dialog

                // Launch Time Picker Dialog
                val timePickerDialog = TimePickerDialog(
                    this,
                    { view, hourOfDay, minute -> binding.tvTime.text = "$hourOfDay:$minute" },
                    mHour,
                    mMinute,
                    true
                )
                timePickerDialog.show()
            }

            binding.tvTomorrow -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow: Date = cal.time
                val tomorrowAsString: String = formatter.format(tomorrow)

                val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val currentTime = formatter.format(time)
                binding.tvDate.text = tomorrowAsString
                binding.tvTime.text = currentTime

            }


            binding.btnSend -> {
                if (checkValidation()) {
                    jobAction.description = binding.etDesc.text.toString().trim()
                    jobAction.datetime =
                        binding.tvDate.text.toString().trim() + " " + binding.tvTime.text.toString()
                            .trim()
                    jobAction.assigned_to = assigned
                    jobAction.show_only_me = if (binding.cbResponse.isChecked) {
                        1
                    } else {
                        0
                    }

                    postJob()
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        return if (binding.etDesc.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Description is required.", Toast.LENGTH_LONG).show()
            false
        } else if (binding.tvDate.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Date is required.", Toast.LENGTH_LONG).show()
            false
        } else if (binding.tvTime.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Time is required.", Toast.LENGTH_LONG).show()
            false
        } else {
            true
        }
    }


    private fun showSubCategoryMenu() {


        val roleAdapter = ArrayAdapter(
            this,
            R.layout.layout_spinner_item,
            memberList.map { groupMember -> groupMember.name }.toList()
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.actItemType.setAdapter(roleAdapter)
        binding.actItemType.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                memberList[position].name?.let { Log.d("MEMBER SELECTIOn", it) }
                if (assigned.filter { i -> i == memberList[position].id }.isEmpty()) {
                    memberList[position].id?.let { assigned.add(it) }
                }

            }


    }


    private fun getGroupMember() {
        scope.launch {
            groupRepository.getGroupMember(groupId) {
                when (it) {
                    is APIResult.Success -> {
                        memberList = it.data
                        showSubCategoryMenu()
                        binding.progressBar.visibility = View.GONE
//                        finish()
                    }

                    is APIResult.Failure -> {
                        Log.d("Failure", it.message.toString())
                        binding.progressBar.visibility = View.GONE
                    }

                    APIResult.InProgress -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }

    }

    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                binding.ivPhoto.visibility = View.VISIBLE
                binding.ivPhotoClose.visibility = View.VISIBLE
                jobAction.photo = Utilities.getFileToByte(file.path).toString()
                binding.ivPhoto.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
//                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }

    private fun postJob() {
        scope.launch {
            groupRepository.postJob(jobAction) {
                when (it) {
                    is APIResult.Success -> {
                        Log.d("Success", it.data)
                        binding.btnSend.text = "Send";
                        binding.progressBar.visibility = View.GONE
                        val i = Intent(this@JobActivity, ChatActivity::class.java)
                        i.putExtra(Extra.GROUP_ID, groupId)
                        i.putExtra(Extra.GROUP_NAME, groupName)
                        i.putExtra(Extra.GROUP_BANNER, groupBanner)
                        startActivity(i)
                    }

                    is APIResult.Failure -> {
                        Log.d("Failure", it.message.toString())
                        binding.btnSend.text = "Send";
                        binding.progressBar.visibility = View.GONE

                    }

                    APIResult.InProgress -> {
                        binding.btnSend.text = "";
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }

}