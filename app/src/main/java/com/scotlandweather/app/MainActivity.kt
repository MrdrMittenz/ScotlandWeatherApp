package com.scotlandweather.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scotlandweather.app.ui.theme.ScotlandWeatherTheme
import com.scotlandweather.app.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            ScotlandWeatherTheme(darkTheme = state.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScotlandWeatherApp(viewModel = viewModel)
                }
            }
        }
    }
}
