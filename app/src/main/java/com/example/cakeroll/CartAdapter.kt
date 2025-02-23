package com.example.cakeroll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val cartItems: MutableList<Cart.CartItem>,
    private val onItemDelete: (Cart.CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_view_content, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount() = cartItems.size

    fun removeItem(position: Int) {
        onItemDelete(cartItems[position])
        cartItems.removeAt(position)
        notifyItemRemoved(position)
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textFoodName: TextView = itemView.findViewById(R.id.txtFoodName)
        private val textQuantity: TextView = itemView.findViewById(R.id.txtQty)
        private val textSubtotal: TextView = itemView.findViewById(R.id.txtTotal)
        private val imageFood: ImageView = itemView.findViewById(R.id.imgFood)
        private val textIngredients: TextView = itemView.findViewById(R.id.txtIngredients)
        private val textGlutenFree: TextView = itemView.findViewById(R.id.txtGlutenFree)
        private val textLactoseFree: TextView = itemView.findViewById(R.id.txtLactoseFree)
        private val textVegan: TextView = itemView.findViewById(R.id.txtVegan)

        fun bind(cartItem: Cart.CartItem) {
            textFoodName.text = cartItem.foodName
            textQuantity.text = "QTY: ${cartItem.quantity}"
            textSubtotal.text = "Total: $${cartItem.subtotal}"
            imageFood.setImageResource(cartItem.imageResId)
            val ingredientsText = StringBuilder("")
            for (ingredient in cartItem.customIngredients) {
                ingredientsText.append("\n• ${ingredient.name} x ${ingredient.quantity}")
            }
            textIngredients.text = ingredientsText.toString()
            textGlutenFree.text = "Gluten-Free: ${if (cartItem.isGlutenFree == true) "Yes" else "No"}"
            textLactoseFree.text = "Lactose-Free: ${if (cartItem.isLactoseFree == true) "Yes" else "No"}"
            textVegan.text = "Vegan: ${if (cartItem.isVegan == true) "Yes" else "No"}"
        }
    }
}
