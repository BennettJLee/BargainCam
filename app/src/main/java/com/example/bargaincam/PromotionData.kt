package com.example.bargaincam

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object PromotionData {

    private const val url = "https://raw.githubusercontent.com/BennettJLee/BargainCam/main/PromotionData.json"
    private lateinit var promotionList: List<PromotionDataItem>

    @OptIn(DelicateCoroutinesApi::class)
    fun loadJsonData(storeNum: Int) {
        val promotionJson = PromotionJson

        GlobalScope.launch(Dispatchers.IO) {
                try {
                    promotionList = promotionJson.loadDataFromUrl(url, storeNum)

                    for (promo in promotionList){
                        Log.e("tag", promo.location)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

    }

    /**
     * This function will load the Promotion List into a list if the promotion list has been initialised
     *
     * @return returns the promotion list
     */
    fun loadPromotionList() : List<PromotionDataItem> {
        if(::promotionList.isInitialized){
            return promotionList
        }
        return emptyList()
    }
}

data class PromotionDataItem(
    val id : String,
    val name : String,
    val legal : String,
    val image : String,
    val startDate : String,
    val endDate : String,
    val count : Int,
    val location : String
)

