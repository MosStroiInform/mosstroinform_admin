package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status.lowercase()) {
        "доступен", "available", "ожидает", "pending" -> Color(0xFFFFA726) // Orange
        "запрошен", "requested", "на рассмотрении", "under_review" -> Color(0xFF42A5F5) // Blue
        "в строительстве", "construction", "в процессе", "in_progress" -> Color(0xFF66BB6A) // Green
        "завершен", "completed" -> Color(0xFF4CAF50) // Dark Green
        "одобрен", "approved" -> Color(0xFF4CAF50) // Green
        "отклонен", "rejected" -> Color(0xFFEF5350) // Red
        else -> MaterialTheme.colorScheme.primary
    }
    
    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    AdminTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge("Доступен")
            StatusBadge("Запрошен")
            StatusBadge("В строительстве")
            StatusBadge("Завершен")
            StatusBadge("Одобрен")
            StatusBadge("Отклонен")
        }
    }
}
