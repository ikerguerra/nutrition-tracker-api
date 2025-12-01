$env:JAVA_HOME = 'C:\Users\ikerg\.antigravity\extensions\redhat.java-1.50.0-win32-x64\jre\21.0.9-win32-x86_64'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "Using Java from: $env:JAVA_HOME"
mvn clean spring-boot:run
