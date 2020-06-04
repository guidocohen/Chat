package com.guido.chat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.guido.chat.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogIn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidEmail(email) && isValidPassword(password)) logInByEmail(email, password)
            else toast("Please make sure all the data is correct.")
        }

        textViewForgotPassword.setOnClickListener {
            goToActivity<ForgotPasswordActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        buttonCreateAccount.setOnClickListener {
            goToActivity<SignUpActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        buttonLogInGoogle.setOnClickListener {
            signIn()
        }

        editTextEmail.validate {
            editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid."
        }

        editTextPassword.validate {
            editTextPassword.error = if (isValidPassword(it)) null else "The password should" +
                    " contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 4 " +
                    "characters lenght at least."
        }
    }

    private fun logInByEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) toast("Your user is now logged in.")
                else toast("Must confirm email first.")
            } else toast("Invalid email or password.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Snackbar.make(loginLayout, "Authentication Failed", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    Snackbar.make(loginLayout, "Authentication success.", Snackbar.LENGTH_SHORT)
                        .show()

                    //updateUI(user)
                } else {
                    Snackbar.make(loginLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                    //updateUI(null)
                }
            }
    }
}
