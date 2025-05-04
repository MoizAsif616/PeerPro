package com.example.peerpro

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.peerpro.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.graphics.drawable.toDrawable
import com.example.peerpro.utils.UserCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

  public val auth = FirebaseAuth.getInstance()
  public val firestore = FirebaseFirestore.getInstance()
  var user = UserCache.getUser()


  private lateinit var binding: ActivityMainBinding

  private lateinit var currentPage: String

  override fun onCreate(savedInstanceState: Bundle?) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportFragmentManager.beginTransaction()
      .replace(R.id.search_bar_frame, SearchBarFragment())
      .commit()

    val tabLayout = binding.tabLayout
    val viewPager = binding.viewPager
    val icons = listOf(R.drawable.sessions, R.drawable.tutors, R.drawable.notes, R.drawable.me)

    // Set up ViewPager with adapter
    viewPager.adapter = ViewPagerAdapter(this as FragmentActivity)
    // Add padding (1.5% of screen width)
    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    val padding = (screenWidth * 0.02).toInt()
    viewPager.setPadding(padding, padding / 3, padding, padding / 3)
    viewPager.clipToPadding = false

    // Connect TabLayout with ViewPager using TabLayoutMediator
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.customView = createTabView(position, false)
    }.attach()

    // Set default selection to Sessions tab (position 0)
    viewPager.setCurrentItem(0, false)
    updateTabAppearance(tabLayout.getTabAt(0), true)
    sessionsSelected()

    // Tab selection listener
    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab) {
        updateTabAppearance(tab, true)
        viewPager.setCurrentItem(tab.position, true)

        when (tab.position) {
          0 -> sessionsSelected()
          1 -> tutorsSelected()
          2 -> notesSelected()
          3 -> profileSelected()
        }
      }

      override fun onTabUnselected(tab: TabLayout.Tab) {
        updateTabAppearance(tab, false)
      }

      override fun onTabReselected(tab: TabLayout.Tab) {}
    })

    binding.searchBtn.setOnClickListener {
      binding.searchBarFrame.visibility = View.VISIBLE
    }

    binding.addBtn.setOnClickListener {
        routeToPosting()
    }

    binding.menuBtn.setOnClickListener {
      showMenuPopup()
    }
  }

  private fun createTabView(position: Int, isSelected: Boolean): View {
    val iconRes = when (position) {
      0 -> R.drawable.sessions
      1 -> R.drawable.tutors
      2 -> R.drawable.notes
      3 -> R.drawable.me
      else -> R.drawable.sessions
    }
    val tabText = when (position) {
      0 -> "Sessions"
      1 -> "Tutors"
      2 -> "Notes"
      3 -> "Me"
      else -> "Tab"
    }

    val frameLayout = FrameLayout(this).apply {
      layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
      )
    }

    val linearLayout = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.CENTER
      layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT,
        Gravity.CENTER
      )
    }

    val imageView = ImageView(this).apply {
      setImageResource(iconRes)
      background = null
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, // Keep wrap_content
        LinearLayout.LayoutParams.WRAP_CONTENT  // Keep wrap_content
      ).apply {
        topMargin = 10.dpToPx()
      }
      scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    val textView = TextView(this).apply {
      text = tabText
      gravity = Gravity.CENTER
      setTextColor(Color.WHITE)
      typeface = ResourcesCompat.getFont(
        this@MainActivity,
        if (isSelected) R.font.poppins_bold else R.font.poppins_regular
      )
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        topMargin = 4.dpToPx()
      }
    }

    linearLayout.addView(imageView)
    linearLayout.addView(textView)
    frameLayout.addView(linearLayout)
    return frameLayout
  }

  private fun updateTabAppearance(tab: TabLayout.Tab?, isSelected: Boolean) {
    tab?.customView?.let { customView ->
      val linearLayout = (customView as FrameLayout).getChildAt(0) as LinearLayout
      val textView = linearLayout.getChildAt(1) as TextView

      // Set the appropriate font family based on selection state
      val fontRes = if (isSelected) R.font.poppins_bold else R.font.poppins_regular
      val typeface = ResourcesCompat.getFont(this, fontRes)

      textView.typeface = typeface

      // Force the view to update
      textView.post {
        textView.invalidate()
        textView.requestLayout()
      }
    }
  }

  fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

  // Fragment Adapter
  class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
      return when (position) {
        0 -> SessionsFragment()
        1 -> TutorsFragment()
        2 -> NotesFragment()
        3 -> ProfileFragment()
        else -> throw IllegalArgumentException("Invalid position")
      }
    }
  }


  @SuppressLint("SetTextI18n")
  private fun sessionsSelected() {
    binding.pageTitle.text = "My Sessions"
    binding.addBtn.visibility = View.GONE
    binding.menuBtn.visibility = View.GONE
    binding.searchBtn.visibility = View.VISIBLE
    currentPage = "sessions"
    closeSearchBar()
  }

  @SuppressLint("SetTextI18n")
  private fun tutorsSelected() {
    binding.pageTitle.text = "Tutors"
    binding.addBtn.visibility = View.VISIBLE
    binding.menuBtn.visibility = View.GONE
    binding.searchBtn.visibility = View.VISIBLE
    currentPage = "tutors"
    closeSearchBar()
  }

  @SuppressLint("SetTextI18n")
  private fun notesSelected() {
    binding.pageTitle.text = "Notes"
    binding.addBtn.visibility = View.VISIBLE
    binding.menuBtn.visibility = View.GONE
    binding.searchBtn.visibility = View.VISIBLE
    currentPage = "notes"
    closeSearchBar()
  }

  @SuppressLint("SetTextI18n")
  private fun profileSelected() {
    binding.pageTitle.text = user?.name?: "Profile"
    binding.addBtn.visibility = View.GONE
    binding.menuBtn.visibility = View.VISIBLE
    binding.searchBtn.visibility = View.GONE
    currentPage = "profile"
    closeSearchBar()
  }

  fun closeSearchBar() {
    val searchBarFragment = supportFragmentManager.findFragmentById(R.id.search_bar_frame) as? SearchBarFragment
    searchBarFragment?.clearInputText()
    binding.searchBarFrame.visibility = View.GONE

  }

  fun performSearch(query: String) {
    Toast.makeText(this, "Search query: $query", Toast.LENGTH_SHORT).show()
  }

  private fun routeToPosting() {
    if (currentPage == "tutors") {
      val intent = Intent(this, PostTutorActivity::class.java)
      startActivity(intent)
    }
    else if (currentPage == "notes") {
      val intent = Intent(this, PostNoteActivity::class.java)
      startActivity(intent)
    }
  }

  private fun showMenuPopup() {
    val popupMenu = PopupMenu(this, binding.menuBtn)
    popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

    popupMenu.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.edit_bio -> {
          callProfileFragmentFunction("editBio")
          true
        }
        R.id.change_profile_pic -> {
          callProfileFragmentFunction("changeProfilePic")
          true
        }
        R.id.logout -> {
          callProfileFragmentFunction("logout")
          true
        }
        R.id.delete_account -> {
          showDeleteConfirmationDialog()
          true
        }
        else -> false
      }
    }
    popupMenu.show()
  }

  @SuppressLint("ResourceType")
  private fun showDeleteConfirmationDialog() {
    val dialog = AlertDialog.Builder(this)
      .setMessage("Are you sure you want to delete your account?")
      .setPositiveButton("Yes") { _, _ ->
        callProfileFragmentFunction("deleteAccount")
      }
      .setNegativeButton("No", null)
      .create()

    dialog.setOnShowListener {
      val backgroundColor = MaterialColors.getColor(
        dialog.context,
        R.attr.bgSecondary,
        Color.BLACK
      )
      dialog.window?.setBackgroundDrawable(backgroundColor.toDrawable())
    }

    dialog.show() // Ensure this is outside the setOnShowListener
  }

  private fun callProfileFragmentFunction(action: String) {
    val currentFragment = supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")

    if (currentFragment is ProfileFragment) {
      when (action) {
        "editBio" -> currentFragment.editBio()
        "changeProfilePic" -> currentFragment.changeProfilePic()
        "logout" -> currentFragment.logout()
        "deleteAccount" -> currentFragment.deleteAccount()
      }
    } else {
      Toast.makeText(this, "ProfileFragment not found", Toast.LENGTH_SHORT).show()
    }
  }
}