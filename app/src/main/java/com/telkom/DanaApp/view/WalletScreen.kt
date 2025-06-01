package com.telkom.DanaApp.view // Or your correct package for WalletScreen

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this import
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.Timestamp // Firebase Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.telkom.DanaApp.EditTransactionActivity
import com.telkom.DanaApp.R
// Import YOUR TransactionData structure
import com.telkom.DanaApp.ui.theme.TransactionData // <<-- YOUR TARGET TransactionData
// Import your theme colors and fonts
import com.telkom.DanaApp.ui.theme.*
import com.telkom.DanaApp.viewmodel.WalletUiState
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// This Composable will manage the state (fetching data)
@Composable
fun WalletScreenStateful(
    uiState: WalletUiState,
    onRefreshTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    // onNavigateToTransactionDetail is now handled internally to launch EditActivity
    // You can remove it from this Composable's parameters if its only purpose was editing.
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)

    // Launcher for EditTransactionActivity to get a result back
    val editTransactionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Transaction was updated, refresh the list
            Log.d("WalletScreenStateful", "Transaction updated, refreshing list.")
            onRefreshTransactions()
        } else {
            Log.d("WalletScreenStateful", "Edit transaction cancelled or failed.")
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefreshTransactions,
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading && uiState.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null && uiState.transactions.isEmpty()) {
            Column( /* ... error display with retry button ... */ ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRefreshTransactions) {
                    Text("Coba Lagi")
                }
            }
        } else {
            WalletScreen(
                transactions = uiState.transactions,
                onAddTransactionClick = onNavigateToAddTransaction,
                onTransactionClick = { transactionToEdit ->
                    // Launch EditTransactionActivity when an item is clicked
                    if (transactionToEdit.documentId == null) {
                        Log.e("WalletScreenStateful", "Document ID is null for transaction: ${transactionToEdit.categoryName}. Cannot edit.")
                        android.widget.Toast.makeText(context, "Tidak dapat mengedit transaksi ini (ID tidak ditemukan).", android.widget.Toast.LENGTH_SHORT).show()
                        return@WalletScreen
                    }

                    val intent = Intent(context, EditTransactionActivity::class.java).apply {
                        putExtra(EditTransactionActivity.EXTRA_DOCUMENT_ID, transactionToEdit.documentId)
                        putExtra(EditTransactionActivity.EXTRA_TRANSACTION_DATA, transactionToEdit) // transactionToEdit is Parcelable
                    }
                    editTransactionLauncher.launch(intent)
                }
            )
            // Optional: Show a Snackbar for errors if data is already present
            uiState.errorMessage?.let { message ->
                // Consider using a SnackbarHost here if you have a Scaffold higher up
                // For simplicity, a Toast can also work for non-blocking error indication.
                LaunchedEffect(message, uiState.transactions) { // Key on transactions to avoid re-showing for same error on recompose
                    if (!uiState.isLoading) { // Don't show error toast if also loading
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

// This is the UI part, taking the already fetched transactions
@Composable
fun WalletScreen(
    transactions: List<com.telkom.DanaApp.ui.theme.TransactionData>, // Uses YOUR TransactionData
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (com.telkom.DanaApp.ui.theme.TransactionData) -> Unit,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    val totalBalance = transactions.sumOf { txn ->
        when (txn.transactionType) { // Adapted to your TransactionData
            "PEMASUKAN" -> txn.amount.toLong()
            "PENGELUARAN" -> -txn.amount.toLong()
            else -> 0L
        }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0 // No decimal places for Rupiah
        }
    }

    // Group transactions by their date string for display purposes
    val groupedTransactions = remember(transactions) {
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        transactions
            .map { transaction ->
                // Prioritize the 'date' string field from TransactionData if it's correctly formatted.
                // This 'date' string is what you set in AddBalanceActivity.
                val dateString = if (transaction.date.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                    transaction.date
                } else {
                    // Fallback for "Hari ini" or if 'date' field wasn't set as expected,
                    // or if you want to derive from a potential transactionTimestamp field (if you add it to your TransactionData).
                    // For now, assuming your 'date' field in TransactionData is the primary source.
                    // If 'date' can be "Hari ini", handle it here:
                    if (transaction.date.equals("Hari ini", ignoreCase = true)) {
                        displayDateFormat.format(Date()) // current date
                    } else {
                        transaction.date // Or a default if 'date' is totally unusable
                    }
                }
                Pair(dateString, transaction)
            }
            .groupBy { it.first } // Group by the "dd/MM/yyyy" string
            .mapValues { entry -> entry.value.map { it.second } }
            .toList()
            .sortedByDescending { (dateStr, _) -> // Sort groups by parsing the date string
                try {
                    displayDateFormat.parse(dateStr)?.time ?: 0L
                } catch (e: Exception) {
                    0L // Fallback for unparseable dates
                }
            }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray) // Use your theme color
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
                onAddTransactionClick = onAddTransactionClick, // Pass the callback
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
            .height(200.dp), // Adjust height as needed
        color = DarkGreen, // Use your theme color
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween // Pushes FAB to bottom of this Column
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dompet Saya",
                    color = White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = InterFontFamily, // Use your theme font
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = { /* TODO: Implement overflow menu action */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu Lainnya",
                        tint = White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Saldo",
                    color = White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = InterFontFamily)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBalanceVisible) currencyFormatter.format(balance) else "••••••••",
                        color = White,
                        style = MaterialTheme.typography.displaySmall.copy( // Or headlineLarge
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold // Make it stand out
                        ),
                        fontSize = 30.sp // Slightly larger
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp) // Slightly larger touch target
                    ) {
                        Icon( // Using Icons for visibility toggle
                            painter = painterResource(id = if (isBalanceVisible) R.drawable.eye_open else R.drawable.eye_closed),
                            contentDescription = if (isBalanceVisible) "Sembunyikan Saldo" else "Tampilkan Saldo",
                            tint = White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Aligns FAB to the end
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = White, // Or your theme's secondary color
                    contentColor = DarkGreen, // Or your theme's onSecondary color
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, "Tambah Transaksi")
                }
            }
        }
    }
}

@Composable
fun TransactionHistory(
    groupedTransactions: List<Pair<String, List<com.telkom.DanaApp.ui.theme.TransactionData>>>, // YOUR TransactionData
    onTransactionClick: (com.telkom.DanaApp.ui.theme.TransactionData) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 60.dp), // Consistent padding
        verticalArrangement = Arrangement.spacedBy(12.dp) // Space between date groups/items
    ) {
        item {
            Text(
                text = "Riwayat Transaksi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = InterFontFamily, // Your font
                    fontWeight = FontWeight.Bold
                ),
                color = Black, // Your theme color
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp) // More space above history
            )
        }

        groupedTransactions.forEach { (date, transactionsOnDate) ->
            item {
                DateHeader(date = date) // DateHeader will format this "dd/MM/yyyy" string
            }
            items(transactionsOnDate, key = { transaction -> transaction.categoryName + transaction.date + transaction.time + transaction.amount }) { txn -> // More robust key
                TransactionItem(
                    transaction = txn,
                    onClick = { onTransactionClick(txn) },
                    currencyFormatter = currencyFormatter
                )
                // Optional: Add a small divider between items if not using Surface elevation
                // if (transactionsOnDate.last() != txn) {
                //     Divider(color = LightGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp)) // Indent divider
                // }
            }
        }
    }
}

@Composable
fun DateHeader(date: String, modifier: Modifier = Modifier) { // date is "dd/MM/yyyy"
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val outputDayMonthYearFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))

    val displayDate = try {
        val parsedDate = displayDateFormat.parse(date)
        if (parsedDate != null) {
            val cal = Calendar.getInstance().apply { time = parsedDate }
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            when {
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hari ini"
                cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Kemarin"
                else -> outputDayMonthYearFormat.format(parsedDate)
            }
        } else {
            date // Fallback if parsing fails
        }
    } catch (e: Exception) {
        date // Fallback on any parsing exception
    }

    Text(
        text = displayDate,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = InterFontFamily, // Your font
            fontWeight = FontWeight.SemiBold
        ),
        color = Black, // Your theme color
        modifier = modifier.padding(bottom = 8.dp, top = 12.dp) // Add some top padding
    )
}

@Composable
fun TransactionItem(
    transaction: com.telkom.DanaApp.ui.theme.TransactionData, // YOUR TransactionData
    onClick: () -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = White, // Your theme color for card background
        shadowElevation = 2.dp // Subtle shadow
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
                        // You can vary this background based on transaction type or category if desired
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), // Example
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = transaction.categoryIconRes), // From your TransactionData
                    contentDescription = transaction.categoryName, // Adapted
                    modifier = Modifier.size(28.dp), // Slightly larger icon
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName, // Adapted
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Black // Your theme text color
                )
                if (transaction.notes.isNotBlank()) { // Adapted
                    Text(
                        text = transaction.notes, // Adapted
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = InterFontFamily),
                        color = Gray, // Your theme secondary text color
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp)) // Reduced spacer

            val amountColor = if (transaction.transactionType == "PEMASUKAN") { // Adapted
                Color(0xFF2E7D32) // Darker Green
            } else {
                MaterialTheme.colorScheme.error // Use theme error color for expenses
            }
            val prefix = if (transaction.transactionType == "PEMASUKAN") "+ " else "- "

            Text(
                text = prefix + currencyFormatter.format(transaction.amount), // Adapted
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
fun EmptyTransactionState(onAddTransactionClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp), // More vertical padding
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.misc), // Replace with a suitable empty state image
            contentDescription = "Tidak Ada Transaksi",
            modifier = Modifier.size(150.dp), // Adjust size
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Belum ada riwayat transaksi.",
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = InterFontFamily),
            color = Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Yuk, catat transaksi pertamamu!",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFontFamily),
            color = Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddTransactionClick,
            shape = RoundedCornerShape(50) // Pill shape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Tambah", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Catat Transaksi")
        }
    }
}

// --- Data Fetching Logic (Adapted) ---
suspend fun getTransactionsForUser(
    userId: String,
    onResult: (List<com.telkom.DanaApp.ui.theme.TransactionData>) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = Firebase.firestore
    val allTransactions = mutableListOf<com.telkom.DanaApp.ui.theme.TransactionData>()

    try {
        // Fetch Penambahan (Income)
        val incomeSnapshot = db.collection("App/MoneyApp/Penambahan")
            .whereEqualTo("user_id", userId)
            // .orderBy("transactionTimestamp", Query.Direction.DESCENDING) // Order later after merging
            .get()
            .await() // Use await for cleaner async code

        for (doc in incomeSnapshot.documents) {
            val transaction = com.telkom.DanaApp.ui.theme.TransactionData(
                categoryName = doc.getString("title") ?: doc.getString("categoryName") ?: "Pemasukan", // Prioritize new field name
                notes = doc.getString("desc") ?: doc.getString("notes") ?: "",
                transactionType = doc.getString("type") ?: "PEMASUKAN",
                amount = (doc.getLong("total") ?: doc.getLong("amount") ?: 0L).toInt(),
                categoryIconRes = (doc.getLong("categoryIconRes") ?: R.drawable.misc.toLong()).toInt(), // Provide a default income icon
                date = doc.getString("transactionDate") ?: "", // This is your "dd/MM/yyyy" string or "Hari ini"
                time = doc.getString("transactionTime") ?: "", // This is your "HH.mm" string
                // If you also store transactionTimestamp in Firestore, fetch it:
                // transactionTimestamp = doc.getTimestamp("transactionTimestamp")
            )
            allTransactions.add(transaction)
        }

        // Fetch Pengeluaran (Expense)
        val expenseSnapshot = db.collection("App/MoneyApp/Pengeluaran")
            .whereEqualTo("user_id", userId)
            // .orderBy("transactionTimestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        for (doc in expenseSnapshot.documents) {
            val transaction = com.telkom.DanaApp.ui.theme.TransactionData(
                categoryName = doc.getString("title") ?: doc.getString("categoryName") ?: "Pengeluaran",
                notes = doc.getString("desc") ?: doc.getString("notes") ?: "",
                transactionType = doc.getString("type") ?: "PENGELUARAN",
                amount = (doc.getLong("total") ?: doc.getLong("amount") ?: 0L).toInt(),
                categoryIconRes = (doc.getLong("categoryIconRes") ?: R.drawable.misc.toLong()).toInt(), // Provide a default expense icon
                date = doc.getString("transactionDate") ?: "",
                time = doc.getString("transactionTime") ?: "",
                // transactionTimestamp = doc.getTimestamp("transactionTimestamp")
            )
            allTransactions.add(transaction)
        }

        // Sort all transactions by date and time (descending)
        // This requires parsing your date and time strings
        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH.mm", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allTransactions.sortWith(compareByDescending { txn ->
            try {
                val dateToSort = if (txn.date.equals("Hari ini", ignoreCase = true)) {
                    displayDateFormat.format(Date())
                } else {
                    txn.date
                }
                dateTimeFormatter.parse("$dateToSort ${txn.time}")?.time ?: 0L
            } catch (e: Exception) {
                0L // Fallback for parsing errors, places problematic items at the end
            }
        })

        onResult(allTransactions)

    } catch (e: Exception) {
        onError(e)
    }
}


// --- Preview (Adapted) ---
@Preview(showBackground = true, device = "spec:width=360dp,height=740dp,dpi=420")
@Composable
fun WalletScreenContentPreview() { // Renamed to avoid conflict with WalletScreenStateful
    // Use YOUR TransactionData for the preview
    val sampleTransactions = listOf(
        com.telkom.DanaApp.ui.theme.TransactionData(
            categoryName = "Makan Siang",
            categoryIconRes = R.drawable.makan, // Use an actual drawable
            amount = 75000,
            date = "27/05/2024",
            time = "12.30",
            notes = "Nasi Padang",
            transactionType = "PENGELUARAN"
        ),
        com.telkom.DanaApp.ui.theme.TransactionData(
            categoryName = "Gaji Mei",
            categoryIconRes = R.drawable.gaji_bulanan, // Use an actual drawable
            amount = 7500000,
            date = "25/05/2024",
            time = "09.00",
            notes = "Gaji bulanan",
            transactionType = "PEMASUKAN"
        ),
        com.telkom.DanaApp.ui.theme.TransactionData(
            categoryName = "Transportasi",
            categoryIconRes = R.drawable.bensin, // Use an actual drawable
            amount = 50000,
            date = "Hari ini", // Example for "Hari ini"
            time = "08.15",
            notes = "Isi bensin motor",
            transactionType = "PENGELUARAN"
        )
    )
    MoneyAppTheme { // Assuming you have a MoneyAppTheme
        WalletScreen(
            transactions = sampleTransactions,
            onAddTransactionClick = {},
            onTransactionClick = {}
        )
    }
}

