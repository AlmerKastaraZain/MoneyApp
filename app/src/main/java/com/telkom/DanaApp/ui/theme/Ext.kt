package com.telkom.DanaApp.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill

fun Modifier.dropShadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    shape: Shape = RectangleShape,
) = this
    .clip(shape) // Clip the content to the specified shape
    .then(
        Modifier.drawBehind {
            val shadowColor = color.copy(alpha = 1f)  // Keep as Compose Color
            val transparent = color.copy(alpha = 0f).toArgb()

            val paint = Paint() // Create paint outside drawIntoCanvas
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparent
            frameworkPaint.setShadowLayer(
                blurRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor.toArgb()  //Use toArgb here
            )
            val drawStyle = Fill // or Stroke

            val outline = shape.createOutline(size, layoutDirection, this) //Use DrawScope here
            drawOutline(
                outline = outline,
                color = shadowColor,  // Pass Compose Color here
                style = drawStyle
            )
        }
    )