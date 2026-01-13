package com.vasmarfas.mosstroiinformadmin.features.projects.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStartRequest
import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ProjectsApi(private val client: HttpClient) {
    
    suspend fun getProjects(page: Int = 0, limit: Int? = null): List<Project> {
        return client.get(ApiConfig.Projects.LIST) {
            parameter("page", page)
            limit?.let { parameter("limit", it) }
        }.body()
    }
    
    suspend fun getRequestedProjects(page: Int = 0, limit: Int? = null): List<Project> {
        return client.get(ApiConfig.Projects.REQUESTED) {
            parameter("page", page)
            limit?.let { parameter("limit", it) }
        }.body()
    }
    
    suspend fun getProject(id: String): Project {
        return client.get(ApiConfig.Projects.byId(id)).body()
    }
    
    suspend fun requestConstruction(id: String) {
        client.post(ApiConfig.Projects.request(id))
    }
    
    suspend fun startConstruction(id: String, address: String) {
        client.post(ApiConfig.Projects.start(id)) {
            contentType(ContentType.Application.Json)
            setBody(ProjectStartRequest(address))
        }
    }
    
    suspend fun getProjectDocuments(projectId: String): List<Document> {
        return client.get(ApiConfig.Projects.documents(projectId)).body()
    }
}
