package com.example.bargaincam

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object PromotionData {

    private const val url = "https://raw.githubusercontent.com/BennettJLee/BargainCam/main/PromotionData.json"
    private lateinit var promotionList: List<PromotionDataItem>

    fun loadJsonData(storeNum: Int) {
        val promotionJson = PromotionJson

        GlobalScope.launch(Dispatchers.IO) {
            try {
                promotionList = promotionJson.loadDataFromUrl(url, storeNum)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * This function will load the Promotion List into a list if the promotionlist has been initialised
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

