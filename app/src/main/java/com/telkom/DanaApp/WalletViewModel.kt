package com.telkom.DanaApp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore


import com.telkom.DanaApp.view.TransactionData
import java.sql.Date
import com.google.firebase.Timestamp

// pastikan tidak ada import TransactionData dari ui.theme



class WalletViewModel : ViewModel() {

    private val _transactions = mutableStateOf<List<TransactionData>>(emptyList())
    val transactions: State<List<TransactionData>> = _transactions


    init {
        fetchTransactions()
    }
    fun fetchTransactions() {
        val penambahanList = mutableListOf<com.telkom.DanaApp.view.TransactionData>()
        val pengeluaranList = mutableListOf<com.telkom.DanaApp.view.TransactionData>()

        val firestore = Firebase.firestore

        // Ambil data penambahan
        val penambahanTask = firestore.collection("Penambahan")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()

        // Ambil data pengeluaran
        val pengeluaranTask = firestore.collection("Pengeluaran")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()

        // Tunggu kedua task selesai
        Tasks.whenAllSuccess<QuerySnapshot>(penambahanTask, pengeluaranTask)
            .addOnSuccessListener { results ->
                // results[0] = Penambahan, results[1] = Pengeluaran
                val penambahanResult = results[0]
                val pengeluaranResult = results[1]

                penambahanList.addAll(penambahanResult.documents.mapNotNull { doc ->
                    val data = doc.data
                    com.telkom.DanaApp.view.TransactionData(
                        title = data?.get("title") as? String ?: "",
                        desc = data?.get("desc") as? String ?: "",
                        type = "PEMASUKAN", // karena dari Penambahan
                        total = (data?.get("total") as? Number)?.toLong() ?: 0L,
                        transactionTimestamp = data?.get("transactionTimestamp") as? Timestamp,
                        createdAt = data?.get("createdAt") as? Timestamp,
                        categoryIconRes = R.drawable.ic_launcher_foreground
                    )
                })

                pengeluaranList.addAll(pengeluaranResult.documents.mapNotNull { doc ->
                    val data = doc.data
                    com.telkom.DanaApp.view.TransactionData(
                        title = data?.get("title") as? String ?: "",
                        desc = data?.get("desc") as? String ?: "",
                        type = "PENGELUARAN", // karena dari Pengeluaran
                        total = (data?.get("total") as? Number)?.toLong() ?: 0L,
                        transactionTimestamp = data?.get("transactionTimestamp") as? Timestamp,
                        createdAt = data?.get("createdAt") as? Timestamp,
                        categoryIconRes = R.drawable.ic_launcher_foreground
                    )
                })

                // Gabungkan dua list
                val combinedList = penambahanList + pengeluaranList

                // Urutkan berdasarkan createdAt descending
                val sortedList = combinedList.sortedByDescending { it.createdAt?.toDate() ?: Date(0) }


                _transactions.value = sortedList
            }
            .addOnFailureListener { e ->
                Log.e("WalletViewModel", "Error fetching transactions", e)
            }
    }

}
