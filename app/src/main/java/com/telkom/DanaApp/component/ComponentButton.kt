package com.telkom.DanaApp.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telkom.DanaApp.ui.theme.InterFontFamily
import kotlin.Int



@Composable
fun ModernRoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Blue, // Default background color
    contentColor: Color = Color.White,   // Default text color
    FontSize: Int = 14,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp), // Adjust for desired roundness
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = FontSize.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.Black)
        )
    }
}