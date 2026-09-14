package com.example.footballnotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // جدولة العمل في الخلفية
        scheduleNotificationWorker()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LeagueSelectionScreen()
                }
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<FootballNotificationWorker>(
            15, TimeUnit.MINUTES // الحد الأدنى الذي يسمح به أندرويد
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "football_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

// --- واجهة المستخدم ---

@Composable
fun LeagueSelectionScreen() {
    val leagues = listOf(
        League("premier_league", "الدوري الإنجليزي"),
        League("la_liga", "الدوري الإسباني"),
        League("serie_a", "الدوري الإيطالي"),
        League("bundesliga", "الدوري الألماني"),
        League("ligue_1", "الدوري الفرنسي")
    )

    val selectedLeagues = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "اختر الدوريات المفضلة",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn {
            items(leagues) { league ->
                LeagueRow(
                    league = league,
                    isSelected = selectedLeagues[league.id] ?: false,
                    onSelectionChanged = { isSelected ->
                        selectedLeagues[league.id] = isSelected
                    }
                )
            }
        }
    }
}

@Composable
fun LeagueRow(
    league: League,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = league.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isSelected,
            onCheckedChange = onSelectionChanged
        )
    }
}

data class League(val id: String, val name: String)
