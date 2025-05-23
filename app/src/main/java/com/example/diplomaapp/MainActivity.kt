package com.example.diplomaapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val navView = findViewById<BottomNavigationView>(R.id.bottomNav)

        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.isUserInputEnabled = false

        navView.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.menu_translate -> 0
                R.id.menu_pronunciation -> 1
                else -> 0
            }
            true
        }
    }
}
