package com.example.peerpro

import android.app.Application
import com.cloudinary.android.MediaManager

class PeerProApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary MediaManager
        val cloudinaryConfig = mapOf(
            "cloud_name" to "dgbgraxii",
            "api_key" to "517979758193673",
            "secure" to true
        )


        MediaManager.init(this, cloudinaryConfig)

    }
}
