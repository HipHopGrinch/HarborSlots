package com.harborreel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harborreel.app.ui.CasinoViewModel
import com.harborreel.app.ui.HarborApp
import com.harborreel.app.ui.HarborTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarborTheme {
                val model: CasinoViewModel = viewModel()
                HarborApp(model)
            }
        }
    }
}
