package com.example.bargaincamprivate

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

class PromotionJson {

    companion object {
        @Throws(JSONException::class)
        fun loadDataFromUrl(url: String, storeNum: Int): List<PromotionDataItem> {

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
            val promotionList = mutableListOf<PromotionDataItem>()

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
                val location = jsonObject.getString("location")

                if(location.contains(storeNum.toString())) {
                    val promotionDataModel = PromotionDataItem(id, name, legal, image, startDate, endDate, count, location)
                    promotionList.add(promotionDataModel)
                }
            }

            return promotionList
        }
    }
}

