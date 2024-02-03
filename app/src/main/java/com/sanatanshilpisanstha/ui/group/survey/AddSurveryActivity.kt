package com.sanatanshilpisanstha.ui.group.survey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.data.entity.group.survey.Question
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityAddSurveryBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.adapter.SurveyQuestionAdapter
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Extra.SURVEY_DESC
import com.sanatanshilpisanstha.utility.Extra.SURVEY_IMAGE
import com.sanatanshilpisanstha.utility.Extra.SURVEY_NAME
import com.sanatanshilpisanstha.utility.Extra.TITLE
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import kotlin.coroutines.CoroutineContext

class AddSurveryActivity : AppCompatActivity(), View.OnClickListener,
    SurveyBottomDialogFragment.ItemClickListener, SurveyQuestionAdapter.ItemClick {
    private lateinit var binding: ActivityAddSurveryBinding
    var question: Question? = null
    var cameraPicker: CameraPicker? = null
    var groupId = 0
    var groupName=""
    var groupBanner = ""
    private lateinit var surveyQuestionAdapter: SurveyQuestionAdapter
    private lateinit var preferenceManager: PreferenceManager
    private val parentJob = Job()
    var coverImage = ""

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    val TAG = "AddSurveryActivity"

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    private lateinit var groupRepository: GroupRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAddSurveryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    private fun init() {
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        binding.editTextTextPersonName3.setText(intent.getStringExtra(Extra.titleSurvey).toString())
        binding.editTextTextPersonName4.setText(intent.getStringExtra(Extra.descriptionSurvey).toString())
        groupRepository = GroupRepository(this)

            binding.rvQuestion.visibility = View.VISIBLE
            surveyQuestionAdapter =
                SurveyQuestionAdapter(this, questionList, this)
            binding.rvQuestion.adapter = surveyQuestionAdapter

        addListener()
    }

    private fun addListener() {
        binding.cvAddQuestion.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.textView17.setOnClickListener(this)
        binding.constraintLayout10.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

            surveyQuestionAdapter.notifyDataSetChanged()

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.cvAddQuestion -> {
                showBottomSheet()
            }
            binding.textView17 -> {
                getGallery()
            }
            binding.constraintLayout10 -> {
            getGallery()
        }
            binding.btnNext -> {
                if (checkValidation()) {
                    setData()
                }
            }
            binding.ivBack -> {
                finish()
            }
        }
    }

    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                file.path
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
                if (image64 != null) {
                    coverImage = image64
                }
                binding.constraintLayout10.visibility = View.GONE
                binding.textView17.visibility = View.GONE
                binding.imageView14.visibility = View.VISIBLE
//                binding.imageView14.setImageURI(Uri.fromFile(file))
                binding.imageView14.load(Uri.fromFile(file)) {
                }
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }


    private fun showBottomSheet() {
        val addBottomDialogFragment: SurveyBottomDialogFragment =
            SurveyBottomDialogFragment()
        addBottomDialogFragment.show(
            supportFragmentManager,
            "tab"
        )
    }

    override fun onItemClick(item: String?) {
        when (item) {
            "clMultipleChoice" -> {
                val intent = Intent(this, SurveyMultiQueActivity::class.java)
                intent.putExtra(TITLE, "Multiple Choice");
                startActivity(intent)
            }
            "clDropDown" -> {
                val intent = Intent(this, SurveyDdQueActivity::class.java)
                intent.putExtra(TITLE, "Drop Down");
                startActivity(intent)
            }
            "clText" -> {
                val intent = Intent(this, ServeyBaseQuestionActivity::class.java)
                intent.putExtra(TITLE, "Text");
                startActivity(intent)
            }
            "clImage" -> {
                val intent = Intent(this, ServeyBaseQuestionActivity::class.java)
                intent.putExtra(TITLE, "Image");
                startActivity(intent)
            }
            "clNumeric" -> {
                val intent = Intent(this, ServeyBaseQuestionActivity::class.java)
                intent.putExtra(TITLE, "Numeric");
                startActivity(intent)
            }
            "clPhone" -> {
                val intent = Intent(this, ServeyBaseQuestionActivity::class.java)
                intent.putExtra(TITLE, "Phone");
                startActivity(intent)
            }
            "clDate" -> {
                val intent = Intent(this, ServeyBaseQuestionActivity::class.java)
                intent.putExtra(TITLE, "Date");
                startActivity(intent)
            }

        }
    }

    fun setData() {
//        val text: Text = Text("", "sdcsdc", "0")
//        val question: Question = Question(null, null, null, null, null, null, text)
//        question?.let { questionList?.add(it) }

        if (questionList?.isNotEmpty() == true) {
            val intent = Intent(this, SurveySettingActivity::class.java)
            intent.putExtra(SURVEY_NAME, binding.editTextTextPersonName3.text.toString().trim())
            intent.putExtra(SURVEY_DESC, binding.editTextTextPersonName4.text.toString())
            intent.putExtra(SURVEY_IMAGE, coverImage)
           intent.putExtra(Extra.GROUP_ID, groupId)
            intent.putExtra(Extra.GROUP_NAME, groupName)
            intent.putExtra(Extra.GROUP_BANNER, groupBanner)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please add question", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkValidation(): Boolean {
        return if (binding.editTextTextPersonName3.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Title is required.", Toast.LENGTH_LONG).show()
            false
        } else if (binding.editTextTextPersonName4.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Description is required.", Toast.LENGTH_LONG).show()
            false
        } /*else if (announcement.attachment.audio.isEmpty() && announcement.attachment.photos.isEmpty() && announcement.attachment.document.ext.isEmpty()) {
            Toast.makeText(this, "Attachment is required.", Toast.LENGTH_LONG).show()
            false
        }*/ else {
            true
        }
    }


    companion object {
        public var questionList: ArrayList<Question?>? = arrayListOf()
    }

    override fun itemClick(id: String) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPicker?.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

}