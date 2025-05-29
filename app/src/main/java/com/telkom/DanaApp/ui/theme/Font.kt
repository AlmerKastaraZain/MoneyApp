package com.telkom.DanaApp.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.telkom.DanaApp.R

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.poppins_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

val InterFontFamily = androidx.compose.ui.text.font.FontFamily(
    Font(R.font.inter_variablefont_opsz_wght, FontWeight.Normal, FontStyle.Normal),
)