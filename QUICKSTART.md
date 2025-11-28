# Quick Start Guide - Nutrition Tracker API

## 🚀 Getting Started in 5 Minutes

### Prerequisites Checklist

- [ ] Java 17 or higher installed
- [ ] Maven 3.8+ installed
- [ ] MySQL 8.0+ running
- [ ] MySQL Workbench (optional)

### Step 1: Verify Java Installation

```bash
java -version
```

Expected output: `java version "17.x.x"` or higher

**If not installed:**
- Download from: https://www.oracle.com/java/technologies/downloads/

### Step 2: Verify Maven Installation

```bash
mvn --version
```

**If not installed:**
1. Download from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH: `C:\Program Files\Apache\maven\bin`

### Step 3: Setup MySQL Database

**Option A: Using MySQL Workbench**
1. Open MySQL Workbench
2. Connect to your MySQL server
3. Run this SQL:
   ```sql
   CREATE DATABASE nutrition_tracker_db;
   ```

**Option B: Using Command Line**
```bash
mysql -u root -p
CREATE DATABASE nutrition_tracker_db;
exit;
```

### Step 4: Configure Database Credentials

Edit `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nutrition_tracker_db
    username: YOUR_USERNAME  # Change this
    password: YOUR_PASSWORD  # Change this
```

### Step 5: Build the Project

**Option A: Using provided script**
```bash
cd nutrition-tracker-api
build.bat
```

**Option B: Using Maven directly**
```bash
cd nutrition-tracker-api
mvn clean compile
```

### Step 6: Run the Application

**Option A: Using provided script**
```bash
run.bat
```

**Option B: Using Maven directly**
```bash
mvn spring-boot:run
```

**Option C: Using your IDE**
- Open project in IntelliJ IDEA / Eclipse / VS Code
- Right-click `NutritionTrackerApplication.java`
- Select "Run"

### Step 7: Verify It's Working

1. **Check console output**:
   - Look for: `Started NutritionTrackerApplication in X seconds`
   - No errors should appear

2. **Open Swagger UI**:
   - Navigate to: http://localhost:8080/swagger-ui.html
   - You should see the API documentation

3. **Test an endpoint**:
   - In Swagger UI, try the `GET /api/v1/foods/stats/count` endpoint
   - Click "Try it out" → "Execute"
   - Should return `0` (no foods yet)

---

## 🎯 Your First API Call

### Create Your First Food

**Using Swagger UI:**
1. Go to http://localhost:8080/swagger-ui.html
2. Find `POST /api/v1/foods`
3. Click "Try it out"
4. Paste this JSON:

```json
{
  "name": "Chicken Breast",
  "brand": "Organic Farms",
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

5. Click "Execute"
6. You should get a `201 Created` response!

**Using curl:**
```bash
curl -X POST http://localhost:8080/api/v1/foods \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Chicken Breast",
    "brand": "Organic Farms",
    "servingSize": 100,
    "servingUnit": "g",
    "nutritionalInfo": {
      "calories": 165,
      "protein": 31,
      "carbohydrates": 0,
      "fats": 3.6
    }
  }'
```

### Get All Foods

```bash
curl http://localhost:8080/api/v1/foods
```

### Search by Barcode

Try scanning a real product barcode:
```bash
curl http://localhost:8080/api/v1/foods/barcode/3017620422003
```

This will search Open Food Facts API and return nutritional information!

---

## 🔧 Troubleshooting

### Issue: "mvn: command not found"

**Solution:**
- Maven is not installed or not in PATH
- Follow Step 2 above to install Maven

### Issue: "Access denied for user"

**Solution:**
- Check MySQL credentials in `application-dev.yml`
- Make sure MySQL server is running

### Issue: "Unknown database 'nutrition_tracker_db'"

**Solution:**
- Run the CREATE DATABASE command from Step 3

### Issue: Port 8080 already in use

**Solution:**
- Change port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

### Issue: Flyway migration fails

**Solution:**
- Drop and recreate the database:
  ```sql
  DROP DATABASE nutrition_tracker_db;
  CREATE DATABASE nutrition_tracker_db;
  ```
- Restart the application

---

## 📱 Connecting Your React Frontend

### CORS is Already Configured

The API accepts requests from:
- `http://localhost:3000` (Create React App)
- `http://localhost:5173` (Vite)

### Example React Fetch

```javascript
// Create a food
const createFood = async (foodData) => {
  const response = await fetch('http://localhost:8080/api/v1/foods', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(foodData),
  });
  
  const result = await response.json();
  return result.data;
};

// Get all foods
const getAllFoods = async () => {
  const response = await fetch('http://localhost:8080/api/v1/foods?page=0&size=20');
  const result = await response.json();
  return result.data;
};

// Search by barcode
const searchByBarcode = async (barcode) => {
  const response = await fetch(`http://localhost:8080/api/v1/foods/barcode/${barcode}`);
  const result = await response.json();
  return result.data;
};
```

---

## 📚 Next Steps

1. **Explore the API**:
   - Use Swagger UI to test all endpoints
   - Try creating, updating, and deleting foods

2. **Check the Database**:
   - Open MySQL Workbench
   - Browse the `foods` and `nutritional_info` tables
   - See how data is stored

3. **Build Your React Frontend**:
   - Use the API endpoints documented in Swagger
   - Implement barcode scanning in your UI
   - Create forms for adding foods

4. **Read the Full Documentation**:
   - See `README.md` for complete API reference
   - Check `walkthrough.md` for architecture details

---

## 🎉 You're All Set!

Your Nutrition Tracker API is now running and ready to use!

- **API Base URL**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

Happy coding! 🚀
