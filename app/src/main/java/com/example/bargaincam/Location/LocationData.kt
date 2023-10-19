package com.example.bargaincam.Location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bargaincam.HomePageActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.pow
import kotlin.math.sqrt

object StoreFinder {

    private const val url = "https://raw.githubusercontent.com/BennettJLee/BargainCam/main/StoreData.json"
    private var storeList: List<StoreDataItem> = emptyList()

    /**
     * This function loads the store data from a json file
     */
    fun loadJsonData() {

        val locationData = LocationJson

        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    storeList = locationData.loadDataFromUrl(url)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * This function gets the users current location and get the closest store to the user
     *
     * @param context The context on the current activity
     * @return The closet store to the user
     */
    fun getCurrentStore(context: Context): Int {

        //set up storeNum variable (if it stays at -1, no store was found or permissions were not granted)
        var storeNum = -1

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
            Toast.makeText(context, "This feature is only available with precise location permissions", Toast.LENGTH_LONG).show()
            val intent = Intent(context, HomePageActivity::class.java)
            context.startActivity(intent)
        }

        //get the users current location
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null) {
            storeNum = matchStoreLocation(location)
        }

        return storeNum
    }

    /**
     * This function compares the user's location to the store location and returns the closet one.
     *
     * @param userLocation The current location of the user
     * @return The closet store to the user
     */
    private fun matchStoreLocation(userLocation: Location): Int {

        var minDistance = -1.0
        var closetStore = -1

        //get user variables
        val userLat = userLocation.latitude
        val userLng = userLocation.longitude

        //for each store in the store data check for the closest store to the user
        for (store in storeList) {

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


data class StoreDataItem(
    val id : Int,
    val name : String,
    val lat : Double,
    val lng : Double
)