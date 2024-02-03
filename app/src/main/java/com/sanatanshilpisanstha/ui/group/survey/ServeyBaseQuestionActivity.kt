package com.sanatanshilpisanstha.ui.group.survey

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.group.survey.*
import com.sanatanshilpisanstha.databinding.ActivityServeyBaseQuestionBinding
import com.sanatanshilpisanstha.utility.Extra
import java.io.File

class ServeyBaseQuestionActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityServeyBaseQuestionBinding
    var filePhoto: File? = null
    var filePath: String = ""
    val TAG = "ServeyBaseQuestionActivity"
    var imagePath = ""
    var str = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityServeyBaseQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        init()
    }

    private fun init() {
        val intent = intent
        if (intent != null) {
            str = intent.getStringExtra(Extra.TITLE).toString()
        }
        if (str == "Image") {
            binding.cbOnlyCam.visibility = View.VISIBLE
            binding.cbSingle.visibility = View.VISIBLE
        }
        binding.tvPersonName.text = str

        addListener()
    }

    private fun addListener() {
        binding.ivBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.btnNext -> {
//                finish()
                if (checkValidation()) {
                    if (AddSurveryActivity.questionList != null) {
                        setData()
                    }
                }
            }
        }
    }

    private fun setData() {

        val single = if (binding.cbOptional.isChecked) {
            "1"
        } else {
            "0"
        }
        val cbSingle = if (binding.cbSingle.isChecked) {
            "1"
        } else {
            "0"
        }
        val cbOnlyCam = if (binding.cbOnlyCam.isChecked) {
            "1"
        } else {
            "0"
        }

        when (str) {
            "Multiple Choice" -> {

            }
            "Drop Down" -> {

            }
            "Text" -> {
                AddSurveryActivity.questionList?.add(
                    Question(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Text("", "" + binding.etQuestion.text.toString(), single)
                    )
                )
                finish()
            }
            "Image" -> {
                AddSurveryActivity.questionList?.add(
                    Question(
                        null,
                        null,
                        Image(
                            arrayListOf(),
                            "" + binding.etQuestion.text.toString(),
                            single,
                            cbSingle
                        ),
                        null,
                        null,
                        null,
                        null

                    )
                )
                finish()
            }
            "Numeric" -> {
                AddSurveryActivity.questionList?.add(
                    Question(
                        null,
                        null,

                        null,
                        null,
                        Numeric("", "" + binding.etQuestion.text.toString(), single),
                        null,
                        null

                    )
                )
                finish()
            }
            "Phone" -> {
                AddSurveryActivity.questionList?.add(
                    Question(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Phone("", "" + binding.etQuestion.text.toString(), single),
                        null

                    )
                )
                finish()
            }
            "Date" -> {
                AddSurveryActivity.questionList?.add(
                    Question(
                        Date("", "" + binding.etQuestion.text.toString(), single),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null

                    )
                )
                finish()
            }

        }
    }

    private fun checkValidation(): Boolean {
        if (binding.etQuestion.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Required field", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}