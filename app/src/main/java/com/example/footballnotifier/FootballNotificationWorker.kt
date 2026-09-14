package com.example.footballnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class FootballNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. جلب المباريات المباشرة (هذا مثال، سنضع رابط API حقيقي لاحقاً)
            val liveMatches = fetchLiveMatches()
            
            // 2. إرسال إشعار لكل مباراة مباشرة
            for (match in liveMatches) {
                if (match.homeScore != null && match.awayScore != null) {
                    sendNotification(
                        "مباراة مباشرة",
                        "${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                    )
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun fetchLiveMatches(): List<FootballMatch> {
        return withContext(Dispatchers.IO) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://sportscore.com/api/") 
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(FootballApiService::class.java)
            service.getLiveMatches().matches
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // إنشاء قناة الإشعارات (مطلوب في أندرويد 8 وما فوق)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "football_channel",
                "إشعارات كرة القدم",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "football_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

// --- واجهة برمجة التطبيقات (API) ---

interface FootballApiService {
    @GET("matches/live")
    suspend fun getLiveMatches(): MatchResponse
}

data class MatchResponse(val matches: List<FootballMatch>)

data class FootballMatch(
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?
)
