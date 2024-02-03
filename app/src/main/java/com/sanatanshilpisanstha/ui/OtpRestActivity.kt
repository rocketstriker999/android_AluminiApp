package com.sanatanshilpisanstha.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Extra.IS_LOGIN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.sanatanshilpisanstha.databinding.ActivityOtpactivityBinding
import com.sanatanshilpisanstha.utility.GenericKeyEvent
import com.sanatanshilpisanstha.utility.GenericTextWatcher


class OtpRestActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityOtpactivityBinding
    var number = ""
    var cTimer: CountDownTimer? = null
    var otp = ""
    var login = ""
    lateinit var auth: FirebaseAuth
    var storedVerificationId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        addListener()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        if (intent != null && intent.hasExtra(Extra.NUMBER)) {
            number = intent.getStringExtra(Extra.NUMBER).toString()
            val end = number.substring(number.length - 2)
            val start = number.substring(0, 2)

            binding.tvNumber.text = "Code has been sent to " + start + "******" + end
        } else {

            binding.tvNumber.text = "Code has been sent to ******"
        }
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        login = intent.getStringExtra(IS_LOGIN).toString()

    }

    private fun addListener() {
        binding.btnVerify.setOnClickListener(this)
        binding.et1.addTextChangedListener(
            GenericTextWatcher(
                binding.et1, binding.et2
            )
        )
        binding.et2.addTextChangedListener(
            GenericTextWatcher(
                binding.et2, binding.et3
            )
        )
        binding.et3.addTextChangedListener(
            GenericTextWatcher(
                binding.et3, binding.et4
            )
        )
        binding.et4.addTextChangedListener(GenericTextWatcher(binding.et4, binding.et5))
        binding.et5.addTextChangedListener(GenericTextWatcher(binding.et5, binding.et6))
        binding.et6.addTextChangedListener(GenericTextWatcher(binding.et6, null))

        binding.et2.setOnKeyListener(GenericKeyEvent(binding.et2, binding.et1))
        binding.et3.setOnKeyListener(GenericKeyEvent(binding.et3, binding.et2))
        binding.et4.setOnKeyListener(GenericKeyEvent(binding.et4, binding.et3))
        binding.et5.setOnKeyListener(GenericKeyEvent(binding.et5, binding.et4))
        binding.et6.setOnKeyListener(GenericKeyEvent(binding.et6, binding.et5))
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.btnVerify -> {
                if (checkValidation()) {
                    otp =
                        binding.et1.text?.toString() + binding.et2.text?.toString() + binding.et3.text?.toString() + binding.et4.text?.toString() +
                                binding.et5.text?.toString() + binding.et6.text?.toString()
                    if (otp.isNotEmpty()) {
                        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                            storedVerificationId, otp
                        )
                        signInWithPhoneAuthCredential(credential)
                    } else {
                        resetTimer()

                        Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
                    }
//                val intent = Intent(this, BottomNavActivity::class.java)
//                this.startActivity(intent)
                }
            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    when (login) {
                        "login" -> {
                            val intent = Intent(this, ResetPasswordActivity::class.java)
                            intent.putExtra(Extra.NUMBER, number)
                            startActivity(intent)
                            finishAffinity()
                        }
                        "home" -> {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                        else -> {

                        }
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun startTimer() {
        cTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimmer.text =
                    "Resend code in  " + (millisUntilFinished / 1000).toString() + "s"
            }

            override fun onFinish() {
                binding.tvResend.visibility = View.VISIBLE;
                binding.tvTimmer.visibility = View.GONE;
            }
        }
        (cTimer as CountDownTimer).start()
    }

    private fun resetTimer() {
        binding.tvResend.visibility = View.GONE;
        binding.tvTimmer.visibility = View.VISIBLE;
        startTimer();
    }

    private fun checkValidation(): Boolean {
        if (binding.et1.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.et2.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.et3.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.et4.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.et5.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.et6.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }


        return true
    }


}
