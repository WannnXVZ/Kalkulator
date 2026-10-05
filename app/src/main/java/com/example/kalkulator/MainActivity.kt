package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.kalkulator.ui.screens.CalculatorScreen
import com.example.kalkulator.ui.theme.KalkulatorTheme

// Activity utama yang menjadi titik masuk (entry point) aplikasi Android
class MainActivity : ComponentActivity() {

    // Metode onCreate dipanggil saat Activity pertama kali dibuat oleh sistem Android
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengaktifkan tampilan Edge-to-Edge agar aplikasi mengisi penuh layar (di bawah status bar & navigation bar)
        enableEdgeToEdge()

        // setContent digunakan untuk menampilkan antarmuka berbasis Jetpack Compose
        setContent {
            // Membungkus aplikasi dengan tema Material Design 3 bawaan proyek
            KalkulatorTheme {
                // Scaffold menyediakan struktur tata letak dasar Material Design
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Memanggil layar kalkulator utama dan menyesuaikan padding dari Scaffold
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
