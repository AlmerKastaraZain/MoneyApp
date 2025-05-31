package com.telkom.DanaApp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.telkom.DanaApp.R
import com.telkom.DanaApp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    transactions: List<TransactionData> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (TransactionData) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    // Hitung total saldo berdasarkan tipe transaksi
    val totalBalance = transactions.sumOf { txn ->
        when (txn.type) {
            "PEMASUKAN" -> txn.total
            "PENGELUARAN" -> -txn.total
            else -> 0L
        }
    }

    // Format rupiah tanpa desimal
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Kelompokkan transaksi berdasarkan tanggal, urut terbaru di atas
    val groupedTransactions = remember(transactions) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        transactions.mapNotNull { transaction ->
            // Convert Firebase Timestamp to date string
            val dateString = transaction.createdAt?.let { timestamp ->
                dateFormat.format(timestamp.toDate())
            } ?: dateFormat.format(Date()) // fallback to current date
            Pair(dateString, transaction)
        }
            .groupBy { it.first }
            .mapValues { entry -> entry.value.map { it.second } }
            .toList()
            .sortedByDescending { (date, _) ->
                runCatching {
                    dateFormat.parse(date)?.time ?: 0L
                }.getOrDefault(0L)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        WalletHeader(
            balance = totalBalance,
            isBalanceVisible = isBalanceVisible,
            onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
            onAddClick = onAddTransactionClick,
            currencyFormatter = currencyFormatter
        )

        if (transactions.isEmpty()) {
            EmptyTransactionState(
                onAddTransactionClick = onAddTransactionClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            TransactionHistory(
                groupedTransactions = groupedTransactions,
                onTransactionClick = onTransactionClick,
                currencyFormatter = currencyFormatter,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WalletHeader(
    balance: Long,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onAddClick: () -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        color = DarkGreen,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Baris atas: Judul dan menu (ikon tiga titik)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet",
                    color = White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = { /* TODO: aksi menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = White
                    )
                }
            }

            // Bagian saldo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Saldo",
                    color = White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = InterFontFamily
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isBalanceVisible) currencyFormatter.format(balance) else "••••••••",
                        color = White,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = if (isBalanceVisible) "👁️" else "🔒",
                            color = White.copy(alpha = 0.8f),
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Tombol tambah transaksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = White,
                    contentColor = DarkGreen,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Transaksi",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionHistory(
    groupedTransactions: List<Pair<String, List<TransactionData>>>,
    onTransactionClick: (TransactionData) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Riwayat Transaksi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        groupedTransactions.forEach { (date, txns) ->
            item { DateHeader(date = date) }
            items(txns) { txn ->
                TransactionItem(
                    transaction = txn,
                    onClick = { onTransactionClick(txn) },
                    currencyFormatter = currencyFormatter
                )
            }
        }
    }
}

@Composable
fun DateHeader(
    date: String,
    modifier: Modifier = Modifier
) {
    val displayDate = when (date) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) -> "Hari ini"
        else -> try {
            val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)
            val cal = Calendar.getInstance().apply { time = parsedDate!! }
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            when {
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) -> "Kemarin"
                else -> SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(parsedDate)
            }
        } catch (e: Exception) {
            date
        }
    }

    Text(
        text = displayDate,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold
        ),
        color = Gray,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TransactionItem(
    transaction: TransactionData,
    onClick: () -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFFFA500).copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = transaction.categoryIconRes),
                    contentDescription = transaction.title,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Black
                )

                if (transaction.desc.isNotBlank()) {
                    Text(
                        text = transaction.desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFontFamily
                        ),
                        color = Gray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            val amountColor = if (transaction.type == "PEMASUKAN") Color(0xFF4CAF50) else Color(0xFFF44336)
            val prefix = if (transaction.type == "PEMASUKAN") "+ " else "- "

            Text(
                text = prefix + currencyFormatter.format(transaction.total),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = amountColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun EmptyTransactionState(
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_guest),
            contentDescription = "No Transactions",
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Tidak ada transaksi untuk saat ini.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium
            ),
            color = Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddTransactionClick) {
            Text(text = "Tambah Transaksi Baru")
        }
    }
}

data class TransactionData(
    var title: String = "",
    var desc: String = "",
    var type: String = "", // "PEMASUKAN" atau "PENGELUARAN"
    var total: Long = 0L,
    var transactionTimestamp: Timestamp? = null,
    var createdAt: Timestamp? = null,
    var categoryIconRes: Int = R.drawable.ic_launcher_foreground,
)

fun getTransactions(
    onResult: (List<TransactionData>) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("transactions")
        .orderBy("transactionTimestamp", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { result ->
            val transactions = result.map { doc ->
                TransactionData(
                    title = doc.getString("title") ?: "",
                    desc = doc.getString("desc") ?: "",
                    type = doc.getString("type") ?: "",
                    total = doc.getLong("total") ?: 0L,
                    transactionTimestamp = doc.getTimestamp("transactionTimestamp"),
                    createdAt = doc.getTimestamp("createdAt"),
                    categoryIconRes = getCategoryIcon(doc.getString("title") ?: "")
                )
            }
            onResult(transactions)
        }
        .addOnFailureListener { e ->
            onError(e)
        }
}

// Ikon berdasarkan kategori
fun getCategoryIcon(title: String): Int {
    return when {
        title.contains("makan", ignoreCase = true) -> R.drawable.makan
        title.contains("gaji", ignoreCase = true) -> R.drawable.gaji_bulanan
        else -> R.drawable.icon_wallet
    }
}

@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    val sampleTransactions = listOf(
        TransactionData(
            categoryIconRes = R.drawable.ic_launcher_foreground,
            title = "Makan & Minum",
            desc = "Makan siang di restoran",
            type = "PENGELUARAN",
            total = 50000,
            createdAt = Timestamp.now()
        ),
        TransactionData(
            categoryIconRes = R.drawable.ic_launcher_foreground,
            title = "Gaji",
            desc = "Gaji bulan Mei",
            type = "PEMASUKAN",
            total = 5000000,
            createdAt = Timestamp.now()
        ),
        TransactionData(
            categoryIconRes = R.drawable.ic_launcher_foreground,
            title = "Hiburan",
            desc = "Nonton bioskop",
            type = "PENGELUARAN",
            total = 80000,
            createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)) // Yesterday
        )
    )

    WalletScreen(
        transactions = sampleTransactions,
        onAddTransactionClick = {},
        onTransactionClick = {}
    )
}