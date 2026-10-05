package com.example.kalkulator.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/*
 * Data Class: HistoryItem
 * Alasan penggunaan Data Class di Kotlin:
 * Data class secara otomatis menyediakan method getter, toString(), equals(), hashCode(), dan copy().
 * Sangat cocok untuk menyimpan data sederhana (value object) seperti satu entitas riwayat perhitungan.
 */
data class HistoryItem(
    val expression: String, // Teks ekspresi matematika lengkap (contoh: "12 + 5 =")
    val result: String      // Teks hasil akhir kalkulasi (contoh: "17")
)

/*
 * Class: CalculatorViewModel
 * Alasan penggunaan Android ViewModel:
 * ViewModel berfungsi memisahkan Logika Bisnis (Business Logic) dan State Management dari tampilan UI.
 * Keuntungan utama ViewModel adalah datanya tahan terhadap 'Configuration Changes' (seperti saat HP diputar/rotasi layar).
 */
class CalculatorViewModel : ViewModel() {

    /*
     * Variabel State: inputDisplay
     * Mengapa menggunakan `mutableStateOf("0")` dan `by` (Delegate Property)?
     * Dalam Jetpack Compose, saat nilai `inputDisplay` berubah, Compose akan secara otomatis
     * memicu 'Recomposition' (menggambar ulang bagian UI yang menggunakan variabel ini).
     * `private set` digunakan agar variabel ini hanya bisa diubah dari dalam ViewModel (Prinsip Enkapsulasi & Unidirectional Data Flow).
     */
    var inputDisplay by mutableStateOf("0")
        private set

    /*
     * Variabel State: expressionDisplay
     * Menyimpan dan menampilkan teks riwayat ekspresi kecil di bagian atas layar (contoh: "10 + 5 =").
     */
    var expressionDisplay by mutableStateOf("")
        private set

    /*
     * Variabel State List: historyList
     * Mengapa menggunakan `mutableStateListOf<HistoryItem>()` alih-alih `ArrayList` biasa?
     * `mutableStateListOf` adalah List khusus Compose yang 'observable'.
     * Saat item ditambah/dihapus dari list ini, UI (seperti LazyColumn riwayat) langsung otomatis diperbarui.
     */
    var historyList = mutableStateListOf<HistoryItem>()
        private set

    /*
     * Variabel Internal: tokens
     * Mengapa menggunakan `MutableList<String>`?
     * List ini bertindak sebagai buffer/penampung potongan angka dan operator (contoh: ["12", "+", "5"]).
     * Dengan pendekatan token, kita dapat dengan mudah menyusun ekspresi panjang dan mengevaluasinya sesuai urutan matematika.
     */
    private val tokens = mutableListOf<String>()

    /*
     * Variabel Internal: isEvaluated
     * Penanda (flag) boolean untuk mengetahui apakah tombol sama dengan (=) baru saja ditekan.
     * Digunakan untuk membedakan apakah ketikan berikutnya adalah awal perhitungan baru atau kelanjutan dari hasil sebelumnya.
     */
    private var isEvaluated = false

    /*
     * Method Privat: resetIfEvaluated()
     * Alasan pembuatan fungsi helper ini:
     * Menghindari duplikasi kode (Prinsip DRY - Don't Repeat Yourself).
     * Fungsi ini meriset buffer token jika pengguna mulai menekan angka/titik baru setelah selesai menghitung hasil (=).
     */
    private fun resetIfEvaluated() {
        if (isEvaluated) {
            tokens.clear()           // Kosongkan buffer token
            expressionDisplay = ""   // Bersihkan riwayat teks ekspresi di atas
            isEvaluated = false      // Reset status evaluasi
        }
    }

    /*
     * Listener / Method: onNumberClick(number: String)
     * Dipanggil saat pengguna menekan salah satu tombol angka (0–9).
     * Parameter `number`: Karakter angka yang ditekan dalam bentuk String.
     */
    fun onNumberClick(number: String) {
        resetIfEvaluated() // Cek apakah perlu meriset input setelah '='

        // Jika daftar token kosong atau elemen terakhir adalah operator (+, -, ×, ÷), tambahkan angka sebagai token baru
        if (tokens.isEmpty() || isOperator(tokens.last())) {
            tokens.add(number)
        } else {
            // Jika elemen terakhir sudah berupa angka, gabungkan angka baru ke angka yang sedang diketik
            val lastToken = tokens.last()
            if (lastToken == "0") {
                // Menghindari bug angka dengan nol di depan seperti "05" (langsung diganti jadi "5")
                tokens[tokens.lastIndex] = number
            } else if (lastToken == "-0") {
                // Menghindari "-05" menjadi "-5"
                tokens[tokens.lastIndex] = "-$number"
            } else if (lastToken.length < 12) {
                // Pembatasan Panjang Angka:
                // Dibatasi maksimal 12 digit per angka untuk mencegah angka meluap keluar dari layar UI (Overflow)
                tokens[tokens.lastIndex] = lastToken + number
            }
        }
        updateDisplay() // Perbarui tampilan teks di layar
    }

    /*
     * Listener / Method: onDecimalClick()
     * Dipanggil saat pengguna menekan tombol titik desimal (.).
     */
    fun onDecimalClick() {
        resetIfEvaluated() // Cek apakah perlu meriset input setelah '='

        if (tokens.isEmpty() || isOperator(tokens.last())) {
            // Jika desimal ditekan di awal atau setelah operator, otomatis buat "0."
            tokens.add("0.")
        } else if (!tokens.last().contains(".")) {
            // Validasi Titik Ganda:
            // Hanya tambahkan titik jika pada token angka terakhir belum ada titik (mencegah "12.3.4")
            tokens[tokens.lastIndex] = "${tokens.last()}."
        }
        updateDisplay() // Perbarui tampilan teks di layar
    }

    /*
     * Listener / Method: onOperatorClick(operator: String)
     * Dipanggil saat pengguna menekan tombol operator (+, -, ×, ÷).
     */
    fun onOperatorClick(operator: String) {
        if (isEvaluated) {
            // Jika baru selesai menghitung (=), gunakan hasil kalkulasi sebelumnya sebagai angka pertama operasi berikutnya
            val prev = inputDisplay
            tokens.clear()
            isEvaluated = false
            // Jika hasil sebelumnya bukan pesan error, masukkan ke token
            tokens.add(if (!prev.contains("Tidak")) prev else "0")
        }

        if (tokens.isEmpty()) {
            if (operator == "-") {
                tokens.add("-") // Mendukung input angka negatif di awal (contoh: "-5")
            } else {
                tokens.add("0") // Jika menekan +, ×, ÷ di awal, anggap dari angka 0
                tokens.add(operator)
            }
        } else if (isOperator(tokens.last()) || tokens.last() == "-") {
            // Koreksi Operator Otomatis:
            // Jika pengguna menekan operator berturut-turut (misal + lalu -), ganti operator lama dengan yang baru
            tokens[tokens.lastIndex] = operator
        } else {
            tokens.add(operator) // Tambahkan operator ke buffer token
        }
        updateDisplay() // Perbarui tampilan teks di layar
    }

    /*
     * Listener / Method: onClearClick()
     * Dipanggil saat pengguna menekan tombol Clear (C) untuk meriset seluruh status kalkulator.
     */
    fun onClearClick() {
        tokens.clear()         // Bersihkan daftar token
        inputDisplay = "0"     // Kembalikan teks layar utama ke "0"
        expressionDisplay = "" // Kosongkan riwayat ekspresi di atas
        isEvaluated = false    // Reset status evaluasi
    }

    /*
     * Listener / Method: onBackspaceClick()
     * Dipanggil saat pengguna menekan tombol Backspace (⌫) untuk menghapus 1 karakter terakhir.
     */
    fun onBackspaceClick() {
        if (isEvaluated) {
            onClearClick() // Jika baru selesai menghitung (=), hapus langsung meriset layar
            return
        }

        if (tokens.isNotEmpty()) {
            val lastToken = tokens.last()
            if (isOperator(lastToken)) {
                // Jika token terakhir adalah operator, langsung hapus token operator tersebut
                tokens.removeAt(tokens.lastIndex)
            } else {
                // Jika token terakhir angka, buang 1 karakter di paling kanan
                val updated = lastToken.dropLast(1)
                if (updated.isEmpty() || updated == "-") {
                    tokens.removeAt(tokens.lastIndex) // Jika tersisa kosong atau hanya "-", hapus tokennya
                } else {
                    tokens[tokens.lastIndex] = updated
                }
            }
        }
        updateDisplay() // Perbarui tampilan teks di layar
    }

    /*
     * Listener / Method: onEqualsClick()
     * Dipanggil saat pengguna menekan tombol sama dengan (=) untuk mengevaluasi hasil matematika.
     */
    fun onEqualsClick() {
        if (tokens.isEmpty()) return

        // Jika ekspresi diakhiri oleh operator menggantung (contoh: "12 + "), hapus operator tersebut sebelum dihitung
        if (isOperator(tokens.last()) || tokens.last() == "-") {
            tokens.removeAt(tokens.lastIndex)
        }

        if (tokens.isEmpty()) return

        // Gabungkan token menjadi string ekspresi utuh (contoh: "12 + 5")
        val fullExpr = tokens.joinToString(" ")
        val exprText = "$fullExpr ="
        expressionDisplay = exprText // Tampilkan di bagian atas layar

        // Eksekusi kalkulasi berdasarkan aturan matematika Kabataku
        val (result, error) = evaluateTokens(tokens)

        // Tentukan teks hasil yang akan ditampilkan
        val resultText = error ?: if (result != null) formatNumber(result) else "0"

        inputDisplay = resultText

        // Simpan riwayat perhitungan di urutan teratas (index 0)
        historyList.add(0, HistoryItem(exprText, resultText))

        isEvaluated = true // Tandai bahwa perhitungan telah selesai
    }

    /*
     * Listener / Method: onHistoryItemClick(item: HistoryItem)
     * Dipanggil saat pengguna menekan salah satu item riwayat di dalam dialog riwayat.
     */
    fun onHistoryItemClick(item: HistoryItem) {
        // Pastikan hasil riwayat bukan teks pesan kesalahan
        if (!item.result.contains("Tidak")) {
            tokens.clear()
            tokens.add(item.result)        // Gunakan angka hasil riwayat sebagai input aktif
            inputDisplay = item.result     // Tampilkan di layar utama
            expressionDisplay = item.expression // Tampilkan ekspresi riwayat di atas
            isEvaluated = true
        }
    }

    /*
     * Listener / Method: clearHistory()
     * Mengosongkan seluruh daftar riwayat perhitungan.
     */
    fun clearHistory() {
        historyList.clear()
    }

    /*
     * Method Privat: updateDisplay()
     * Memperbarui variabel `inputDisplay` berdasarkan gabungan daftar token yang ada saat ini.
     */
    private fun updateDisplay() {
        inputDisplay = if (tokens.isEmpty()) "0" else tokens.joinToString(" ")
    }

    /*
     * Method Privat: isOperator(token: String): Boolean
     * Memeriksa apakah sebuah string merupakan simbol operator matematika dasar.
     */
    private fun isOperator(token: String) = token == "+" || token == "-" || token == "×" || token == "÷"

    /*
     * Method Privat: evaluateTokens(inputTokens: List<String>)
     * Logika Inti Perhitungan Matematika (Hierarki Kabataku: Kali Bagi Tambah Kurang)
     * Mengembalikan Pair<Double?, String?>: (Nilai Hasil, Pesan Error jika Ada).
     */
    private fun evaluateTokens(inputTokens: List<String>): Pair<Double?, String?> {
        val numbers = mutableListOf<Double>()
        val ops = mutableListOf<String>()

        // Tahap Parsing: Memisahkan token menjadi daftar angka (Double) dan daftar operator (String)
        for (token in inputTokens) {
            if (isOperator(token) && numbers.isNotEmpty()) {
                ops.add(token)
            } else {
                val num = token.toDoubleOrNull()
                if (num != null) numbers.add(num) else if (token == "-") numbers.add(0.0)
            }
        }

        if (numbers.isEmpty()) return Pair(0.0, null)

        // Tahap 1 Evaluasi: Dahulukan Perkalian (×) dan Pembagian (÷) dari kiri ke kanan
        var i = 0
        while (i < ops.size) {
            val op = ops[i]
            if (op == "×" || op == "÷") {
                val a = numbers[i]
                val b = numbers[i + 1]

                /*
                 * Catatan Penanganan Error / Edge Case:
                 * Pembagian dengan 0 di dalam komputer menghasilkan nilai 'Infinity' atau 'NaN'.
                 * Di sini ditangani secara khusus agar tidak menyebabkan crash pada aplikasi.
                 */
                if (op == "÷" && b == 0.0) return Pair(null, "Tidak dapat dibagi 0")

                numbers[i] = if (op == "×") a * b else a / b
                numbers.removeAt(i + 1) // Hapus angka yang sudah dihitung
                ops.removeAt(i)         // Hapus operator yang sudah dieksekusi
            } else {
                i++
            }
        }

        // Tahap 2 Evaluasi: Lanjutkan Penjumlahan (+) dan Pengurangan (-) dari kiri ke kanan
        while (ops.isNotEmpty()) {
            val op = ops.removeAt(0)
            val a = numbers[0]
            val b = numbers.removeAt(1)
            numbers[0] = if (op == "+") a + b else a - b
        }

        // Kembalikan hasil akhir kalkulasi
        return Pair(numbers.firstOrNull() ?: 0.0, null)
    }

    /*
     * Method Privat: formatNumber(value: Double): String
     * Memformat tampilan desimal:
     * Jika angka bulat (misal 15.0), diubah menjadi "15" agar tampilan rapi tanpa akhiran .0.
     */
    private fun formatNumber(value: Double) = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
