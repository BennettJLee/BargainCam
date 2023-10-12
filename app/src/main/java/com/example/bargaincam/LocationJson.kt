package com.example.bargaincam

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

class LocationJson {

    companion object {
        @Throws(JSONException::class)
        fun loadDataFromUrl(url: String): List<StoreDataItem> {

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
            val storeList = mutableListOf<StoreDataItem>()

            //for all objects in the json Array,
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getInt("@store-id")
                val name = jsonObject.getString("name")
                val lat = jsonObject.getDouble("latitude")
                val lng = jsonObject.getDouble("longitude")

                val storeDataItem = StoreDataItem(id, name, lat, lng)
                storeList.add(storeDataItem)

            }

            return storeList
        }
    }

}