package com.example.kalkulator.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Composable Component: CalculatorButton
 * Komponen tombol kustom yang responsif terhadap mode Potret maupun Lanskap.
 */
@Composable
fun CalculatorButton(
    text: String,                                                          // Teks/karakter tombol (misal: "1", "+", "C")
    modifier: Modifier = Modifier,                                         // Modifier untuk mengatur ukuran/bobot (weight) layout
    buttonHeight: Dp = 68.dp,                                              // Tinggi tombol (dinamis: 68dp di potret, 36dp di lanskap)
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,// Warna latar belakang tombol (mengikuti Material 3 Theme)
    contentColor: Color = MaterialTheme.colorScheme.onSurface,             // Warna teks/konten di dalam tombol
    onClick: () -> Unit                                                    // Callback fungsi/listener saat tombol diklik
) {
    /*
     * Component Material 3 Button:
     * Menyediakan efek visual klik bawaan Android (Ripple effect) dan aksesibilitas.
     */
    Button(
        onClick = onClick,                             // Menghubungkan listener klik ke fungsi callback
        modifier = modifier.height(buttonHeight),       // Tinggi tombol disesuaikan dengan orientasi layar
        shape = RoundedCornerShape(if (buttonHeight < 45.dp) 10.dp else if (buttonHeight < 55.dp) 14.dp else 20.dp), // Sudut tombol membulat
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,          // Latar belakang tombol
            contentColor = contentColor                // Warna isi/teks tombol
        ),
        contentPadding = PaddingValues(0.dp)           // Menghilangkan padding bawaan agar teks presisi di tengah
    ) {
        /*
         * Component Text: Menampilkan karakter teks tombol (Ukuran font menyesuaikan mode layar)
         */
        Text(
            text = text,
            fontSize = if (buttonHeight < 40.dp) 16.sp else if (buttonHeight < 50.dp) 18.sp else 24.sp,
            fontWeight = FontWeight.Bold               // Cetak tebal (Bold) untuk mempertegas karakter
        )
    }
}
