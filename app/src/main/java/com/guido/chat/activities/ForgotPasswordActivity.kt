package com.guido.chat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.guido.chat.*
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth.currentUser

        editTextEmail.validate {
            editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid."
        }

        buttonGoLogIn.setOnClickListener {
            goToLogin()
        }

        buttonForgot.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (isValidEmail(email)) mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) {
                    toast("Email has been sent to reset your password.")
                    goToLogin()
                } else toast("Please make sure the email address is correct.")

        }
    }

    private fun goToLogin() {
        goToActivity<LoginActivity> {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
