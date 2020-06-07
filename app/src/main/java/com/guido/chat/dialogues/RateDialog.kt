package com.guido.chat.dialogues

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.guido.chat.R
import com.guido.chat.models.NewRateEvent
import com.guido.chat.models.Rate
import com.guido.chat.utils.RxBus
import com.guido.chat.utils.toast
import kotlinx.android.synthetic.main.dialog_rate.view.*
import java.util.*

class RateDialog : DialogFragment() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setUpCurrentUser()
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_rate, null)

        return AlertDialog.Builder(context!!)
            .setTitle(getString(R.string.dialog_title))
            .setView(view)
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                val textRate = view.editTextRateFeedback.text.toString()
                if (textRate.isNotEmpty()) {
                    val imgURL = currentUser.photoUrl?.toString() ?: run { "" }
                    val rate = Rate(
                        currentUser.uid,
                        textRate,
                        view.ratingBarFeedback.rating,
                        Date(),
                        imgURL
                    )
                    RxBus.publish(NewRateEvent(rate))
                }
                activity!!.toast("Rate sended")

            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ ->
                activity!!.toast("Pressed Cancel")
            }
            .create()
    }

}