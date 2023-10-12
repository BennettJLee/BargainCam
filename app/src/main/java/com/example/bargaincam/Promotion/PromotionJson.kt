package com.example.bargaincam.Promotion

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

class PromotionJson {

    companion object
    {
        private val promotionList = mutableListOf<PromotionDataItem>()
        private var storeNum: Int = -1

        /**
         * This function loads the promotion data from a json file
         *
         * @param url The url for the data
         * @param storeNum The store the user is currently located
         * @return The list of promotion data
         */
        @Throws(JSONException::class)
        fun loadDataFromUrl(url: String, storeNum: Int): List<PromotionDataItem>{

            Companion.storeNum = storeNum

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

        /**
         * This function parses the json data into a list
         *
         * @param jsonArray The JSON data array
         */
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
                val location = filterLocation(locations) //filter the location

                val promotionDataModel = PromotionDataItem(id, name, legal, image, startDate, endDate, count, location)
                promotionList.add(promotionDataModel)
            }
        }

        /**
         * This function filters the location so only data relevant to current location is present
         *
         * @param location The location string to be filtered
         * @return Return the filtered location
         */
        private fun filterLocation(location:String): String{

            //split the locations and initialise the StringBuilder
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

