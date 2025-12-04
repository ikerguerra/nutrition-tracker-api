# Authentication & Security

## Overview

Sistema de autenticación basado en JWT (JSON Web Tokens) con soporte para:
- Registro y login local
- OAuth2 (Google, Facebook) - En desarrollo
- Refresh tokens
- Protección de endpoints

## JWT Tokens

### Access Token

- **Duración**: 15 minutos
- **Uso**: Autenticación de requests
- **Header**: `Authorization: Bearer <token>`

### Refresh Token

- **Duración**: 7 días
- **Uso**: Renovar access token
- **Almacenamiento**: Cliente (localStorage/cookies)

## Flow de Autenticación

### 1. Registro

```mermaid
sequenceDiagram
    Client->>API: POST /auth/register
    API->>Database: Check email exists
    Database-->>API: Email available
    API->>API: Hash password (BCrypt)
    API->>Database: Create user
    API->>API: Generate JWT tokens
    API-->>Client: {accessToken, refreshToken}
```

**Request:**
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "SecurePass123!"
}
```

**Validaciones:**
- Email válido y único
- Password mínimo 8 caracteres
- Name no vacío

### 2. Login

```mermaid
sequenceDiagram
    Client->>API: POST /auth/login
    API->>Database: Find user by email
    Database-->>API: User found
    API->>API: Verify password (BCrypt)
    API->>API: Generate JWT tokens
    API-->>Client: {accessToken, refreshToken}
```

**Request:**
```json
{
  "email": "juan@example.com",
  "password": "SecurePass123!"
}
```

### 3. Refresh Token

```mermaid
sequenceDiagram
    Client->>API: POST /auth/refresh
    API->>API: Validate refresh token
    API->>API: Generate new access token
    API-->>Client: {accessToken, refreshToken}
```

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

## JWT Structure

### Access Token Payload

```json
{
  "sub": "1",
  "email": "juan@example.com",
  "iat": 1701691200,
  "exp": 1701692100
}
```

### Refresh Token Payload

```json
{
  "sub": "1",
  "type": "refresh",
  "iat": 1701691200,
  "exp": 1702296000
}
```

## Security Configuration

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Endpoints Públicos

- `/api/v1/auth/**` - Autenticación
- `/swagger-ui/**` - Documentación
- `/v3/api-docs/**` - OpenAPI spec
- `/actuator/health` - Health check

### Endpoints Protegidos

Todos los demás requieren JWT válido.

## JwtAuthenticationFilter

Filtro que intercepta todas las requests y valida el token JWT.

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) {
        String token = extractToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            
            // Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## JwtTokenProvider

Servicio para generar y validar tokens.

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
```

## Password Hashing

### BCrypt

Usamos BCrypt con strength 12 para hashear passwords.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**Ejemplo:**
```java
String rawPassword = "SecurePass123!";
String hashedPassword = passwordEncoder.encode(rawPassword);
// $2a$12$KIXQjH5yGxH5yGxH5yGxH...

boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
// true
```

## CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## OAuth2 Integration (Futuro)

### Google OAuth2

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
```

### Flow OAuth2

1. Cliente redirige a Google
2. Usuario autoriza
3. Google redirige con código
4. Backend intercambia código por token
5. Backend crea/actualiza usuario
6. Backend genera JWT propio
7. Cliente recibe JWT

## Security Best Practices

### ✅ Implementado

- ✅ Passwords hasheados con BCrypt
- ✅ JWT con expiración corta
- ✅ Refresh tokens
- ✅ HTTPS en producción (recomendado)
- ✅ CORS configurado
- ✅ SQL injection prevention (JPA)
- ✅ XSS prevention (JSON responses)

### 🔜 Pendiente

- [ ] Rate limiting
- [ ] Account lockout después de intentos fallidos
- [ ] Email verification
- [ ] Password reset flow
- [ ] 2FA (Two-Factor Authentication)
- [ ] Session management
- [ ] Audit logging

## Error Handling

### 401 Unauthorized

Retornado cuando:
- Token no proporcionado
- Token inválido
- Token expirado
- Usuario no encontrado

```json
{
  "timestamp": "2025-12-04T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden

Retornado cuando:
- Usuario no tiene permisos
- Recurso pertenece a otro usuario

```json
{
  "timestamp": "2025-12-04T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

## Testing Authentication

### Obtener Token

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'

# Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Usar Token

```bash
# Get profile
curl -X GET http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }'
```

## Production Considerations

### Environment Variables

```bash
# NUNCA commitear estos valores
JWT_SECRET=your-super-secret-key-min-256-bits
DATABASE_PASSWORD=your-db-password
GOOGLE_CLIENT_SECRET=your-google-secret
```

### HTTPS Only

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### Token Storage

**Frontend:**
- ❌ localStorage (vulnerable a XSS)
- ✅ httpOnly cookies (más seguro)
- ✅ Secure flag en producción
- ✅ SameSite=Strict

### Monitoring

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: when-authorized
```
