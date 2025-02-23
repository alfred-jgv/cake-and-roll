package com.example.cakeroll

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Checkout: AppCompatActivity() {

    private lateinit var btnClose : ImageButton
    private lateinit var btnViewCart : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val itemName = intent.getStringExtra("FOOD_NAME")
        val itemQuantity = intent.getStringExtra("FOOD_QUANTITY")
        val itemSubtotal = intent.getStringExtra("SUBTOTAL")
        val imageResId = intent.getIntExtra("FOOD_IMAGE", R.drawable.cakes)

        val imgFood = findViewById<ImageView>(R.id.imgFood)
        val txtFoodName = findViewById<TextView>(R.id.txtFoodName)
        val txtQty = findViewById<TextView>(R.id.txtQty)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)

        imgFood.setImageResource(imageResId)
        txtFoodName.text = itemName
        txtQty.text = "QTY: $itemQuantity"
        txtTotal.text = "Total: $itemSubtotal"

        btnClose = findViewById(R.id.btnClose)
        btnViewCart = findViewById(R.id.btnViewCart)

        btnClose.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnViewCart.setOnClickListener {
            val intent = Intent(this, Cart::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}