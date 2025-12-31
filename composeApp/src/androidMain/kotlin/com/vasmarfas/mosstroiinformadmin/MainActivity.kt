package com.vasmarfas.mosstroiinformadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vasmarfas.mosstroiinformadmin.core.storage.initDataStore
import com.vasmarfas.mosstroiinformadmin.core.utils.AndroidContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Инициализация Application Context для утилит
        AndroidContextHolder.init(this)
        
        // Инициализация DataStore
        initDataStore(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}