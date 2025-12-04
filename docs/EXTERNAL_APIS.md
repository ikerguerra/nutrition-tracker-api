# External APIs Integration

## OpenFoodFacts API

Integración con la base de datos abierta de productos alimenticios más grande del mundo.

### Overview

- **URL Base**: `https://world.openfoodfacts.org/api/v2`
- **Documentación**: https://openfoodfacts.github.io/openfoodfacts-server/api/
- **Rate Limit**: 100 requests/minuto
- **Autenticación**: No requerida para lectura

### Configuración

```yaml
external:
  openfoodfacts:
    base-url: https://world.openfoodfacts.org/api/v2
    timeout: 5000
```

## OpenFoodFactsClient

Cliente HTTP para comunicación con la API.

```java
@Component
public class OpenFoodFactsClient {
    
    private final WebClient webClient;
    
    public OpenFoodFactsClient(
        @Value("${external.openfoodfacts.base-url}") String baseUrl,
        @Value("${external.openfoodfacts.timeout}") int timeout
    ) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, "NutritionTracker/1.0")
            .build();
    }
    
    public Mono<OpenFoodFactsSearchResponse> searchProducts(
        String query, 
        int page, 
        int size
    ) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("search_terms", query)
                .queryParam("page", page)
                .queryParam("page_size", size)
                .queryParam("fields", "code,product_name,brands,image_url,nutriments")
                .build())
            .retrieve()
            .bodyToMono(OpenFoodFactsSearchResponse.class);
    }
    
    public Mono<OpenFoodFactsProduct> getProductByBarcode(String barcode) {
        return webClient.get()
            .uri("/product/{barcode}", barcode)
            .retrieve()
            .bodyToMono(OpenFoodFactsProductResponse.class)
            .map(response -> response.getProduct());
    }
}
```

## OpenFoodFactsService

Servicio que transforma datos de OpenFoodFacts a nuestro modelo.

```java
@Service
@RequiredArgsConstructor
public class OpenFoodFactsService {
    
    private final OpenFoodFactsClient client;
    private final FoodRepository foodRepository;
    
    public List<FoodDTO> searchProducts(String query, int page, int size) {
        OpenFoodFactsSearchResponse response = client
            .searchProducts(query, page, size)
            .block();
        
        return response.getProducts().stream()
            .map(this::transformToFoodDTO)
            .collect(Collectors.toList());
    }
    
    public Food importProduct(String barcode, Long userId) {
        OpenFoodFactsProduct product = client
            .getProductByBarcode(barcode)
            .block();
        
        // Check if already exists
        Optional<Food> existing = foodRepository.findByBarcode(barcode);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Transform and save
        Food food = transformToFood(product);
        food.setUserId(userId);
        food.setSource("OPENFOODFACTS");
        
        return foodRepository.save(food);
    }
    
    private FoodDTO transformToFoodDTO(OpenFoodFactsProduct product) {
        Nutriments nutriments = product.getNutriments();
        
        return FoodDTO.builder()
            .name(product.getProductName())
            .brand(product.getBrands())
            .barcode(product.getCode())
            .imageUrl(product.getImageUrl())
            .servingSize(100.0) // OpenFoodFacts usa 100g como base
            .servingUnit("g")
            .calories(nutriments.getEnergyKcal100g())
            .protein(nutriments.getProteins100g())
            .carbs(nutriments.getCarbohydrates100g())
            .fats(nutriments.getFat100g())
            .fiber(nutriments.getFiber100g())
            .sugar(nutriments.getSugars100g())
            .sodium(nutriments.getSodium100g())
            .build();
    }
}
```

## DTOs

### OpenFoodFactsProduct

```java
@Data
public class OpenFoodFactsProduct {
    private String code;
    
    @JsonProperty("product_name")
    private String productName;
    
    private String brands;
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    private Nutriments nutriments;
}
```

### Nutriments

```java
@Data
public class Nutriments {
    @JsonProperty("energy-kcal_100g")
    private Double energyKcal100g;
    
    @JsonProperty("proteins_100g")
    private Double proteins100g;
    
    @JsonProperty("carbohydrates_100g")
    private Double carbohydrates100g;
    
    @JsonProperty("fat_100g")
    private Double fat100g;
    
    @JsonProperty("fiber_100g")
    private Double fiber100g;
    
    @JsonProperty("sugars_100g")
    private Double sugars100g;
    
    @JsonProperty("sodium_100g")
    private Double sodium100g;
}
```

## API Endpoints

### Search Products

```http
GET /api/v1/external/foods/search?q=coca+cola&page=0&size=10
Authorization: Bearer <token>
```

**Response:**
```json
{
  "products": [
    {
      "code": "5449000000996",
      "name": "Coca-Cola",
      "brand": "Coca-Cola",
      "imageUrl": "https://images.openfoodfacts.org/...",
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

### Get Product by Barcode

```http
GET /api/v1/external/foods/barcode/5449000000996
Authorization: Bearer <token>
```

**Response:**
```json
{
  "code": "5449000000996",
  "name": "Coca-Cola",
  "brand": "Coca-Cola",
  "imageUrl": "https://...",
  "calories": 42.0,
  "protein": 0.0,
  "carbs": 10.6,
  "fats": 0.0
}
```

### Import Product

```http
POST /api/v1/external/foods/5449000000996/import
Authorization: Bearer <token>
```

**Response:** `201 Created`
```json
{
  "id": 43,
  "name": "Coca-Cola",
  "brand": "Coca-Cola",
  "barcode": "5449000000996",
  "source": "OPENFOODFACTS",
  "calories": 42.0,
  "protein": 0.0,
  "carbs": 10.6,
  "fats": 0.0
}
```

## Error Handling

### API No Disponible

```java
@ExceptionHandler(WebClientException.class)
public ResponseEntity<ErrorResponse> handleWebClientException(WebClientException ex) {
    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse("External API temporarily unavailable"));
}
```

### Producto No Encontrado

```java
public Food importProduct(String barcode, Long userId) {
    try {
        OpenFoodFactsProduct product = client.getProductByBarcode(barcode).block();
        if (product == null) {
            throw new ResourceNotFoundException("Product not found in OpenFoodFacts");
        }
        // ...
    } catch (WebClientResponseException.NotFound ex) {
        throw new ResourceNotFoundException("Product not found in OpenFoodFacts");
    }
}
```

## Caching

### Cache de Búsquedas

```java
@Cacheable(value = "openfoodfacts-search", key = "#query + '-' + #page")
public List<FoodDTO> searchProducts(String query, int page, int size) {
    // ...
}
```

### Cache de Productos

```java
@Cacheable(value = "openfoodfacts-product", key = "#barcode")
public Food getProductByBarcode(String barcode) {
    // ...
}
```

**Configuración:**
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

## Rate Limiting

### Client-Side Rate Limiting

```java
@Component
public class RateLimiter {
    private final Bucket bucket;
    
    public RateLimiter() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
    
    public void checkRateLimit() {
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many requests to OpenFoodFacts");
        }
    }
}
```

## Data Quality

### Validación de Datos

```java
private FoodDTO transformToFoodDTO(OpenFoodFactsProduct product) {
    // Validar que tenga datos mínimos
    if (product.getProductName() == null || product.getProductName().isEmpty()) {
        throw new InvalidDataException("Product name is required");
    }
    
    Nutriments nutriments = product.getNutriments();
    if (nutriments == null || nutriments.getEnergyKcal100g() == null) {
        throw new InvalidDataException("Nutritional data is incomplete");
    }
    
    // ...
}
```

### Normalización

```java
private Double normalizeNutrient(Double value) {
    if (value == null) return 0.0;
    if (value < 0) return 0.0;
    return Math.round(value * 10.0) / 10.0; // 1 decimal
}
```

## Testing

### Mock OpenFoodFacts Client

```java
@MockBean
private OpenFoodFactsClient openFoodFactsClient;

@Test
public void testSearchProducts() {
    OpenFoodFactsSearchResponse mockResponse = new OpenFoodFactsSearchResponse();
    // ... setup mock
    
    when(openFoodFactsClient.searchProducts(anyString(), anyInt(), anyInt()))
        .thenReturn(Mono.just(mockResponse));
    
    List<FoodDTO> results = openFoodFactsService.searchProducts("test", 0, 10);
    
    assertNotNull(results);
    assertEquals(1, results.size());
}
```

## Future Integrations

### USDA FoodData Central

- **URL**: https://fdc.nal.usda.gov/api-guide.html
- **Requiere**: API Key
- **Ventaja**: Datos más precisos para alimentos US

### Nutritionix API

- **URL**: https://www.nutritionix.com/business/api
- **Requiere**: API Key
- **Ventaja**: Búsqueda natural language

### Edamam Food Database

- **URL**: https://developer.edamam.com/food-database-api
- **Requiere**: API Key
- **Ventaja**: Análisis de recetas

## Best Practices

1. **Siempre usar User-Agent**: Identificar tu app
2. **Respetar rate limits**: Implementar backoff
3. **Cachear resultados**: Reducir llamadas
4. **Validar datos**: No confiar ciegamente
5. **Manejar errores**: API puede fallar
6. **Logging**: Registrar llamadas para debugging
7. **Timeout**: No esperar indefinidamente
8. **Retry logic**: Reintentar en fallos temporales
