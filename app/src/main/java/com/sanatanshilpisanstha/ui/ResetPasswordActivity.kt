package com.sanatanshilpisanstha.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.databinding.ActivityResetPasswordBinding
import com.sanatanshilpisanstha.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ResetPasswordActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityResetPasswordBinding
    var visiblePassword = false
    var cvisiblePassword = false

    //Create a new Job
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    var number = ""
    private lateinit var accountRepository: AccountRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        accountRepository = AccountRepository(this)

        number = intent.getStringExtra(Extra.NUMBER).toString()
        addListener()

    }

    fun toggle() {
        if (visiblePassword) {
            binding.etPassword.transformationMethod = SingleLineTransformationMethod()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.ivpassword.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.visibility_off,
                        applicationContext.theme
                    )
                )
            } else {
                binding.ivpassword.setImageDrawable(resources.getDrawable(R.drawable.visibility_off))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.ivpassword.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.visibility,
                        applicationContext.theme
                    )
                )
            } else {
                binding.ivpassword.setImageDrawable(resources.getDrawable(R.drawable.visibility))
            }
            binding.etPassword.transformationMethod = PasswordTransformationMethod()
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
        visiblePassword = !visiblePassword
    }

    fun cToggle() {
        if (cvisiblePassword) {
            binding.etCPassword.transformationMethod = SingleLineTransformationMethod()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.ivCpassword.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.visibility_off,
                        applicationContext.theme
                    )
                )
            } else {
                binding.ivCpassword.setImageDrawable(resources.getDrawable(R.drawable.visibility_off))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.ivCpassword.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.visibility,
                        applicationContext.theme
                    )
                )
            } else {
                binding.ivCpassword.setImageDrawable(resources.getDrawable(R.drawable.visibility))
            }
            binding.etCPassword.transformationMethod = PasswordTransformationMethod()
        }
        binding.etCPassword.setSelection(binding.etCPassword.text.length)
        cvisiblePassword = !cvisiblePassword
    }

    private fun addListener() {
        binding.btnSubmit.setOnClickListener(this)
        binding.ivpassword.setOnClickListener(this)
        binding.ivCpassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubmit -> {
                if (checkValidation()) {
                    resetPasswordAPI()
                }
            }
            binding.ivpassword -> {
                toggle()

            }
            binding.ivCpassword -> {
                cToggle()

            }
        }
    }

    fun resetPasswordAPI() {

        scope.launch {
            accountRepository.resetPassword(
                number,
                binding.etPassword.text.toString(),
                binding.etCPassword.text.toString()
            ) {
                when (it) {
                    is APIResult.Success -> {
                        binding.btnSubmit.text = "Confirm Password"
                        binding.progressBar.visibility = View.GONE
                        val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                        intent.putExtra(Extra.NUMBER, number)
                        startActivity(intent)
                        finishAffinity()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                    }

                    is APIResult.Failure -> {
                        binding.btnSubmit.text = "Confirm Password"
                        binding.progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnSubmit.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        if (binding.etCPassword.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Contact Number is required", Toast.LENGTH_SHORT).show()

            return false
        }
        if (binding.etCPassword.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()

            return false
        }
        if (binding.etPassword.text == binding.etCPassword.text) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()

            return false
        }

        return true
    }


}