package com.nicholasnixon.dailychore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nicholasnixon.dailychore.ui.DailyChoreApp
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme

/**
 * The single Activity. It just sets up the theme and hands over to DailyChoreApp,
 * which holds the bottom bar and the three screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyChoreTheme {
                DailyChoreApp()
            }
        }
    }
}
