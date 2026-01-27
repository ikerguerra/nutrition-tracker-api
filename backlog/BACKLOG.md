# 📋 Nutrition Tracker - Product Backlog

## 📊 Estado Actual del Proyecto

### ✅ Funcionalidades Implementadas

#### Backend (Spring Boot 3.2 + MySQL)
- ✅ **Módulo de Alimentos (Foods)**
  - CRUD completo de alimentos con información nutricional
  - Búsqueda por nombre, marca y código de barras
  - Integración con Open Food Facts API para escaneo de códigos de barras
  - Paginación y ordenamiento
  - Validación de datos con Jakarta Bean Validation
  - Documentación OpenAPI/Swagger

- ✅ **Módulo de Registro Diario (Daily Log)**
  - Entidades: `DailyLog` y `MealEntry`
  - CRUD de registros diarios por fecha
  - Gestión de entradas de comidas por tipo (BREAKFAST, LUNCH, DINNER, SNACK)
  - Cálculo automático de macronutrientes totales
  - Snapshot de valores nutricionales calculados según cantidad

- ✅ **Infraestructura**
  - Migraciones de base de datos con Flyway
  - Manejo global de excepciones
  - Configuración CORS para frontend
  - Perfiles de configuración (dev/prod)

#### Frontend (React + TypeScript + Vite)
- ✅ **Dashboard de Registro Diario**
  - Visualización de comidas por tipo (Desayuno, Almuerzo, Cena, Snacks)
  - Resumen de calorías y macronutrientes totales
  - Edición y eliminación de entradas

- ✅ **Biblioteca de Alimentos**
  - Listado de alimentos con paginación
  - Búsqueda de alimentos
  - Creación y edición de alimentos
  - Eliminación de alimentos
  - Agregar alimentos al registro diario

- ✅ **Escaneo de Códigos de Barras**
  - Modal para escaneo de códigos
  - Integración con backend para búsqueda

- ✅ **Componentes UI**
  - Sistema de diseño básico (Button, Card, Input, Modal, LoadingSpinner)
  - Layout con header
  - Notificaciones con react-hot-toast

---

## 🎯 Backlog de Funcionalidades

### 🔐 EPIC 1: Sistema de Autenticación y Usuarios

> **Prioridad:** ALTA  
> **Objetivo:** Implementar un sistema completo de autenticación para asociar datos a usuarios específicos

#### Backend Tasks

- [x] **US-1.1: Crear entidad User** ✅
  - Campos: id, email, password (hash), firstName, lastName, createdAt, updatedAt
  - Validaciones de email único
  - Relación con DailyLog (userId)
  
- [x] **US-1.2: Implementar Spring Security con JWT** ✅
  - Configuración de Spring Security
  - Generación y validación de tokens JWT
  - Filtros de autenticación
  - Endpoints: `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/auth/refresh`

- [x] **US-1.3: Crear UserProfile entity** ✅
  - Campos: userId, height (cm), weight (kg), dateOfBirth, gender, activityLevel
  - Relación OneToOne con User
  - Endpoint CRUD para perfil de usuario

- [x] **US-1.4: Implementar sistema de objetivos nutricionales** ✅
  - Enum: NutritionalGoal (LOSE_WEIGHT, MAINTAIN, GAIN_MUSCLE, GAIN_WEIGHT)
  - Campos en UserProfile: goal, targetWeight, targetDate
  - Cálculo automático de calorías diarias según objetivo

- [x] **US-1.5: Implementar cálculo de macronutrientes** ✅
  - Servicio para calcular macros según objetivo y perfil
  - Fórmulas: TMB (Tasa Metabólica Basal), TDEE (Total Daily Energy Expenditure)
  - Distribución de macros por defecto según objetivo

- [x] **US-1.6: Permitir personalización de macronutrientes** ✅
  - Campos en UserProfile: customProteinPercentage, customCarbsPercentage, customFatsPercentage
  - Flag: useCustomMacros (boolean)
  - Validación: suma de porcentajes = 100%

- [x] **US-1.6a: Implementar tipos de dieta específicos** ✅
  - Enum: DietType (STANDARD, KETOGENIC, VEGAN, VEGETARIAN, PALEO, MEDITERRANEAN, LOW_CARB, HIGH_PROTEIN)
  - Campo en UserProfile: dietType
  - Macros predefinidos por tipo de dieta
  - Restricciones de alimentos según tipo de dieta (ej: vegana excluye productos animales)

- [x] **US-1.6b: Crear entidad MacroPreset** ✅
  - Campos: id, userId, name, proteinPercentage, carbsPercentage, fatsPercentage, isDefault
  - Endpoints CRUD para presets
  - Validación de porcentajes (suma = 100%)
  - Aplicar preset al perfil de usuario

- [x] **US-1.6c: Implementar sistema de unidades** ✅
  - Enum: UnitSystem (METRIC, IMPERIAL)
  - Campo en UserProfile: preferredUnitSystem
  - Conversión automática en endpoints (kg ↔ lb, cm ↔ in)
  - Almacenar siempre en métrico en BD, convertir en presentación

- [ ] **US-1.7: Actualizar DailyLog para asociar con User**
  - Modificar constraint UNIQUE (userId, date)
  - Añadir campos: dailyCalorieGoal, dailyProteinGoal, dailyCarbsGoal, dailyFatsGoal
  - Endpoint para obtener objetivos del día

- [ ] **US-1.8: Implementar recálculo dinámico de objetivos**
  - Trigger o servicio que actualice objetivos futuros al cambiar peso/objetivo
  - Endpoint para recalcular objetivos manualmente
  - Historial de cambios de peso

- [x] **US-1.9: Implementar OAuth2 con Google** ✅
  - Configurar Spring Security OAuth2 Client
  - Endpoint de callback para Google OAuth
  - Crear o vincular usuario existente con cuenta de Google
  - Generar JWT tras autenticación OAuth exitosa

#### Frontend Tasks

- [x] **US-1.11: Crear páginas de autenticación** ✅
  - Página de Login con email/password
  - Página de Registro
  - Página de Recuperación de contraseña
  - Diseño responsive y atractivo

- [x] **US-1.12: Implementar botones de OAuth2** ✅
  - Botón "Continuar con Google" en login/registro
  - Manejo de redirecciones OAuth
  - Loading states durante autenticación OAuth

- [x] **US-1.13: Implementar gestión de tokens** ✅
  - Almacenamiento seguro de JWT (localStorage/sessionStorage)
  - Interceptor de Axios para añadir token a requests
  - Manejo de refresh token
  - Redirección automática al expirar sesión

- [x] **US-1.14: Crear página de Perfil de Usuario** ✅
  - Formulario para datos personales (altura, peso, fecha nacimiento)
  - Selector de objetivo nutricional
  - Selector de tipo de dieta específica (cetogénica, vegana, etc.)
  - Selector de sistema de unidades (métrico/imperial)
  - Visualización de calorías diarias calculadas
  - Visualización de macronutrientes recomendados

- [x] **US-1.15: Implementar editor de macronutrientes personalizados** ✅
  - Sliders o inputs para ajustar % de proteínas, carbohidratos, grasas
  - Visualización en tiempo real de gramos por macro
  - Toggle para usar macros personalizados vs. recomendados
  - Validación de suma = 100%
  - Guardar como preset con nombre personalizado
  - Listado de presets guardados (editar/eliminar)

- [x] **US-1.16: Añadir contexto de autenticación** ✅
  - React Context para usuario autenticado
  - Hook useAuth() para acceder a datos de usuario
  - Rutas protegidas (ProtectedRoute component)

- [ ] **US-1.17: Actualizar Dashboard con objetivos diarios**
  - Mostrar objetivos de calorías y macros del día
  - Barras de progreso (consumido vs. objetivo)
  - Indicadores visuales (verde/rojo) según cumplimiento

---

### 📅 EPIC 2: Navegación Temporal y Consulta de Registros

> **Prioridad:** ALTA  
> **Objetivo:** Permitir consultar registros de días anteriores con navegación tipo carrusel

#### Backend Tasks

- [ ] **US-2.1: Endpoint para obtener DailyLog por rango de fechas**
  - GET `/api/v1/daily-logs?startDate={date}&endDate={date}`
  - Filtrado por userId del token
  - Ordenamiento por fecha

- [x] **US-2.2: Endpoint para obtener DailyLog de un día específico** ✅
  - GET `/api/v1/daily-logs/{date}`
  - Crear log vacío si no existe para esa fecha
  - Incluir objetivos del día

#### Frontend Tasks

- [x] **US-2.3: Implementar selector de fecha en Dashboard** ✅
  - Botones de navegación: Anterior / Hoy / Siguiente
  - Date picker para selección directa
  - Animación de transición tipo carrusel al cambiar fecha

- [x] **US-2.4: Crear hook useDateNavigation** ✅
  - Estado para fecha seleccionada
  - Funciones: goToPreviousDay, goToNextDay, goToToday, selectDate
  - Integración con useDailyLog

- [x] **US-2.5: Implementar animaciones de carrusel** ✅
  - Usar framer-motion para transiciones suaves
  - Animación slide left/right al cambiar día
  - Loading state durante carga de datos

- [x] **US-2.6: Añadir indicador de fecha actual** ✅
  - Mostrar fecha seleccionada de forma prominente
  - Indicador visual si es día actual vs. día pasado
  - Formato de fecha localizado (español)

---

### ⭐ EPIC 3: Gestión de Alimentos Favoritos y Frecuentes

> **Prioridad:** MEDIA  
> **Objetivo:** Facilitar el registro de alimentos mediante favoritos, frecuentes y recientes

#### Backend Tasks

- [x] **US-3.1: Crear entidad FavoriteFood** ✅
  - Campos: id, userId, foodId, createdAt
  - Constraint UNIQUE (userId, foodId)
  - Endpoints CRUD para favoritos

- [x] **US-3.2: Implementar cálculo de alimentos frecuentes** ✅
  - Query para obtener top N alimentos más consumidos por usuario
  - Basado en count de MealEntry agrupado por foodId
  - Endpoint: GET `/api/v1/foods/frequent?limit=10`

- [x] **US-3.3: Implementar listado de alimentos recientes** ✅
  - Query para obtener alimentos consumidos en últimos 14 días
  - Ordenados por fecha de última consumición
  - Endpoint: GET `/api/v1/foods/recent?days=14`

- [x] **US-3.4: Optimizar queries con índices** ✅
  - Índice en MealEntry (userId, createdAt)
  - Índice en FavoriteFood (userId)

#### Frontend Tasks

- [x] **US-3.5: Crear sección de Favoritos en biblioteca** ✅
  - Tab o sección separada para alimentos favoritos
  - Botón para marcar/desmarcar como favorito (icono estrella)
  - Filtro rápido para mostrar solo favoritos

- [x] **US-3.6: Crear sección de Frecuentes** ✅
  - Tab para alimentos más consumidos
  - Badge con número de veces consumido
  - Ordenamiento por frecuencia

- [x] **US-3.7: Crear sección de Recientes** ✅
  - Tab para alimentos consumidos recientemente
  - Mostrar última fecha de consumo
  - Agrupación por día (últimos 14 días)

- [x] **US-3.8: Implementar buscador unificado** ✅
  - Búsqueda en todos los alimentos (biblioteca completa)
  - Búsqueda en favoritos
  - Búsqueda en frecuentes
  - Búsqueda en recientes
  - Debounce para optimizar requests

- [x] **US-3.9: Mejorar UX de AddEntryModal** ✅
  - Mostrar sugerencias de alimentos frecuentes/recientes al abrir
  - Quick add desde favoritos
  - Autocompletado en búsqueda

---

### 🤖 EPIC 4: Sistema de Recomendaciones de Dietas

> **Prioridad:** MEDIA  
> **Objetivo:** Generar recomendaciones diarias de dietas basadas en hábitos alimenticios del usuario

#### Backend Tasks

- [ ] **US-4.1: Crear servicio de análisis de patrones alimenticios**
  - Analizar últimos 30 días de registros
  - Identificar alimentos más consumidos por tipo de comida
  - Calcular promedios de macros por comida

- [ ] **US-4.2: Implementar algoritmo de generación de dietas**
  - Input: objetivos diarios de macros, patrones del usuario
  - Output: sugerencia de alimentos por comida para alcanzar objetivos
  - Optimización para minimizar diferencia con objetivos
  - **Límite de variedad**: No repetir el mismo alimento en diferentes comidas del mismo día
  - Parámetro configurable: maxRepetitionsPerDay (default: 1)

- [ ] **US-4.3: Crear entidad DietRecommendation**
  - Campos: id, userId, date, mealType, foodId, suggestedQuantity, reason
  - Endpoint: GET `/api/v1/recommendations/{date}`

- [ ] **US-4.4: Implementar endpoint para generar recomendación**
  - POST `/api/v1/recommendations/generate?date={date}`
  - Validar que no exista ya un log completo para ese día
  - Guardar recomendaciones generadas

- [ ] **US-4.5: Añadir feedback de recomendaciones**
  - Campo: accepted (boolean) en DietRecommendation
  - Usar feedback para mejorar futuras recomendaciones
  - Endpoint para marcar recomendación como aceptada/rechazada

#### Frontend Tasks

- [ ] **US-4.6: Crear página de Recomendaciones**
  - Botón "Generar dieta del día" en Dashboard
  - Visualización de recomendaciones por comida
  - Mostrar macros totales de la dieta recomendada

- [ ] **US-4.7: Implementar preview de recomendación**
  - Modal con preview de dieta completa
  - Comparación: macros recomendados vs. objetivos
  - Botones: Aceptar todo / Aceptar parcial / Rechazar

- [ ] **US-4.8: Añadir funcionalidad de aceptar recomendaciones**
  - Botón para añadir todas las recomendaciones al log
  - Botón para añadir recomendaciones individuales
  - Confirmación antes de añadir

- [ ] **US-4.9: Mostrar razón de recomendación**
  - Tooltip o badge explicando por qué se recomienda ese alimento
  - Ej: "Consumes este alimento frecuentemente en el desayuno"

---

### 🎨 EPIC 5: Rediseño Completo del Frontend

> **Prioridad:** BAJA (para más adelante)  
> **Objetivo:** Modernizar y mejorar la experiencia visual de la aplicación

#### Frontend Tasks

- [ ] **US-5.1: Definir sistema de diseño**
  - Paleta de colores
  - Tipografía
  - Espaciado y grid system
  - Componentes base

- [ ] **US-5.2: Rediseñar componentes UI base**
  - Button, Input, Card, Modal
  - Select, Checkbox, Radio
  - Tabs, Accordion, Dropdown

- [ ] **US-5.3: Rediseñar Dashboard**
  - Layout moderno con cards
  - Gráficos visuales para macros
  - Animaciones y transiciones

- [ ] **US-5.4: Rediseñar biblioteca de alimentos**
  - Vista de grid/lista
  - Filtros avanzados
  - Diseño responsive

- [ ] **US-5.5: Implementar tema oscuro/claro**
  - Toggle de tema
  - Persistencia de preferencia
  - Variables CSS para temas

- [ ] **US-5.6: Mejorar responsive design**
  - Optimización para móviles
  - Optimización para tablets
  - PWA enhancements

---

### 📆 EPIC 6: Calendario de Cumplimiento

> **Prioridad:** MEDIA  
> **Objetivo:** Visualizar el cumplimiento de objetivos calóricos en un calendario

#### Backend Tasks

- [x] **US-6.1: Endpoint para obtener resumen mensual** ✅
  - GET `/api/v1/daily-logs/summary?month={month}&year={year}`
  - Retornar array con: date, totalCalories, calorieGoal, achieved (boolean), weight
  - Optimizar query para evitar N+1

- [x] **US-6.2: Implementar lógica de cumplimiento** ✅
  - Calcular si se alcanzó objetivo (margen de tolerancia ±5%)
  - Campo: goalAchieved en DailyLog
  - Actualizar al modificar entradas

- [x] **US-6.2a: Añadir registro de peso diario** ✅
  - Campo en DailyLog: dailyWeight (opcional)
  - Endpoint para actualizar peso del día: PATCH `/api/v1/daily-logs/{date}/weight`
  - Actualizar peso en UserProfile si es el registro más reciente
  - Historial de peso para gráficos de tendencia

#### Frontend Tasks

- [x] **US-6.3: Crear componente Calendar** ✅
  - Vista mensual con grid de días
  - Navegación entre meses
  - Responsive design

- [x] **US-6.4: Implementar indicadores visuales** ✅
  - Icono/color verde: objetivo alcanzado
  - Icono/color rojo: objetivo no alcanzado
  - Gris: sin datos para ese día

- [x] **US-6.5: Añadir interactividad al calendario** ✅
  - Click en día para navegar a ese registro
  - Tooltip con resumen al hacer hover
  - Mostrar calorías consumidas vs. objetivo
  - **Mostrar peso registrado** en cada día (si existe)

- [x] **US-6.5a: Implementar input de peso en Dashboard** ✅
  - Campo para registrar peso del día actual
  - Mostrar peso anterior para referencia
  - Conversión automática según sistema de unidades preferido
  - Gráfico de tendencia de peso (últimos 30 días)

- [x] **US-6.6: Crear página de Calendario** ✅
  - Ruta: `/calendar`
  - Integración con navegación principal
  - Estadísticas del mes (días cumplidos, racha, etc.)

---

### 📊 EPIC 7: Desglose de Macronutrientes

> **Prioridad:** MEDIA  
> **Objetivo:** Visualizar desglose detallado de macronutrientes por alimento y comida

#### Backend Tasks

- [ ] **US-7.1: Endpoint para desglose por comida**
  - GET `/api/v1/daily-logs/{date}/breakdown`
  - Retornar macros agrupados por mealType
  - Incluir porcentaje de cada macro respecto al total

- [ ] **US-7.2: Añadir micronutrientes al desglose**
  - Incluir fibra, azúcares, grasas saturadas
  - Incluir vitaminas y minerales
  - Sumar totales por día

- [ ] **US-7.2a: Implementar sistema de RDA (Recommended Daily Allowance)**
  - Tabla de referencia con RDA por micronutriente
  - Ajustar RDA según edad, género, peso del usuario
  - Endpoint: GET `/api/v1/nutrition/rda` (retorna RDA personalizado)
  - Cálculo de % de RDA alcanzado por día

#### Frontend Tasks

- [ ] **US-7.3: Crear página de Desglose Nutricional**
  - Ruta: `/nutrition-breakdown`
  - Selector de fecha
  - Tabs: Por Comida / Por Alimento / Por Macro

- [ ] **US-7.4: Implementar vista "Por Comida"**
  - Tabla o cards con macros por tipo de comida
  - Gráfico de barras apiladas
  - Porcentaje de contribución al total

- [ ] **US-7.5: Implementar vista "Por Alimento"**
  - Listado de todos los alimentos del día
  - Macros de cada alimento
  - Ordenamiento por calorías/proteínas/etc.

- [ ] **US-7.6: Implementar vista "Por Macro"**
  - Gráfico de pastel para distribución de macros
  - Comparación con objetivos
  - Desglose de fuentes principales de cada macro

- [ ] **US-7.7: Añadir visualización de micronutrientes**
  - Sección expandible con micronutrientes
  - **Barras de progreso con % de RDA alcanzado**
  - Código de colores: verde (>100% RDA), amarillo (50-100%), rojo (<50%)
  - Alertas si hay deficiencias críticas (<30% RDA)
  - Tooltip con información de RDA personalizado según perfil

---

---

### 🌍 EPIC 8: Internacionalización (i18n)

> **Prioridad:** MEDIA  
> **Objetivo:** Soportar múltiples idiomas (español e inglés inicialmente)

#### Backend Tasks

- [ ] **US-8.1: Configurar i18n en Spring Boot**
  - Añadir dependencia spring-boot-starter-validation con MessageSource
  - Crear archivos messages_es.properties y messages_en.properties
  - Configurar LocaleResolver basado en header Accept-Language
  - Internacionalizar mensajes de validación y errores

- [ ] **US-8.2: Internacionalizar respuestas de API**
  - Traducir mensajes de error
  - Traducir mensajes de éxito
  - Traducir descripciones de enums (MealType, NutritionalGoal, etc.)

- [ ] **US-8.3: Soporte de idioma en User**
  - Campo en User: preferredLanguage (default: 'es')
  - Endpoint para cambiar idioma: PATCH `/api/v1/users/me/language`
  - Usar idioma preferido en emails y notificaciones

#### Frontend Tasks

- [ ] **US-8.4: Configurar react-i18next**
  - Instalar i18next y react-i18next
  - Crear archivos de traducción: es.json, en.json
  - Configurar detección automática de idioma del navegador
  - Provider de i18n en App.tsx

- [ ] **US-8.5: Traducir toda la interfaz**
  - Traducir labels, botones, mensajes
  - Traducir placeholders y tooltips
  - Traducir mensajes de error y validación
  - Traducir nombres de comidas y objetivos

- [ ] **US-8.6: Implementar selector de idioma**
  - Dropdown en header o perfil
  - Persistir preferencia en localStorage
  - Sincronizar con backend al cambiar
  - Recargar traducciones dinámicamente

- [ ] **US-8.7: Localización de fechas y números**
  - Formatear fechas según locale (DD/MM/YYYY vs MM/DD/YYYY)
  - Formatear números (coma vs punto decimal)
  - Formatear unidades según locale

---

## 🚀 Propuestas Adicionales de Innovación

### 💡 EPIC 9: Funcionalidades Avanzadas (Propuestas)

#### Análisis y Estadísticas

- [ ] **US-9.1: Dashboard de estadísticas semanales/mensuales**
  - Gráficos de tendencias de peso
  - Gráficos de consumo calórico promedio
  - Análisis de cumplimiento de objetivos
  - Comparación semanal/mensual de macros

- [ ] **US-9.2: Reportes exportables**
  - Exportar datos a PDF con gráficos
  - Exportar datos a CSV/Excel
  - Compartir reportes por email
  - Generar informe nutricional mensual

#### Mejoras de Usabilidad - Plantillas de Comidas ⭐

- [ ] **US-9.3: Backend - Entidad MealTemplate**
  - Campos: id, userId, name, description, mealType, isPublic
  - Tabla intermedia: MealTemplateFood (templateId, foodId, quantity, unit)
  - Endpoints CRUD para plantillas
  - Endpoint para aplicar plantilla: POST `/api/v1/meal-templates/{id}/apply?date={date}`

- [ ] **US-9.4: Frontend - Gestión de plantillas**
  - Página de plantillas guardadas
  - Botón "Guardar como plantilla" en cada comida
  - Modal para nombrar y describir plantilla
  - Quick add desde plantillas en Dashboard

- [ ] **US-9.5: Plantillas predefinidas del sistema**
  - Crear plantillas comunes (Desayuno proteico, Cena ligera, etc.)
  - Filtrar plantillas por tipo de dieta
  - Marketplace de plantillas compartidas por usuarios

- [ ] **US-9.6: Copiar día completo**
  - Endpoint: POST `/api/v1/daily-logs/{sourceDate}/copy?targetDate={date}`
  - Botón "Copiar día" en Dashboard
  - Confirmación antes de sobrescribir día existente
  - Útil para rutinas repetitivas

- [ ] **US-9.7: Modo offline (PWA)**
  - Service Worker para PWA
  - Sincronización cuando vuelva conexión
  - Cache de alimentos frecuentes
  - Indicador de estado online/offline

#### Gamificación ⭐

- [ ] **US-9.8: Backend - Sistema de logros**
  - Entidad Achievement: id, userId, type, unlockedAt, progress
  - Tipos de logros: STREAK_7, STREAK_30, STREAK_100, FIRST_GOAL, WEIGHT_MILESTONE
  - Endpoint para obtener logros: GET `/api/v1/achievements`
  - Cálculo automático de rachas

- [ ] **US-9.9: Frontend - Visualización de logros**
  - Página de logros y badges
  - Animación al desbloquear nuevo logro
  - Barra de progreso hacia próximo logro
  - Compartir logros en redes sociales

- [ ] **US-9.10: Sistema de niveles**
  - Calcular nivel según días activos y cumplimiento
  - XP por acciones (registrar comida, cumplir objetivo, racha)
  - Mostrar nivel en perfil
  - Recompensas por subir de nivel

- [ ] **US-9.11: Recordatorios y notificaciones**
  - Backend: Servicio de notificaciones programadas
  - Recordatorio para registrar comidas (configurable por usuario)
  - Notificación si falta registrar cena
  - Push notifications (PWA)
  - Preferencias de notificaciones en perfil

#### Integraciones

- [ ] **US-9.12: Integración con Google Fit**
  - OAuth2 con Google Fit API
  - Importar calorías quemadas del día
  - Ajustar objetivos según actividad física
  - Sincronización automática diaria

- [ ] **US-9.13: Integración con Apple Health**
  - HealthKit integration (iOS PWA)
  - Importar datos de actividad
  - Exportar datos nutricionales a Health app

#### Sistema de Recetas ⭐

- [ ] **US-9.14: Backend - Entidad Recipe**
  - Campos: id, userId, name, description, servings, prepTime, cookTime, instructions, imageUrl, isPublic
  - Tabla intermedia: RecipeIngredient (recipeId, foodId, quantity, unit)
  - Cálculo automático de macros totales y por porción
  - Endpoints CRUD para recetas

- [ ] **US-9.15: Backend - Compartir recetas**
  - Endpoint para publicar receta: POST `/api/v1/recipes/{id}/publish`
  - Endpoint para buscar recetas públicas: GET `/api/v1/recipes/public?query={query}`
  - Sistema de likes y favoritos en recetas
  - Comentarios en recetas

- [ ] **US-9.16: Frontend - Crear y editar recetas**
  - Página de creación de recetas
  - Editor de ingredientes (buscar y añadir alimentos)
  - Editor de instrucciones paso a paso
  - Upload de imagen de receta
  - Preview de macros totales y por porción

- [ ] **US-9.17: Frontend - Explorar recetas**
  - Página de exploración de recetas públicas
  - Filtros por tipo de dieta, macros, tiempo de preparación
  - Añadir receta completa al daily log
  - Guardar recetas favoritas

#### Optimizaciones Técnicas

- [ ] **US-9.18: Implementar caché con Redis**
  - Configurar Redis en backend
  - Cachear alimentos frecuentes por usuario
  - Cachear cálculos de macros
  - Cache de recetas públicas
  - TTL configurables por tipo de dato

- [ ] **US-9.19: Búsqueda full-text con Elasticsearch ⭐**
  - Configurar Elasticsearch
  - Indexar alimentos con información nutricional
  - Indexar recetas con ingredientes
  - Búsqueda fuzzy (tolerante a errores tipográficos)
  - Búsqueda por ingredientes
  - Sugerencias de autocompletado
  - Filtros avanzados (rango de calorías, macros, etc.)

- [ ] **US-9.20: Tests automatizados ⭐**
  - **Backend**: Tests unitarios con JUnit 5 y Mockito
  - **Backend**: Tests de integración con @SpringBootTest
  - **Backend**: Tests de repositorio con @DataJpaTest
  - **Backend**: Tests de API con MockMvc
  - **Frontend**: Tests unitarios con Vitest
  - **Frontend**: Tests de componentes con React Testing Library
  - **Frontend**: Tests E2E con Playwright
  - Cobertura mínima: 80%

- [ ] **US-9.21: CI/CD Pipeline ⭐**
  - GitHub Actions workflow para backend
  - GitHub Actions workflow para frontend
  - Build automático en cada push
  - Ejecución de tests en cada PR
  - Deploy automático a staging en merge a develop
  - Deploy a producción en merge a main (con aprobación manual)
  - Notificaciones de Slack/Discord en fallos

- [ ] **US-9.22: Monitoreo y logging**
  - Integrar Spring Boot Actuator
  - Métricas con Micrometer
  - Logging centralizado con ELK Stack
  - Alertas de errores con Sentry
  - Dashboard de métricas con Grafana

---

## 📝 Notas Importantes

### Buenas Prácticas a Seguir

#### Backend
- ✅ Usar DTOs para separar capa de presentación de entidades
- ✅ Implementar validaciones con Bean Validation
- ✅ Usar transacciones (@Transactional) apropiadamente
- ✅ Logging consistente con SLF4J
- ✅ Manejo de excepciones centralizado
- ✅ Documentar endpoints con OpenAPI
- ✅ Versionado de API (v1, v2, etc.)
- ✅ Migraciones de BD con Flyway (nunca modificar migraciones existentes)

#### Frontend
- ✅ Componentes reutilizables y modulares
- ✅ Custom hooks para lógica compartida
- ✅ TypeScript estricto (evitar `any`)
- ✅ Manejo de errores consistente
- ✅ Loading states en todas las operaciones async
- ✅ Optimistic updates donde sea apropiado
- ✅ Lazy loading de componentes pesados
- ✅ Memoización con useMemo/useCallback cuando sea necesario

### Escalabilidad

#### Backend
- Usar paginación en todos los listados
- Implementar índices en columnas frecuentemente consultadas
- Considerar particionado de tablas para datos históricos
- Implementar rate limiting en endpoints públicos
- Preparar para multi-tenancy (separación por userId)

#### Frontend
- Code splitting por rutas
- Virtualización para listas largas
- Debounce en búsquedas
- Infinite scroll en lugar de paginación tradicional
- Optimización de bundle size

---

## 🔄 Proceso de Actualización del Backlog

Este backlog es un documento vivo que se actualizará continuamente:

1. **Nuevas funcionalidades**: Se añadirán como nuevos EPICs o User Stories
2. **Priorización**: Se ajustará según necesidades del negocio
3. **Refinamiento**: Las historias se detallarán más antes de implementación
4. **Seguimiento**: Se marcarán como completadas con ✅ al finalizar

### Formato para Nuevas User Stories

```markdown
- [ ] **US-X.Y: Título descriptivo**
  - Descripción de la funcionalidad
  - Criterios de aceptación
  - Notas técnicas si aplica
```

---

## ✅ Decisiones Confirmadas

1. **Autenticación**: ✅ OAuth2 (Google/Facebook) además de email/password
2. **Objetivos nutricionales**: ✅ Incluir objetivos específicos como "Dieta cetogénica" o "Dieta vegana"
3. **Macronutrientes**: ✅ Los porcentajes personalizados pueden guardarse como presets
4. **Recomendaciones**: ✅ Implementar límite de variedad (no repetir mismo alimento en el día)
5. **Calendario**: ✅ Mostrar también el peso registrado cada día en el calendario
6. **Desglose**: ✅ Comparación con RDA (Recommended Daily Allowance) de micronutrientes
7. **Idioma**: ✅ Soporte multiidioma (español, inglés inicialmente)
8. **Unidades**: ✅ Soportar sistema imperial (lb, oz) además de métrico (kg, g)
