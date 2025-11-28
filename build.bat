    echo ========================================
    echo.
    echo To run the application:
    echo   mvn spring-boot:run
    echo.
    echo Or build JAR:
    echo   mvn clean package
    echo   java -jar target\nutrition-tracker-api-1.0.0-SNAPSHOT.jar
    echo.
) else (
    echo.
    echo ========================================
    echo [ERROR] Build failed!
    echo ========================================
    echo Please check the error messages above.
    echo.
)

pause
