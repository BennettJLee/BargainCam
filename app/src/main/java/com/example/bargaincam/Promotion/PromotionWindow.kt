package com.example.bargaincam.Promotion

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.bargaincam.R
import com.squareup.picasso.Picasso

class PromotionWindow(private val activity: Activity) {

    private lateinit var popupWindow: PopupWindow

    private lateinit var promotionData: PromotionData

    /**
     * This function displays the promotion pop-up window on the screen
     *
     * @param aisleNum The aisle number that has been scanned
     */
    @SuppressLint("SetTextI18n")
    fun showPromotionWindow(aisleNum: Int) {

        // Initialise the pop-up window
        val popupView = LayoutInflater.from(activity).inflate(R.layout.promotion_window, null)
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Initialise the variables for the pop-up window
        val marginHori = activity.resources.getDimensionPixelSize(R.dimen.popup_margin_hori)
        val marginVert = activity.resources.getDimensionPixelSize(R.dimen.popup_margin_vert)
        val aisleNumText = popupView.findViewById<TextView>(R.id.aisleNumText)
        val promotionText = popupView.findViewById<TextView>(R.id.promotionText)
        val endDateText = popupView.findViewById<TextView>(R.id.endDateText)
        val promotionImage = popupView.findViewById<ImageView>(R.id.promotionImage)

        // Set the variables for the pop-up window
        aisleNumText.text = ""
        promotionText.text = "Promotion Not Found"
        endDateText.text = ""

        //load the promotion data
        promotionData = PromotionData
        val promotionList = promotionData.loadPromotionList()

        //for all items in the promotion list, check if the number that's been scanned exists
        for (item in promotionList) {

            if(item.location.contains("Isle$aisleNum")) {

                aisleNumText.text = "Aisle $aisleNum"
                promotionText.text = item.name
                endDateText.text = "Ends " + item.endDate
                Picasso.get().load(item.image).into(promotionImage)
                break;
            }
        }

        // Show the pop-up window at the bottom right of the screen
        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM or Gravity.END, marginHori, marginVert)
    }

    /**
     * This function checks if the window is showing or initialized
     *
     * @return The boolean value of weather the popup window is showing
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