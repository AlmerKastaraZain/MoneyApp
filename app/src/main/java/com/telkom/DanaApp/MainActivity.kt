package com.telkom.DanaApp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.telkom.DanaApp.component.MainScreen
import com.telkom.DanaApp.ui.theme.MoneyAppTheme
import com.telkom.DanaApp.ui.theme.White

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ComposeView>(R.id.composeableHome).setContent {
            MoneyAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = White,
                ) {
                    MainScreen(
                        onGoToAddBalance = {
                            val intent = Intent(this, AddBalance::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}