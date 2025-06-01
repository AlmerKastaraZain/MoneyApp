package com.telkom.DanaApp.ui.theme

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.telkom.DanaApp.R
import kotlinx.android.parcel.Parcelize
import java.security.Timestamp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)


@Parcelize
data class TransactionData(
    var documentId: String? = null,
    val userId: String = "",
    val categoryName: String,
    @DrawableRes val categoryIconRes: Int,
    val amount: Int,
    val date: String, // e.g., "28/05/2024"
    val time: String, // e.g., "10.30"
    val notes: String,
    val transactionType: String // "PENGELUARAN" or "PEMASUKAN"
) : Parcelable

data class SpendingIcon(
    val name: String,
    @DrawableRes val iconResId: Int
)

fun getSpendingIcons(): List<SpendingIcon> {
    return listOf(
        SpendingIcon("Bensin", R.drawable.bensin), // Ensure R.drawable.bensin exists
        SpendingIcon("Pakaian", R.drawable.pakaian),
        SpendingIcon("Pulsa", R.drawable.pulsa),
        SpendingIcon("Belanja", R.drawable.belanja),
        SpendingIcon("Makan", R.drawable.makan),
        SpendingIcon("Obat", R.drawable.obat),
        SpendingIcon("Sewa Rumah", R.drawable.sewa_rumah),
        SpendingIcon("Doktor", R.drawable.doktor),
        SpendingIcon("Asuransi", R.drawable.asuransi),
        SpendingIcon("Buku dan Alat Tulis", R.drawable.buku),
        SpendingIcon("Gadget dan Aksesori", R.drawable.aksesori),
        SpendingIcon("Hobi dan Rekreasi", R.drawable.hobi_rekreasi),
        SpendingIcon("Listrik", R.drawable.listrik),
        SpendingIcon("Tabungan", R.drawable.tabungan),
        SpendingIcon("Air", R.drawable.air),
        SpendingIcon("Lain-Lainnnya", R.drawable.misc)
    )
}

data class IncomeIcons(
    val name: String,
    @DrawableRes val iconResId: Int
)

fun getIncomeIcons(): List<IncomeIcons> {
    return listOf(
        IncomeIcons("Sewa", R.drawable.sewa),
        IncomeIcons("Pengembalian Dana", R.drawable.pengembalian_dana),
        IncomeIcons("Bonus", R.drawable.bonus),
        IncomeIcons("Penjualan", R.drawable.penjualan),
        IncomeIcons("Dividen / Investasi", R.drawable.dividen_investasi),
        IncomeIcons("Gaji Bulanan", R.drawable.gaji_bulanan),
        IncomeIcons("Jasa", R.drawable.jasa),
        IncomeIcons("Bisnis", R.drawable.bisnis),
        IncomeIcons("Lain-Lainnnya", R.drawable.misc)
    )
}