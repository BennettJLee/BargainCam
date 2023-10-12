package com.example.bargaincam.Promotion

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object PromotionData {

    private const val url = "https://raw.githubusercontent.com/BennettJLee/BargainCam/main/PromotionData.json"
    private lateinit var promotionList: List<PromotionDataItem>

    /**
     * This function loads the promotion data into a list
     *
     * @param storeNum The current store the user is located
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun loadJsonData(storeNum: Int) {

        val promotionJson = PromotionJson

        GlobalScope.launch(Dispatchers.IO) {

            try {
                promotionList = PromotionJson.loadDataFromUrl(url, storeNum)

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

        if(PromotionData::promotionList.isInitialized){
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
    val location : Pair<String,String>
)

