package com.sanatanshilpisanstha.ui.group.survey

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.group.survey.MutipleChoice
import com.sanatanshilpisanstha.data.entity.group.survey.Option
import com.sanatanshilpisanstha.data.entity.group.survey.Question
import com.sanatanshilpisanstha.databinding.ActivitySurveyMultiQuelBinding
import com.sanatanshilpisanstha.ui.adapter.MultiOptionAdapter
import com.sanatanshilpisanstha.utility.Extra.TITLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class SurveyMultiQueActivity : AppCompatActivity(), View.OnClickListener,
    MultiOptionAdapter.ItemClick {
    private lateinit var binding: ActivitySurveyMultiQuelBinding
    private lateinit var adapter: MultiOptionAdapter
    var a1: java.util.ArrayList<Option> = arrayListOf()
    val TAG = "SurveyMultiQueActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySurveyMultiQuelBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    private fun init() {
        val intent = intent
        val str = intent.getStringExtra(TITLE)
        binding.tvPersonName.text = str


        a1.add(
            Option(
                "", ""
            )
        )
        a1.add(
            Option(
                "", ""
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
            binding.btnNext -> {
                var aa = ""
                for (item in a1) {
                    Log.i(TAG, "onClick: " + item.option)
                    aa += item.option

                }
                aa.dropLast(1)
                Toast.makeText(this, "testv " + aa, Toast.LENGTH_SHORT).show()
                if (checkValidation()) {
                    if (AddSurveryActivity.questionList != null) {
                        setData()
                    }
                }
            }
            binding.ivAddOption -> {
                a1.add(
                    Option(
                        "", ""
                    )
                )
                adapter.updateList(a1)
            }
        }
    }

    fun setData() {
        val cbOutAllow = if (binding.cbOutAllow.isChecked) {
            "1"
        } else {
            "0"
        }
        val cbResponse = if (binding.cbResponse.isChecked) {
            "1"
        } else {
            "0"
        }


        AddSurveryActivity.questionList?.add(
            Question(
                null,
                null,
                null,
                MutipleChoice(
                    "" + cbOutAllow,
                    "",
                    a1,
                    "" + binding.editTextTextPersonName4.text.toString(),
                    "" + cbResponse
                ),
                null,
                null,
                null
            )
        )
        finish()
    }

    private fun checkValidation(): Boolean {
        if (binding.editTextTextPersonName4.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Required field", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun itemClick(id: Int, data: String) {
        a1[id].option = data
    }

}