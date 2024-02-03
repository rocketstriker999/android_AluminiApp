package com.sanatanshilpisanstha.ui.group.survey

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.Option
import com.sanatanshilpisanstha.data.entity.group.survey.Dropdown
import com.sanatanshilpisanstha.data.entity.group.survey.Question
import com.sanatanshilpisanstha.databinding.ActivitySurveyMultiQuelBinding
import com.sanatanshilpisanstha.ui.adapter.OptionAdapter
import com.sanatanshilpisanstha.utility.Extra.TITLE


class SurveyDdQueActivity : AppCompatActivity(), View.OnClickListener, OptionAdapter.ItemClick {
    private lateinit var binding: ActivitySurveyMultiQuelBinding
    private lateinit var adapter: OptionAdapter
    var a1: java.util.ArrayList<Option> = arrayListOf()
    var option = ""
    val TAG = "SurveyDdQueActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySurveyMultiQuelBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        binding.cbResponse.visibility = View.GONE
        init()
    }

    private fun init() {
        val intent = intent
        val str = intent.getStringExtra(TITLE)

        binding.tvPersonName.text = str
        a1.add(
            Option(
                "",
            )
        )
        a1.add(
            Option(
                "",
            )
        )
        adapter =
            OptionAdapter(this, a1, this)
        binding.rvOption.adapter = adapter
        addListener()
    }

    private fun addListener() {
        binding.ivBack.setOnClickListener(this)
        binding.ivAddOption.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.cbResponse.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.btnNext -> {

                for (item in a1) {
                    Log.i(TAG, "onClick: " + item.option)
                    option = option + "," +item.option
                }
                option.dropLast(1)
                Toast.makeText(this, "testoption" + option, Toast.LENGTH_SHORT).show()
                if (checkValidation()) {
                    if (AddSurveryActivity.questionList != null) {
                        setData()
                    }
                }
            }
            binding.ivAddOption -> {
                a1.add(
                    Option(
                        ""
                    )
                )
                adapter.updateList(a1)
            }
        }
    }

    private fun checkValidation(): Boolean {
        if (binding.editTextTextPersonName4.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Required field", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun setData() {
        val cbOutAllow = if (binding.cbOutAllow.isChecked) {
            "1"
        } else {
            "0"
        }

        AddSurveryActivity.questionList?.add(
            Question(
                null,
                Dropdown(
                    "" + option,
                    "",
                    "" + binding.editTextTextPersonName4.text.toString(),
                    "" + cbOutAllow
                ),
                null,
                null,
                null,
                null,
                null
            )
        )
        finish()
    }


    override fun itemClick(id: String) {
//        a1[id].option = data
    }

    override fun optionClick(id: Int, data: String) {
        a1[id].option = data
    }

}