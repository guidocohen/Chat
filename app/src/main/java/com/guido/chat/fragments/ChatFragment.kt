package com.guido.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.guido.chat.R
import com.guido.chat.adapters.ChatAdapter
import com.guido.chat.models.Message
import com.guido.chat.models.TotalMessagesEvent
import com.guido.chat.utils.RxBus
import com.guido.chat.utils.toast
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class ChatFragment : Fragment() {

    private lateinit var _view: View
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = ArrayList<Message>()

    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store = FirebaseFirestore.getInstance()
    private lateinit var chatCollectionRef: CollectionReference

    private lateinit var chatSubscription: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _view = inflater.inflate(R.layout.fragment_chat, container, false)

        setUpChatDB()
        setUpCurrentUser()
        setUpRecyclerView()
        setUpChatBtn()
        suscribeToChatMesseges()

        return _view
    }

    private fun setUpChatDB() {
        chatCollectionRef = store.collection("chat")
    }

    private fun setUpCurrentUser() {
        currentUser = auth.currentUser!!
    }

    private fun setUpRecyclerView() = with(_view.recyclerView) {
        chatAdapter = ChatAdapter(messageList, currentUser.uid)
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        itemAnimator = DefaultItemAnimator()
        adapter = chatAdapter
    }

    private fun setUpChatBtn() {
        _view.buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                val photo = currentUser.photoUrl?.toString() ?: ""
                val message =
                    Message(currentUser.uid, messageText, photo, Date())
                saveMessage(message)
                _view.editTextMessage.setText("")
            }
        }
    }

    private fun saveMessage(message: Message) {
        val newMessage = HashMap<String, Any?>()
        newMessage["authorId"] = message.authorId
        newMessage["message"] = message.message
        newMessage["profileImageUrl"] = message.profileImageUrl
        newMessage["sentAt"] = message.sentAt!!

        chatCollectionRef.add(newMessage)
            .addOnCompleteListener {
                activity!!.toast("Message added!")
            }
            .addOnFailureListener {
                activity!!.toast("Message error, try again!")
            }
    }

    private fun suscribeToChatMesseges() {
        chatSubscription = chatCollectionRef
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener(
                object : java.util.EventListener,
                    EventListener<QuerySnapshot> {
                    override fun onEvent(
                        snapshot: QuerySnapshot?,
                        exception: FirebaseFirestoreException?
                    ) {
                        exception?.let {
                            activity!!.toast("hola")
                            return
                        }

                        snapshot?.let {
                            messageList.clear()
                            val messages = it.toObjects(Message::class.java)
                            messageList.addAll(messages.asReversed())
                            chatAdapter.notifyDataSetChanged()
                            _view.recyclerView.smoothScrollToPosition(messageList.size)
                            RxBus.publish(TotalMessagesEvent(messages.size))
                        }
                    }
                })
    }

    override fun onDestroyView() {
        chatSubscription.remove()
        super.onDestroyView()
    }
}
