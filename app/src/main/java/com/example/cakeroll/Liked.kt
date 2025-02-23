package com.example.cakeroll

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.example.cakeroll.Home.FoodItem
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class Liked : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var btnToggle: ImageButton
    private lateinit var btnClose: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnToggle = findViewById(R.id.btnToggleButton)
        val headerView = navigationView.getHeaderView(0)
        btnClose = headerView.findViewById(R.id.btnClose)

        btnToggle.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnClose.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        fetchLikedItems()
    }

    private fun fetchLikedItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val likedItemsRef =  db.collection("users").document(userId).collection("likedItems")

        likedItemsRef
            .get()
            .addOnSuccessListener { result ->
                val foodItems = mutableListOf<FoodItem>()

                for (document in result) {
                    try {
                        val name = document.getString("name") ?: ""
                        val rating = document.getDouble("rating")?.toFloat() ?: 0f
                        val price = document.getDouble("price") ?: 0.0
                        val description = document.getString("description") ?: ""
                        val customIngredient1 = document.getString("customIngredient1") ?: ""
                        val customIngredient1Price = document.getDouble("customIngredient1Price") ?: 0.0
                        val customIngredient2 = document.getString("customIngredient2") ?: ""
                        val customIngredient2Price = document.getDouble("customIngredient2Price") ?: 0.0
                        val customIngredient3 = document.getString("customIngredient3") ?: ""
                        val customIngredient3Price = document.getDouble("customIngredient3Price") ?: 0.0
                        val imageResId = document.getLong("imageResId")?.toInt() ?: 0
                        val uuid = document.getString("uuid") ?: UUID.randomUUID().toString()

                        val foodItem = FoodItem(
                            name,
                            rating,
                            price,
                            description,
                            customIngredient1,
                            customIngredient1Price,
                            customIngredient2,
                            customIngredient2Price,
                            customIngredient3,
                            customIngredient3Price,
                            imageResId,
                            uuid
                        )

                        foodItems.add(foodItem)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to FoodItem: ${e.message}")
                    }
                }

                setupContent(R.id.gridLayout, foodItems)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }



    private fun setupContent(gridId: Int, foodItems: List<FoodItem>) {
        val gridLayout = findViewById<GridLayout>(gridId)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val imageWidth = (screenWidth * 0.423).toInt()
        val imageHeight = (screenHeight * 0.30).toInt()
        val cardCornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        val cardElevation = resources.getDimension(R.dimen.card_elevation)

        for (foodItem in foodItems) {
            val cardView = CardView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = imageWidth
                    height = imageHeight
                    setMargins(22, 22, 22, 22)
                }
                radius = cardCornerRadius
                this.cardElevation = cardElevation
                setCardBackgroundColor(Color.WHITE)
            }

            val cardContentView = layoutInflater.inflate(R.layout.custom_cardview, cardView, false)

            val imageView = cardContentView.findViewById<ImageView>(R.id.food_image).apply {
                setImageResource(foodItem.imageResId)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val nameView = cardContentView.findViewById<TextView>(R.id.food_name).apply {
                text = foodItem.name
            }

            val ratingView = cardContentView.findViewById<TextView>(R.id.food_rating).apply {
                text = "Rating: ${foodItem.rating}"
            }

            val priceView = cardContentView.findViewById<TextView>(R.id.food_price).apply {
                text = "Price: $${foodItem.price}"
            }

            cardView.addView(cardContentView)
            gridLayout.addView(cardView)

            imageView.setOnClickListener {
                onImageClick(foodItem)
            }
        }
    }

    private fun onImageClick(foodItem: FoodItem) {
        val intent = Intent(this, LikedItem::class.java)
        intent.putExtra("FOOD_ITEM", foodItem)
        startActivity(intent)
    }


    override fun getLayoutResourceId(): Int {
        return R.layout.activity_liked
    }
}
