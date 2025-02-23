package com.example.cakeroll

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class Home : BaseActivity() {

    private lateinit var btnIceCream: ImageButton
    private lateinit var btnDrinks: ImageButton
    private lateinit var btnCakes: ImageButton
    private lateinit var btnSnacks: ImageButton
    private lateinit var toggleButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var contentFrame: FrameLayout
    private lateinit var searchEditText: EditText
    private lateinit var refreshSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnIceCream = findViewById(R.id.btnIceCream)
        btnDrinks = findViewById(R.id.btnDrinks)
        btnCakes = findViewById(R.id.btnCake)
        btnSnacks = findViewById(R.id.btnSnacks)
        toggleButton = findViewById(R.id.btnViewHeadline)
        refreshSearch = findViewById(R.id.imgRefreshSearch)

        contentFrame = findViewById(R.id.content_frame)
        loadContent(R.layout.default_content)

        val headerView = navigationView.getHeaderView(0)
        closeButton = headerView.findViewById(R.id.btnClose)

        toggleButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        searchEditText = findViewById(R.id.editTextSearch)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        refreshSearch.setOnClickListener {
            checkDefaultContent()
            refreshSearch.visibility = View.GONE
        }

        setupButtonToggle(btnIceCream)
        setupButtonToggle(btnDrinks)
        setupButtonToggle(btnCakes)
        setupButtonToggle(btnSnacks)

        checkDefaultContent()
    }

    private fun setupButtonToggle(button: ImageButton) {
        button.setOnClickListener {
            button.isSelected = !button.isSelected
            when (button.id) {
                R.id.btnCake -> {
                    btnIceCream.isSelected = false
                    btnDrinks.isSelected = false
                    btnSnacks.isSelected = false

                    loadContent(R.layout.cakes_content)
                    setupContent(R.id.cakeGrid, getCakes())
                }
                R.id.btnDrinks -> {
                    btnCakes.isSelected = false
                    btnSnacks.isSelected = false
                    btnIceCream.isSelected = false

                    loadContent(R.layout.drinks_content)
                    setupContent(R.id.drinksGrid, getDrinks())
                }
                R.id.btnIceCream -> {
                    btnCakes.isSelected = false
                    btnDrinks.isSelected = false
                    btnSnacks.isSelected = false

                    loadContent(R.layout.ice_cream_content)
                    setupContent(R.id.icecreamGrid, getIceCreams())
                }
                R.id.btnSnacks -> {
                    btnCakes.isSelected = false
                    btnDrinks.isSelected = false
                    btnIceCream.isSelected = false

                    loadContent(R.layout.snacks_content)
                    setupContent(R.id.snacksGrid, getSnacks())
                }
            }

            checkDefaultContent()
        }
    }

    private fun searchFoodItems(query: String): List<FoodItem> {
        val allFoodItems = mutableListOf<FoodItem>()
        allFoodItems.addAll(getCakes())
        allFoodItems.addAll(getDrinks())
        allFoodItems.addAll(getIceCreams())
        allFoodItems.addAll(getSnacks())

        return allFoodItems.filter { foodItem ->
            foodItem.name.contains(query, ignoreCase = true)
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        val searchResults = searchFoodItems(query)
        loadContent(R.layout.search_content)
        setupContent(R.id.searchGrid, searchResults)

        searchEditText.text.clear()
        refreshSearch.visibility = View.VISIBLE
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

    data class FoodItem(
        val name: String,
        val rating: Float,
        val price: Double,
        val description: String,
        val customIngredient1: String,
        val customIngredient1Price: Double,
        val customIngredient2: String,
        val customIngredient2Price: Double,
        val customIngredient3: String,
        val customIngredient3Price: Double,
        val imageResId: Int,
        val uuid: String = UUID.randomUUID().toString()
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readFloat(),
            parcel.readDouble(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readString() ?: UUID.randomUUID().toString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeFloat(rating)
            parcel.writeDouble(price)
            parcel.writeString(description)
            parcel.writeString(customIngredient1)
            parcel.writeDouble(customIngredient1Price)
            parcel.writeString(customIngredient2)
            parcel.writeDouble(customIngredient2Price)
            parcel.writeString(customIngredient3)
            parcel.writeDouble(customIngredient3Price)
            parcel.writeInt(imageResId)
            parcel.writeString(uuid)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<FoodItem> {
            override fun createFromParcel(parcel: Parcel): FoodItem = FoodItem(parcel)
            override fun newArray(size: Int): Array<FoodItem?> = arrayOfNulls(size)
        }
    }

    private fun getCakes(): List<FoodItem> {
        return listOf(
            FoodItem(
                name = "Strawberry Cake",
                rating = 4.7f,
                price = 22.00,
                description = "Experience the delightful freshness of our strawberry cake, made with layers of soft, fluffy sponge cake and real strawberry puree. Topped with juicy, fresh strawberries and a light, creamy frosting, it's a feast for both the eyes and the palate. Savor the taste of summer with every slice of our exquisite strawberry cake.",
                customIngredient1 = "Fresh Strawberries",
                customIngredient1Price = 2.50,
                customIngredient2 = "Whipped Cream",
                customIngredient2Price = 1.75,
                customIngredient3 = "Vanilla Bean",
                customIngredient3Price = 1.25,
                imageResId = R.drawable.test_cake_one
            ),
            FoodItem(
                name = "Cocoa Dream Delight Cake",
                rating = 4.7f,
                price = 26.00,
                description = "Indulge in the rich flavors of our Cocoa Dream Delight Cake, where crunchy nuts are perfectly blended into a cocoa-infused sponge. Each slice offers a decadent experience with its deep chocolatey notes, complemented by a creamy frosting and a sprinkle of toasted nuts.",
                customIngredient1 = "Mixed Nuts",
                customIngredient1Price = 3.50,
                customIngredient2 = "Chocolate Ganache",
                customIngredient2Price = 2.00,
                customIngredient3 = "Caramelized Pecans",
                customIngredient3Price = 1.75,
                imageResId = R.drawable.test_cake_two
            ),
            FoodItem(
                name = "Raspberry Charlotte Cake",
                rating = 4.8f,
                price = 28.00,
                description = "Indulge in our Raspberry Charlotte Cake, a luxurious dessert with layers of delicate ladyfingers and fresh raspberries. Each bite offers a delightful contrast of textures and flavors, enhanced by a luscious mascarpone or custard filling. Topped with a dusting of powdered sugar and fresh raspberries, it's a perfect blend of sweetness and tanginess.",
                customIngredient1 = "Fresh Raspberries",
                customIngredient1Price = 4.00,
                customIngredient2 = "Mascarpone Cream",
                customIngredient2Price = 3.50,
                customIngredient3 = "Ladyfinger Biscuits",
                customIngredient3Price = 2.50,
                imageResId = R.drawable.test_cake_three
            ),
            FoodItem(
                name = "Decadent Chocolate Bliss Cake",
                rating = 4.9f,
                price = 30.00,
                description = "Treat yourself to our Decadent Chocolate Bliss Cake, a rich and indulgent dessert that combines layers of moist chocolate sponge with velvety chocolate ganache. Each slice is a symphony of deep cocoa flavors, complemented by a smooth chocolate frosting and topped with chocolate shavings. Perfect for chocolate lovers and special occasions alike.",
                customIngredient1 = "Chocolate Ganache",
                customIngredient1Price = 3.50,
                customIngredient2 = "Chocolate Frosting",
                customIngredient2Price = 2.50,
                customIngredient3 = "Chocolate Shavings",
                customIngredient3Price = 1.50,
                imageResId = R.drawable.test_cake_four
            ),
            FoodItem(
                name = "Black Forest Cherry Cake",
                rating = 4.8f,
                price = 32.00,
                description = "Delight in our Black Forest Cherry Cake, a classic dessert with layers of rich chocolate sponge, whipped cream, and juicy cherries. Each bite offers a perfect balance of chocolatey richness and fruity sweetness, topped with chocolate shavings and more cherries for a decadent finish.",
                customIngredient1 = "Juicy Cherries",
                customIngredient1Price = 4.00,
                customIngredient2 = "Whipped Cream",
                customIngredient2Price = 3.00,
                customIngredient3 = "Chocolate Shavings",
                customIngredient3Price = 2.00,
                imageResId = R.drawable.test_cake_five
            )
        )
    }

    private fun getDrinks(): List<FoodItem> {
        return listOf(
            FoodItem(
                name = "Espresso Euphoria",
                rating = 4.8f,
                price = 4.5,
                description = "Indulge in Espresso Euphoria, a rich and intense coffee experience crafted from freshly ground Arabica beans, expertly brewed to perfection. This bold espresso shot delivers a deep, complex flavor profile with notes of dark chocolate and roasted almonds, topped with a velvety crema for a smooth finish.",
                customIngredient1 = "Arabica Beans",
                customIngredient1Price = 0.0, // No additional cost for beans
                customIngredient2 = "Dark Chocolate",
                customIngredient2Price = 0.5,
                customIngredient3 = "Roasted Almonds",
                customIngredient3Price = 0.4,
                imageResId = R.drawable.test_drinks_one
            ),
            FoodItem(
                name = "Choco Bliss Shake",
                rating = 4.7f,
                price = 6.0,
                description = "Treat yourself to our Choco Bliss Shake, a decadent blend of rich chocolate ice cream, topped with fluffy whipped cream and drizzled with chocolate syrup. This indulgent shake offers a creamy texture with a perfect balance of sweetness and cocoa richness, making it a delightful treat for chocolate lovers.",
                customIngredient1 = "Chocolate Ice Cream",
                customIngredient1Price = 2.0,
                customIngredient2 = "Whipped Cream",
                customIngredient2Price = 0.5,
                customIngredient3 = "Chocolate Syrup",
                customIngredient3Price = 0.3,
                imageResId = R.drawable.test_drinks_two
            ),
            FoodItem(
                name = "Mocha Coffee Fusion",
                rating = 4.6f,
                price = 5.5,
                description = "Experience our Mocha Coffee Fusion, a delightful blend of robust coffee and rich mocha flavors, topped with velvety whipped cream. This harmonious fusion offers a perfect balance of espresso intensity and creamy chocolate goodness, making it an irresistible treat for coffee enthusiasts.",
                customIngredient1 = "Espresso",
                customIngredient1Price = 1.0,
                customIngredient2 = "Chocolate Sauce",
                customIngredient2Price = 0.5,
                customIngredient3 = "Whipped Cream",
                customIngredient3Price = 0.3,
                imageResId = R.drawable.test_drinks_three
            ),
            FoodItem(
                name = "Pretty in Pink Lemonade",
                rating = 4.5f,
                price = 3.5,
                description = "Quench your thirst with our Pretty in Pink Lemonade, a refreshing blend of tangy lemon juice and sweet raspberry syrup, garnished with a twist of fresh mint. This vibrant pink drink offers a burst of citrusy flavor with a hint of berry sweetness, perfect for cooling down on a sunny day.",
                customIngredient1 = "Lemon Juice",
                customIngredient1Price = 0.5,
                customIngredient2 = "Raspberry Syrup",
                customIngredient2Price = 0.7,
                customIngredient3 = "Fresh Mint",
                customIngredient3Price = 0.3,
                imageResId = R.drawable.test_drinks_four
            ),
            FoodItem(
                name = "Classic Lemonade",
                rating = 4.7f,
                price = 2.5,
                description = "Savor the timeless taste of our Classic Lemonade, made with freshly squeezed lemon juice, pure cane sugar, and chilled water. This refreshing drink offers a perfect balance of tartness and sweetness, served over ice for a cool and revitalizing experience.",
                customIngredient1 = "Freshly Squeezed Lemon Juice",
                customIngredient1Price = 0.5,
                customIngredient2 = "Cane Sugar",
                customIngredient2Price = 0.3,
                customIngredient3 = "Ice Cubes",
                customIngredient3Price = 0.0,
                imageResId = R.drawable.test_drinks_five
            )
        )
    }

    private fun getIceCreams(): List<FoodItem> {
        return listOf(
            FoodItem(
                name = "Berry Bliss Ice Cream",
                rating = 4.8f,
                price = 12.0,
                description = "Indulge in our Berry Bliss Ice Cream, a delightful fusion of fresh mixed berries blended into creamy vanilla ice cream. Each scoop is bursting with the sweet-tart flavors of strawberries, blueberries, and raspberries, complemented by a drizzle of berry sauce. Perfect for berry enthusiasts looking to cool down on a hot day.",
                customIngredient1 = "Mixed Berries",
                customIngredient1Price = 1.0,
                customIngredient2 = "Berry Sauce",
                customIngredient2Price = 0.5,
                customIngredient3 = "Whipped Cream",
                customIngredient3Price = 0.8,
                imageResId = R.drawable.test_ic_one
            ),
            FoodItem(
                name = "Mint Chocolate Chip Ice Cream",
                rating = 4.6f,
                price = 11.0,
                description = "Enjoy the refreshing taste of our Mint Chocolate Chip Ice Cream, featuring cool mint flavor infused with chocolate chips. Each spoonful offers a refreshing burst of minty freshness complemented by the rich sweetness of chocolate. Perfect for mint lovers looking for a delightful treat.",
                customIngredient1 = "Chocolate Chips",
                customIngredient1Price = 0.5,
                customIngredient2 = "Mint Extract",
                customIngredient2Price = 0.7,
                customIngredient3 = "Whipped Cream",
                customIngredient3Price = 0.8,
                imageResId = R.drawable.test_ic_two
            ),
            FoodItem(
                name = "Crunchy Ice Cream Sandwich",
                rating = 4.5f,
                price = 8.5,
                description = "Indulge in our Crunchy Ice Cream Sandwich, featuring creamy vanilla ice cream sandwiched between two soft chocolate cookies. Loaded with a mix of crunchy almonds and walnuts, adding a satisfying texture and nutty flavor to each bite. Ideal for those who enjoy the contrast of creamy ice cream with crunchy nuts.",
                customIngredient1 = "Almonds",
                customIngredient1Price = 0.5,
                customIngredient2 = "Walnuts",
                customIngredient2Price = 0.6,
                customIngredient3 = "Chocolate Cookies",
                customIngredient3Price = 0.7,
                imageResId = R.drawable.test_ic_three
            ),
            FoodItem(
                name = "Neapolitan Ice Cream",
                rating = 4.7f,
                price = 12.0,
                description = "Enjoy our Neapolitan Ice Cream, a classic trio of creamy vanilla, rich chocolate, and refreshing strawberry flavors. Each scoop offers a delightful blend of flavors in every bite, perfect for those who love a variety of ice cream flavors in one dessert.",
                customIngredient1 = "Chocolate Syrup",
                customIngredient1Price = 0.5,
                customIngredient2 = "Fresh Strawberries",
                customIngredient2Price = 0.7,
                customIngredient3 = "Whipped Cream",
                customIngredient3Price = 0.8,
                imageResId = R.drawable.test_ic_four
            ),
            FoodItem(
                name = "Berry Bliss Ice Cream",
                rating = 4.6f,
                price = 10.0,
                description = "Savor our Berry Bliss Ice Cream, a delightful fusion of fresh mixed berries blended into a creamy base. Each scoop is bursting with the sweet-tart flavors of strawberries, blueberries, and raspberries, providing a refreshing and indulgent treat.",
                customIngredient1 = "Mixed Berries",
                customIngredient1Price = 1.0,
                customIngredient2 = "Berry Sauce",
                customIngredient2Price = 0.7,
                customIngredient3 = "Whipped Cream",
                customIngredient3Price = 0.8,
                imageResId = R.drawable.test_ic_five
            )
        )
    }

    private fun getSnacks(): List<FoodItem> {
        return listOf(
            FoodItem(
                name = "Fish and Pineapple Bites",
                rating = 4.3f,
                price = 5.0,
                description = "Delight in our Fish and Pineapple Bites, bite-sized treats featuring tender fish pieces and juicy pineapple chunks, perfectly skewered for easy snacking. Each bite offers a harmonious blend of savory fish and sweet pineapple, ideal for a quick and satisfying snack.",
                customIngredient1 = "Skewers",
                customIngredient1Price = 0.1,
                customIngredient2 = "Seasoning",
                customIngredient2Price = 0.2,
                customIngredient3 = "Dipping Sauce",
                customIngredient3Price = 0.3,
                imageResId = R.drawable.test_snack_one
            ),
            FoodItem(
                name = "Salted Pretzels",
                rating = 4.5f,
                price = 2.5,
                description = "Enjoy our Salted Pretzels, freshly baked to golden perfection with a delightful crunch and a sprinkle of sea salt. Each bite offers a satisfying balance of soft, chewy dough and savory saltiness, perfect for snacking alone or with your favorite dips.",
                customIngredient1 = "Mustard Dip",
                customIngredient1Price = 0.2,
                customIngredient2 = "Cheese Dip",
                customIngredient2Price = 0.3,
                customIngredient3 = "Honey Mustard Dip",
                customIngredient3Price = 0.4,
                imageResId = R.drawable.test_snack_two
            ),
            FoodItem(
                name = "Nutty Crunch Bread",
                rating = 4.6f,
                price = 4.0,
                description = "Indulge in our Nutty Crunch Bread, featuring a hearty slice of freshly baked bread with a crispy crust and packed with a delightful mix of crunchy almonds, walnuts, and pecans. Each bite offers a satisfying crunch and a blend of nutty flavors that complement the soft bread perfectly.",
                customIngredient1 = "Almonds",
                customIngredient1Price = 0.5,
                customIngredient2 = "Walnuts",
                customIngredient2Price = 0.6,
                customIngredient3 = "Pecans",
                customIngredient3Price = 0.7,
                imageResId = R.drawable.test_snack_three
            ),
            FoodItem(
                name = "Rolled Eggplant Delight",
                rating = 4.3f,
                price = 5.5,
                description = "Discover our Rolled Eggplant Delight, featuring tender eggplant slices delicately rolled and filled with a savory mixture of seasoned ground meat, herbs, and cheese. Each bite offers a rich combination of flavors and textures, enhanced by a golden-brown crust and topped with melted cheese.",
                customIngredient1 = "Ground Meat",
                customIngredient1Price = 1.0,
                customIngredient2 = "Herbs",
                customIngredient2Price = 0.3,
                customIngredient3 = "Cheese",
                customIngredient3Price = 0.5,
                imageResId = R.drawable.test_snack_four
            ),
            FoodItem(
                name = "Savory Asparagus Medallions",
                rating = 4.5f,
                price = 5.5,
                description = "Delight in our Savory Asparagus Medallions, exquisite bite-sized treats featuring succulent medallions of seasoned meat, tender asparagus spears, and a delicate slice of herb-infused bread. Each medallion is carefully crafted to deliver a burst of savory flavors and a satisfying crunch in every bite.",
                customIngredient1 = "Seasoned Meat",
                customIngredient1Price = 1.5,
                customIngredient2 = "Fresh Asparagus",
                customIngredient2Price = 1.0,
                customIngredient3 = "Herb-infused Bread Slice",
                customIngredient3Price = 0.5,
                imageResId = R.drawable.test_snack_five
            )
        )
    }



    private fun loadContent(layoutResId: Int) {
        val inflater = layoutInflater
        contentFrame.removeAllViews()
        inflater.inflate(layoutResId, contentFrame)
    }

    private fun checkDefaultContent() {
        if (!btnCakes.isSelected && !btnDrinks.isSelected && !btnIceCream.isSelected && !btnSnacks.isSelected) {
            loadContent(R.layout.default_content)
            viewFlipper = findViewById(R.id.viewFlipper)

            val viewFlipperImages = arrayOf(
                R.drawable.recommendation_one,
                R.drawable.recommendation_two,
                R.drawable.recommendation_three,
                R.drawable.recommendation_four
            )

            loadRecentLiked()

            val allItems = getCakes() + getDrinks() + getIceCreams() + getSnacks()
            val sortedItems = allItems.sortedByDescending { it.rating }.take(10)

            val popularImages = sortedItems.map { it.imageResId }.toTypedArray()

            addImagesToScrollView(popularImages, R.id.imageContainerPopular)
            addImagesToViewFlipper(viewFlipperImages)
        }
    }

    private fun loadRecentLiked() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userLikesRef = db.collection("users").document(userId).collection("likedItems")

        userLikesRef
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val likedItemCount = querySnapshot.size()
                val likedItemIds = Array(likedItemCount) { 0 }
                var index = 0

                for (document in querySnapshot.documents) {
                    val imageResId = document.getLong("imageResId")?.toInt()
                    if (imageResId != null) {
                        likedItemIds[index] = imageResId
                        index++
                    }
                }

                if (likedItemIds.isNotEmpty()) {
                    addImagesToScrollView(likedItemIds, R.id.imageContainer)
                }
            }
    }

    private fun addImagesToViewFlipper(images: Array<Int>) {
        for (image in images) {
            val imageView = ImageView(this)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(image)

            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            imageView.layoutParams = layoutParams
            viewFlipper.addView(imageView)
        }
    }

    private fun addImagesToScrollView(images: Array<Int>, containerId: Int) {
        val imageContainer = findViewById<LinearLayout>(containerId)
        imageContainer.orientation = LinearLayout.HORIZONTAL

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val imageWidth = (screenWidth * 0.38).toInt()
        val imageHeight = (screenHeight * 0.25).toInt()
        val cardCornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        val cardElevation = resources.getDimension(R.dimen.card_elevation)

        val allFoodItems = listOf(
            getCakes(), getDrinks(), getIceCreams(), getSnacks()
        ).flatten()

        for (imageResId in images) {
            val foodItem = allFoodItems.find { it.imageResId == imageResId }

            foodItem?.let { item ->
                val cardView = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        imageWidth,
                        imageHeight
                    ).apply {
                        marginEnd = resources.getDimensionPixelSize(R.dimen.card_margin)
                    }
                    radius = cardCornerRadius
                    this.cardElevation = cardElevation
                    setCardBackgroundColor(Color.WHITE)
                }

                val cardContentView = layoutInflater.inflate(R.layout.custom_cardview, cardView, false)

                val imageView = cardContentView.findViewById<ImageView>(R.id.food_image).apply {
                    setImageResource(item.imageResId)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val nameView = cardContentView.findViewById<TextView>(R.id.food_name).apply {
                    text = item.name
                }

                val ratingView = cardContentView.findViewById<TextView>(R.id.food_rating).apply {
                    text = "Rating: ${item.rating}"
                }

                val priceView = cardContentView.findViewById<TextView>(R.id.food_price).apply {
                    text = "Price: $${item.price}"
                }

                cardView.addView(cardContentView)
                imageContainer.addView(cardView)

                imageView.setOnClickListener {
                    onImageClick(item)
                }
            }
        }
    }


    override fun getLayoutResourceId(): Int {
        return R.layout.activity_home
    }
}
