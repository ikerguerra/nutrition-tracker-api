# API Documentation

## Base URL

```
http://localhost:8080/api/v1
```

## Autenticación

Todos los endpoints (excepto `/auth/**`) requieren JWT token en el header:

```
Authorization: Bearer <access_token>
```

---

## Auth Endpoints

### Register

Crear nueva cuenta de usuario.

**Endpoint:** `POST /auth/register`

**Request:**
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Login

Autenticar usuario existente.

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "email": "juan@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Refresh Token

Obtener nuevo access token usando refresh token.

**Endpoint:** `POST /auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

---

## User Profile Endpoints

### Get My Profile

Obtener perfil del usuario autenticado.

**Endpoint:** `GET /profile`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "height": 175.0,
  "weight": 75.0,
  "dateOfBirth": "1990-01-01",
  "age": 35,
  "gender": "MALE",
  "nutritionalGoal": "LOSE_WEIGHT",
  "dietType": "STANDARD",
  "activityLevel": "MODERATELY_ACTIVE",
  "preferredUnitSystem": "METRIC",
  "preferredLanguage": "es",
  "dailyCalorieGoal": 2106.0,
  "dailyProteinGoal": 158.0,
  "dailyCarbsGoal": 211.0,
  "dailyFatsGoal": 70.0,
  "useCustomMacros": false
}
```

### Update My Profile

Actualizar perfil del usuario. Los objetivos se recalculan automáticamente.

**Endpoint:** `PUT /profile`

**Headers:**
```
Authorization: Bearer <token>
```

**Request:**
```json
{
  "height": 175.0,
  "weight": 75.0,
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "nutritionalGoal": "LOSE_WEIGHT",
  "activityLevel": "MODERATELY_ACTIVE",
  "dietType": "STANDARD"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "height": 175.0,
  "weight": 75.0,
  "dailyCalorieGoal": 2106.0,
  "dailyProteinGoal": 158.0,
  "dailyCarbsGoal": 211.0,
  "dailyFatsGoal": 70.0
}
```

---

## Food Endpoints

### List Foods

Listar alimentos con paginación y búsqueda.

**Endpoint:** `GET /foods`

**Query Parameters:**
- `page` (optional): Número de página (default: 0)
- `size` (optional): Tamaño de página (default: 20)
- `search` (optional): Búsqueda por nombre

**Example:**
```
GET /foods?page=0&size=10&search=pollo
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Pechuga de Pollo",
      "brand": "Genérico",
      "servingSize": 100.0,
      "servingUnit": "g",
      "calories": 165.0,
      "protein": 31.0,
      "carbs": 0.0,
      "fats": 3.6
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### Get Food by ID

Obtener detalles de un alimento.

**Endpoint:** `GET /foods/{id}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Pechuga de Pollo",
  "brand": "Genérico",
  "servingSize": 100.0,
  "servingUnit": "g",
  "calories": 165.0,
  "protein": 31.0,
  "carbs": 0.0,
  "fats": 3.6,
  "fiber": 0.0,
  "sugar": 0.0,
  "sodium": 74.0
}
```

### Create Food

Crear nuevo alimento personalizado.

**Endpoint:** `POST /foods`

**Request:**
```json
{
  "name": "Mi Proteína Custom",
  "brand": "Marca X",
  "servingSize": 30.0,
  "servingUnit": "g",
  "calories": 120.0,
  "protein": 25.0,
  "carbs": 2.0,
  "fats": 1.5
}
```

**Response:** `201 Created`
```json
{
  "id": 42,
  "name": "Mi Proteína Custom",
  "brand": "Marca X",
  "servingSize": 30.0,
  "servingUnit": "g",
  "calories": 120.0,
  "protein": 25.0,
  "carbs": 2.0,
  "fats": 1.5
}
```

### Update Food

Actualizar alimento existente.

**Endpoint:** `PUT /foods/{id}`

**Request:**
```json
{
  "name": "Proteína Actualizada",
  "calories": 125.0
}
```

**Response:** `200 OK`

### Delete Food

Eliminar alimento.

**Endpoint:** `DELETE /foods/{id}`

**Response:** `204 No Content`

---

## Daily Log Endpoints

### Get Today's Log

Obtener log del día actual.

**Endpoint:** `GET /daily-logs/today`

**Response:** `200 OK`
```json
{
  "id": 1,
  "date": "2025-12-04",
  "totalCalories": 1850.0,
  "totalProtein": 145.0,
  "totalCarbs": 180.0,
  "totalFats": 62.0,
  "entries": [
    {
      "id": 1,
      "mealType": "BREAKFAST",
      "food": {
        "id": 1,
        "name": "Avena"
      },
      "quantity": 50.0,
      "calories": 190.0,
      "protein": 7.0,
      "carbs": 34.0,
      "fats": 3.5
    }
  ]
}
```

### Get Log by Date

Obtener log de una fecha específica.

**Endpoint:** `GET /daily-logs/{date}`

**Example:**
```
GET /daily-logs/2025-12-03
```

**Response:** `200 OK` (mismo formato que today)

### Add Meal Entry

Agregar entrada de comida al log.

**Endpoint:** `POST /daily-logs/entries`

**Request:**
```json
{
  "date": "2025-12-04",
  "mealType": "LUNCH",
  "foodId": 1,
  "quantity": 150.0
}
```

**Response:** `201 Created`
```json
{
  "id": 5,
  "mealType": "LUNCH",
  "food": {
    "id": 1,
    "name": "Pechuga de Pollo"
  },
  "quantity": 150.0,
  "calories": 247.5,
  "protein": 46.5,
  "carbs": 0.0,
  "fats": 5.4
}
```

### Delete Meal Entry

Eliminar entrada de comida.

**Endpoint:** `DELETE /daily-logs/entries/{id}`

**Response:** `204 No Content`

---

## External Food Endpoints

### Search OpenFoodFacts

Buscar productos en OpenFoodFacts.

**Endpoint:** `GET /external/foods/search`

**Query Parameters:**
- `q`: Término de búsqueda
- `page` (optional): Número de página
- `size` (optional): Tamaño de página

**Example:**
```
GET /external/foods/search?q=coca+cola&page=0&size=10
```

**Response:** `200 OK`
```json
{
  "products": [
    {
      "code": "5449000000996",
      "name": "Coca-Cola",
      "brand": "Coca-Cola",
      "imageUrl": "https://...",
      "servingSize": 100.0,
      "servingUnit": "ml",
      "calories": 42.0,
      "protein": 0.0,
      "carbs": 10.6,
      "fats": 0.0
    }
  ],
  "page": 0,
  "totalPages": 5
}
```

### Import from OpenFoodFacts

Importar producto de OpenFoodFacts a la base de datos local.

**Endpoint:** `POST /external/foods/{barcode}/import`

**Example:**
```
POST /external/foods/5449000000996/import
```

**Response:** `201 Created`
```json
{
  "id": 43,
  "name": "Coca-Cola",
  "brand": "Coca-Cola",
  "barcode": "5449000000996",
  "calories": 42.0,
  "protein": 0.0,
  "carbs": 10.6,
  "fats": 0.0
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-12-04T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "must be a valid email address"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-12-04T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-12-04T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Food with id 999 not found"
}
```

---

## Enums Reference

### Gender
- `MALE`
- `FEMALE`
- `OTHER`

### NutritionalGoal
- `LOSE_WEIGHT` - Déficit calórico
- `MAINTAIN` - Mantener peso
- `GAIN_MUSCLE` - Superávit moderado
- `GAIN_WEIGHT` - Superávit alto

### ActivityLevel
- `SEDENTARY` - Poco o ningún ejercicio
- `LIGHTLY_ACTIVE` - Ejercicio ligero 1-3 días/semana
- `MODERATELY_ACTIVE` - Ejercicio moderado 3-5 días/semana
- `VERY_ACTIVE` - Ejercicio intenso 6-7 días/semana
- `EXTREMELY_ACTIVE` - Ejercicio muy intenso o trabajo físico

### DietType
- `STANDARD` - 30% proteína, 40% carbos, 30% grasas
- `KETOGENIC` - 25% proteína, 5% carbos, 70% grasas
- `HIGH_PROTEIN` - 40% proteína, 30% carbos, 30% grasas
- `LOW_CARB` - 30% proteína, 20% carbos, 50% grasas
- `VEGAN` - Dieta vegana
- `VEGETARIAN` - Dieta vegetariana
- `PALEO` - Dieta paleo

### MealType
- `BREAKFAST` - Desayuno
- `MORNING_SNACK` - Snack matutino
- `LUNCH` - Almuerzo
- `AFTERNOON_SNACK` - Snack vespertino
- `DINNER` - Cena
- `EVENING_SNACK` - Snack nocturno
