package com.telkom.DanaApp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.telkom.DanaApp.ui.theme.MoneyAppTheme
import com.telkom.DanaApp.ui.theme.TransactionData as ThemeTransactionData // Alias for your theme's TransactionData
import com.telkom.DanaApp.ui.theme.White
import com.telkom.DanaApp.view.EditTransactionScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSACTION_DATA = "extra_transaction_data"
        const val EXTRA_DOCUMENT_ID = "extra_document_id"
    }

    private var documentIdToEdit: String? = null
    private var initialData: ThemeTransactionData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_transaction) // Ensure this layout exists and has the ComposeView

        documentIdToEdit = intent.getStringExtra(EXTRA_DOCUMENT_ID)
        initialData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRANSACTION_DATA, ThemeTransactionData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRANSACTION_DATA)
        }

        if (documentIdToEdit == null || initialData == null) {
            Toast.makeText(this, "Data transaksi tidak valid.", Toast.LENGTH_LONG).show()
            finish() // Close activity if essential data is missing
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ComposeView>(R.id.composeableEditBalance).setContent { // Ensure R.id.composeableEditBalance exists
            MoneyAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = White) {
                    EditTransactionScreen(
                        initialTransactionData = initialData!!,
                        documentId = documentIdToEdit!!,
                        onUpdateTransaction = ::handleUpdateTransaction,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }

    private fun handleUpdateTransaction(
        updatedData: ThemeTransactionData,
        docId: String
    ) {
        Log.d("EditTransaction", "Attempting to update doc: $docId with data: $updatedData")
        val db = Firebase.firestore

        // Convert date and time strings back to Firebase Timestamp for storage
        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH.mm", Locale.getDefault())
        var transactionTimestamp: Timestamp? = null
        try {
            val dateToParse = if (updatedData.date.equals("Hari ini", ignoreCase = true)) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            } else {
                updatedData.date
            }
            val combinedDateTimeString = "$dateToParse ${updatedData.time}"
            val parsedDate = dateTimeFormatter.parse(combinedDateTimeString)
            if (parsedDate != null) {
                transactionTimestamp = Timestamp(parsedDate)
            }

        } catch (e: Exception) {
            Log.e("EditTransaction", "Error parsing date/time for update: ${updatedData.date} ${updatedData.time}", e)
            // Decide on fallback: use original timestamp, current time, or block update
            // For simplicity, if parsing fails, we might not update the timestamp or use current
            // Or better, ensure date/time from UI are always valid before this point.
            // If originalTimestamp was passed, you could use that as a fallback.
        }


        // Fields to update. User cannot change userId, createdAt.
        // We also keep the original transactionType, as changing it implies moving collections.
        val transactionUpdateMap = hashMapOf<String, Any?>(
            "title" to updatedData.categoryName,       // Map to your Firestore field name
            "desc" to updatedData.notes,                // Map to your Firestore field name
            "total" to updatedData.amount,              // Map to your Firestore field name
            "categoryIconRes" to updatedData.categoryIconRes,
            "transactionDate" to updatedData.date,      // Storing the string date
            "transactionTime" to updatedData.time,      // Storing the string time
            "transactionTimestamp" to transactionTimestamp, // Updated timestamp
            "updatedAt" to FieldValue.serverTimestamp() // Always update this
            // "type" is NOT updated here. If you allow type change, logic is more complex.
        )

        // Path to the document in the single collection
        val documentPath = "App/MoneyApp/TransactionData/$docId"

        db.document(documentPath)
            .update(transactionUpdateMap)
            .addOnSuccessListener {
                Log.d("EditTransaction", "DocumentSnapshot successfully updated!")
                Toast.makeText(this, "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK) // <-- IMPORTANT: Set result before finishing
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("EditTransaction", "Error updating document", e)
                Toast.makeText(this, "Gagal memperbarui transaksi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}