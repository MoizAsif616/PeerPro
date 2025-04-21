package com.example.peerpro

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Optional: Force dark/light mode (remove if you want system default)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val icons = listOf(R.drawable.chats, R.drawable.tutors, R.drawable.notes, R.drawable.me)

        // Create tabs with centered icons using FrameLayout
        icons.forEach { iconRes ->
            val tab = tabLayout.newTab()
            val frameLayout = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val imageView = ImageView(this).apply {
                setImageResource(iconRes)
                background = null
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                ).apply{
                    topMargin=8
                }
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            frameLayout.addView(imageView)
            tab.customView = frameLayout
            tabLayout.addTab(tab)
        }

        // Add tab selection listener with Toast messages
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    val message = when (position) {
                        0 -> "Chats selected"
                        1 -> "Tutors selected"
                        2 -> "Notes selected"
                        3 -> "Profile selected"
                        else -> "Unknown tab selected"
                    }
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
