package com.example.bargaincam

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bargaincam.Camera.CameraActivity

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val bargainCamButton = findViewById<View>(R.id.bargainCamButton)

        bargainCamButton.setOnClickListener {
            // Create an intent to launch BargainCamActivity

            if (!hasPermissions(baseContext)) {

                // if not, request permissions
                activityResultLauncher.launch(REQUIRED_PERMISSIONS.toTypedArray())
            } else {

                // is so, proceed to camera activity
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
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
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            // check if the permission has been granted
            if (!permissionGranted) {
                // If not, inform the user that this feature can't run without the permissions
                Toast.makeText(this, "Both Camera and Precise Location permissions are needed to use this feature.", Toast.LENGTH_LONG).show()
            } else {
                // If so, open the Camera Activity
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
        }

    /**
     * This object stores the permissions and has a function that checks permissions status
     */
    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}