package com.telkom.DanaApp.viewmodel // Or a suitable package

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.telkom.DanaApp.R // Your R file
import com.telkom.DanaApp.ui.theme.TransactionData // Your TransactionData from ui.theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Define a UI state for the WalletScreen
data class WalletUiState(
    val transactions: List<TransactionData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WalletViewModel : ViewModel() {

    var uiState by mutableStateOf(WalletUiState())
        private set

    init {
        fetchUserTransactions()
    }

    fun fetchUserTransactions() {

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Pengguna belum login.")
                return@launch
            }

            try {
                val db = Firebase.firestore
                val fetchedTransactions = mutableListOf<TransactionData>()

                // Fetch from the single "TransactionData" collection
                val snapshot = db.collection("App/MoneyApp/TransactionData")
                    .whereEqualTo("user_id", currentUser.uid)
                    // Order by transactionTimestamp if you have it, otherwise by createdAt
                    .orderBy("transactionTimestamp", Query.Direction.DESCENDING) // Or "createdAt"
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    // Assuming Firestore doc fields match your TransactionData for saving:
                    // "title" -> categoryName, "desc" -> notes, "total" -> amount, "type" -> transactionType
                    // "categoryIconRes" (if stored), "transactionDate", "transactionTime"
                    // And "transactionTimestamp" (Firebase Timestamp)

                    val transaction = TransactionData(
                        documentId = doc.id,
                        categoryName = doc.getString("title") ?: "N/A",
                        categoryIconRes = (doc.getLong("categoryIconRes") ?: R.drawable.misc.toLong()).toInt(), // Default icon
                        amount = (doc.getLong("total") ?: 0L).toInt(),
                        // Retrieve date/time strings if you stored them.
                        // If you only stored transactionTimestamp, you'd format it here.
                        date = doc.getString("transactionDate") ?: formatDateFromTimestamp(doc.getTimestamp("transactionTimestamp")),
                        time = doc.getString("transactionTime") ?: formatTimeFromTimestamp(doc.getTimestamp("transactionTimestamp")),
                        notes = doc.getString("desc") ?: "",
                        transactionType = doc.getString("type") ?: "PENGELUARAN"
                        // Add transactionTimestamp to your TransactionData if you want to pass it to UI
                        // transactionTimestamp = doc.getTimestamp("transactionTimestamp")
                    )
                    fetchedTransactions.add(transaction)
                }
                uiState = uiState.copy(isLoading = false, transactions = fetchedTransactions)

            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error fetching transactions", e)
                uiState = uiState.copy(isLoading = false, errorMessage = "Gagal memuat data: ${e.localizedMessage}")
            }
        }
    }

    // Helper functions to format Timestamp if date/time strings are not directly stored
    private fun formatDateFromTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) // Fallback
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    private fun formatTimeFromTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date()) // Fallback
        val sdf = SimpleDateFormat("HH.mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}