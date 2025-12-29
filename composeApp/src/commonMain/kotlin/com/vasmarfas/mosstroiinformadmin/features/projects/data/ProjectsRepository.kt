package com.vasmarfas.mosstroiinformadmin.features.projects.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult

class ProjectsRepository(private val api: ProjectsApi) {
    
    suspend fun getProjects(page: Int = 0, limit: Int? = null): ApiResult<List<Project>> {
        return try {
            val projects = api.getProjects(page, limit)
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки проектов")
        }
    }
    
    suspend fun getRequestedProjects(page: Int = 0, limit: Int? = null): ApiResult<List<Project>> {
        return try {
            val projects = api.getRequestedProjects(page, limit)
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки запрошенных проектов")
        }
    }
    
    suspend fun getProject(id: String): ApiResult<Project> {
        return try {
            val project = api.getProject(id)
            ApiResult.Success(project)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки проекта")
        }
    }
    
    suspend fun requestConstruction(id: String): ApiResult<Unit> {
        return try {
            api.requestConstruction(id)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка запроса строительства")
        }
    }
    
    suspend fun startConstruction(id: String, address: String): ApiResult<Unit> {
        return try {
            api.startConstruction(id, address)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка начала строительства")
        }
    }
}
