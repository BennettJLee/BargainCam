package com.example.bargaincam

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileInputStream
import java.io.InputStream

class LocationHandler {

    data class Store(
        val id: String,
        var name: String,
        var address1: String,
        var address2: String,
        var city: String,
        var longitude: Double,
        var latitude: Double
    )

    private fun parseXML(xmlFilePath: String): List<Store> {
        val stores = mutableListOf<Store>()
        var currentStore: Store? = null
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        val inputStream: InputStream = FileInputStream(xmlFilePath)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Store" -> {
                            currentStore = Store(
                                id = parser.getAttributeValue(null, "id"),
                                name = "",
                                address1 = "",
                                address2 = "",
                                city = "",
                                longitude = 0.0,
                                latitude = 0.0
                            )
                        }
                        "Name" -> currentStore?.name = parser.nextText()
                        "Address1" -> currentStore?.address1 = parser.nextText()
                        "Address2" -> currentStore?.address2 = parser.nextText()
                        "City" -> currentStore?.city = parser.nextText()
                        "Longitude" -> currentStore?.longitude = parser.nextText().toDouble()
                        "Latitude" -> currentStore?.latitude = parser.nextText().toDouble()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "Store" && currentStore != null) {
                        stores.add(currentStore)
                        currentStore = null
                    }
                }
            }
            eventType = parser.next()
        }
            inputStream.close()
        return stores
    }

    fun main() {
        // Example XML file path
        val xmlFilePath = "path_to_store_data.xml"

        val stores = parseXML(xmlFilePath)

        for (store in stores) {
            println("Store ID: ${store.id}")
            println("Name: ${store.name}")
            println("Address 1: ${store.address1}")
            println("Address 2: ${store.address2}")
            println("City: ${store.city}")
            println("Longitude: ${store.longitude}")
            println("Latitude: ${store.latitude}")
        }
    }
}