package com.vasmarfas.mosstroiinformadmin.features.projects.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class ProjectsRepository(private val api: ProjectsApi) {
    
    // Максимальное количество попыток при таймауте
    private val MAX_RETRIES = 3
    // Задержка между попытками (в миллисекундах) - экспоненциальная
    private val RETRY_DELAY_MS = 500L
    // Таймаут для первого запроса (5 секунд)
    private val FIRST_REQUEST_TIMEOUT_MS = 1_000L
    
    private fun isTimeoutException(e: Exception): Boolean {
        return e is HttpRequestTimeoutException ||
                e is TimeoutCancellationException ||
                e.message?.contains("timeout", ignoreCase = true) == true ||
                e.message?.contains("request timeout has expired", ignoreCase = true) == true ||
                e.cause?.message?.contains("timeout", ignoreCase = true) == true
    }
    
    private suspend fun <T> retryOnTimeout(
        maxRetries: Int = MAX_RETRIES,
        isFirstRequest: Boolean = false,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        
        for (attempt in 1..maxRetries) {
            try {
                // Для первой попытки используем короткий таймаут (5 секунд)
                // Для остальных попыток используем стандартный таймаут из HttpClient (30 секунд)
                return if (attempt == 1 && isFirstRequest) {
                    withTimeout(FIRST_REQUEST_TIMEOUT_MS) {
                        block()
                    }
                } else {
                    block()
                }
            } catch (e: Exception) {
                lastException = e
                if (isTimeoutException(e) && attempt < maxRetries) {
                    // Экспоненциальная задержка: 500ms, 1000ms, 2000ms
                    delay(RETRY_DELAY_MS * attempt)
                    continue
                } else {
                    throw e
                }
            }
        }
        
        throw lastException ?: Exception("Неизвестная ошибка")
    }
    
    suspend fun getProjects(page: Int = 0, limit: Int? = null, isFirstRequest: Boolean = false): ApiResult<List<Project>> {
        return try {
            val projects = retryOnTimeout(isFirstRequest = isFirstRequest) {
                api.getProjects(page, limit)
            }
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки проектов")
        }
    }
    
    suspend fun getRequestedProjects(page: Int = 0, limit: Int? = null): ApiResult<List<Project>> {
        return try {
            val projects = retryOnTimeout {
                api.getRequestedProjects(page, limit)
            }
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки запрошенных проектов")
        }
    }
    
    suspend fun getProject(id: String): ApiResult<Project> {
        return try {
            val project = retryOnTimeout {
                api.getProject(id)
            }
            ApiResult.Success(project)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки проекта")
        }
    }
    
    suspend fun requestConstruction(id: String): ApiResult<Unit> {
        return try {
            retryOnTimeout {
                api.requestConstruction(id)
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка запроса строительства")
        }
    }
    
    suspend fun startConstruction(id: String, address: String): ApiResult<Unit> {
        return try {
            retryOnTimeout {
                api.startConstruction(id, address)
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка начала строительства")
        }
    }
}
