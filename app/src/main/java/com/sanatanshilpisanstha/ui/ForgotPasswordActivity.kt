package com.sanatanshilpisanstha.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.utility.Extra
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.sanatanshilpisanstha.databinding.ActivityForgotPasswordBinding
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityForgotPasswordBinding

    var number: String = ""

    // create instance of firebase auth
    lateinit var auth: FirebaseAuth

    // we will use this to match the sent otp from firebase
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        FirebaseApp.initializeApp(this)


        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        auth = FirebaseAuth.getInstance()

        addListener()
        supportActionBar?.hide()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                binding.progressbar.visibility = View.GONE;
               startActivity(Intent(applicationContext, ResetPasswordActivity::class.java))
                finish()
                Log.d("GFG", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("GFG", "onVerificationFailed  $e")
                binding.progressbar.visibility = View.GONE;
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(this@ForgotPasswordActivity, "Invalid request!", Toast.LENGTH_SHORT).show()

                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(this@ForgotPasswordActivity, "SMS quota  has been exceeded!", Toast.LENGTH_SHORT).show()

                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                    Toast.makeText(this@ForgotPasswordActivity, "reCAPTCHA verification attempted Failed!", Toast.LENGTH_SHORT).show()

                }
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
                val intent = Intent(applicationContext, OtpRestActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("resendToken", resendToken)
                intent.putExtra(Extra.NUMBER, binding.etNumber.text.toString())
                intent.putExtra(Extra.IS_LOGIN, "login")
                startActivity(intent)
                finish()
            }
        }

    }

    private fun addListener() {
        binding.ivBack.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.btnVerify -> {
                if (binding.etNumber.text?.toString()?.trim()?.isEmpty() == true) {
                    Toast.makeText(this, "Mobile Number is required!", Toast.LENGTH_SHORT).show()
                } else {
                    binding.progressbar.visibility = View.VISIBLE;
                    sendVerificationCode(binding.ccpPhone.selectedCountryCode()+binding.etNumber.text.toString())
                }
//                val intent = Intent(this, OtpRestActivity::class.java)
//                intent.putExtra(Extra.NUMBER, binding.etNumber.text.toString())
//                this.startActivity(intent)

            }

        }
    }

    private fun sendVerificationCode(number: String) {
        // this method is used for getting
        // OTP on user phone number.
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}