Para poder levantar el proyecto en local se deben cargar variables de entorno ya que Java no lee el archivo .env:
'''
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
$env:JAVA_TOOL_OPTIONS = "-Duser.timezone=UTC"
'''

Luego esto:
'''
Get-Content .env |
Where-Object { $_ -match '^[A-Za-z_][A-Za-z0-9_]*=' } |
  ForEach-Object {
    $key, $value = $_ -split '=', 2
    Set-Item -Path "Env:$key" -Value $value.Trim().Trim('"')
}
'''

Luego otras variables:
'''
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5432/$env:DB_NAME"
$env:SPRING_DATA_REDIS_URL = "redis://127.0.0.1:6379"
$env:REDIS_HOST = "127.0.0.1"
$env:REDIS_PORT = "6379"
'''

y por ultimo, detener la API de Docker si estaba activa y mantener la infraestructura:

```powershell
docker compose stop app
docker compose up -d postgres redis
.\mvnw.cmd spring-boot:run
```

Si se renombro una migracion, ejecutar una vez `.\mvnw.cmd clean spring-boot:run`
para eliminar las copias anteriores de `target/classes`. Cada migracion debe tener
una version unica: V20 corresponde a los adjuntos y V21 a los bloques de lectura.
