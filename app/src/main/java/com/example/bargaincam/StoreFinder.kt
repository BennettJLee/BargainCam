package com.example.bargaincam

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import kotlin.math.pow
import kotlin.math.sqrt

object StoreFinder {

    private const val url = "https://raw.githubusercontent.com/BennettJLee/BargainCam/main/locationdata.xml"
    private var storeLocations: List<StoreData>

    init {
        val locationData = LocationData
        storeLocations = locationData.loadDataFromUrl(url)
    }

    /**
     * This function gets the users current location and get the closest store to the user
     *
     * @param context The context on the current activity
     * @return The closet store to the user
     */
    fun getCurrentLocation(context: Context): Int {

        //set up storeNum variable (if it stays at -1, no store was found or permissions were not granted)
        val storeNum = -1

        //check for gps permissions and if there isn't get the user to turn them on
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(settingsIntent)
        }

        //check that location permissions have been granted otherwise return
        if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)){
            return storeNum
        }

        //get the users current location
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            matchStoreLocation(location)

        }

        return storeNum
    }

    /**
     * This function compares the user's location to the store location and returns the closet one.
     *
     * @param location The current location of the user
     * @return The closet store to the user
     */
    private fun matchStoreLocation(userLocation: Location): Int {

        var minDistance = -1.0
        var closetStore = -1

        //get user variables
        val userLat = userLocation.latitude
        val userLng = userLocation.longitude

        //for each store in the store data check for the closest store to the user
        for (store in storeLocations) {

            //get store variables and calculate distance from user
            val storeLat = store.lat
            val storeLng = store.lng
            val distance = calculateDistance(userLat, userLng, storeLat, storeLng)

            //if current store distance is shorter than min then insert unless it is the first.
            if (minDistance == -1.0 || distance < minDistance) {
                minDistance = distance
                closetStore = store.id
            }
        }

        return closetStore
    }

    /**
     * This function uses Euclidean geometry to calculate the distances between 2 locations.
     *
     * @param latA The latitude of Location A
     * @param lngB The longitude of Location B
     * @param latA The latitude of Location A
     * @param lngB The longitude of Location B
     * @return The result of the equation
     */
    private fun calculateDistance(latA: Double, lngA: Double, latB: Double, lngB: Double): Double {
        val latDiff = latB - latA
        val lngDiff = lngB - lngA
        return sqrt(latDiff.pow(2) + lngDiff.pow(2))
    }
}


data class StoreData(
    val id : Int,
    val name : String,
    val lat : Double,
    val lng : Double
)