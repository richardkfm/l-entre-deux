package org.entredeux.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.entredeux.app.ui.PlaceholderScreen
import org.entredeux.app.ui.theme.EntreDeuxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EntreDeuxTheme {
                PlaceholderScreen()
            }
        }
    }
}
