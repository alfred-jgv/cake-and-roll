package com.example.cakeroll

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.view.GestureDetectorCompat

interface OnItemRemovedListener {
    fun onItemRemoved(itemId: String)
}

class OnSwipeTouchListener(
    context: Context,
    private val view: View,
    private val itemId: String,
    private val listener: OnItemRemovedListener? = null
) : View.OnTouchListener {
    private val gestureDetector = GestureDetectorCompat(context, GestureListener())

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - (e1?.x ?: 0f)
            if (diffX < 0) {
                animateAndRemoveItem(view, itemId)
            }
            return true
        }
    }

    private fun animateAndRemoveItem(view: View, itemId: String) {
        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0.0f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                val parent = view.parent
                if (parent is ViewGroup) {
                    parent.removeView(view)
                    listener?.onItemRemoved(itemId)
                }
            }
            .start()
    }
}
