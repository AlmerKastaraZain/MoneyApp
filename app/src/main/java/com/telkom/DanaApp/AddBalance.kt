package com.telkom.DanaApp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.telkom.DanaApp.ui.theme.MoneyAppTheme
import com.telkom.DanaApp.ui.theme.White
import com.telkom.DanaApp.view.AddBalance
import com.google.firebase.Timestamp // Import Firebase Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue // For server timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.telkom.DanaApp.ui.theme.TransactionData
import java.text.ParseException
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

class AddBalance : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore // Declare Firestore instance
    private lateinit var auth: FirebaseAuth // Declare FirebaseAuth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_balance)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = Firebase.firestore
        auth = Firebase.auth

        findViewById<ComposeView>(R.id.composeableAddBalance).setContent {
            MoneyAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = White,
                ) {
                    AddBalance( // Call the composable
                        onSaveTransaction = { transactionData ->
                            // This is where you get all the data
                            handleSaveTransaction(transactionData)
                            finish()
                        },
                        onNavigateBack = {
                            // Handle back navigation, e.g., finish the activity
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun handleSaveTransaction(data: TransactionData) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("AddBalanceActivity", "No user signed in. Cannot save transaction.")
            Toast.makeText(this, "Anda harus login untuk menyimpan transaksi.", Toast.LENGTH_LONG).show()
            return
        }
        val userId = currentUser.uid

        Log.d("AddBalanceActivity", "Data to save: $data")

        // --- Convert date and time strings to a Firebase Timestamp ---
        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH.mm", Locale.getDefault())
        var transactionTimestamp: Timestamp? = null // Initialize as nullable

        try {
            // Ensure "Hari ini" is handled correctly by getting current date if needed for parsing
            val dateStringToParse = if (data.date.equals("Hari ini", ignoreCase = true)) {
                val todayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                todayFormatter.format(Date()) // Get current date as "dd/MM/yyyy"
            } else {
                data.date
            }

            val combinedDateTimeString = "$dateStringToParse ${data.time}" // e.g., "07/05/2024 10.30"
            val parsedDate: Date? = dateTimeFormatter.parse(combinedDateTimeString)

            if (parsedDate != null) {
                transactionTimestamp = Timestamp(parsedDate)
            } else {
                Log.e("AddBalanceActivity", "ParsedDate is null for: $combinedDateTimeString. Using current time as fallback.")
                transactionTimestamp = Timestamp.now() // Fallback to current time if parsing fails
            }
        } catch (e: ParseException) {
            Log.e("AddBalanceActivity", "Error parsing date/time: '${data.date} ${data.time}'. Using current time as fallback.", e)
            transactionTimestamp = Timestamp.now() // Fallback to current time on parsing error
        }
        // --- End of Timestamp conversion ---


        val collectionPath = if (data.transactionType == "PEMASUKAN") {
            "App/MoneyApp/TransactionData"
        } else {
            "App/MoneyApp/TransactionData"
        }

        val firestoreDocument = hashMapOf(
            "user_id" to userId,
            "title" to data.categoryName,
            "desc" to data.notes,
            "total" to data.amount,
            "type" to data.transactionType,
            "categoryIconRes" to data.categoryIconRes,
            "transactionTimestamp" to transactionTimestamp, // <-- STORE AS TIMESTAMP
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection(collectionPath)
            .add(firestoreDocument)
            .addOnSuccessListener { documentReference ->
                Log.d("AddBalanceActivity", "DocumentSnapshot added with ID: ${documentReference.id} for user $userId")
                Toast.makeText(this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("AddBalanceActivity", "Error adding document for user $userId", e)
                Toast.makeText(this, "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_LONG).show()
            }

    }
}