package com.example.cloudnotify.ui.activity

import HomeFragment
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.cloudnotify.R
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager
import com.example.cloudnotify.databinding.ActivityMainBinding
import com.example.cloudnotify.ui.fragment.AlarmFragment
import com.example.cloudnotify.ui.fragment.FavouriteFragment
import com.example.cloudnotify.ui.fragment.SettingsFragment
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the toolbar and navigation drawer
        val drawerLayout = binding.drawerLayout
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // Setup the navigation view listener
        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        // Setup ActionBarDrawerToggle for the drawer layout
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_nav, R.string.close_nav // Ensure these strings are defined in strings.xml
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Load the default fragment on startup
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment()) // Load HomeFragment initially
            navigationView.setCheckedItem(R.id.nav_home) // Highlight home in navigation
        }
        localizationManger()
    }

    // Replace the current fragment with the provided fragment
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    // Override the back button behavior to close the drawer if it's open
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed() // Only call super if drawer is not open
        }
    }

    // Handle navigation item selection from the drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.nav_fav -> replaceFragment(FavouriteFragment())
            R.id.nav_alram -> replaceFragment(AlarmFragment())
            R.id.nav_settings -> replaceFragment(SettingsFragment())
            R.id.nav_exit -> Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
        }
        // Close the drawer after an item is selected
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

fun localizationManger(){

    val userLang = SharedPreferencesManager(this).getLanguage()
    val local =resources.configuration.locales[0]

    if(userLang != local.language){
        val newLocal=Locale(userLang)
        val cong=resources.configuration
        cong.setLocale(newLocal)
        cong.setLayoutDirection(newLocal)
        resources.updateConfiguration(cong,resources.displayMetrics)
        recreate()
    }

}


}