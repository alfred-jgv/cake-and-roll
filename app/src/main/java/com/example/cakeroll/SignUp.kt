package com.example.cakeroll

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.cakeroll.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var btnBack : ImageButton
    private lateinit var btnProceed : Button
    private lateinit var editFirstName : EditText
    private lateinit var editLastName : EditText
    private lateinit var editEmail : EditText
    private lateinit var editPassword : EditText
    private lateinit var editRePassword : EditText

    private lateinit var binding:ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnBack = findViewById(R.id.btnBack)
        btnProceed = findViewById(R.id.btnProceed)

        editFirstName = findViewById(R.id.editFirstName)
        editLastName = findViewById(R.id.editLastName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editRePassword = findViewById(R.id.editRePassword)

        btnBack.setOnClickListener {
            onBackClick()
        }

        binding.btnProceed.setOnClickListener{
            onProceedClick()
        }

        setupTextWatcher(editFirstName)
        setupTextWatcher(editLastName)
        setupTextWatcher(editEmail)
        setupTextWatcher(editPassword)
        setupTextWatcher(editRePassword)

    }


    private fun onBackClick() {
        val intent = Intent(this, WelcomePage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun onProceedClick() {
        firebaseAuth = FirebaseAuth.getInstance()
        val lastName = binding.editLastName.text.toString()
        val firstName = binding.editFirstName.text.toString()
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        val rePassword = binding.editRePassword.text.toString()

        if( lastName.isNotEmpty() && firstName.isNotEmpty() &&
            email.isNotEmpty() && password.isNotEmpty() &&
            rePassword.isNotEmpty()) {

            if(password == rePassword) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val userId = user!!.uid
                        val userDetails = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "fullName" to "$lastName, $firstName",
                            "email" to email,
                            "password" to password,
                            "likedItems" to emptyList<String>(),
                            "cartItems" to emptyList<String>(),
                            "picture" to null
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userId).set(userDetails)

                        Toast.makeText(this, "Created account successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, WelcomePage::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all the required fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val hint = editText.hint?.toString() ?: ""

                val montserratTypeface = ResourcesCompat.getFont(this@SignUp, R.font.montserrat_regular)
                if (s.isNullOrEmpty() || s.toString() == hint) {
                    val color = Color.parseColor("#BEFFFFFF")
                    editText.setTypeface(montserratTypeface, Typeface.ITALIC)
                    editText.setTextColor(color)
                } else {

                    val color = Color.WHITE
                    editText.setTypeface(montserratTypeface, Typeface.NORMAL)
                    editText.setTextColor(color)
                }
            }
        })
    }
}