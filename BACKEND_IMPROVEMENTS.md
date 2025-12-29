# Рекомендации по улучшению Backend API

## 📋 Текущее состояние

Текущий backend предоставляет **только READ операции** для админ-панели:
- ✅ Просмотр проектов
- ✅ Просмотр документов
- ✅ Одобрение/отклонение документов
- ✅ Просмотр строительных площадок и камер
- ✅ Просмотр и отправка сообщений в чатах
- ✅ Просмотр финальных документов

## 🚀 Необходимые улучшения для полноценной админ-панели

### 1. **Управление проектами** (Высокий приоритет)

#### Создание проекта
```http
POST /api/v1/projects
Content-Type: application/json

{
  "name": "string",
  "address": "string",
  "description": "string",
  "area": number,
  "floors": number,
  "price": number,
  "bedrooms": number,
  "bathrooms": number,
  "imageUrl": "string"
}

Response: 201 Created
{
  "id": "string",
  ...project fields
}
```

#### Обновление проекта
```http
PUT /api/v1/projects/{id}
Content-Type: application/json

{
  "name": "string",
  "address": "string",
  ...other fields (partial update)
}

Response: 200 OK
{
  ...updated project
}
```

#### Удаление проекта
```http
DELETE /api/v1/projects/{id}

Response: 204 No Content
```

#### Загрузка изображения проекта
```http
POST /api/v1/projects/{id}/upload-image
Content-Type: multipart/form-data

file: <binary>

Response: 200 OK
{
  "imageUrl": "string"
}
```

#### Управление этапами проекта
```http
POST /api/v1/projects/{id}/stages
PUT /api/v1/projects/{id}/stages/{stageId}
DELETE /api/v1/projects/{id}/stages/{stageId}
```

---

### 2. **Управление документами** (Высокий приоритет)

#### Создание документа
```http
POST /api/v1/documents
Content-Type: application/json

{
  "projectId": "string",
  "title": "string",
  "description": "string",
  "fileUrl": "string"
}

Response: 201 Created
```

#### Обновление документа
```http
PUT /api/v1/documents/{id}

Response: 200 OK
```

#### Удаление документа
```http
DELETE /api/v1/documents/{id}

Response: 204 No Content
```

#### Загрузка файла документа
```http
POST /api/v1/documents/{id}/upload
Content-Type: multipart/form-data

file: <binary>

Response: 200 OK
{
  "fileUrl": "string"
}
```

---

### 3. **Управление строительными площадками** (Средний приоритет)

#### Создание площадки
```http
POST /api/v1/construction-sites
Content-Type: application/json

{
  "projectId": "string",
  "startDate": "2024-01-01T00:00:00Z",
  "expectedCompletionDate": "2024-12-31T00:00:00Z"
}

Response: 201 Created
```

#### Обновление площадки (прогресс, даты)
```http
PUT /api/v1/construction-sites/{id}
Content-Type: application/json

{
  "progress": 0.65,
  "expectedCompletionDate": "2024-12-31T00:00:00Z"
}

Response: 200 OK
```

#### Удаление площадки
```http
DELETE /api/v1/construction-sites/{id}

Response: 204 No Content
```

---

### 4. **Управление камерами** (Средний приоритет)

#### Добавление камеры
```http
POST /api/v1/construction-sites/{siteId}/cameras
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "streamUrl": "string",
  "thumbnailUrl": "string"
}

Response: 201 Created
```

#### Обновление камеры
```http
PUT /api/v1/construction-sites/{siteId}/cameras/{cameraId}

Response: 200 OK
```

#### Удаление камеры
```http
DELETE /api/v1/construction-sites/{siteId}/cameras/{cameraId}

Response: 204 No Content
```

#### Активация/деактивация камеры
```http
PATCH /api/v1/construction-sites/{siteId}/cameras/{cameraId}/toggle
Content-Type: application/json

{
  "isActive": boolean
}

Response: 200 OK
```

---

### 5. **Управление чатами** (Средний приоритет)

#### Создание чата
```http
POST /api/v1/chats
Content-Type: application/json

{
  "projectId": "string",
  "specialistName": "string",
  "specialistAvatarUrl": "string"
}

Response: 201 Created
```

#### Обновление чата (назначение специалиста)
```http
PUT /api/v1/chats/{id}
Content-Type: application/json

{
  "specialistName": "string",
  "specialistAvatarUrl": "string",
  "isActive": boolean
}

Response: 200 OK
```

#### Удаление чата
```http
DELETE /api/v1/chats/{id}

Response: 204 No Content
```

---

### 6. **Управление финальными документами** (Средний приоритет)

#### Создание финального документа
```http
POST /api/v1/projects/{projectId}/final-documents
Content-Type: application/json

{
  "title": "string",
  "description": "string",
  "fileUrl": "string"
}

Response: 201 Created
```

#### Обновление финального документа
```http
PUT /api/v1/projects/{projectId}/final-documents/{documentId}

Response: 200 OK
```

#### Удаление финального документа
```http
DELETE /api/v1/projects/{projectId}/final-documents/{documentId}

Response: 204 No Content
```

---

### 7. **Статистика и аналитика** (Низкий приоритет, но полезно)

#### Общая статистика
```http
GET /api/v1/admin/stats

Response: 200 OK
{
  "totalProjects": number,
  "availableProjects": number,
  "requestedProjects": number,
  "constructionProjects": number,
  "completedProjects": number,
  "pendingDocuments": number,
  "approvedDocuments": number,
  "rejectedDocuments": number,
  "activeChats": number,
  "unreadMessages": number,
  "activeCameras": number
}
```

#### Статистика по проекту
```http
GET /api/v1/projects/{id}/stats

Response: 200 OK
{
  "projectId": "string",
  "documentsCount": number,
  "approvedDocuments": number,
  "chatsCount": number,
  "messagesCount": number,
  "camerasCount": number,
  "progress": number
}
```

---

### 8. **Управление пользователями** (Опционально)

#### Список всех пользователей
```http
GET /api/v1/admin/users

Response: 200 OK
[
  {
    "id": "string",
    "email": "string",
    "name": "string",
    "phone": "string",
    "role": "user | admin",
    "createdAt": "string",
    "projectsCount": number
  }
]
```

#### Детали пользователя
```http
GET /api/v1/admin/users/{id}

Response: 200 OK
```

#### Проекты пользователя
```http
GET /api/v1/admin/users/{id}/projects

Response: 200 OK
[...projects]
```

---

## 🔐 Авторизация и роли

Рекомендуется добавить **role-based access control (RBAC)**:

- **Admin** - полный доступ ко всем операциям
- **Specialist** - доступ к чатам, документам, просмотр проектов
- **User** - текущая функциональность мобильного приложения

### Обновленная схема User:
```json
{
  "id": "string",
  "email": "string",
  "name": "string",
  "phone": "string",
  "role": "user | specialist | admin",
  "permissions": ["read:projects", "write:documents", ...]
}
```

---

## 📦 Загрузка файлов

Рекомендуется добавить единый эндпойнт для загрузки файлов:

```http
POST /api/v1/upload
Content-Type: multipart/form-data

file: <binary>
type: "image" | "document"

Response: 200 OK
{
  "url": "string",
  "filename": "string",
  "size": number,
  "type": "string"
}
```

---

## 🔄 Пагинация

Для списков рекомендуется добавить пагинацию:

```http
GET /api/v1/projects?page=1&limit=20&sort=createdAt&order=desc

Response: 200 OK
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

---

## 🔍 Фильтрация и поиск

Добавить query parameters для фильтрации:

```http
GET /api/v1/projects?status=available&minPrice=1000000&maxPrice=5000000
GET /api/v1/documents?status=pending&projectId=xxx
GET /api/v1/chats?isActive=true&hasUnread=true
```

---

## 📝 Логирование действий

Рекомендуется добавить audit log для отслеживания всех действий администраторов:

```http
GET /api/v1/admin/audit-logs

Response: 200 OK
[
  {
    "id": "string",
    "userId": "string",
    "action": "create | update | delete | approve | reject",
    "resource": "project | document | chat | camera",
    "resourceId": "string",
    "timestamp": "string",
    "details": {...}
  }
]
```

---

## 🚀 Приоритеты внедрения

### Фаза 1 (Критически важно):
1. ✅ CRUD для проектов
2. ✅ CRUD для документов
3. ✅ Загрузка файлов (изображения, документы)
4. ✅ RBAC (роли и права)

### Фаза 2 (Важно):
5. ✅ CRUD для строительных площадок
6. ✅ CRUD для камер
7. ✅ CRUD для чатов
8. ✅ Статистика для дашборда

### Фаза 3 (Хорошо иметь):
9. ✅ Пагинация
10. ✅ Фильтрация и поиск
11. ✅ Управление пользователями
12. ✅ Audit logs

---

## 🛠️ Технические рекомендации

1. **Валидация**: Используйте Pydantic для валидации всех входящих данных
2. **Ошибки**: Возвращайте подробные error messages с HTTP статусами
3. **Документация**: Обновите OpenAPI/Swagger документацию
4. **Тесты**: Покройте новые эндпойнты unit и integration тестами
5. **Миграции**: Создайте Alembic миграции для изменений БД
6. **Права**: Проверяйте права доступа на уровне роутов

---

## 📖 Пример реализации

### FastAPI роут для создания проекта:

```python
@router.post("/projects", response_model=ProjectResponse, status_code=status.HTTP_201_CREATED)
async def create_project(
    project: ProjectCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_admin)  # Только админы
):
    """Создание нового проекта"""
    new_project = Project(**project.dict())
    db.add(new_project)
    db.commit()
    db.refresh(new_project)
    return new_project
```

---

## 🎯 Итоги

После внедрения всех улучшений, админ-панель сможет:
- ✅ **Полностью управлять** проектами без доступа к БД
- ✅ **Создавать и редактировать** все сущности
- ✅ **Загружать файлы** через интерфейс
- ✅ **Мониторить статистику** в реальном времени
- ✅ **Управлять пользователями** и правами доступа
- ✅ **Отслеживать действия** через audit logs

Это сделает систему **полностью самодостаточной** и готовой к продакшн использованию! 🚀

