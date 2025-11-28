# Nutrition Tracker API

Backend REST API for a nutrition tracking application built with Spring Boot 3.2 and MySQL.

## 🚀 Features

- **CRUD Operations** for food items with complete nutritional information
- **Barcode Scanning** integration with Open Food Facts API
- **Search Functionality** by name, brand, or barcode
- **Pagination & Sorting** for efficient data retrieval
- **Comprehensive Validation** using Jakarta Bean Validation
- **OpenAPI Documentation** (Swagger UI)
- **Database Migrations** with Flyway
- **Exception Handling** with standardized error responses
- **CORS Support** for React frontend integration

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **MySQL Workbench** (optional, for database management)

## 🛠️ Technology Stack

- **Spring Boot 3.2.0**
- **Spring Data JPA** - Database persistence
- **Spring Web** - REST API
- **Spring Validation** - Input validation
- **MySQL** - Relational database
- **Flyway** - Database migrations
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation
- **WebClient** - External API integration

## ⚙️ Configuration

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE nutrition_tracker_db;
```

2. Update database credentials in `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nutrition_tracker_db
    username: your_username
    password: your_password
```

### Environment Profiles

- **Development**: `application-dev.yml` (default)
- **Production**: `application-prod.yml`

To run with a specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🚀 Running the Application

### Using Maven

```bash
# Navigate to project directory
cd nutrition-tracker-api

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Building JAR

```bash
# Build the project
mvn clean package

# Run the JAR
java -jar target/nutrition-tracker-api-1.0.0-SNAPSHOT.jar
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔗 API Endpoints

### Food Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/foods` | Create a new food |
| GET | `/api/v1/foods/{id}` | Get food by ID |
| GET | `/api/v1/foods` | Get all foods (paginated) |
| GET | `/api/v1/foods/search?query={query}` | Search foods by name/brand |
| GET | `/api/v1/foods/barcode/{barcode}` | Search food by barcode |
| PUT | `/api/v1/foods/{id}` | Update food |
| DELETE | `/api/v1/foods/{id}` | Delete food |
| GET | `/api/v1/foods/stats/count` | Get total food count |

### Example Request

**Create Food:**
```json
POST /api/v1/foods
{
  "name": "Chicken Breast",
  "brand": "Organic Farms",
  "barcode": "1234567890123",
  "servingSize": 100,
  "servingUnit": "g",
  "nutritionalInfo": {
    "calories": 165,
    "protein": 31,
    "carbohydrates": 0,
    "fats": 3.6,
    "fiber": 0,
    "sugars": 0,
    "saturatedFats": 1.0,
    "sodium": 74,
    "calcium": 15,
    "iron": 1.0
  }
}
```

## 🗄️ Database Schema

### Tables

**foods**
- `id` - Primary key
- `name` - Food name
- `brand` - Brand name
- `barcode` - Unique barcode
- `serving_size` - Serving size
- `serving_unit` - Unit (g, ml, etc.)
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

**nutritional_info**
- `id` - Primary key
- `food_id` - Foreign key to foods
- Macronutrients: `calories`, `protein`, `carbohydrates`, `fats`, `fiber`, `sugars`, `saturated_fats`
- Micronutrients: `sodium`, `calcium`, `iron`, `potassium`, `vitamin_a`, `vitamin_c`, `vitamin_d`, `vitamin_e`, `vitamin_b12`

## 🏗️ Project Structure

```
nutrition-tracker-api/
├── src/main/java/com/nutritiontracker/
│   ├── NutritionTrackerApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── WebClientConfig.java
│   ├── common/
│   │   ├── dto/
│   │   │   ├── ApiResponse.java
│   │   │   └── ErrorResponse.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── ResourceNotFoundException.java
│   │       ├── ValidationException.java
│   │       └── ExternalApiException.java
│   └── modules/food/
│       ├── controller/
│       │   └── FoodController.java
│       ├── service/
│       │   ├── FoodService.java
│       │   └── BarcodeService.java
│       ├── repository/
│       │   └── FoodRepository.java
│       ├── entity/
│       │   ├── Food.java
│       │   └── NutritionalInfo.java
│       ├── dto/
│       │   ├── FoodRequestDto.java
│       │   ├── FoodResponseDto.java
│       │   └── BarcodeSearchResponseDto.java
│       └── mapper/
│           └── FoodMapper.java
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── db/migration/
        └── V1__initial_schema.sql
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 🔮 Future Enhancements

- **Authentication & Authorization** with Spring Security and JWT
- **User Management** module
- **Diet Planning** with macro calculations
- **Workout Routines** tracking
- **Daily Meal Logging**
- **Analytics & Reports**

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 📧 Contact

For questions or support, contact: support@nutritiontracker.com
