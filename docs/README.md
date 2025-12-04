# Nutrition Tracker API

Sistema backend para seguimiento nutricional personalizado con cálculo automático de objetivos calóricos y macronutrientes.

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17 o superior
- Maven 3.8+
- PostgreSQL 14+
- IDE recomendado: IntelliJ IDEA o VS Code con extensiones Java

### Instalación

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd nutrition-tracker-api
```

2. **Configurar base de datos**
```bash
# Crear base de datos PostgreSQL
createdb nutrition_tracker_db
```

3. **Configurar variables de entorno**
```bash
# Copiar archivo de ejemplo
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# Editar con tus credenciales
# - spring.datasource.url
# - spring.datasource.username
# - spring.datasource.password
# - jwt.secret
```

4. **Ejecutar migraciones**
```bash
mvn flyway:migrate
```

5. **Compilar y ejecutar**
```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Verificar instalación

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

## 📚 Documentación

- [Arquitectura](./ARCHITECTURE.md) - Estructura del proyecto y patrones
- [API](./API.md) - Endpoints y ejemplos de uso
- [Base de Datos](./DATABASE.md) - Esquema y migraciones
- [Autenticación](./AUTHENTICATION.md) - JWT y seguridad
- [APIs Externas](./EXTERNAL_APIS.md) - Integración con OpenFoodFacts
- [Contribuir](./CONTRIBUTING.md) - Guía para desarrolladores

## 🏗️ Stack Tecnológico

- **Framework**: Spring Boot 3.2
- **Seguridad**: Spring Security + JWT
- **Base de Datos**: PostgreSQL + Flyway
- **Documentación**: SpringDoc OpenAPI (Swagger)
- **Build**: Maven
- **Java**: 17 (LTS)

## 📦 Estructura del Proyecto

```
src/main/java/com/nutritiontracker/
├── config/              # Configuración (Security, CORS, etc.)
├── modules/
│   ├── auth/           # Autenticación y usuarios
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── dto/
│   │   └── enums/
│   ├── food/           # Gestión de alimentos
│   ├── dailylog/       # Registro diario
│   └── external/       # APIs externas (OpenFoodFacts)
└── NutritionTrackerApplication.java
```

## 🔑 Características Principales

- ✅ Autenticación JWT con refresh tokens
- ✅ Gestión de perfiles de usuario
- ✅ Cálculo automático de TMB y TDEE
- ✅ Objetivos nutricionales personalizados
- ✅ Distribución de macros por tipo de dieta
- ✅ CRUD de alimentos personalizado
- ✅ Registro diario por tipo de comida
- ✅ Integración con OpenFoodFacts
- ✅ Documentación Swagger interactiva

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

## 🐛 Troubleshooting

### Error: "Port 8080 already in use"
```bash
# Encontrar proceso usando el puerto
lsof -i :8080

# Matar proceso
kill -9 <PID>
```

### Error: "Could not connect to database"
- Verificar que PostgreSQL esté ejecutándose
- Revisar credenciales en `application-dev.yml`
- Verificar que la base de datos exista

### Error: "Flyway migration failed"
```bash
# Limpiar migraciones (CUIDADO: borra datos)
mvn flyway:clean
mvn flyway:migrate
```

## 📝 Licencia

Este proyecto es privado y confidencial.

## 👥 Equipo

- Desarrollador Principal: [Tu Nombre]
- Contacto: [Tu Email]
