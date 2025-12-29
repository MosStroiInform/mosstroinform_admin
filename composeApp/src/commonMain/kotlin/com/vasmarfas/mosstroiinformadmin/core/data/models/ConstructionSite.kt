package com.vasmarfas.mosstroiinformadmin.core.data.models

import com.vasmarfas.mosstroiinformadmin.core.data.serializers.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable(with = com.vasmarfas.mosstroiinformadmin.core.data.serializers.ConstructionSiteSerializer::class)
data class ConstructionSite(
    val id: String,
    val projectId: String,
    val projectName: String,
    val address: String,
    val cameras: List<Camera> = emptyList(),
    val startDate: Instant? = null,
    val expectedCompletionDate: Instant? = null,
    val progress: Float = 0.0f
)

@Serializable(with = com.vasmarfas.mosstroiinformadmin.core.data.serializers.CameraSerializer::class)
data class Camera(
    val id: String,
    val name: String,
    val description: String = "",
    val streamUrl: String,
    val isActive: Boolean = true,
    val thumbnailUrl: String? = null
)
