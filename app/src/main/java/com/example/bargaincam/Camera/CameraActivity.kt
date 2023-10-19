package com.example.bargaincam.Camera

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.media.Image
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.bargaincam.Location.StoreFinder
import com.example.bargaincam.Promotion.PromotionData
import com.example.bargaincam.Promotion.PromotionWindow
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


class CameraActivity : ComponentActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var promotionWindow: PromotionWindow

    private lateinit var promotionData: PromotionData

    private lateinit var storeFinder: StoreFinder

    /**
     * This function is called when this activity is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // set up promotion pop-up window
        promotionWindow = PromotionWindow(this)

        //Initialise the store and promotion data
        storeFinder = StoreFinder
        promotionData = PromotionData

        // if so, display the camera preview to the screen
        setContent {
            CameraLaunch()
        }

        //Load the store data, find the current store and load the relevant promotion data
        storeFinder.loadJsonData()
        val storeNum = storeFinder.getCurrentStore(this)
        Toast.makeText(this, storeNum.toString(), Toast.LENGTH_LONG).show()
        promotionData.loadJsonData(storeNum)

    }

    /**
     * This function sets up the Camera Preview and Functionality
     */
    @Composable
    private fun CameraLaunch() {

        val context: Context = LocalContext.current
        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
        var detectedText: String by remember { mutableStateOf(" ") }
        var numText: String
        var aisleNum: Int
        var lastAisleNum = -1

        //filters the result of a text update and tries to display the promotion window for the aisle it receives
        fun onTextUpdated(updatedText: String) {
            numText = updatedText.filter { it.isDigit() }
            detectedText = numText
            val textLength : Int = detectedText.length
            if(textLength in 1..2){
                aisleNum = Integer.parseInt(detectedText)
                if(!promotionWindow.isShowing())
                {
                    lastAisleNum = -1
                }
                if(aisleNum > 0 && ( aisleNum != lastAisleNum || !promotionWindow.isShowing())){
                    lastAisleNum = aisleNum
                    promotionWindow.closePromotionWindow()
                    promotionWindow.showPromotionWindow(aisleNum)
                }
            }
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



    /**
     * This function sets up the Text Recognition to the camera feed
     */
    private fun startTextRecognition(context: Context, cameraController: LifecycleCameraController, lifecycleOwner: LifecycleOwner,
        previewView: PreviewView, onDetectedTextUpdated: (String) -> Unit) {
        cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated))
        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
    }

    /**
     * This function analyses the text and currently works to filter the received text
     * down to the line fragment that has the largest bounding height and consists of only numbers
     */
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

                            var boundingBoxLarge  = Rect()
                            var textLarge = " "
                            for (block in visionText.textBlocks) {
                                for (line in block.lines) {
                                    val boundingBox = line.boundingBox
                                    val text = line.text
                                    //series of checks that results it the line with the largest bounding-box height that consists of only numbers being saved
                                    if(!text.contains("$")) {
                                        text.filter { it.isDigit() }
                                        if (boundingBox != null && text.isNotBlank()) {
                                            if (boundingBox.height() > boundingBoxLarge.height()) {
                                                boundingBoxLarge = boundingBox
                                                if (text.isNotBlank()) {
                                                    textLarge = text
                                                    boundingBoxLarge = boundingBox
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            //double checking result is not blank then updating the detected text with the new aisle number being looked at
                            if (textLarge.isNotBlank()) {
                                val detectedText = textLarge
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
}