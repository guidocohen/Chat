package com.guido.chat.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.guido.chat.R
import com.guido.chat.adapters.PagerAdapter
import com.guido.chat.fragments.ChatFragment
import com.guido.chat.fragments.InfoFragment
import com.guido.chat.fragments.RatesFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity() {

    private var prevBottomSelected: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        setUpViewPager(getPagerAdapter())
        setUpBottomNavigationBar()
    }

    private fun getPagerAdapter(): PagerAdapter {
        val adapter = PagerAdapter(supportFragmentManager)

        adapter.addFragment(InfoFragment())
        adapter.addFragment(RatesFragment())
        adapter.addFragment(ChatFragment())

        return adapter
    }

    private fun setUpViewPager(adapter: PagerAdapter) {
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                onPageSelected(position)
            }

            override fun onPageSelected(position: Int) {
                if (prevBottomSelected == null)
                    bottomNavigation.menu.getItem(0).isChecked = false
                else prevBottomSelected!!.isChecked = true

                bottomNavigation.menu.getItem(position).isChecked = true
                prevBottomSelected = bottomNavigation.menu.getItem(position)
            }

        })

    }

    private fun setUpBottomNavigationBar() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_info -> {
                    viewPager.currentItem = 0; true
                }
                R.id.bottom_nav_rates -> {
                    viewPager.currentItem = 1; true
                }
                R.id.bottom_nav_chat -> {
                    viewPager.currentItem = 2; true
                }
                else -> false
            }
        }
    }
}
