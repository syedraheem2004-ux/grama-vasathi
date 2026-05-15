package com.grama.vasathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.grama.vasathi.ui.NavGraph
import com.grama.vasathi.ui.Screen
import com.grama.vasathi.ui.theme.GramaVasathiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GramaVasathiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    } catch (e: Exception) {
                        null
                    }
                    val startDestination = if (currentUser != null) "role_selection" else "onboarding"
                    
                    NavGraph(navController = navController, startDestination = startDestination)
                }
            }
        }
    }
}
