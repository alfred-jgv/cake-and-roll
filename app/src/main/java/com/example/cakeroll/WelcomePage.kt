package com.example.cakeroll

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class WelcomePage : AppCompatActivity() {
    private lateinit var btnGuest : Button
    private lateinit var btnLogin : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGuest = findViewById(R.id.btnGuest)
        btnLogin = findViewById(R.id.btnLogin)

        btnGuest.setOnClickListener {
            onGuestClick();
        }

        btnLogin.setOnClickListener {
            onLoginClick()
        }

    }

    private fun onLoginClick() {
        val intent = Intent(this, SignIn::class.java);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent);
    }

    private fun onGuestClick() {
        val intent = Intent(this, Home::class.java);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent);
    }

    fun onButtonClick(view: View) {
        val intent = Intent(this, SignUp::class.java);
        startActivity(intent);
    }
}