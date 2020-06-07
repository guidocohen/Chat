package com.guido.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.guido.chat.R
import com.guido.chat.utils.CircleTransform
import com.guido.chat.utils.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_info.view.*

class InfoFragment : Fragment() {

    private lateinit var _view: View

    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store = FirebaseFirestore.getInstance()
    private lateinit var chatCollectionRef: CollectionReference

    private lateinit var chatSubscription: ListenerRegistration


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _view = inflater.inflate(R.layout.fragment_info, container, false)

        setUpChatDB()
        setUpCurrentUser()
        setUpCurrentUserInfoUI()
        subscribeToTotalMessagesFirebaseStyle()


        return _view
    }

    private fun setUpChatDB() {
        chatCollectionRef = store.collection("chat")
    }

    private fun setUpCurrentUser() {
        currentUser = auth.currentUser!!
    }

    private fun setUpCurrentUserInfoUI() {
        _view.textViewInfoEmail.text = currentUser.email
        _view.textViewInfoName.text = currentUser.displayName ?: getString(R.string.info_no_name)

        currentUser.photoUrl?.let {
            Picasso.get().load(currentUser.photoUrl).resize(300, 300)
                .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        } ?: run {
            Picasso.get().load(R.drawable.ic_person).resize(300, 300)
                .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        }
    }

    private fun subscribeToTotalMessagesFirebaseStyle() {
        chatSubscription = chatCollectionRef.addSnapshotListener(
            object : java.util.EventListener, EventListener<QuerySnapshot> {
                override fun onEvent(
                    snapshot: QuerySnapshot?,
                    exception: FirebaseFirestoreException?
                ) {
                    exception?.let {
                        activity!!.toast("hola")
                        return
                    }

                    snapshot?.let {
                        _view.textViewInfoTotalMessages.text = "${it.size()}"
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        chatSubscription.remove()
        super.onDestroyView()
    }
}
