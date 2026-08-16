# Marginalia API

API REST para gestionar anotaciones estructuradas de libros. Cada usuario puede organizar sus notas mediante la jerarquía:

```text
Book -> Chapter (recursivo) -> ContentBlock
```

Los bloques de contenido soportan notas, listas de pasos, código, fórmulas, ejercicios e imágenes. Un libro completo puede exportarse como PDF conservando la jerarquía de capítulos.

## Stack

- Java 21 y Spring Boot 3
- Spring Web, Validation y Spring Data JPA
- Spring Security con JWT y OAuth2 de Google
- PostgreSQL 16 y Flyway
- Redis 7, Bucket4j y Lettuce
- Brevo para emails transaccionales
- Cloudinary para imágenes
- OpenPDF para exportaciones
- Springdoc OpenAPI y Swagger UI
- JUnit 5, Mockito y AssertJ
- Maven Wrapper, Docker y GitHub Actions

## Funcionalidades

- Registro, login y verificación de email.
- Access tokens de 15 minutos y refresh tokens de 7 días.
- Login adicional mediante Google OAuth2.
- Bloqueo durante 15 minutos después de 5 intentos de login fallidos.
- Rate limiting por IP en `/api/auth/**`: 10 solicitudes por minuto.
- CRUD de libros, capítulos recursivos y bloques de contenido.
- Autorización por propietario para todos los recursos.
- Imágenes de hasta 5 MB alojadas en Cloudinary.
- Exportación PDF síncrona hasta 50 bloques y asíncrona para libros mayores.
- Cambio de contraseña, email y username.
- Soft delete de cuentas y purga permanente después de 30 días.
- Errores centralizados con una estructura JSON consistente.
- Contrato OpenAPI generado automáticamente y consola Swagger UI.

## Requisitos

Para ejecutar todo con Docker:

- Docker Desktop con Docker Compose.

Para ejecutar la API directamente:

- JDK 21.
- PostgreSQL 16.
- Redis 7.

No es necesario instalar Maven: el Wrapper incluido descarga y utiliza Maven 3.9.16.

## Variables de entorno

Copiar el archivo de ejemplo y reemplazar todos los placeholders:

```powershell
Copy-Item .env.example .env
```

| Variable | Uso |
| --- | --- |
| `DB_NAME` | Nombre de la base PostgreSQL. |
| `DB_USER` | Usuario de PostgreSQL. |
| `DB_PASSWORD` | Contraseña de PostgreSQL. |
| `APP_PORT` | Puerto publicado por Docker Compose. |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring: `default`, `dev` o `prod`. |
| `REDIS_HOST` | Host de Redis; usar `redis` dentro de Compose y `localhost` al ejecutar Maven localmente. |
| `REDIS_PORT` | Puerto de Redis. |
| `JWT_SECRET` | Secreto HMAC de al menos 32 caracteres. |
| `GOOGLE_CLIENT_ID` | Client ID de Google OAuth2. |
| `GOOGLE_CLIENT_SECRET` | Client secret de Google OAuth2. |
| `BREVO_API_KEY` | API key de Brevo. |
| `BREVO_SENDER_EMAIL` | Remitente verificado en Brevo. |
| `BREVO_SENDER_NAME` | Nombre visible del remitente. |
| `CLOUDINARY_CLOUD_NAME` | Nombre del cloud de Cloudinary. |
| `CLOUDINARY_API_KEY` | API key de Cloudinary. |
| `CLOUDINARY_API_SECRET` | API secret de Cloudinary. |

El archivo `.env` está ignorado por Git. No se deben guardar credenciales reales en `.env.example`, `application.yml` ni en el historial del repositorio.

## Ejecución con Docker

Esta opción levanta la API, PostgreSQL y Redis:

```powershell
docker compose up --build
```

Comprobar el estado:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Respuesta esperada:

```json
{
  "status": "UP"
}
```

Detener los contenedores conservando la base de datos:

```powershell
docker compose down
```

Para eliminar también el volumen persistente de PostgreSQL:

```powershell
docker compose down --volumes
```

> Este último comando elimina los datos locales de forma irreversible.

## Ejecución local con Maven Wrapper

Definir `JAVA_HOME` con la ruta del JDK 21 instalado. Por ejemplo, en Windows:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd --version
```

Levantar solamente la infraestructura:

```powershell
docker compose up -d postgres redis
```

Configurar las variables requeridas en la terminal. La aplicación no carga `.env` automáticamente cuando se ejecuta fuera de Docker:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_URL = "jdbc:postgresql://localhost:5432/marginalia"
$env:DB_USER = "marginalia"
$env:DB_PASSWORD = "marginalia"
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:JWT_SECRET = "replace-with-a-secure-secret-of-at-least-32-characters"
$env:GOOGLE_CLIENT_ID = "replace-with-google-client-id"
$env:GOOGLE_CLIENT_SECRET = "replace-with-google-client-secret"
$env:BREVO_API_KEY = "replace-with-brevo-api-key"
$env:BREVO_SENDER_EMAIL = "replace-with-verified-sender@example.com"
$env:BREVO_SENDER_NAME = "Marginalia"
$env:CLOUDINARY_CLOUD_NAME = "replace-with-cloudinary-cloud-name"
$env:CLOUDINARY_API_KEY = "replace-with-cloudinary-api-key"
$env:CLOUDINARY_API_SECRET = "replace-with-cloudinary-api-secret"
```

Iniciar la API:

```powershell
.\mvnw.cmd spring-boot:run
```

Flyway aplica automáticamente las migraciones pendientes. Hibernate está configurado con `ddl-auto: validate` y no crea ni modifica el esquema.

La primera ejecución del Wrapper descarga Maven 3.9.16 en el caché local del usuario.

En Linux o macOS se utiliza `./mvnw` en lugar de `.\mvnw.cmd`. Si el sistema no conserva el permiso de ejecución, ejecutar una sola vez `chmod +x mvnw`.

## OpenAPI y Swagger UI

Con la API en ejecución, la documentación interactiva está disponible en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Contrato OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Ambas rutas son públicas. Para probar endpoints protegidos desde Swagger UI:

1. Iniciar sesión mediante `POST /api/auth/login`.
2. Copiar el valor de `accessToken`.
3. Seleccionar **Authorize** y pegar solamente el token; Swagger agrega el prefijo `Bearer`.

## Autenticación

Los endpoints protegidos requieren el access token:

```http
Authorization: Bearer <accessToken>
```

Flujo mediante contraseña:

1. Registrar la cuenta con `POST /api/auth/register`.
2. Obtener el código enviado por Brevo.
3. Verificar la cuenta con `POST /api/auth/verify`.
4. Iniciar sesión con `POST /api/auth/login`.
5. Renovar el access token con `POST /api/auth/refresh`.
6. Revocar el refresh token con `POST /api/auth/logout`.

Google OAuth2 comienza en:

```text
/oauth2/authorization/google
```

## Endpoints

### Públicos

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/health` | Estado de la API. |
| `POST` | `/api/auth/register` | Registrar una cuenta. |
| `POST` | `/api/auth/login` | Iniciar sesión con email y contraseña. |
| `POST` | `/api/auth/verify` | Verificar el email con un código de seis dígitos. |
| `POST` | `/api/auth/refresh` | Obtener un nuevo access token. |
| `POST` | `/api/auth/logout` | Revocar un refresh token. |

### Cuenta autenticada

| Método | Ruta | Descripción |
| --- | --- | --- |
| `PATCH` | `/api/users/me/password` | Cambiar contraseña. |
| `PATCH` | `/api/users/me/email` | Cambiar email y reiniciar la verificación. |
| `PATCH` | `/api/users/me/username` | Cambiar username. |
| `DELETE` | `/api/users/me` | Soft delete con confirmación de contraseña. |

### Libros y exportaciones

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/books` | Crear un libro. |
| `GET` | `/api/books` | Listar libros propios. |
| `GET` | `/api/books/{id}` | Obtener un libro propio. |
| `DELETE` | `/api/books/{id}` | Eliminar un libro. |
| `GET` | `/api/books/{bookId}/export` | Descargar o encolar una exportación PDF. |
| `GET` | `/api/books/{bookId}/exports/{exportId}` | Consultar una exportación asíncrona. |
| `GET` | `/api/books/{bookId}/exports/{exportId}/download` | Descargar una exportación terminada. |

### Capítulos

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/books/{bookId}/chapters` | Crear un capítulo o subcapítulo. |
| `GET` | `/api/books/{bookId}/chapters` | Listar capítulos en formato plano. |
| `PUT` | `/api/chapters/{id}` | Actualizar un capítulo. |
| `DELETE` | `/api/chapters/{id}` | Eliminar un capítulo. |

### Bloques y adjuntos

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/chapters/{chapterId}/blocks` | Crear un bloque. |
| `GET` | `/api/chapters/{chapterId}/blocks` | Listar bloques ordenados. |
| `PUT` | `/api/blocks/{id}` | Actualizar un bloque. |
| `PATCH` | `/api/blocks/{id}/resolve` | Alternar el estado de un ejercicio. |
| `DELETE` | `/api/blocks/{id}` | Eliminar un bloque. |
| `POST` | `/api/blocks/{blockId}/attachments` | Subir una imagen multipart para un bloque `IMAGE`. |

Tipos de bloque disponibles:

- `NOTE`: texto plano.
- `STEP_LIST`: lista numérica o alfabética.
- `CODE`: código y lenguaje.
- `MATH`: expresión LaTeX almacenada como texto.
- `EXERCISE`: ejercicio con estado resuelto.
- `IMAGE`: bloque con imágenes alojadas en Cloudinary.

## Errores

Los errores gestionados por `GlobalExceptionHandler` usan la siguiente estructura:

```json
{
  "timestamp": "2026-08-14T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation detail",
  "path": "/api/example"
}
```

El rate limiter se ejecuta antes de Spring MVC. Cuando rechaza una solicitud, responde con `429 Too Many Requests`, el encabezado `Retry-After` y un cuerpo `application/problem+json`.

Los recursos de otro usuario responden `403 Forbidden`. Los recursos inexistentes responden `404 Not Found`.

## Base de datos

El esquema está versionado mediante 14 migraciones Flyway en `src/main/resources/db/migration`:

- Usuarios, soft delete y verificación de email.
- Libros y capítulos recursivos.
- Bloques y pasos de listas.
- Adjuntos de imagen.
- Refresh tokens.
- Trabajos de exportación PDF.

PostgreSQL persiste sus datos en el volumen Docker `postgres_data`.

## Estructura

```text
src/main/java/com/marginalia/api/
├── config/       Configuración de Spring e integraciones
├── controller/   Endpoints REST
├── domain/       Entidades y enums
├── dto/          Contratos de entrada y salida
├── exception/    Excepciones y manejo global
├── repository/   Repositorios Spring Data JPA
├── security/     JWT, OAuth2, filtros y rate limiting
└── service/      Casos de uso y reglas de negocio

src/main/resources/
├── db/migration/ Migraciones Flyway
└── templates/    Plantillas HTML de email
```

Los controladores nunca exponen entidades JPA directamente. El flujo principal es:

```text
Controller -> Service -> Repository -> PostgreSQL
```

## Calidad y tests

Ejecutar compilación, Checkstyle y todos los tests:

```powershell
.\mvnw.cmd verify
```

Ejecutar solamente los tests:

```powershell
.\mvnw.cmd test
```

Generar la documentación Javadoc:

```powershell
.\mvnw.cmd javadoc:javadoc
```

La salida se genera en `target/reports/apidocs`.

## Integración continua

El workflow `.github/workflows/ci.yml` se ejecuta en cada push y pull request. Configura Java 21 con caché de Maven y ejecuta:

```text
./mvnw --batch-mode --no-transfer-progress verify
```

Cualquier error de compilación, test fallido o violación de Checkstyle hace fallar el workflow.
