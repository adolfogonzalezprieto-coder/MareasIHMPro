package com.adolfogonzalez.mareasihmpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adolfogonzalez.mareasihmpro.data.Point

@Composable
fun Stat(label: String, value: String, color: Color) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = color, fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun Chart(points: List<Point>, color: Color, modifier: Modifier) {
    Canvas(modifier.background(Panel, RoundedCornerShape(18.dp))) {
        if (points.size < 2) return@Canvas
        val padding = 10.dp.toPx()
        val minimum = points.minOf { point -> point.value }
        val maximum = points.maxOf { point -> point.value }
        val range = (maximum - minimum).coerceAtLeast(0.1)
        fun x(hour: Double): Float = padding + ((hour / 24.0) * (size.width - padding * 2)).toFloat()
        fun y(value: Double): Float = size.height - padding - (((value - minimum) / range) * (size.height - padding * 2)).toFloat()
        val path = Path()
        points.forEachIndexed { index, point ->
            val pointX = x(point.hour)
            val pointY = y(point.value)
            if (index == 0) path.moveTo(pointX, pointY)
            else {
                val previous = points[index - 1]
                val middle = (x(previous.hour) + pointX) / 2f
                path.cubicTo(middle, y(previous.value), middle, pointY, pointX, pointY)
            }
        }
        drawPath(path, color, style = Stroke(3.dp.toPx()))
        drawCircle(Color.White, 5.dp.toPx(), Offset(x(points.first().hour), y(points.first().value)))
        drawCircle(color, 3.dp.toPx(), Offset(x(points.first().hour), y(points.first().value)))
    }
}

@Composable
fun Axis() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Ahora", "+6h", "+12h", "+18h", "+24h").forEach { label -> Text(label, color = Muted, fontSize = 8.sp) }
    }
}
