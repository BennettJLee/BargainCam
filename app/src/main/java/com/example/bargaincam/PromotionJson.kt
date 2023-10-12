package com.example.bargaincam

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

class PromotionJson {

    companion object {
        private val promotionList = mutableListOf<PromotionDataItem>()

        @Throws(JSONException::class)
        fun loadDataFromUrl(url: String, storeNum: Int): List<PromotionDataItem>{

            //declare connection variables
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            //get connection response
            val response = client.newCall(request).execute()
            val jsonData = response.body?.string() ?: ""

            //load the json
            val jsonArray = JSONArray(jsonData)

            //for all objects in the json Array,
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getString("promotionId")
                val name = jsonObject.getString("promotionName")
                val legal = jsonObject.getString("promotionLegal")
                val image = jsonObject.getString("promotionImageUrl")
                val startDate = jsonObject.getString("promotionStartDate")
                val endDate = jsonObject.getString("promotionEndDate")
                val count = jsonObject.getInt("promotionProductCount")
                val locationString = jsonObject.getString("location")
                //val locations = parseLocation(locationString)

                val promotionDataModel = PromotionDataItem(id, name, legal, image, startDate, endDate, count, locationString)
                promotionList.add(promotionDataModel)

            }
            //purgeLocations(storeNum.toString())
            return promotionList
        }

        private fun parseLocation(locationString: String): List<Pair<Int, String>> {
            val locationPattern = "\\((\\d+),(\\w+)\\)".toRegex()
            return locationPattern.findAll(locationString).map { matchResult ->
                val storeId = matchResult.groupValues[1].toInt()
                val aisle = matchResult.groupValues[2]
                Pair(storeId, aisle)
            }.toList()
        }

        private fun purgeLocations(storeNumber: String) {
            for (promotion in promotionList) {
                val tempStore = promotion.location
                if (!tempStore.contains(storeNumber)) {
                    promotionList.remove(promotion)
                }
            }
        }

        private fun findPromotions(
            aisleNumber: String, storeNumber: String): List<PromotionDataItem> {
            val matchingPromotionIds = mutableListOf<String>()
            for (promotion in promotionList) {
                val tempAisle = promotion.location
                if (tempAisle.contains(storeNumber + "Isle" + aisleNumber)) {
                    matchingPromotionIds.add(promotion.id)
                }
            }
            return promotionList
        }
    }
}

