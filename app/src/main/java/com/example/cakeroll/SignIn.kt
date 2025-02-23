package com.example.cakeroll

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.cakeroll.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var binding:ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var editEmail : EditText
    private lateinit var editPassword : EditText
    private lateinit var btnBack : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editEmail = findViewById(R.id.editUserEmail)
        editPassword = findViewById(R.id.editUserPassword)
        btnBack = findViewById(R.id.btnBack)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnUserLogin.setOnClickListener {
            onLoginClick()
        }

        btnBack.setOnClickListener{
            val intent = Intent(this, WelcomePage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        setupTextWatcher(editEmail)

    }

    private fun onLoginClick() {
        val email = binding.editUserEmail.text.toString()
        val password = binding.editUserPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            Toast.makeText(this, "Please fill in all the required fields.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val hint = editText.hint?.toString() ?: ""

                val montserratTypeface = ResourcesCompat.getFont(this@SignIn, R.font.montserrat_regular)
                if (s.isNullOrEmpty() || s.toString() == hint) {
                    val color = Color.parseColor("#BEFFFFFF")
                    editText.setTypeface(montserratTypeface, Typeface.NORMAL)
                    editText.setTextColor(color)
                } else {
                    val color = Color.BLACK
                    editText.setTypeface(montserratTypeface, Typeface.NORMAL)
                    editText.setTextColor(color)
                }
            }
        })
    }
}