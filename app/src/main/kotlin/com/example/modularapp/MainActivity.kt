package com.example.modularapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.modularapp.bootstrap.RouterConfiguration
import com.example.modularapp.core.designsystem.theme.AppTheme
import com.example.modularapp.core.router.impl.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val router = RouterConfiguration((application as ModularApplication).koin)

        setContent {
            AppTheme {
                AppNavHost(
                    initialRoute = router.initialRoute,
                    moduleRouters = router.moduleRouters,
                )
            }
        }
    }
}
