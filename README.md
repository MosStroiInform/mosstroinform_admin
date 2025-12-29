# 🎨 МосСтройИнформ - Админ-панель

Красивая, современная админ-панель для управления строительными проектами на **Kotlin Multiplatform + Compose Multiplatform**.

## ✨ Особенности

- 🎯 **Kotlin Multiplatform** - работает на Android, iOS, Desktop (JVM), Web
- 🎨 **Material Design 3** - современный, красивый UI
- 📱 **Адаптивный дизайн** - идеально выглядит на любых экранах
- 🏗️ **Clean Architecture** - чистая архитектура с разделением слоёв
- 🔄 **Real-time** - WebSocket поддержка для чатов
- ⚡ **Быстрая разработка** - Ktor Client, Koin DI, Voyager Navigation

## 🚀 Технологический стек

### Core
- **Kotlin Multiplatform** 2.3.0
- **Compose Multiplatform** 1.9.3
- **Coroutines** 1.10.2
- **Kotlinx Serialization** 1.8.0

### Network
- **Ktor Client** 3.1.1 (HTTP + WebSocket)
- **Content Negotiation** с JSON

### DI & Navigation
- **Koin** 4.1.0 (Dependency Injection)
- **Navigation Compose** 2.8.0 (Official JetBrains Navigation)

### Storage
- **DataStore** 1.1.1 (Official Google/JetBrains, токены, настройки)

## 📱 Поддерживаемые платформы

- ✅ **Android** (API 24+)
- ✅ **iOS** (iOS 14.0+)
- ✅ **Desktop** (Windows, macOS, Linux)
- ✅ **Web** (JS/WASM)

## 🏗️ Архитектура

```
composeApp/
├── commonMain/
│   ├── core/
│   │   ├── data/models/         # Модели данных
│   │   ├── network/             # Ktor Client, API config
│   │   ├── storage/             # TokenStorage
│   │   ├── di/                  # Koin modules
│   │   ├── theme/               # Material3 theme
│   │   ├── ui/                  # Адаптивные компоненты
│   │   └── navigation/          # Navigation setup
│   │
│   ├── features/
│   │   ├── auth/                # Авторизация
│   │   │   ├── data/            # AuthApi, AuthRepository
│   │   │   ├── presentation/    # LoginViewModel, LoginScreen
│   │   │   └── di/              # AuthModule
│   │   │
│   │   ├── dashboard/           # Главная панель
│   │   │   └── presentation/    # DashboardViewModel, DashboardScreen
│   │   │
│   │   ├── projects/            # Управление проектами
│   │   │   └── data/            # ProjectsApi, ProjectsRepository
│   │   │
│   │   ├── documents/           # Управление документами
│   │   │   └── data/            # DocumentsApi, DocumentsRepository
│   │   │
│   │   └── chats/               # Чаты с WebSocket
│   │       └── data/            # ChatsApi, ChatsRepository
│   │
│   ├── MainApp.kt               # Главное приложение
│   └── App.kt                   # Entry point с Koin
│
├── androidMain/...
├── iosMain/...
├── jvmMain/...
└── jsMain/...
```

### Слои архитектуры

1. **Data Layer** - API, Repository, Models
2. **Domain Layer** - Use Cases, Entities (при необходимости)
3. **Presentation Layer** - ViewModel, UI (Compose)

## 🎨 UI/UX Особенности

### Адаптивный дизайн

Приложение автоматически адаптируется под размер экрана:

- **Compact** (<600dp) - телефоны
  - Bottom Navigation Bar
  - Компактные карточки
  - 1-2 колонки в Grid

- **Medium** (600-840dp) - планшеты, телефоны в landscape
  - Bottom Navigation Bar
  - 2-3 колонки в Grid

- **Expanded** (>840dp) - десктопы, большие планшеты
  - Navigation Rail (боковая панель)
  - 3-4 колонки в Grid
  - Расширенные карточки

### Material Design 3

- 🎨 **Красивая цветовая палитра** - синий primary, оранжевый secondary
- 🌙 **Dark/Light themes** - автоматическое определение
- 📊 **Статус badges** - цветовая индикация статусов
- 💫 **Smooth transitions** - плавные анимации

## 🚀 Быстрый старт

### Требования

- JDK 11+
- Android Studio или IntelliJ IDEA
- Kotlin 2.3.0+

### Установка

1. Клонируйте репозиторий:
```bash
git clone https://github.com/your-repo/mosstroinform_admin.git
cd mosstroinform_admin
```

2. Соберите проект:
```bash
./gradlew build
```

### Запуск

#### Desktop (JVM)
```bash
./gradlew :composeApp:run
```

#### Android
```bash
./gradlew :composeApp:assembleDebug
```
Или используйте Android Studio для запуска

#### iOS
Откройте `iosApp/iosApp.xcodeproj` в Xcode и запустите

#### Web (JS)
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

#### Web (WASM)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## 🔐 Конфигурация

### API URL

Измените в `core/network/ApiConfig.kt`:

```kotlin
object ApiConfig {
    const val BASE_URL = "https://your-api.com"
    const val API_VERSION = "/api/v1"
}
```

### Авторизация

Токены автоматически сохраняются в `TokenStorage` и используются во всех запросах через Ktor Auth plugin.

## 📚 Документация

### Основные компоненты

#### 1. **Dashboard**
- Статистика проектов, документов, чатов
- Карточки с иконками и числами
- Адаптивный grid layout
- Последние действия

#### 2. **Авторизация**
- Красивый Login экран
- Email/Password поля
- Показ/скрытие пароля
- Loading state
- Error handling

#### 3. **Адаптивные компоненты**
- `AdaptivePadding` - отступы для разных экранов
- `AdaptiveCardSize` - размеры карточек
- `WindowSize` - определение типа экрана
- `isCompactScreen()` / `isExpandedScreen()` - хелперы

#### 4. **UI компоненты**
- `LoadingIndicator` - индикатор загрузки
- `ErrorView` - экран ошибки с retry
- `EmptyState` - пустое состояние
- `StatusBadge` - цветные badges для статусов

### API клиенты

Все API клиенты следуют одному паттерну:

```kotlin
class SomeApi(private val client: HttpClient) {
    suspend fun getData(): List<Data> {
        return client.get("/endpoint").body()
    }
    
    suspend fun postData(data: Data): Data {
        return client.post("/endpoint") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.body()
    }
}
```

### Repositories

```kotlin
class SomeRepository(private val api: SomeApi) {
    suspend fun getData(): ApiResult<List<Data>> {
        return try {
            val data = api.getData()
            ApiResult.Success(data)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка")
        }
    }
}
```

### ViewModels

```kotlin
class SomeViewModel(
    private val repository: SomeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            when (val result = repository.getData()) {
                is ApiResult.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
```

## 🔄 Текущая функциональность

### ✅ Реализовано

- ✅ Авторизация (Login)
- ✅ Dashboard с статистикой
- ✅ Просмотр проектов
- ✅ Просмотр документов
- ✅ Одобрение/отклонение документов
- ✅ Просмотр чатов
- ✅ Отправка сообщений
- ✅ Адаптивный дизайн
- ✅ Material3 тема
- ✅ Navigation
- ✅ DI с Koin

### 🚧 В разработке

- 🚧 CRUD проектов (требуется backend)
- 🚧 CRUD документов (требуется backend)
- 🚧 WebSocket для real-time чатов
- 🚧 Управление стройплощадками
- 🚧 Управление камерами
- 🚧 Загрузка файлов

## 📋 Backend требования

Для полной функциональности требуется добавить в backend:

1. **CRUD для проектов** (POST, PUT, DELETE)
2. **CRUD для документов** (POST, PUT, DELETE)
3. **CRUD для строительных площадок**
4. **CRUD для камер**
5. **CRUD для чатов**
6. **Загрузка файлов** (изображения, документы)
7. **Статистика для dashboard** (`GET /admin/stats`)
8. **RBAC** (роли: admin, specialist, user)

Подробности в файле `BACKEND_IMPROVEMENTS.md`

## 🤝 Вклад разработчиков

Проект создан с использованием **best practices**:

- Clean Architecture
- SOLID principles
- Separation of Concerns
- Dependency Injection
- Repository pattern
- MVVM pattern

## 📄 Лицензия

MIT License

---

**Сделано с ❤️ и Kotlin Multiplatform**
