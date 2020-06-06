package com.guido.chat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.guido.chat.R
import com.guido.chat.utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setListeners()

        setValidates()
    }

    private fun setListeners() {
        textViewForgotPassword.setOnClickListener(this)
        buttonLogIn.setOnClickListener(this)
        buttonCreateAccount.setOnClickListener(this)
        buttonLogInGoogle.setOnClickListener(this)
    }

    private fun setValidates() {
        editTextEmail.validate {
            editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        editTextPassword.validate {
            editTextPassword.error = if (isValidPassword(it)) null else "The password should" +
                    " contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 4 " +
                    "characters lenght at least"
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonLogIn -> logIn()
            R.id.buttonLogInGoogle -> signIn()
            R.id.buttonCreateAccount -> createAccount()
            R.id.textViewForgotPassword -> forgotPassword()
        }
    }

    private fun logIn() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        if (isValidEmail(email) && isValidPassword(password)) logInByEmail(email, password)
        else toast("Please make sure all the data is correct.")
    }

    private fun createAccount() {
        goToActivity<SignUpActivity>()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun forgotPassword() {
        goToActivity<ForgotPasswordActivity>()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun logInByEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) goToMainActivity()
                else toast("Must confirm email first")
            } else toast("Invalid email or password")
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
                snackbar(loginLayout, "Authentication Failed")
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
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val user = auth.currentUser
                snackbar(loginLayout, "Authentication success")

                //auth.signOut()
                goToMainActivity()
                //updateUI(user)
            } else {
                snackbar(loginLayout, "Authentication failed")
                //updateUI(null)
            }
        }
    }

    private fun goToMainActivity() {
        goToActivity<MainActivity> {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
}
