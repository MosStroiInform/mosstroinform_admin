package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

@Composable
fun AvatarImage(
    imageUrl: String?,
    name: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    httpClient: HttpClient = koinInject()
) {
    var imageBitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }
    
    LaunchedEffect(imageUrl) {
        if (imageUrl != null && imageUrl.isNotBlank()) {
            isLoading = true
            imageBitmap = loadImageFromUrl(imageUrl, httpClient)
            isLoading = false
        } else {
            isLoading = false
            imageBitmap = null
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            when {
                imageBitmap != null -> {
                    // Показываем загруженное изображение
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // Показываем иконку человека в качестве плейсхолдера
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.6f),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarImagePreview() {
    AdminTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarImage(
                imageUrl = null,
                name = "Иван Иванов",
                size = 48.dp
            )
            AvatarImage(
                imageUrl = null,
                name = "Петр Петров",
                size = 64.dp
            )
            AvatarImage(
                imageUrl = null,
                name = "Мария Сидорова",
                size = 80.dp
            )
        }
    }
}

