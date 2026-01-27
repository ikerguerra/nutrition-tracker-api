$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "Using Java from: $env:JAVA_HOME"
mvn clean spring-boot:run
