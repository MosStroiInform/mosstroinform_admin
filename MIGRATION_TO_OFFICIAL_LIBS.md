# 🔄 Миграция на официальные библиотеки

## Что изменилось

Заменили сторонние библиотеки на **официальные решения** от JetBrains и Google:

### ❌ Удалено (сторонние библиотеки)

| Библиотека | Версия | Зачем использовалась |
|-----------|--------|---------------------|
| `cafe.adriel.voyager` | 1.1.0 | Навигация между экранами |
| `com.russhwolf:multiplatform-settings` | 1.2.0 | Хранение настроек/токенов |

### ✅ Добавлено (официальные библиотеки)

| Библиотека | Версия | От кого | Зачем |
|-----------|--------|---------|-------|
| `androidx.navigation:navigation-compose` | 2.8.0-alpha10 | JetBrains | Официальная навигация для Compose Multiplatform |
| `androidx.datastore:datastore-preferences-core` | 1.1.1 | Google/JetBrains | Официальное хранилище настроек |

---

## 🎯 Почему официальные библиотеки лучше?

### 1. **Navigation Compose (вместо Voyager)**

#### Преимущества:
- ✅ **Официальная поддержка** от JetBrains
- ✅ **Типобезопасность** из коробки (с Kotlin Serialization)
- ✅ **Интеграция с Android** (deep links, back stack)
- ✅ **Активная разработка** и обновления
- ✅ **Документация** на официальном сайте
- ✅ **Стабильность** - используется в production приложениях Google

#### Voyager недостатки:
- ❌ Сторонняя библиотека (один разработчик)
- ❌ Меньше комьюнити и примеров
- ❌ Может отставать от обновлений Compose
- ❌ Нет гарантий долгосрочной поддержки

### 2. **DataStore (вместо Multiplatform Settings)**

#### Преимущества:
- ✅ **Официальная библиотека** от Google
- ✅ **Корутины из коробки** (async операции)
- ✅ **Типобезопасность** с Preferences
- ✅ **Миграция с SharedPreferences** - официальная рекомендация Google
- ✅ **Flow API** для реактивных обновлений
- ✅ **Транзакции** - атомарные операции

#### Multiplatform Settings недостатки:
- ❌ Сторонняя обёртка
- ❌ Синхронный API (блокирующий)
- ❌ Меньше функциональности

---

## 📚 Как использовать новые библиотеки

### 1. Navigation Compose

#### Определение роутов:
```kotlin
object Routes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val DASHBOARD = "dashboard"
}
```

#### Настройка NavHost:
```kotlin
val navController = rememberNavController()

NavHost(
    navController = navController,
    startDestination = Routes.AUTH
) {
    composable(Routes.AUTH) {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            }
        )
    }
    
    composable(Routes.MAIN) {
        MainScreen()
    }
}
```

#### Навигация:
```kotlin
// Переход вперёд
navController.navigate(Routes.DASHBOARD)

// Переход с очисткой back stack
navController.navigate(Routes.MAIN) {
    popUpTo(Routes.AUTH) { inclusive = true }
}

// Назад
navController.popBackStack()
```

#### С аргументами:
```kotlin
// Определение
composable(
    route = "project/{id}",
    arguments = listOf(navArgument("id") { type = NavType.StringType })
) { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")
    ProjectScreen(id = id!!)
}

// Навигация
navController.navigate("project/123")
```

### 2. DataStore

#### Создание через expect/actual:
```kotlin
// commonMain
expect fun createDataStore(): DataStore<Preferences>

// androidMain
actual fun createDataStore(): DataStore<Preferences> {
    val context = LocalContext.current.applicationContext
    return context.dataStore
}

// jvmMain (Desktop)
actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { 
            File(System.getProperty("user.home"), ".app/prefs.pb").toPath()
        }
    )
}
```

#### Использование:
```kotlin
class TokenStorage(private val dataStore: DataStore<Preferences>) {
    
    // Сохранение
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }
    
    // Чтение
    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOKEN]
        }.first()
    }
    
    // Flow для реактивности
    val tokenFlow: Flow<String?> = dataStore.data.map { 
        it[KEY_TOKEN] 
    }
    
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
    }
}
```

#### DI с Koin:
```kotlin
val coreModule = module {
    single { createDataStore() }
    single { TokenStorage(get()) }
}
```

---

## 🔄 Что изменилось в коде

### 1. TokenStorage

**Было (Settings):**
```kotlin
class TokenStorage(private val settings: Settings) {
    fun saveToken(token: String) {
        settings["token"] = token  // Синхронно
    }
    
    fun getToken(): String? {
        return settings.getStringOrNull("token")
    }
}
```

**Стало (DataStore):**
```kotlin
class TokenStorage(private val dataStore: DataStore<Preferences>) {
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token  // Асинхронно, через корутины
        }
    }
    
    suspend fun getToken(): String? {
        return dataStore.data.map { it[KEY_TOKEN] }.first()
    }
}
```

### 2. AuthRepository

**Было:**
```kotlin
fun isLoggedIn(): Boolean {
    return tokenStorage.getToken() != null  // Синхронно
}
```

**Стало:**
```kotlin
suspend fun isLoggedIn(): Boolean {
    return tokenStorage.getToken() != null  // Асинхронно
}
```

### 3. Navigation

**Было (Voyager):**
```kotlin
class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LoginScreenContent(
            onSuccess = { navigator.push(MainScreen()) }
        )
    }
}
```

**Стало (Navigation Compose):**
```kotlin
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    LoginScreenContent(onSuccess = onLoginSuccess)
}

// В NavHost:
composable(Routes.AUTH) {
    LoginScreen(
        onLoginSuccess = {
            navController.navigate(Routes.MAIN)
        }
    )
}
```

---

## ⚡ Производительность

### DataStore vs Settings

| Операция | Settings | DataStore |
|----------|----------|-----------|
| Запись | Синхронная (блокирует поток) | Асинхронная (корутина) |
| Чтение | Синхронное | Асинхронное с кэшированием |
| Транзакции | Нет | Есть (атомарные) |
| Реактивность | Нет | Flow API |
| Типобезопасность | Weak | Strong |

### Navigation Compose vs Voyager

| Критерий | Voyager | Navigation Compose |
|----------|---------|-------------------|
| Bundle size | ~50KB | ~200KB (но официальная) |
| API сложность | Простой | Чуть сложнее, но мощнее |
| Deep Links | Базовая | Полная поддержка |
| Type-safety | Manual | Kotlinx.serialization |
| Back Stack | Custom | Android-native |

---

## 🎯 Итоги

### Плюсы миграции:
- ✅ **Официальная поддержка** от JetBrains/Google
- ✅ **Долгосрочная стабильность**
- ✅ **Лучшая документация** и примеры
- ✅ **Большое комьюнити**
- ✅ **Типобезопасность** из коробки
- ✅ **Корутины everywhere** (асинхронность)
- ✅ **Совместимость с Android best practices**

### Минусы:
- ⚠️ Чуть больше boilerplate кода для DataStore (expect/actual)
- ⚠️ Navigation Compose API немного сложнее Voyager

### Вердикт:
**Однозначно стоит использовать официальные библиотеки!** Они стабильнее, мощнее и будут поддерживаться годами.

---

## 📖 Полезные ссылки

- [Navigation Compose Docs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html)
- [DataStore Docs](https://developer.android.com/topic/libraries/architecture/datastore)
- [KMP Best Practices](https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html)

