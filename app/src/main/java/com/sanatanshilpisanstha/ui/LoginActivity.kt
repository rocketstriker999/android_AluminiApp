package com.sanatanshilpisanstha.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.databinding.ActivityLoginBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity
import com.sanatanshilpisanstha.utility.Utilities
import com.sanatanshilpisanstha.utility.Utilities.showErrorSnackBar
import com.sanatanshilpisanstha.utility.Utilities.showSnackBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding

    //Create a new Job
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    private lateinit var accountRepository: AccountRepository
    var visiblePassword = false
    var token = "token"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        accountRepository = AccountRepository(this)

//        binding.etNumber.setText("9033482536")
//        binding.etNumber.setText("9925951533")
//        binding.etPassword.setText("123456789")
//        loginApi( binding.etNumber.text.toString(),
//            binding.etPassword.text.toString(),)


        addListener()
        supportActionBar?.hide()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            token = task.result
        })

    }

    private fun addListener() {
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.ivpassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvForgotPassword -> {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                this.startActivity(intent)
            }
            binding.tvSignUp -> {
                val intent = Intent(this, RegistrationActivity::class.java)
                this.startActivity(intent)
            }
            binding.btnLogin -> {
                if (checkValidation()) {
                    loginApi(
                        binding.etNumber.text.toString(),
                        binding.etPassword.text.toString(),
                    )
                }
            }
            binding.ivpassword -> {
                toggle()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun toggle() {
        if (visiblePassword) {
            binding.etPassword.transformationMethod = SingleLineTransformationMethod()
            binding.ivpassword.setImageDrawable(
                resources.getDrawable(
                    R.drawable.visibility_off,
                    applicationContext.theme
                )
            )
        } else {
            binding.ivpassword.setImageDrawable(
                resources.getDrawable(
                    R.drawable.visibility,
                    applicationContext.theme
                )
            )
            binding.etPassword.transformationMethod = PasswordTransformationMethod()
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
        visiblePassword = !visiblePassword
    }

    private fun checkValidation(): Boolean {
        if (binding.etNumber.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Mobile Number is required", Toast.LENGTH_SHORT).show()

            return false
        }

        if (!Utilities.isValidMobile(binding.etNumber.text?.trim().toString())) {
            Toast.makeText(this, "Please enter valid mobile number", Toast.LENGTH_LONG).show()
            return false
        }

        if (binding.etPassword.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()

            return false
        }

        return true
    }

    fun loginApi(phone: String, password: String) {

        scope.launch {
            accountRepository.login(
                phone,
                password,
                token
            ) {
                when (it) {
                    is APIResult.Success -> {
                        binding.btnLogin.text = "Sign In"
                        binding.progressBar.visibility = View.GONE
                        showSnackBar(binding.cvRoot, it.message.toString())
                        val intent = Intent(this@LoginActivity, BottomNavActivity::class.java)
                        this@LoginActivity.startActivity(intent)
                        finishAffinity()
                    }

                    is APIResult.Failure -> {
                        binding.btnLogin.text = "Sign In"
                        binding.progressBar.visibility = View.GONE
                        showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnLogin.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}