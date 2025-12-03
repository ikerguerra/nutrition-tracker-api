# Problema de Compilación - Java 25 Incompatibilidad

## Problema Detectado

El proyecto no compila debido a incompatibilidad entre **Java 25** y **Lombok 1.18.36**.

```
ERROR: ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag
```

## Causa

- **Java instalado:** Java 25.0.1 (muy reciente)
- **Spring Boot:** 3.2.0 (soporta Java 17-21)
- **Lombok:** No tiene soporte completo para Java 25 aún

## Soluciones

### ✅ Opción 1: Usar Java 17 LTS (Recomendado)

**Descargar:**
- [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [OpenJDK 17](https://adoptium.net/temurin/releases/?version=17)

**Configurar:**
```powershell
# Verificar instalación
java -version  # Debe mostrar "17.x.x"

# Si tienes múltiples versiones, configurar JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### ✅ Opción 2: Usar Java 21 LTS

**Descargar:**
- [Oracle JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [OpenJDK 21](https://adoptium.net/temurin/releases/?version=21)

### ⚠️ Opción 3: Maven Toolchains (Si tienes Java 17/21 instalado)

Crear `~/.m2/toolchains.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>17</version>
    </provides>
    <configuration>
      <jdkHome>C:\Program Files\Java\jdk-17</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### ❌ Opción 4: Eliminar Lombok (No recomendado)

Requeriría reescribir todo el código sin anotaciones Lombok (getters, setters, builders, etc.).

## Verificación

Después de cambiar a Java 17/21:

```powershell
# Verificar versión
java -version

# Limpiar y compilar
mvn clean install -DskipTests

# Debería compilar exitosamente
```

## Estado Actual

- ❌ Compilación fallida con Java 25
- ✅ Código fuente correcto
- ✅ Dependencias correctas
- ✅ Configuración correcta
- ⏳ Esperando cambio de versión de Java

## Próximos Pasos

1. Instalar/configurar Java 17 o 21
2. Ejecutar `mvn clean install -DskipTests`
3. Ejecutar `mvn spring-boot:run`
4. Verificar migraciones de Flyway
5. Verificar Spring Security activo

### ⚠️ Problema: Maven usa Java incorrecto (TypeTag :: UNKNOWN)

**Síntoma:**
Error `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN` al compilar, aunque `java -version` diga 17.

**Causa:**
Maven puede estar usando una versión de Java diferente (ej. Java 25) si está en el PATH antes que Java 17, o si `JAVA_HOME` no está configurado explícitamente.

**Solución:**
Forzar `JAVA_HOME` antes de ejecutar Maven:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn clean install
```
