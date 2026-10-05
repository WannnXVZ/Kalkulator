package com.example.kalkulator.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalkulator.R
import com.example.kalkulator.logic.CalculatorViewModel
import com.example.kalkulator.ui.components.CalculatorButton
import com.example.kalkulator.ui.theme.KalkulatorTheme

/*
 * Enum: ButtonType
 * Mengelompokkan jenis-jenis tombol untuk mempermudah pewarnaan dinamis di skema Material 3 Theme.
 */
enum class ButtonType { NORMAL, OPERATOR, CLEAR, BACKSPACE, EQUALS }

/*
 * Data Model UI: ButtonSpec
 * Menyimpan konfigurasi satu tombol: teks karakter, bobot lebar (weight), dan tipe tombolnya.
 */
data class ButtonSpec(
    val text: String,
    val weight: Float,
    val type: ButtonType = ButtonType.NORMAL
)

/*
 * Composable Screen Utama: CalculatorScreen
 * Berfungsi menyusun tata letak antarmuka (UI) kalkulator yang responsif terhadap mode Potret maupun Lanskap.
 */
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = remember { CalculatorViewModel() }
) {
    // Membaca konfigurasi orientasi layar (Potret / Lanskap) secara dinamis
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Penyesuaian proporsi ukuran tinggi & jarak tombol berdasarkan orientasi layar
    val buttonHeight = if (isLandscape) 36.dp else 68.dp
    val buttonSpacing = if (isLandscape) 4.dp else 12.dp

    // State lokal untuk mengontrol visibilitas dialog popup riwayat
    var showHistoryDialog by remember { mutableStateOf(false) }

    // Matriks/Grid Tombol Kalkulator (2D List)
    val buttonGrid = listOf(
        listOf(
            ButtonSpec("C", 2f, ButtonType.CLEAR),
            ButtonSpec("⌫", 1f, ButtonType.BACKSPACE),
            ButtonSpec("÷", 1f, ButtonType.OPERATOR)
        ),
        listOf(
            ButtonSpec("7", 1f), ButtonSpec("8", 1f), ButtonSpec("9", 1f),
            ButtonSpec("×", 1f, ButtonType.OPERATOR)
        ),
        listOf(
            ButtonSpec("4", 1f), ButtonSpec("5", 1f), ButtonSpec("6", 1f),
            ButtonSpec("-", 1f, ButtonType.OPERATOR)
        ),
        listOf(
            ButtonSpec("1", 1f), ButtonSpec("2", 1f), ButtonSpec("3", 1f),
            ButtonSpec("+", 1f, ButtonType.OPERATOR)
        ),
        listOf(
            ButtonSpec("0", 2f), ButtonSpec(".", 1f),
            ButtonSpec("=", 1f, ButtonType.EQUALS)
        )
    )

    // Dialog Popup Riwayat Perhitungan
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Riwayat Perhitungan", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (viewModel.historyList.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Hapus All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            text = {
                if (viewModel.historyList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada riwayat perhitungan", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.historyList) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onHistoryItemClick(item)
                                        showHistoryDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(item.expression, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.result, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Tutup") }
            }
        )
    }

    /*
     * Tata Letak Utama Screen:
     * Menampilkan Layar Hasil / Ekspresi secara Full-Width di Bagian ATAS,
     * dan Grid Tombol Angka/Operator secara Full-Width di Bagian BAWAH.
     * Disesuaikan presisi agar di mode lanskap tidak ada bagian yang terpotong.
     */
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isLandscape) 6.dp else 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Layar Display Utama Atas (Layar Ekspresi & Hasil Perhitungan)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = if (isLandscape) 4.dp else 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(if (isLandscape) 14.dp else 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 4.dp else 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Baris Atas Display: Tombol Riwayat & Teks Ekspresi Kecil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.size(if (isLandscape) 28.dp else 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "Riwayat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                        )
                    }
                    Text(
                        text = viewModel.expressionDisplay,
                        fontSize = if (isLandscape) 13.sp else 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Teks Utama (Angka Input / Hasil Kalkulasi)
                Text(
                    text = viewModel.inputDisplay,
                    fontSize = when {
                        viewModel.inputDisplay.length > 18 -> if (isLandscape) 16.sp else 20.sp
                        viewModel.inputDisplay.length > 12 -> if (isLandscape) 22.sp else 28.sp
                        else -> if (isLandscape) 26.sp else 38.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Layout Grid Tombol-Tombol Angka & Operator di Bagian BAWAH (Full Width)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            buttonGrid.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    row.forEach { btn ->
                        // Skema warna dinamis berdasarkan jenis tombol
                        val (bgColor, contentColor) = when (btn.type) {
                            ButtonType.CLEAR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            ButtonType.BACKSPACE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                            ButtonType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                            ButtonType.EQUALS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                            ButtonType.NORMAL -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface
                        }

                        CalculatorButton(
                            text = btn.text,
                            modifier = Modifier.weight(btn.weight),
                            buttonHeight = buttonHeight,
                            backgroundColor = bgColor,
                            contentColor = contentColor,
                            onClick = {
                                when (btn.type) {
                                    ButtonType.CLEAR -> viewModel.onClearClick()
                                    ButtonType.BACKSPACE -> viewModel.onBackspaceClick()
                                    ButtonType.EQUALS -> viewModel.onEqualsClick()
                                    ButtonType.OPERATOR -> viewModel.onOperatorClick(btn.text)
                                    ButtonType.NORMAL -> {
                                        if (btn.text == ".") viewModel.onDecimalClick() else viewModel.onNumberClick(btn.text)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Preview untuk mode Potret & Lanskap di Android Studio Design Tab
@Preview(showBackground = true)
@Composable
fun CalculatorPreviewPortrait() {
    KalkulatorTheme {
        CalculatorScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
fun CalculatorPreviewLandscape() {
    KalkulatorTheme {
        CalculatorScreen()
    }
}
