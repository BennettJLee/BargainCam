package com.example.bargaincamprivate

import android.R
import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.bargaincamprivate.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : ComponentActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var cameraController: LifecycleCameraController

    private lateinit var promotionWindow: PromotionWindow

    private lateinit var promotionData: PromotionData

    /**
     * This function is called when this activity is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // set up promotion pop-up window
        promotionWindow = PromotionWindow(this)

        //Initialise the promotion data
        promotionData = PromotionData

        // Check if the app already has permissions
        if (!hasPermissions(baseContext)) {
            // if not, request permissions
            activityResultLauncher.launch(REQUIRED_PERMISSIONS.toTypedArray())
        } else {
            // if so, display the camera preview to the screen
            startCamera()


            // ** TESTING: testing the json **
            promotionData.loadJsonData(405)
            Toast.makeText(this, "Loading Data.", Toast.LENGTH_LONG).show()

            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    var dataList = promotionData.loadPromotionList()
                    if(dataList.isEmpty()) {
                            Toast.makeText(this, "Loading Data pd", Toast.LENGTH_SHORT).show()
                    }else{
                        for (data in dataList) {
                            val toastMessage = "ID: ${data.id}"
                            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }, 1000)
            } catch (e: Exception){
                e.printStackTrace()
            }

            // ** TESTING: testing the pop-up **
            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    promotionWindow.showPromotionWindow(12)
                }, 5000)
            } catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    /**
     * This function starts the Camera Preview
     */
    private fun startCamera() {
        val previewView: PreviewView = viewBinding.viewFinder
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        previewView.controller = cameraController
    }

    /**
     * This function asks the user for there permissions
     */
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            // check if the permission has been granted
            if (!permissionGranted) {
                // If not, inform the user that this feature can't run without the permissions
                Toast.makeText(this, "Both Camera and Location permissions are needed to use this feature.", Toast.LENGTH_LONG).show()
            } else {
                // If so, display the camera preview
                startCamera()


                // ** TESTING: testing the json **
                promotionData.loadJsonData(405)
                Toast.makeText(this, "Loading Data.", Toast.LENGTH_LONG).show()

                try {
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        var dataList = promotionData.loadPromotionList()
                        if(dataList.isEmpty()) {
                            Toast.makeText(this, "Loading Data", Toast.LENGTH_SHORT).show()
                        }else{
                            for (data in dataList) {
                                val toastMessage = "ID: ${data.id}"
                                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, 1000)
                } catch (e: Exception){
                    e.printStackTrace()
                }

                // ** TESTING: testing the pop-up **
                try {
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        promotionWindow.showPromotionWindow(12)
                    }, 5000)
                } catch (e: Exception){
                    e.printStackTrace()
                }

            }
        }

    /**
     * This object stores the permissions and has a function that checks permissions status
     */
    companion object {
        private const val TAG = "BargainCamPrivate"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}