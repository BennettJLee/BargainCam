package com.example.bargaincamprivate

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView

class PromotionWindow(private val activity: Activity) {

    private lateinit var popupWindow: PopupWindow

    /**
     * This function displays the promotion pop-up window on the screen
     */
    fun showPromotionWindow() {

        // Initialise the pop-up window
        val popupView = LayoutInflater.from(activity).inflate(R.layout.promotion_window, null)
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Initialise the variables for the pop-up window
        val margin = activity.resources.getDimensionPixelSize(R.dimen.popup_margin)
        val aisleNumText = popupView.findViewById<TextView>(R.id.aisleNumText)
        val promotionText = popupView.findViewById<TextView>(R.id.promotionText)
        val endDateText = popupView.findViewById<TextView>(R.id.endDateText)

        // Set the variables for the pop-up window
        // ** EXTRACT: Extract data here **
        aisleNumText.text = "Aisle Num"
        promotionText.text = "Promotion"
        endDateText.text = "End Date"

        // Show the pop-up window at the bottom right of the screen
        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM or Gravity.END, margin, margin)
    }

    /**
     * This function checks if the window is showing or initialized
     */
    fun isShowing() : Boolean {
        return this::popupWindow.isInitialized && popupWindow.isShowing
    }

    /**
     * This function closes the promotion window
     */
    fun closePromotionWindow() {
        if (isShowing()) {
            popupWindow.dismiss()
        }
    }
}