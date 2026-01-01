package com.vasmarfas.mosstroiinformadmin.features.chats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Chat
import com.vasmarfas.mosstroiinformadmin.core.data.models.Message
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.chats.data.ChatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatDetailState(
    val isLoading: Boolean = false,
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val isSending: Boolean = false,
    val isConnected: Boolean = false
)

class ChatDetailViewModel(
    private val chatId: String,
    private val repository: ChatsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatDetailState())
    val state: StateFlow<ChatDetailState> = _state.asStateFlow()
    
    private var currentChatId: String? = null
    
    init {
        currentChatId = chatId
        loadChat()
        loadMessages()
        connectWebSocket()
    }
    
    // Проверяем, изменился ли chatId, и перезагружаем чат если нужно
    fun updateChatId(newChatId: String) {
        if (currentChatId != newChatId) {
            // Отключаемся от старого WebSocket
            repository.disconnectFromChat()
            // Сбрасываем состояние
            _state.value = ChatDetailState()
            // Обновляем chatId
            currentChatId = newChatId
            // Загружаем новый чат
            loadChat()
            loadMessages()
            connectWebSocket()
        }
    }
    
    private fun loadChat() {
        viewModelScope.launch {
            val idToLoad = currentChatId ?: chatId
            when (val result = repository.getChat(idToLoad)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(chat = result.data)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(error = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    private fun loadMessages() {
        viewModelScope.launch {
            val idToLoad = currentChatId ?: chatId
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getMessages(idToLoad)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        messages = result.data
                    )
                    markAsRead()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun connectWebSocket() {
        viewModelScope.launch {
            val idToLoad = currentChatId ?: chatId
            try {
                // Устанавливаем флаг подключения сразу при начале подключения
                _state.value = _state.value.copy(isConnected = true)
                
                repository.connectToChat(idToLoad).collect { message ->
                    // Оптимизированная проверка на дубликаты - используем Set для быстрого поиска
                    val messageIds = _state.value.messages.map { it.id }.toSet()
                    
                    if (message.id !in messageIds) {
                        // Добавляем новое сообщение в конец списка
                        _state.value = _state.value.copy(
                            messages = _state.value.messages + message,
                            isConnected = true,
                            isSending = false // Сбрасываем флаг отправки при получении сообщения
                        )
                    } else {
                        // Обновляем существующее сообщение (например, если изменился статус прочитанности)
                        _state.value = _state.value.copy(
                            messages = _state.value.messages.map { 
                                if (it.id == message.id) message else it 
                            },
                            isConnected = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isConnected = false,
                    error = "Ошибка подключения к WebSocket: ${e.message}"
                )
            }
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true)
            
            try {
                // Всегда пытаемся использовать WebSocket для отправки (быстрее и реактивнее)
                // Если WebSocket подключен, отправляем через него
                if (_state.value.isConnected) {
                    repository.sendMessageViaWebSocket(text)
                    // Флаг isSending будет сброшен автоматически при получении сообщения через WebSocket
                } else {
                    // Если WebSocket не подключен, используем REST API как fallback
                    val idToUse = currentChatId ?: chatId
                    when (val result = repository.sendMessage(idToUse, text)) {
                        is ApiResult.Success -> {
                            _state.value = _state.value.copy(
                                messages = _state.value.messages + result.data,
                                isSending = false
                            )
                        }
                        is ApiResult.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message,
                                isSending = false
                            )
                        }
                        is ApiResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка отправки сообщения",
                    isSending = false
                )
            }
        }
    }
    
    private fun markAsRead() {
        viewModelScope.launch {
            val idToUse = currentChatId ?: chatId
            repository.markAsRead(idToUse)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.disconnectFromChat()
    }
}

