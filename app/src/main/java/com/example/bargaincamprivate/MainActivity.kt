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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        promotionWindow = PromotionWindow(this)

        if (!hasPermissions(baseContext)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS.toTypedArray())
        } else {
            startCamera()
            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    promotionWindow.showPromotionWindow()
                }, 1000)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun startCamera() {
        val previewView: PreviewView = viewBinding.viewFinder
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        previewView.controller = cameraController
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(this, "Both Camera and Location permissions are needed to use this feature.", Toast.LENGTH_LONG).show()
            } else {
                startCamera()
                promotionWindow.showPromotionWindow()
            }
        }

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