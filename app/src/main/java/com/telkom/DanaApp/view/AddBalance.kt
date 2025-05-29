package com.telkom.DanaApp.view

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telkom.DanaApp.R
import com.telkom.DanaApp.ui.theme.DarkGreen
import com.telkom.DanaApp.ui.theme.InterFontFamily
import com.telkom.DanaApp.ui.theme.LightGray
import android.app.TimePickerDialog
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.telkom.DanaApp.ui.theme.Black
import com.telkom.DanaApp.ui.theme.Gray
import com.telkom.DanaApp.ui.theme.IncomeIcons
import com.telkom.DanaApp.ui.theme.SpendingIcon
import com.telkom.DanaApp.ui.theme.TransactionData
import com.telkom.DanaApp.ui.theme.White
import com.telkom.DanaApp.ui.theme.getIncomeIcons
import com.telkom.DanaApp.ui.theme.getSpendingIcons
import java.text.SimpleDateFormat
import java.util.*

@Composable()
fun AddBalance(
    onSaveTransaction: (TransactionData) -> Unit,
    onNavigateBack: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateText by remember { mutableStateOf("Hari ini") }
    var selectedTimeText by remember { mutableStateOf("00.00") }
    var notes by remember { mutableStateOf("") }
    var currentTransactionType by remember { mutableStateOf(TransactionType.PENGELUARAN) } // To store the type


    // Attempt to find "Pakaian" or use a fallback
    val initialSpendingIcon = getSpendingIcons().find { it.name == "Pakaian" }
        ?: getSpendingIcons().firstOrNull()
        ?: SpendingIcon("Pakaian", R.drawable.p_misc) // Absolute fallback

    var currentCategoryIconRes by remember { mutableStateOf(initialSpendingIcon.iconResId) }
    var currentCategoryName by remember { mutableStateOf(initialSpendingIcon.name) }
    var isCategorySelectorVisible by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf(0) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        // Top Green Header
        Column(
            modifier = Modifier
                .background(DarkGreen)
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Image(
                imageVector = Icons.Filled.ArrowBack,
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        onNavigateBack()
                    },
            )

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { isCategorySelectorVisible = !isCategorySelectorVisible }, // Toggle selector
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = currentCategoryIconRes), // Dynamic icon
                    contentDescription = "Selected Category Icon",
                    modifier = Modifier.size(42.dp),
                )
                Text(
                    text = currentCategoryName, // Dynamic name
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontFamily = InterFontFamily, // Use your font
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            // Amount Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                ClickableNumberInput( // Your custom or a simple TextField
                    initialValue = amount,
                    onValueChange = { newNumber ->
                        amount = newNumber
                        println("New number set: $newNumber")
                    }
                )
            }
        }

        // Main Content Area (Gray background)
        Column(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .weight(1f), // Takes remaining vertical space
        ) {
            // Form Area
            Surface(
                modifier = Modifier
                    .widthIn(max = 360.dp) // Max width for the form card
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface // Use theme's surface color or Color.White
            ) {
                MyCustomInputForm(
                    selectedDateText = selectedDateText,
                    onDateChange = { selectedDateText = it },
                    selectedTimeText = selectedTimeText,
                    onTimeChange = { selectedTimeText = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
            }

            // This Spacer pushes subsequent content (Button or Selector) to the bottom
            Spacer(modifier = Modifier.weight(1f))

            if (isCategorySelectorVisible) {
                // Category Selector Area (appears at the bottom)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.65).dp), // Max 65% of screen
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp // Makes it look like it's on top
                ) {
                    TransactionCategorySelector(
                        spendingCategories = getSpendingIcons(),
                        incomeCategories = getIncomeIcons(),
                        onCategorySelected = { categoryData, type ->
                            val (name, iconResId) = when (categoryData) {
                                is SpendingIcon -> categoryData.name to categoryData.iconResId
                                is IncomeIcons -> categoryData.name to categoryData.iconResId
                                else -> initialSpendingIcon.name to initialSpendingIcon.iconResId // Fallback
                            }
                            currentCategoryName = name
                            currentCategoryIconRes = iconResId
                            isCategorySelectorVisible = false // Hide selector after selection
                            println("Selected:  $name ($type), IconRes: $iconResId")
                        },
                        modifier = Modifier.fillMaxSize() // Selector's LazyColumn handles scrolling
                    )
                }
            } else {
                // Save Button (appears at the bottom when selector is hidden)
                Button(
                    onClick = {
                        // Prepare the data
                        val finalDate = if (selectedDateText == "Hari ini") {
                            dateFormatter.format(Date()) // Get current date formatted
                        } else {
                            selectedDateText
                        }

                        val transactionData = TransactionData(
                            categoryName = currentCategoryName,
                            categoryIconRes = currentCategoryIconRes,
                            amount = amount,
                            date = finalDate,
                            time = selectedTimeText,
                            notes = notes,
                            transactionType = currentTransactionType.name // PENGELUARAN or PEMASUKAN
                        )
                        onSaveTransaction(transactionData) // Call the hoisted function
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Transaksi", fontSize = 16.sp)
                }
            }
        }
    }
}


// In your com.telkom.DanaApp.view package
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCustomInputForm(
    selectedDateText: String,
    onDateChange: (String) -> Unit,
    selectedTimeText: String,
    onTimeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth().background(White).padding(start = 12.dp, end = 12.dp)) {
        InputRow(
            icon = Icons.Filled.DateRange,
            iconContentDescription = "Tanggal Icon",
            label = "Tanggal",
            value = selectedDateText,
            onClick = { showDatePickerDialog = true }
        )
        HorizontalDivider()
        InputRow(
            icon = Icons.Outlined.DateRange,
            iconContentDescription = "Waktu Icon",
            label = "Waktu",
            value = selectedTimeText,
            onClick = { showTimePickerDialog = true }
        )
        HorizontalDivider()
        NotesInputRow(
            icon = Icons.Filled.Edit,
            iconContentDescription = "Catatan Icon",
            label = "Catatan",
            notes = notes,
            onNotesChange = onNotesChange, // Use the callback
            placeholder = "Masukkan catatan"
        )
    }

    if (showDatePickerDialog) {
        // ... (DatePickerDialog setup)
        // Inside DatePickerDialog's confirmButton onClick:
        // datePickerState.selectedDateMillis?.let { millis ->
        //     ...
        //     val formattedDate = dateFormatter.format(localCalendar.time)
        //     onDateChange(formattedDate) // <-- Call the hoisted function
        // }
        // showDatePickerDialog = false
        // --- Corrected DatePickerDialog logic ---
        val calendar = Calendar.getInstance()
        if (selectedDateText != "Hari ini") {
            try {
                calendar.time = dateFormatter.parse(selectedDateText) ?: Date()
            } catch (e: Exception) { /* Keep current date if parsing fails */ }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis,
            yearRange = (calendar.get(Calendar.YEAR) - 100)..(calendar.get(Calendar.YEAR) + 100)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedCalendar = Calendar.getInstance().apply { timeInMillis = millis }
                            val localCalendar = Calendar.getInstance().apply {
                                clear()
                                set(selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH))
                            }
                            onDateChange(dateFormatter.format(localCalendar.time)) // Use callback
                        }
                        showDatePickerDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePickerDialog = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePickerDialog) {
        // ... (TimePickerDialog setup)
        // Inside TimePickerDialog's callback:
        // val formattedTime = String.format(Locale.getDefault(), "%02d.%02d", hour, minute)
        // onTimeChange(formattedTime) // <-- Call the hoisted function
        // showTimePickerDialog = false
        // --- Corrected TimePickerDialog logic ---
        val currentTime = Calendar.getInstance()
        val initialHour: Int
        val initialMinute: Int
        if (selectedTimeText.matches(Regex("\\d{2}\\.\\d{2}"))) {
            val parts = selectedTimeText.split(".")
            initialHour = parts[0].toIntOrNull() ?: currentTime.get(Calendar.HOUR_OF_DAY)
            initialMinute = parts[1].toIntOrNull() ?: currentTime.get(Calendar.MINUTE)
        } else {
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY)
            initialMinute = currentTime.get(Calendar.MINUTE)
        }
        val timeDialog = TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                onTimeChange(String.format(Locale.getDefault(), "%02d.%02d", hour, minute)) // Use callback
                showTimePickerDialog = false
            },
            initialHour, initialMinute, true
        )
        timeDialog.setOnDismissListener { showTimePickerDialog = false }
        timeDialog.show()
    }
}

@Composable
fun InputRow(
    icon: ImageVector,
    iconContentDescription: String,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp), // Increased vertical padding for better touch
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = Color.Gray // As per image
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Gray,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold, // Bolder label
            modifier = Modifier.width(80.dp) // Give label a fixed width for alignment
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = Black,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // Value takes remaining space
        )
    }
}

@Composable
fun NotesInputRow(
    icon: ImageVector,
    iconContentDescription: String,
    label: String,
    notes: String,
    onNotesChange: (String) -> Unit,
    placeholder: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), // Less vertical for TextField
        verticalAlignment = Alignment.Top // Align to top for multi-line notes
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = Color.Gray, // As per image
            modifier = Modifier.padding(top = 12.dp) // Align icon with first line of text
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Gray,
            modifier = Modifier
                .width(80.dp) // Give label a fixed width
                .padding(top = 12.dp) // Align label with TextField baseline
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = { Text(placeholder, color = Gray, style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, // No underline
                unfocusedIndicatorColor = Color.Transparent, // No underline
                disabledIndicatorColor = Color.Transparent // No underline
            ),
            textStyle = TextStyle(color = Black),
            singleLine = false, // Allow multiline for notes
            maxLines = 5
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableNumberInput(
    modifier: Modifier = Modifier,
    initialValue: Int = 0,
    onValueChange: (Int) -> Unit
) {
    var currentValue by remember { mutableStateOf(initialValue) }
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(currentValue.toString()) }

    Box(
        modifier = modifier
            .clickable {
                inputText = currentValue.toString() // Reset input text to current value when dialog opens
                showDialog = true
            }
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Displaying the number. You can append "=" if you like
        // For "0=", you could do: Text(text = "$currentValue=", ...)
        Text(
            text = currentValue.toString(),
            style = TextStyle(
                color = White,
                fontSize = 24.sp, // Large font size
                fontWeight = FontWeight.Bold
            )
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Set Number",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { newValue ->
                            // Allow only digits or empty string for intermediate typing
                            if (newValue.all { it.isDigit() }) {
                                inputText = newValue
                            }
                        },
                        label = { Text("Enter number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newNumber = inputText.toIntOrNull()
                                if (newNumber != null) {
                                    currentValue = newNumber
                                    onValueChange(newNumber)
                                }
                                showDialog = false
                            },
                            enabled = inputText.isNotBlank() // Enable OK if input is not empty
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ClickableNumberInputPreview() {
    MaterialTheme {
        var number by remember { mutableStateOf(0) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ClickableNumberInput(
                initialValue = number,
                onValueChange = { newNumber ->
                    number = newNumber
                    // You can do something with the new number here
                    println("New number set: $newNumber")
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Current external state: $number")
        }
    }
}



enum class TransactionType {
    PENGELUARAN, PEMASUKAN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCategorySelector(
    spendingCategories: List<SpendingIcon>,
    incomeCategories: List<IncomeIcons>,
    onCategorySelected: (category: Any, type: TransactionType) -> Unit, // Returns the selected object and its type
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("PENGELUARAN", "PEMASUKAN")

    // State to hold the currently selected item's name for highlighting
    // We use Any? because it can be SpendingIcon or IncomeIcons
    var selectedItemKey by remember { mutableStateOf<Any?>(null) }

    Column(modifier = modifier.fillMaxWidth().background(White)) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp).background(White),
            containerColor = White,
            contentColor = White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = DarkGreen // Teal-like color as in image
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        selectedItemKey = null // Reset selection when tab changes
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) Color(0xFF008080) else Black
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn (
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes remaining space if parent has fixed height or is weighted
        ) {
            when (selectedTabIndex) {
                0 -> { // PENGELUARAN
                    items(spendingCategories, key = { it.name }) { category ->
                        CategoryRow(
                            name = category.name,
                            iconResId = category.iconResId,
                            isSelected = selectedItemKey == category,
                            onClick = {
                                selectedItemKey = category
                                onCategorySelected(category, TransactionType.PENGELUARAN)
                            }
                        )
                        HorizontalDivider(color = Black.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
                1 -> { // PEMASUKAN
                    items(incomeCategories, key = { it.name }) { category ->
                        CategoryRow(
                            name = category.name,
                            iconResId = category.iconResId,
                            isSelected = selectedItemKey == category,
                            onClick = {
                                selectedItemKey = category
                                onCategorySelected(category, TransactionType.PEMASUKAN)
                            }
                        )
                        HorizontalDivider(color = Black.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(
    name: String,
    @DrawableRes iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp) // Container for the icon
                .background(
                    color = Color(0xFFFFA500).copy(alpha = 0.15f), // Light orange background
                    shape = MaterialTheme.shapes.medium // Rounded corners for the box
                )
                .padding(8.dp), // Padding inside the box
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = name,
                modifier = Modifier.size(24.dp), // Icon size
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = Black
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun TransactionCategorySelectorPreview() {
    // Dummy drawables for preview - replace with your actual R.drawable... if they exist
    // Or create dummy ones in res/drawable for preview to work
    val dummySpending = listOf(
        SpendingIcon("Bensin", R.drawable.bensin),
        SpendingIcon("Pakaian", R.drawable.pakaian),
        SpendingIcon("Pulsa", R.drawable.pulsa),
        SpendingIcon("Belanja", R.drawable.belanja),
        SpendingIcon("Makan", R.drawable.makan)
    )
    val dummyIncome = listOf(
        IncomeIcons("Sewa", R.drawable.sewa),
        IncomeIcons("Gaji", R.drawable.gaji_bulanan) // Assuming gaji_bulanan exists
    )

    MaterialTheme { // Ensure a MaterialTheme is applied for previews
        Surface {
            TransactionCategorySelector(
                spendingCategories = getSpendingIcons(), // Use your actual data functions
                incomeCategories = getIncomeIcons(),
                onCategorySelected = { category, type ->
                    println("Selected: $category, Type: $type")
                }
            )
        }
    }
}