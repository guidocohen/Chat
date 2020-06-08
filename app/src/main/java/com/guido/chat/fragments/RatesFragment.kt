package com.guido.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.guido.chat.R
import com.guido.chat.adapters.RatesAdapter
import com.guido.chat.dialogues.RateDialog
import com.guido.chat.models.NewRateEvent
import com.guido.chat.models.Rate
import com.guido.chat.utils.RxBus
import com.guido.chat.utils.toast
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.view.recyclerView
import kotlinx.android.synthetic.main.fragment_rates.view.*

class RatesFragment : Fragment() {

    private lateinit var _view: View

    private lateinit var ratesAdapter: RatesAdapter
    private val ratesList = ArrayList<Rate>()
    private lateinit var scrollListener: RecyclerView.OnScrollListener

    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store = FirebaseFirestore.getInstance()
    private lateinit var ratesCollectionRef: CollectionReference

    private var ratesSubscription: ListenerRegistration? = null
    private lateinit var rateBusListener: Disposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _view = inflater.inflate(R.layout.fragment_rates, container, false)

        setUpRatesDB()
        setUpCurrentUser()

        setUpRecyclerView()
        setUpFAB()

        subscribeToRatings()
        subscribeToNewRatings()

        return _view
    }

    private fun setUpRatesDB() {
        ratesCollectionRef = store.collection("rates")
    }

    private fun setUpCurrentUser() {
        currentUser = auth.currentUser!!
    }


    private fun setUpRecyclerView() = with(_view.recyclerView) {
        ratesAdapter = RatesAdapter(ratesList)

        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        itemAnimator = DefaultItemAnimator()
        adapter = ratesAdapter

        setUpScrollListener()
    }

    private fun setUpFAB() {
        _view.fabRating.setOnClickListener { RateDialog().show(fragmentManager!!, "") }
    }

    private fun hasUserRated(rates: ArrayList<Rate>): Boolean {
        var result = false
        rates.forEach {
            if (it.userId == currentUser.uid) {
                result = true
            }
        }
        return result
    }

    private fun removeFABIfRated(rated: Boolean) {
        if (rated) {
            _view.fabRating.hide()
            _view.recyclerView.removeOnScrollListener(scrollListener)
        }
    }

    private fun saveRate(rate: Rate) {
        val newRating = HashMap<String, Any>()
        newRating["userId"] = rate.userId
        newRating["text"] = rate.text
        newRating["rate"] = rate.rate
        newRating["createdAt"] = rate.createdAt
        newRating["profileImgURL"] = rate.profileImgURL

        ratesCollectionRef.add(newRating)
            .addOnCompleteListener {
                activity!!.toast("Rating added!")
            }
            .addOnFailureListener {
                activity!!.toast("Rating error, try again!")
            }
    }

    private fun subscribeToRatings() {
        ratesSubscription = ratesCollectionRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener(object : java.util.EventListener, EventListener<QuerySnapshot> {
                override fun onEvent(
                    snapshot: QuerySnapshot?,
                    exception: FirebaseFirestoreException?
                ) {
                    exception?.let {
                        activity!!.toast("Exception!")
                        return
                    }

                    snapshot?.let {
                        ratesList.clear()
                        val rates = it.toObjects(Rate::class.java)
                        ratesList.addAll(rates)
                        removeFABIfRated(hasUserRated(ratesList))
                        ratesAdapter.notifyDataSetChanged()
                        _view.recyclerView.smoothScrollToPosition(0)
                    }
                }
            })
    }

    private fun subscribeToNewRatings() {
        rateBusListener = RxBus.listen(NewRateEvent::class.java).subscribe {
            saveRate(it.rate)
        }
    }

    private fun setUpScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && _view.fabRating.isShown) {
                    _view.fabRating.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    _view.fabRating.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }
        _view.recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onDestroyView() {
        _view.recyclerView.removeOnScrollListener(scrollListener)
        rateBusListener.dispose()
        ratesSubscription?.remove()
        super.onDestroyView()
    }
}
