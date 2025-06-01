package com.telkom.DanaApp.view // Or a suitable package

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp // For converting back if needed
import com.telkom.DanaApp.R
import com.telkom.DanaApp.ui.theme.* // Your theme colors, fonts, and TARGET TransactionData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditTransactionScreen(
    initialTransactionData: com.telkom.DanaApp.ui.theme.TransactionData, // The data to edit
    documentId: String, // Crucial for updating the correct document
    onUpdateTransaction: (updatedData: com.telkom.DanaApp.ui.theme.TransactionData, docId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // State for editable fields, initialized with initialTransactionData
    var currentCategoryIconRes by remember { mutableStateOf(initialTransactionData.categoryIconRes) }
    var currentCategoryName by remember { mutableStateOf(initialTransactionData.categoryName) }
    var amount by remember { mutableStateOf(initialTransactionData.amount) }
    var selectedDateText by remember { mutableStateOf(initialTransactionData.date) } // "dd/MM/yyyy" or "Hari ini"
    var selectedTimeText by remember { mutableStateOf(initialTransactionData.time) } // "HH.mm"
    var notes by remember { mutableStateOf(initialTransactionData.notes) }
    // Transaction type is usually not editable for an existing transaction,
    // as it might change its collection. If it IS editable, you need a state for it.
    // For now, assume it's fixed from initialTransactionData.
    val transactionType = initialTransactionData.transactionType
    var currentSelectedTransactionTypeEnum by remember {
        mutableStateOf(
            if (transactionType.equals("PEMASUKAN", ignoreCase = true)) TransactionType.PEMASUKAN
            else TransactionType.PENGELUARAN
        )
    }


    var isCategorySelectorVisible by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray) // Use your theme color
    ) {
        // Top Green Header (Similar to AddBalance)
        Column(
            modifier = Modifier
                .background(DarkGreen)
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Image(
                imageVector = Icons.Filled.ArrowBack,
                colorFilter = ColorFilter.tint(White),
                contentDescription = "Kembali",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() },
            )

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { isCategorySelectorVisible = !isCategorySelectorVisible },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = currentCategoryIconRes),
                    contentDescription = "Ikon Kategori",
                    modifier = Modifier.size(42.dp),
                )
                Text(
                    text = currentCategoryName,
                    color = White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Use your existing ClickableNumberInput or a similar input for amount
                ClickableNumberInput( // Assuming this is your Composable for number input
                    initialValue = amount,
                    onValueChange = { newAmount -> amount = newAmount }
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .background(Color.LightGray) // Use your theme color
                .fillMaxWidth()
                .weight(1f),
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                // Use your existing MyCustomInputForm, passing current states
                MyCustomInputForm(
                    selectedDateText = selectedDateText,
                    onDateChange = { selectedDateText = it },
                    selectedTimeText = selectedTimeText,
                    onTimeChange = { selectedTimeText = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes button/selector to bottom

            if (isCategorySelectorVisible) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.65).dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = White,
                    shadowElevation = 8.dp
                ) {
                    TransactionCategorySelector(
                        spendingCategories = getSpendingIcons(),
                        incomeCategories = getIncomeIcons(),
                        onCategorySelected = { categoryData, type ->
                            val (name, iconResId) = when (categoryData) {
                                is SpendingIcon -> categoryData.name to categoryData.iconResId
                                is IncomeIcons -> categoryData.name to categoryData.iconResId
                                else -> initialTransactionData.categoryName to initialTransactionData.categoryIconRes // Fallback
                            }
                            currentCategoryName = name
                            currentCategoryIconRes = iconResId
                            currentSelectedTransactionTypeEnum = type // Update enum for internal logic if needed
                            // Note: We are NOT changing initialTransactionData.transactionType here.
                            // If type change is allowed, it's a more complex update (move between collections).
                            isCategorySelectorVisible = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Button(
                    onClick = {
                        val finalDate = if (selectedDateText.equals("Hari ini", ignoreCase = true)) {
                            dateFormatter.format(Date())
                        } else {
                            selectedDateText
                        }
                        val updatedTxData = com.telkom.DanaApp.ui.theme.TransactionData(
                            categoryName = currentCategoryName,
                            categoryIconRes = currentCategoryIconRes,
                            amount = amount,
                            date = finalDate,
                            time = selectedTimeText,
                            notes = notes,
                            transactionType = transactionType // Keep original type for simplicity
                        )
                        onUpdateTransaction(updatedTxData, documentId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Perubahan", fontSize = 16.sp)
                }
            }
        }
    }
}

// You'll need MyCustomInputForm and ClickableNumberInput composables
// (similar to what you have in AddBalance view) available here.
// For brevity, I'm assuming they exist and function as they do in AddBalance.
// Make sure they take initial values and provide onValueChange callbacks.

@Preview(showBackground = true, device = "spec:width=360dp,height=740dp,dpi=420")
@Composable
fun EditTransactionScreenPreview() {
    val sampleTx = com.telkom.DanaApp.ui.theme.TransactionData(
        categoryName = "Makan Malam",
        categoryIconRes = R.drawable.makan,
        amount = 120000,
        date = "28/05/2024",
        time = "19.30",
        notes = "Steak dan jus",
        transactionType = "PENGELUARAN"
    )
    MoneyAppTheme { // Your app's theme
        EditTransactionScreen(
            initialTransactionData = sampleTx,
            documentId = "sampleDocId123",
            onUpdateTransaction = { data, docId ->
                println("Preview Update: $docId, $data")
            },
            onNavigateBack = { println("Preview Navigate Back") }
        )
    }
}