package com.example.task5

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        bottomNav = findViewById(R.id.bottom_nav)

        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280f, resources.displayMetrics).toInt()
        navView.layoutParams = (navView.layoutParams).apply { width = widthPx }

        try { navView.itemIconTintList = null } catch (e: Exception) {}
        try { bottomNav.itemIconTintList = null } catch (e: Exception) {}

        val csl = try {
            ContextCompat.getColorStateList(this, R.color.nav_item_color)
        } catch (e: Exception) {
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        }
        navView.itemTextColor = csl
        bottomNav.itemTextColor = csl
        bottomNav.itemIconTintList = csl

        navView.menu.clear()
        navView.inflateMenu(R.menu.drawer_menu)

        fun showFragment(tag: String, fragmentFactory: () -> androidx.fragment.app.Fragment) {
            val existing = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (existing != null && existing::class.java.simpleName == tag) return
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentFactory(), tag)
                .commit()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    showFragment("FetchFragment") { FetchFragment() }
                    bottomNav.selectedItemId = R.id.nav_home
                }
                R.id.nav_results -> {
                    showFragment("ResultsFragment") { ResultsFragment.newInstance("") }
                    bottomNav.selectedItemId = R.id.nav_results
                }
                R.id.nav_submissions -> {
                    showFragment("SubmissionsFragment") { SubmissionsFragment.newInstance("", 5) }
                    bottomNav.selectedItemId = R.id.nav_submissions
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            menuItem.isChecked = true
            true
        }

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> showFragment("FetchFragment") { FetchFragment() }
                R.id.nav_results -> showFragment("ResultsFragment") { ResultsFragment.newInstance("") }
                R.id.nav_submissions -> showFragment("SubmissionsFragment") { SubmissionsFragment.newInstance("", 5) }
            }
            navView.setCheckedItem(menuItem.itemId)
            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
            navView.setCheckedItem(R.id.nav_home)
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FetchFragment(), "FetchFragment").commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
