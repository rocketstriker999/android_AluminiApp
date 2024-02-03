package com.sanatanshilpisanstha.ui.group.survey

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.group.survey.Survey
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.databinding.ActivitySurveySettingBinding
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.group.ChatActivity
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class SurveySettingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySurveySettingBinding
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    val TAG = "AddSurveryActivity"
    private lateinit var groupRepository: GroupRepository
    var survey: Survey? = null
    var groupId = 0
    var name = ""
    var desc = ""
    var cover = ""
    var expire = ""
    var cal = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm")
    val formatter1 = SimpleDateFormat("EEE dd MMM, hh:mm a")

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySurveySettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        groupRepository = GroupRepository(this)

        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        name = intent.getStringExtra(Extra.SURVEY_NAME).toString()
        desc = intent.getStringExtra(Extra.SURVEY_DESC).toString()
        cover = intent.getStringExtra(Extra.SURVEY_IMAGE).toString()

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow: Date = cal.time
        val tomorrowAsString: String = formatter1.format(tomorrow)
        expire = tomorrowAsString
        binding.tvExpiry.text = "Survey Expiry: $expire"
        binding.tvSryCount.text =
            "This Survey has ${AddSurveryActivity.questionList?.size} Question"
        addListener()
    }

    private fun addListener() {
        binding.clExpiry.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
    }

    fun setData() {


        survey = Survey(
            "" + cover, "" + desc, 0, "" + expire, groupId,
            0, 0, AddSurveryActivity.questionList, 0, "" + name, 0
        )
        survey!!.editResponse = if (binding.cbResponsees.isChecked) {
            1
        } else {
            0
        }
        survey!!.multipleResponse = if (binding.cbResponsees.isChecked) {
            1
        } else {
            0
        }
        survey!!.sendReminder = if (binding.cbReminder.isChecked) {
            1
        } else {
            0
        }
        postSurvey()


    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnNext -> {
                setData()
            }
            binding.btnNext -> {
                finish()
            }
            binding.clExpiry -> {
                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, monthOfYear)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        expire = formatter.format(cal.time)
                        binding.tvExpiry.text = "Survey Expiry: $expire"

                    }
                DatePickerDialog(
                    this,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            binding.btnNext -> {
                setData()
            }
        }
    }

    private fun postSurvey() {

        scope.launch {
            survey?.let {
                groupRepository.postSurvey(
                    it
                ) {
                    when (it) {
                        is APIResult.Success -> {
                            Log.d("Success", it.data)
                            binding.btnNext.text = "Send";
                            binding.progressBar.visibility = View.GONE
                            val i =
                                Intent(this@SurveySettingActivity, ChatActivity::class.java)
                            i.putExtra(Extra.GROUP_ID, groupId)
                            startActivity(i)
                            finishAffinity()
                            Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                            binding.progressBar.visibility = View.GONE
                        }

                        is APIResult.Failure -> {
                            Log.d("Failure", it.message.toString())
                            binding.btnNext.text = "Send";
                            binding.progressBar.visibility = View.GONE
                            Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
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
    }

}