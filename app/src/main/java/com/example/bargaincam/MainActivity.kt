package com.example.bargaincam

import android.R
import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.bargaincam.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : ComponentActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var cameraController: LifecycleCameraController

    private lateinit var promotionWindow: PromotionWindow

    /**
     * This function is called when this activity is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // set up promotion pop-up window
        promotionWindow = PromotionWindow(this)

        // Check if the app already has permissions
        if (!hasPermissions(baseContext)) {
            // if not, request permissions
            activityResultLauncher.launch(REQUIRED_PERMISSIONS.toTypedArray())
        } else {
            // if so, display the camera preview to the screen
            setContent {
                CameraLaunch()
            }


            // ** TESTING: testing the pop-up **
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

    /**
     * This function starts the Camera Preview
     */
//    private fun startCamera() {
//        val previewView: PreviewView = viewBinding.viewFinder
//        cameraController = LifecycleCameraController(baseContext)
//        cameraController.bindToLifecycle(this)
//        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//        previewView.controller = cameraController
//    }

    @Composable
    private fun CameraLaunch() {

        val context: Context = LocalContext.current
        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
        var detectedText: String by remember { mutableStateOf(" ") }
        var NumText: String

        fun onTextUpdated(updatedText: String) {
            NumText = updatedText.filter { it.isDigit() }
            detectedText = NumText
            promotionWindow.closePromotionWindow()
            promotionWindow.updateAisle(detectedText)
            promotionWindow.showPromotionWindow()
        }

        Scaffold(modifier = Modifier.fillMaxSize())
        { paddingValues: PaddingValues ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter)
            {

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    factory = { context ->
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            setBackgroundColor(Color.BLACK)
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_START
                        }.also { previewView ->
                            startTextRecognition(
                                context = context,
                                cameraController = cameraController,
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                onDetectedTextUpdated = ::onTextUpdated
                            )
                        }
                    }
                )
            }
        }
    }

    private fun startTextRecognition(context: Context, cameraController: LifecycleCameraController, lifecycleOwner: LifecycleOwner,
        previewView: PreviewView, onDetectedTextUpdated: (String) -> Unit) {
        cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated))
        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
    }

    class TextRecognitionAnalyzer(private val onDetectedTextUpdated: (String) -> Unit) : ImageAnalysis.Analyzer {

        companion object {
            const val THROTTLE_TIMEOUT_MS = 1_000L
        }

        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            scope.launch {
                val mediaImage: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
                val inputImage: InputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                suspendCoroutine { continuation ->
                    textRecognizer.process(inputImage)
                        .addOnSuccessListener { visionText: Text ->
                            val detectedText: String = visionText.text
                            if (detectedText.isNotBlank()) {
                                onDetectedTextUpdated(detectedText)
                            }
                        }
                        .addOnCompleteListener {
                            continuation.resume(Unit)
                        }
                }

                delay(THROTTLE_TIMEOUT_MS)
            }.invokeOnCompletion { exception ->
                exception?.printStackTrace()
                imageProxy.close()
            }
        }
    }



    /**
     * This function asks the user for their permissions
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
                setContent {
                    CameraLaunch()
                }


                // ** TESTING: testing the pop-up **
                promotionWindow.showPromotionWindow()


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