package com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.components.EmptyState
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructionObjectsListScreen(
    onObjectClick: (String) -> Unit,
    viewModel: ConstructionObjectsListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.objects.isEmpty() -> LoadingIndicator()
            state.error != null && state.objects.isEmpty() -> ErrorView(
                message = state.error ?: "Ошибка загрузки",
                onRetry = viewModel::loadObjects
            )
            state.objects.isEmpty() -> EmptyState(
                message = "Нет объектов строительства",
                subtitle = "Объекты появятся после начала строительства проектов"
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.objects,
                        key = { it.id }
                    ) { obj ->
                        ConstructionObjectCard(
                            obj = obj,
                            onClick = { onObjectClick(obj.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConstructionObjectCard(
    obj: ConstructionObject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obj.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                if (obj.isCompleted) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Завершен",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Адрес
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = obj.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Характеристики
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.Home,
                    text = "${obj.area.toInt()} м²"
                )
                InfoChip(
                    icon = Icons.Default.Build,
                    text = "${obj.floors} эт."
                )
                if (obj.bedrooms > 0) {
                    InfoChip(
                        icon = Icons.Default.Person,
                        text = "${obj.bedrooms} комн."
                    )
                }
            }

            // Статус документов
            if (obj.allDocumentsSigned) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Все документы подписаны",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Цена
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Стоимость:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${formatPrice(obj.price)} ₽",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatPrice(price: Int): String {
    return price.toString().reversed().chunked(3).joinToString(" ").reversed()
}

@Preview(showBackground = true)
@Composable
private fun ConstructionObjectCardPreview() {
    AdminTheme(darkTheme = true) {
        ConstructionObjectCard(
            obj = ConstructionObject(
                id = "1",
                projectId = "proj1",
                name = "Жилой комплекс 'Солнечный'",
                address = "г. Москва, ул. Ленина, д. 1",
                area = 150.0f,
                floors = 5,
                bedrooms = 3,
                price = 50000000,
                isCompleted = false,
                allDocumentsSigned = false
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConstructionObjectCardCompletedPreview() {
    AdminTheme(darkTheme = true) {
        ConstructionObjectCard(
            obj = ConstructionObject(
                id = "2",
                projectId = "proj2",
                name = "Жилой комплекс 'Весенний'",
                address = "г. Москва, ул. Пушкина, д. 10",
                area = 200.0f,
                floors = 7,
                bedrooms = 4,
                price = 75000000,
                isCompleted = true,
                allDocumentsSigned = true
            ),
            onClick = {}
        )
    }
}

