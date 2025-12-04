# Contributing Guide

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Git
- IDE (IntelliJ IDEA recomendado)

### Setup Development Environment

1. **Fork y clonar**
```bash
git clone https://github.com/your-username/nutrition-tracker-api.git
cd nutrition-tracker-api
```

2. **Crear rama de desarrollo**
```bash
git checkout -b feature/my-new-feature
```

3. **Configurar base de datos local**
```bash
createdb nutrition_tracker_db
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# Editar application-dev.yml con tus credenciales
```

4. **Ejecutar migraciones**
```bash
mvn flyway:migrate
```

5. **Ejecutar aplicación**
```bash
mvn spring-boot:run
```

## Code Style

### Java Conventions

- **Indentación**: 4 espacios
- **Naming**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

### Example

```java
@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private static final int MAX_RETRIES = 3;
    private final UserProfileRepository userProfileRepository;
    
    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }
}
```

### Lombok

Usar Lombok para reducir boilerplate:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private Double height;
}
```

## Project Structure

```
src/main/java/com/nutritiontracker/
├── config/                 # Configuración
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── modules/
│   ├── auth/              # Módulo de autenticación
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── dto/
│   │   └── enums/
│   ├── food/              # Módulo de alimentos
│   └── dailylog/          # Módulo de logs diarios
└── NutritionTrackerApplication.java
```

### Naming Conventions

- **Controllers**: `*Controller.java`
- **Services**: `*Service.java`
- **Repositories**: `*Repository.java`
- **DTOs**: `*Request.java`, `*Response.java`
- **Entities**: Nombre del dominio (e.g., `User.java`)

## Database Migrations

### Creating Migrations

```bash
# Crear nueva migración
touch src/main/resources/db/migration/V5__add_favorites_table.sql
```

**Naming**: `V{version}__{description}.sql`

**Example**:
```sql
-- V5__add_favorites_table.sql
CREATE TABLE user_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    food_id BIGINT NOT NULL REFERENCES foods(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, food_id)
);

CREATE INDEX idx_user_favorites_user ON user_favorites(user_id);
```

### Migration Rules

1. **NUNCA** modificar migraciones aplicadas
2. **SIEMPRE** crear nueva migración para cambios
3. **PROBAR** migración en local antes de commit
4. **INCLUIR** rollback script si es posible

## Testing

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
    
    @Mock
    private UserProfileRepository repository;
    
    @InjectMocks
    private UserProfileService service;
    
    @Test
    void shouldGetUserProfile() {
        // Given
        Long userId = 1L;
        UserProfile profile = new UserProfile();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));
        
        // When
        UserProfile result = service.getUserProfile(userId);
        
        // Then
        assertNotNull(result);
        verify(repository).findByUserId(userId);
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserProfileControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

### Running Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=UserProfileServiceTest

# Con cobertura
mvn test jacoco:report
```

## Git Workflow

### Branch Naming

- `feature/` - Nuevas funcionalidades
- `fix/` - Bug fixes
- `refactor/` - Refactoring
- `docs/` - Documentación
- `test/` - Tests

**Examples**:
- `feature/oauth2-integration`
- `fix/profile-calculation-bug`
- `refactor/food-service`

### Commit Messages

Formato: `type(scope): message`

**Types**:
- `feat`: Nueva funcionalidad
- `fix`: Bug fix
- `docs`: Documentación
- `refactor`: Refactoring
- `test`: Tests
- `chore`: Tareas de mantenimiento

**Examples**:
```
feat(auth): add OAuth2 Google integration
fix(profile): correct BMR calculation for women
docs(api): update authentication endpoints
refactor(food): simplify search logic
test(profile): add unit tests for calculations
```

### Pull Request Process

1. **Crear PR** desde tu rama a `develop`
2. **Título claro**: Describe el cambio
3. **Descripción**:
   - ¿Qué cambia?
   - ¿Por qué?
   - ¿Cómo probarlo?
4. **Tests**: Asegurar que pasen todos
5. **Review**: Esperar aprobación
6. **Merge**: Squash and merge

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings
```

## Code Review Guidelines

### As Reviewer

- ✅ Revisar lógica de negocio
- ✅ Verificar tests
- ✅ Comprobar seguridad
- ✅ Validar performance
- ✅ Sugerir mejoras
- ❌ No ser excesivamente crítico
- ❌ No aprobar sin revisar

### As Author

- ✅ Responder a comentarios
- ✅ Hacer cambios solicitados
- ✅ Agradecer feedback
- ✅ Aprender de sugerencias
- ❌ No tomar críticas personal
- ❌ No ignorar comentarios

## Documentation

### JavaDoc

```java
/**
 * Calculates daily caloric needs based on user profile.
 * Uses Mifflin-St Jeor equation for BMR and Harris-Benedict for TDEE.
 *
 * @param profile User profile with height, weight, age, gender, activity level
 * @return Daily caloric goal adjusted for nutritional objective
 * @throws IllegalArgumentException if required profile data is missing
 */
public double calculateDailyCalories(UserProfile profile) {
    // ...
}
```

### README Updates

Actualizar README.md cuando:
- Añades nueva funcionalidad
- Cambias configuración
- Añades dependencia
- Modificas setup

## Dependencies

### Adding Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

**Checklist**:
- [ ] Versión compatible con Spring Boot
- [ ] Licencia compatible
- [ ] Documentar uso en README
- [ ] Actualizar dependencias relacionadas

## Security

### Sensitive Data

**NUNCA** commitear:
- Passwords
- API keys
- JWT secrets
- Database credentials

**Usar** variables de entorno:
```yaml
jwt:
  secret: ${JWT_SECRET}
```

### Security Checklist

- [ ] Input validation
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection
- [ ] Authentication required
- [ ] Authorization checked
- [ ] Sensitive data encrypted
- [ ] Error messages safe

## Performance

### Database Queries

```java
// ❌ N+1 Query Problem
List<DailyLog> logs = dailyLogRepository.findAll();
logs.forEach(log -> {
    log.getEntries().size(); // Lazy loading
});

// ✅ Fetch Join
@Query("SELECT dl FROM DailyLog dl LEFT JOIN FETCH dl.entries WHERE dl.userId = :userId")
List<DailyLog> findByUserIdWithEntries(@Param("userId") Long userId);
```

### Caching

```java
@Cacheable(value = "foods", key = "#id")
public Food getFoodById(Long id) {
    return foodRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Food not found"));
}
```

## Debugging

### Logging

```java
@Slf4j
@Service
public class UserProfileService {
    
    public UserProfile updateProfile(UserProfile profile) {
        log.debug("Updating profile for user: {}", profile.getUserId());
        
        try {
            UserProfile updated = userProfileRepository.save(profile);
            log.info("Profile updated successfully for user: {}", profile.getUserId());
            return updated;
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", profile.getUserId(), e);
            throw e;
        }
    }
}
```

### Levels

- `ERROR`: Errores que requieren atención
- `WARN`: Situaciones anormales pero manejables
- `INFO`: Eventos importantes
- `DEBUG`: Información detallada para debugging

## Release Process

1. **Update version** en `pom.xml`
2. **Update CHANGELOG.md**
3. **Create release branch**: `release/v1.0.0`
4. **Run all tests**
5. **Build**: `mvn clean package`
6. **Tag**: `git tag v1.0.0`
7. **Merge** to `main` and `develop`
8. **Deploy** to production

## Getting Help

- **Documentation**: `/docs` folder
- **Issues**: GitHub Issues
- **Slack**: #nutrition-tracker-dev
- **Email**: dev@nutritiontracker.com

## License

Este proyecto es privado y confidencial.
