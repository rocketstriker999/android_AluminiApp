package com.sanatanshilpisanstha.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.databinding.ActivityJoinGroupBinding

class JoinGroupActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityJoinGroupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityJoinGroupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    fun init() {
        addlistner()
    }

    fun addlistner() {
        binding.cdNew.setOnClickListener(this)
        binding.cdExissting.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.cdNew -> {
                val intent = Intent(this, NewGroupActivity::class.java)
                this.startActivity(intent)
            }
            binding.cdExissting -> {
                val intent = Intent(this, JoinGroupCodeActivity::class.java)
                this.startActivity(intent)
            }
            binding.ivBack -> {
                finish();
            }
        }
    }
}