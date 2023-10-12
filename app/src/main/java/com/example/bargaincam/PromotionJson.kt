package com.example.bargaincam

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

class PromotionJson {

    companion object
    {
        private val promotionList = mutableListOf<PromotionDataItem>()
        private var storeNum: Int = -1

        @Throws(JSONException::class)
        fun loadDataFromUrl(url: String, storeNum: Int): List<PromotionDataItem>{

            this.storeNum = storeNum

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

            parsePromotionData(jsonArray)

            return promotionList
        }

        private fun parsePromotionData(jsonArray: JSONArray){
            //for all objects in the json Array
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getString("promotionId")
                val name = jsonObject.getString("promotionName")
                val legal = jsonObject.getString("promotionLegal")
                val image = jsonObject.getString("promotionImageUrl")
                val startDate = jsonObject.getString("promotionStartDate")
                val endDate = jsonObject.getString("promotionEndDate")
                val count = jsonObject.getInt("promotionProductCount")
                val locations = jsonObject.getString("location")
                val location = filterLocation(locations)

                val promotionDataModel = PromotionDataItem(id, name, legal, image, startDate, endDate, count, location)
                promotionList.add(promotionDataModel)

            }
        }

        private fun filterLocation(location:String): String{
            val splitLocation = location.split("\n")
            val filteredLocation = StringBuilder()

            // Filter stores that match the given store number
            for (loc in splitLocation){
                if(loc.contains(storeNum.toString())){
                    filteredLocation.append(loc)
                    break
                }
            }

            return filteredLocation.toString()
        }

    }
}

