package com.project.pkm_ud_lima.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.project.pkm_ud_lima.R
import com.project.pkm_ud_lima.data.retrofit.ApiConfig
import com.project.pkm_ud_lima.ui.activity.MainActivity
import com.project.pkm_ud_lima.ui.activity.PeringatanActivity
import kotlinx.coroutines.*

class FlameService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var shouldContinue = true

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopServiceIntent = Intent(this, FlameService::class.java)
        stopServiceIntent.action = "Matikan"
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            0, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, "flameServiceChannel")
            .setContentTitle("FireGuard")
            .setContentText("Mendeteksi api...")
            .setSmallIcon(R.mipmap.logo5_background)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.mipmap.logo5_background, "Matikan", stopServicePendingIntent)
            .build()

        startForeground(1, notification)

        serviceScope.launch {
            while (shouldContinue) {
                fetchFlameData()
                delay(5000)  // delay for 5 seconds
            }
        }

        if (intent?.action == "Matikan") {
            shouldContinue = false
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Flame Service Channel"
            val descriptionText = "Channel for flame service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("flameServiceChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private suspend fun fetchFlameData() {
        withContext(Dispatchers.IO) {
            val apiService = ApiConfig.getFlameService()
            try {
                val response = apiService.getFlamePaginated(
                    limit = 10,
                    offset = 0
                ).execute()
                if (response.isSuccessful) {
                    val flameResponse = response.body()
                    if (flameResponse != null) {
                        val analogValue = flameResponse.data.firstOrNull()?.analogValue?.toIntOrNull()
                        if (analogValue != null) {
                            val voltase = (analogValue.toFloat() / 4095f) * 3.3f
                            Log.d("FlameService", "Analog: $analogValue, Voltase: ${String.format("%.2f", voltase)}V")

                            if (voltase > 2.1 && analogValue in 2100..3000 && shouldContinue) {
                                sendNotification(
                                    "Peringatan Kebakaran!",
                                    "Api terdeteksi. Lakukan penyiraman sekarang."
                                )
                            } else {
                                Log.d("FlameService", "Kondisi aman.")
                            }
                        } else {
                            Log.d("FlameService", "Analog value tidak valid.")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationIntent: Intent
        var requestCode = 0

        if (title == "Peringatan Kebakaran!") {
            notificationIntent = Intent(this, PeringatanActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(notificationIntent) // <--- Inisiasi pindah ke Activity
            requestCode = 1
        } else {
            notificationIntent = Intent(this, MainActivity::class.java)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopServiceIntent = Intent(this, FlameService::class.java)
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            0, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, "flameServiceChannel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.logo5_background)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.mipmap.logo5_background, "Matikan", stopServicePendingIntent)
            .build()

        startForeground(1, notification)
    }

}