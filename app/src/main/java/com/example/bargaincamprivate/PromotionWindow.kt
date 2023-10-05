package com.example.bargaincamprivate

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PromotionWindow(private val activity: Activity) {

    private lateinit var popupWindow: PopupWindow

    fun showPromotionWindow() {

        val popupView = LayoutInflater.from(activity).inflate(R.layout.promotion_window, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val marginInPx = activity.resources.getDimensionPixelSize(R.dimen.popup_margin)

        val popupText = popupView.findViewById<TextView>(R.id.popupText)

        popupText.text = "This is a Popup Window"

        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM or Gravity.END, marginInPx, marginInPx)
    }

    fun isShowing() : Boolean {
        return popupWindow.isShowing
    }

    fun closePromotionWindow() {
        if (this::popupWindow.isInitialized && popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }
}