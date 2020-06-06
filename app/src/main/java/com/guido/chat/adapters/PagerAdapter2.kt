package com.guido.chat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter2(manager: FragmentActivity) : FragmentStateAdapter(manager) {

    private val fragmentList = ArrayList<Fragment>()

    override fun createFragment(position: Int) = fragmentList[position]

    override fun getItemCount() = fragmentList.size

}