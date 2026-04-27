package xyz.nxprojects.dracin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import xyz.nxprojects.dracin.ui.NxApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val systemUiController = rememberSystemUiController()
            val darkColorScheme = darkColorScheme(
                primary = Color(0xFFF43F5E),
                secondary = Color(0xFFEAB308),
                tertiary = Color(0xFF27272A),
                background = Color(0xFF09090B),
                surface = Color(0xFF18181B),
                onBackground = Color.White,
                onSurface = Color.White
            )

            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = false
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent,
                    darkIcons = false
                )
            }

            MaterialTheme(
                colorScheme = darkColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NxApp()
                }
            }
        }
    }
}
