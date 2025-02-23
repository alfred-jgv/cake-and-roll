package com.example.cakeroll

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        drawerLayout = findViewById(R.id.side_drawer)
        navigationView = findViewById(R.id.navigationView)

        setupNavigationView()
    }

    private fun setupNavigationView() {
        navigationView.itemTextColor = ContextCompat.getColorStateList(this, R.color.white)
        navigationView.itemIconTintList = ContextCompat.getColorStateList(this, R.color.white)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            navigationFunctions(menuItem)
            true
        }
    }

    private fun navigationFunctions(item: MenuItem) {
        when (item.itemId) {
            R.id.navHome -> {
                if (this !is Home) {
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            R.id.navCart -> {
                if (this !is Cart) {
                    val intent = Intent(this, Cart::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            R.id.navLiked -> {
                if (this !is Liked) {
                    val intent = Intent(this, Liked::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            R.id.navTrackOrder -> {
                if (this !is Track) {
                    val intent = Intent(this, Track::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            R.id.navProfile -> {
                if (this !is Profile) {
                    val intent = Intent(this, Profile::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            R.id.navExit -> {
                val intent = Intent(this, WelcomePage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    abstract fun getLayoutResourceId(): Int
}
