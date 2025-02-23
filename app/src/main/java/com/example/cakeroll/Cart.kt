package com.example.cakeroll

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class Cart : BaseActivity(), OnItemRemovedListener {

    private lateinit var toggleButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var subtotalAmount: TextView
    private lateinit var totalAmount: TextView

    private var subtotalComputedAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scrollView = findViewById(R.id.scrollView)
        linearLayout = scrollView.findViewById(R.id.content_frame)
        toggleButton = findViewById(R.id.btnViewHeadline)

        val headerView = navigationView.getHeaderView(0)
        closeButton = headerView.findViewById(R.id.btnClose)

        toggleButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        subtotalAmount = findViewById(R.id.txtSubtotalCount)
        totalAmount = findViewById(R.id.txtTotalCount)

        addContent()
    }

    private fun addContent() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).collection("cartItems")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Log.d(TAG, "No items found")
                    } else {
                        subtotalComputedAmount = 0.0
                        linearLayout.removeAllViews()
                        for (document in result) {
                            val cartItem = CartItem(
                                id = document.getString("itemId") ?:"",
                                foodName = document.getString("foodName") ?: "",
                                quantity = document.getString("quantity") ?: "",
                                subtotal = document.getString("subtotal") ?: "",
                                imageResId = document.getLong("imageResId")?.toInt() ?: R.drawable.cakes,
                                customIngredient1 = document.getString("customIngredient1"),
                                customIngredient1Quantity = document.getLong("customIngredient1Quantity")?.toInt(),
                                customIngredient2 = document.getString("customIngredient2"),
                                customIngredient2Quantity = document.getLong("customIngredient2Quantity")?.toInt(),
                                customIngredient3 = document.getString("customIngredient3"),
                                customIngredient3Quantity = document.getLong("customIngredient3Quantity")?.toInt(),
                                isGlutenFree = document.getBoolean("isGlutenFree") ?: false,
                                isLactoseFree = document.getBoolean("isLactoseFree") ?: false,
                                isVegan = document.getBoolean("isVegan") ?: false
                            )

                            val customIngredients = mutableListOf<CustomIngredient>()
                            if (!cartItem.customIngredient1.isNullOrEmpty()) {
                                customIngredients.add(
                                    CustomIngredient(
                                        name = cartItem.customIngredient1,
                                        quantity = cartItem.customIngredient1Quantity ?: 0
                                    )
                                )
                            }
                            if (!cartItem.customIngredient2.isNullOrEmpty()) {
                                customIngredients.add(
                                    CustomIngredient(
                                        name = cartItem.customIngredient2,
                                        quantity = cartItem.customIngredient2Quantity ?: 0
                                    )
                                )
                            }
                            if (!cartItem.customIngredient3.isNullOrEmpty()) {
                                customIngredients.add(
                                    CustomIngredient(
                                        name = cartItem.customIngredient3,
                                        quantity = cartItem.customIngredient3Quantity ?: 0
                                    )
                                )
                            }

                            cartItem.customIngredients = customIngredients

                            val customCardView = layoutInflater.inflate(R.layout.item_card_view_content, linearLayout, false)

                            val textFoodName = customCardView.findViewById<TextView>(R.id.txtFoodName)
                            textFoodName.text = cartItem.foodName

                            val textQuantity = customCardView.findViewById<TextView>(R.id.txtQty)
                            textQuantity.text = "QTY: ${cartItem.quantity}"
                            textQuantity.setTextColor(resources.getColor(R.color.bright_red))

                            val textSubtotal = customCardView.findViewById<TextView>(R.id.txtTotal)
                            textSubtotal.text = "Total: $${cartItem.subtotal}"
                            textSubtotal.setTextColor(resources.getColor(R.color.bright_red))

                            val imageFood = customCardView.findViewById<ImageView>(R.id.imgFood)
                            imageFood.setImageResource(cartItem.imageResId)

                            val textIngredients = customCardView.findViewById<TextView>(R.id.txtIngredients)
                            val ingredientsText = StringBuilder("")
                            for (ingredient in cartItem.customIngredients) {
                                ingredientsText.append("\n• ${ingredient.name} x ${ingredient.quantity}")
                            }
                            textIngredients.text = ingredientsText.toString()

                            val textGlutenFree = customCardView.findViewById<TextView>(R.id.txtGlutenFree)
                            textGlutenFree.text = "\nGluten-Free: ${if (cartItem.isGlutenFree == true) "Yes" else "No"}"

                            val textLactoseFree = customCardView.findViewById<TextView>(R.id.txtLactoseFree)
                            textLactoseFree.text = "Lactose-Free: ${if (cartItem.isLactoseFree == true) "Yes" else "No"}"

                            val textVegan = customCardView.findViewById<TextView>(R.id.txtVegan)
                            textVegan.text = "Vegan: ${if (cartItem.isVegan == true) "Yes" else "No"}"

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 16, 0, 16)
                            customCardView.layoutParams = params

                            subtotalComputedAmount += getNumericValue(cartItem.subtotal)
                            customCardView.setOnTouchListener(OnSwipeTouchListener(this@Cart, customCardView, cartItem.id, this))
                            linearLayout.addView(customCardView)
                        }

                        updateTotalAmount()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting documents.", e)
                }
        }
    }

    private fun getNumericValue(text: String): Double {
        Log.d("text numeric", text)
        val cleanedText = text.replace("[^\\d.]".toRegex(), "").replace("(\\.)(?!.*\\.)".toRegex(), "$1")
        return cleanedText.toDoubleOrNull() ?: 0.0
    }

    private fun updateTotalAmount() {
        val deliveryFee = findViewById<TextView>(R.id.txtDeliveryFee)

        val deliveryFeeText = deliveryFee.text.toString()
        val deliveryFeeValue = getNumericValue(deliveryFeeText)

        val computeTotal = deliveryFeeValue + subtotalComputedAmount

        subtotalAmount.text = String.format("$%.2f", subtotalComputedAmount)
        totalAmount.text = String.format("$%.2f", computeTotal)
    }

    override fun onItemRemoved(itemId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).collection("cartItems").document(itemId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val subtotalToRemove = document.getString("subtotal") ?: "0.0"
                        val amountToRemove = getNumericValue(subtotalToRemove)
                        subtotalComputedAmount -= amountToRemove
                        updateTotalAmount()

                        db.collection("users").document(userId).collection("cartItems").document(itemId)
                            .delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "Document successfully deleted")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error deleting document", e)
                            }
                    } else {
                        Log.d(TAG, "Document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error fetching document", e)
                }
        }
    }


    data class CartItem(
        val id: String = "",
        val foodName: String = "",
        val quantity: String = "",
        val subtotal: String = "",
        val imageResId: Int = R.drawable.cakes,
        var customIngredients: List<CustomIngredient> = emptyList(),
        val customIngredient1: String? = null,
        val customIngredient1Quantity: Int? = null,
        val customIngredient2: String? = null,
        val customIngredient2Quantity: Int? = null,
        val customIngredient3: String? = null,
        val customIngredient3Quantity: Int? = null,
        val isGlutenFree: Boolean? = null,
        val isLactoseFree: Boolean? = null,
        val isVegan: Boolean? = null
    )

    data class CustomIngredient(
        val name: String = "",
        val quantity: Int = 0
    )

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_cart
    }
}
