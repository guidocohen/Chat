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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store = FirebaseFirestore.getInstance()
    private lateinit var ratesCollectionRef: CollectionReference

    private lateinit var ratesSubscription: ListenerRegistration
    private lateinit var rateBusListener: Disposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _view = inflater.inflate(R.layout.fragment_rates, container, false)

        setUpRatesDB()
        setUpCurrentUser()

        setUpRecyclerView()
        setUpFav()

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
    }

    private fun setUpFav() {
        _view.favRating.setOnClickListener { RateDialog().show(fragmentManager!!, "") }
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
    }

    private fun subscribeToNewRatings() {
        rateBusListener = RxBus.listen(NewRateEvent::class.java).subscribe {
            saveRate(it.rate)
        }
    }

}
