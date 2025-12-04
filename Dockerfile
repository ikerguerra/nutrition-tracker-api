# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Railway will override this with PORT env var)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
