package com.example.cakeroll

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class LikedItem : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnLike: ImageButton
    private var imageResId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked_item_content)

        btnBack = findViewById(R.id.btnBack)
        btnLike = findViewById(R.id.btnLike)

        btnBack.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val foodItem: Home.FoodItem? = intent.getParcelableExtra("FOOD_ITEM")

        foodItem?.let {
            inflateContent(R.layout.item_modifications_content, it)
            setupLikeButton(it)
        }
    }

    private fun setupLikeButton(foodItem: Home.FoodItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userLikesRef = db.collection("users").document(userId).collection("likedItems")
        val foodImageResId = foodItem.imageResId

        userLikesRef.whereEqualTo("imageResId", foodImageResId).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    btnLike.isSelected = true
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking liked item", e)
            }

        btnLike.setOnClickListener {
            if (btnLike.isSelected) {
                btnLike.isSelected = false
                userLikesRef.whereEqualTo("imageResId", foodImageResId).get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            userLikesRef.document(document.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Item removed from likes", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error removing document", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error finding document to remove", e)
                    }
            } else {
                btnLike.isSelected = true
                val likedItem = hashMapOf(
                    "name" to foodItem.name,
                    "rating" to foodItem.rating,
                    "price" to foodItem.price,
                    "description" to foodItem.description,
                    "customIngredient1" to foodItem.customIngredient1,
                    "customIngredient2" to foodItem.customIngredient2,
                    "customIngredient3" to foodItem.customIngredient3,
                    "customIngredient1Price" to foodItem.customIngredient1Price,
                    "customIngredient2Price" to foodItem.customIngredient2Price,
                    "customIngredient3Price" to foodItem.customIngredient3Price,
                    "imageResId" to foodItem.imageResId,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                userLikesRef.add(likedItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item added to likes", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }
        }
    }



    private fun inflateContent(layoutResId: Int, foodItem: Home.FoodItem) {
        val frameLayout: FrameLayout = findViewById(R.id.content_frame_liked_content)

        val inflater = LayoutInflater.from(this)
        val inflatedLayout = inflater.inflate(layoutResId, frameLayout, false)

        frameLayout.addView(inflatedLayout)
        setupInflatedContentButtons(inflatedLayout, foodItem)
    }

    private fun setupInflatedContentButtons(inflatedLayout: View, foodItem: Home.FoodItem) {
        imageResId = foodItem.imageResId
        if (imageResId != -1) {
            val imageView = findViewById<ImageView>(R.id.imageView4)
            imageView.setImageResource(imageResId)
        }

        val txtQty = inflatedLayout.findViewById<TextView>(R.id.txtQuantityCount)
        val txtFoodName = inflatedLayout.findViewById<TextView>(R.id.txtItemTitle)
        val txtRating = inflatedLayout.findViewById<TextView>(R.id.txtRating)
        val txtPrice = inflatedLayout.findViewById<TextView>(R.id.txtPrice)
        val txtSubTotal = inflatedLayout.findViewById<TextView>(R.id.txtSubTotal)
        val txtCustomOneQuantity = inflatedLayout.findViewById<TextView>(R.id.txtCustomOne)
        val txtCustomTwoQuantity = inflatedLayout.findViewById<TextView>(R.id.txtCustomTwo)
        val txtCustomThreeQuantity = inflatedLayout.findViewById<TextView>(R.id.txtCustomThree)
        val txtCustomIngredientOne = inflatedLayout.findViewById<TextView>(R.id.txtCustomIngredientOne)
        val txtCustomIngredientTwo = inflatedLayout.findViewById<TextView>(R.id.txtCustomIngredientTwo)
        val txtCustomIngredientThree = inflatedLayout.findViewById<TextView>(R.id.txtCustomIngredientThree)
        val txtCustomOnePrice = inflatedLayout.findViewById<TextView>(R.id.txtIngredientOnePrice)
        val txtCustomTwoPrice = inflatedLayout.findViewById<TextView>(R.id.txtIngredientTwoPrice)
        val txtCustomThreePrice = inflatedLayout.findViewById<TextView>(R.id.txtIngredientThreePrice)
        val txtDescription = inflatedLayout.findViewById<TextView>(R.id.txtDescriptionContent)

        val btnQtyIncrease = inflatedLayout.findViewById<ImageButton>(R.id.btnIncreaseQuantity)
        val btnQtyDecrease = inflatedLayout.findViewById<ImageButton>(R.id.btnDecreaseQuantity)
        val btnIngredientOneIncrease = inflatedLayout.findViewById<ImageButton>(R.id.btnIncreaseCustomOne)
        val btnIngredientTwoIncrease = inflatedLayout.findViewById<ImageButton>(R.id.btnIncreaseCustomTwo)
        val btnIngredientThreeIncrease = inflatedLayout.findViewById<ImageButton>(R.id.btnIncreaseCustomThree)
        val btnIngredientOneDecrease = inflatedLayout.findViewById<ImageButton>(R.id.btnDecreaseCustomOne)
        val btnIngredientTwoDecrease = inflatedLayout.findViewById<ImageButton>(R.id.btnDecreaseCustomTwo)
        val btnIngredientThreeDecrease = inflatedLayout.findViewById<ImageButton>(R.id.btnDecreaseCustomThree)
        val btnAddToCart = inflatedLayout.findViewById<Button>(R.id.btnAddToCart)
        val btnGluten = inflatedLayout.findViewById<Button>(R.id.btnGluten)
        val btnLactose = inflatedLayout.findViewById<Button>(R.id.btnLactose)
        val btnVegan = inflatedLayout.findViewById<Button>(R.id.btnVegan)

        val cardViewGluten = inflatedLayout.findViewById<CardView>(R.id.cardViewGluten)
        val cardViewLactose = inflatedLayout.findViewById<CardView>(R.id.cardViewLactose)
        val cardViewVegan = inflatedLayout.findViewById<CardView>(R.id.cardViewVegan)

        cardViewGluten.tag = false
        cardViewLactose.tag = false
        cardViewVegan.tag = false

        txtFoodName.text = foodItem.name
        txtRating.text = foodItem.rating.toString()
        txtPrice.text = String.format("$%.2f", foodItem.price)
        txtCustomIngredientOne.text = foodItem.customIngredient1
        txtCustomIngredientTwo.text = foodItem.customIngredient2
        txtCustomIngredientThree.text = foodItem.customIngredient3
        txtCustomOnePrice.text = String.format("$%.2f", foodItem.customIngredient1Price)
        txtCustomTwoPrice.text = String.format("$%.2f", foodItem.customIngredient2Price)
        txtCustomThreePrice.text = String.format("$%.2f", foodItem.customIngredient3Price)
        txtDescription.text = foodItem.description

        setupQuantityButtons(btnQtyIncrease, btnQtyDecrease, txtQty, txtSubTotal, inflatedLayout)
        setupQuantityButtons(btnIngredientOneIncrease, btnIngredientOneDecrease, txtCustomOneQuantity, txtSubTotal, inflatedLayout)
        setupQuantityButtons(btnIngredientTwoIncrease, btnIngredientTwoDecrease, txtCustomTwoQuantity, txtSubTotal, inflatedLayout)
        setupQuantityButtons(btnIngredientThreeIncrease, btnIngredientThreeDecrease, txtCustomThreeQuantity, txtSubTotal, inflatedLayout)

        setupButtonColorToggle(btnGluten, cardViewGluten, R.color.bright_red, R.color.white)
        setupButtonColorToggle(btnLactose, cardViewLactose, R.color.bright_red, R.color.white)
        setupButtonColorToggle(btnVegan, cardViewVegan, R.color.bright_red, R.color.white)

        btnAddToCart.setOnClickListener {
            val itemName = txtFoodName.text.toString()
            val itemSubtotal = txtSubTotal.text.toString()
            val itemQuantity = txtQty.text.toString()
            val customOneQuantity = txtCustomOneQuantity.text.toString().toIntOrNull() ?: 0
            val customTwoQuantity = txtCustomTwoQuantity.text.toString().toIntOrNull() ?: 0
            val customThreeQuantity = txtCustomThreeQuantity.text.toString().toIntOrNull() ?: 0

            val glutenSelected = cardViewGluten.tag as? Boolean ?: false
            val lactoseSelected = cardViewLactose.tag as? Boolean ?: false
            val veganSelected = cardViewVegan.tag as? Boolean ?: false

            val customIngredients = mutableListOf<Pair<String, Int>>()
            if (customOneQuantity > 0) customIngredients.add(txtCustomIngredientOne.text.toString() to customOneQuantity)
            if (customTwoQuantity > 0) customIngredients.add(txtCustomIngredientTwo.text.toString() to customTwoQuantity)
            if (customThreeQuantity > 0) customIngredients.add(txtCustomIngredientThree.text.toString() to customThreeQuantity)

            addItemToCart(itemName, itemQuantity, itemSubtotal, customIngredients, glutenSelected, lactoseSelected, veganSelected, foodItem.imageResId)
        }
    }

    private fun setupQuantityButtons(
        increaseButton: ImageButton,
        decreaseButton: ImageButton,
        quantityTextView: TextView,
        subTotalTextView: TextView,
        inflatedLayout: View
    ) {
        fun getNumericValue(text: String): Double {
            return text.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
        }


        var quantity = getNumericValue(quantityTextView.text.toString()).toInt()
        fun updateSubTotal() {
            val itemQuantity = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtQuantityCount).text.toString()).toInt()
            val customOneQuantity = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtCustomOne).text.toString()).toInt()
            val customTwoQuantity = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtCustomTwo).text.toString()).toInt()
            val customThreeQuantity = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtCustomThree).text.toString()).toInt()
            val customItemPrice = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtPrice).text.toString())
            val customOnePrice = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtIngredientOnePrice).text.toString())
            val customTwoPrice = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtIngredientTwoPrice).text.toString())
            val customThreePrice = getNumericValue(inflatedLayout.findViewById<TextView>(R.id.txtIngredientThreePrice).text.toString())

            val subtotal = (customItemPrice + (customOneQuantity * customOnePrice) + (customTwoQuantity * customTwoPrice) + (customThreeQuantity * customThreePrice)) * itemQuantity
            subTotalTextView.text = String.format("%.2f", subtotal)
        }

        increaseButton.setOnClickListener {
            quantity++
            quantityTextView.text = quantity.toString()
            updateSubTotal()
        }

        decreaseButton.setOnClickListener {
            if (quantity > 0) {
                quantity--
                quantityTextView.text = quantity.toString()
                updateSubTotal()
            } else {
                Toast.makeText(increaseButton.context, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show()
            }
        }

        updateSubTotal()
    }

    private fun addItemToCart(
        itemName: String,
        itemQuantity: String,
        itemSubtotal: String,
        customIngredients: List<Pair<String, Int>>,
        isGlutenFree: Boolean,
        isLactoseFree: Boolean,
        isVegan: Boolean,
        imageResId: Int
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val cartItemRef: DocumentReference = db.collection("users").document(userId).collection("cartItems").document()
            val cartItemId = cartItemRef.id

            val cartItem = hashMapOf(
                "foodName" to itemName,
                "quantity" to itemQuantity,
                "subtotal" to itemSubtotal,
                "imageResId" to imageResId,
                "timestamp" to FieldValue.serverTimestamp(),
                "itemId" to cartItemId
            )

            customIngredients.forEachIndexed { index, pair ->
                cartItem["customIngredient${index + 1}"] = pair.first
                cartItem["customIngredient${index + 1}Quantity"] = pair.second
            }

            if (isGlutenFree) cartItem["isGlutenFree"] = true
            if (isLactoseFree) cartItem["isLactoseFree"] = true
            if (isVegan) cartItem["isVegan"] = true

            cartItemRef.set(cartItem)
            displayPopupDialog(customIngredients, itemName, itemQuantity, itemSubtotal, isGlutenFree, isLactoseFree, isVegan, imageResId)
        }
    }

    private fun setupButtonColorToggle(button: Button, cardView: CardView, backgroundColorResId: Int, textColorResId: Int) {
        val defaultBackgroundColor = ContextCompat.getColor(button.context, android.R.color.white)
        val selectedBackgroundColor = ContextCompat.getColor(button.context, backgroundColorResId)

        val defaultTextColor = ContextCompat.getColor(button.context, R.color.hot_pink)
        val selectedTextColor = ContextCompat.getColor(button.context, textColorResId)

        var isSelected = false

        button.setOnClickListener {
            if (isSelected) {
                button.setTextColor(defaultTextColor)
                cardView.setCardBackgroundColor(defaultBackgroundColor)
                cardView.tag = false
            } else {
                button.setTextColor(selectedTextColor)
                cardView.setCardBackgroundColor(selectedBackgroundColor)
                cardView.tag = true
            }
            isSelected = !isSelected
        }
    }

    private fun displayPopupDialog(
        customIngredients: List<Pair<String, Int>>,
        foodName: String,
        mainItemQuantity: String,
        total: String,
        isGlutenFree: Boolean,
        isLactoseFree: Boolean,
        isVegan: Boolean,
        imageResId: Int
    ) {
        val popupDialog = Dialog(this)
        popupDialog.setCancelable(false)
        popupDialog.setContentView(R.layout.activity_checkout)
        popupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        val btnClose = popupDialog.findViewById<ImageButton>(R.id.btnClose)
        val btnViewCart = popupDialog.findViewById<Button>(R.id.btnViewCart)

        val txtFoodName = popupDialog.findViewById<TextView>(R.id.txtFoodName)
        val txtCustomIngredients = popupDialog.findViewById<TextView>(R.id.txtIngredients)
        val txtQuantity = popupDialog.findViewById<TextView>(R.id.txtQty)
        val txtTotal = popupDialog.findViewById<TextView>(R.id.txtTotal)
        val txtGlutenFree = popupDialog.findViewById<TextView>(R.id.txtGlutenFree)
        val txtLactoseFree = popupDialog.findViewById<TextView>(R.id.txtLactoseFree)
        val txtVegan = popupDialog.findViewById<TextView>(R.id.txtVegan)

        val imgFood = popupDialog.findViewById<ImageView>(R.id.imgFood)
        imgFood.setImageResource(imageResId)

        txtFoodName.text = foodName
        txtQuantity.text = "QTY: $mainItemQuantity"
        txtTotal.text = "Total: $${total}"

        val customIngredientsText = StringBuilder()
        customIngredients.forEach { (ingredient, quantity) ->
            customIngredientsText.append("$ingredient x $quantity\n")
        }
        txtCustomIngredients.text = customIngredientsText.toString()

        txtGlutenFree.text = if (isGlutenFree) "Gluten-Free: Yes" else "Gluten-Free: No"
        txtLactoseFree.text = if (isLactoseFree) "Lactose-Free: Yes" else "Lactose-Free: No"
        txtVegan.text = if (isVegan) "Vegan: Yes" else "Vegan: No"

        btnClose.setOnClickListener {
            popupDialog.dismiss()
        }

        btnViewCart.setOnClickListener {
            val intent = Intent(this, Cart::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        popupDialog.show()
    }

}