# AGENTS.md — colegio-user-service

## Stack

- **Spring Boot 4.0.6 / Java 21**, compilación con Maven (`mvnw` wrapper en `user-service/`)
- **PostgreSQL** vía JPA + Flyway (ddl-auto=none — todo el schema en `db/migration/`)
- **Spring Security** con autenticación JWT (jjwt 0.11.5, HS256), cookies para transporte de tokens
- **Lombok**, `@RequiredArgsConstructor` en todo el proyecto
- **Puerto 9091**

## Inicio rápido

```bash
cd user-service
./mvnw clean package -DskipTests   # compilar
./mvnw test                         # ejecutar tests (solo 1 smoke test)
./mvnw spring-boot:run              # servidor de desarrollo
```

## API

### Auth (`/api/v1/auth/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| POST | `/register` | No | `@Valid RegisterRequest` con validadores personalizados de RUT y contraseña |
| POST | `/authenticate` | No | Login, retorna JWT + refresh en cookies y body |
| POST | `/refresh-token` | No | Body: `{"refreshToken":"..."}` — retorna nuevo JWT |
| POST | `/refresh-token-cookie` | No | Lee refresh token desde cookie, asigna nueva cookie JWT |
| GET | `/info` | Sí | Retorna el `Usuario` actual (desde `@AuthenticationPrincipal`) |
| POST | `/logout` | Sí | Elimina el refresh token de la BD, limpia ambas cookies |

### Usuarios (`/api/v1/usuarios/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| GET | `/{id}` | ADMIN/DOCENTE/ESTUDIANTE | |
| GET | `/rut/{rut}` | ADMIN/DOCENTE/ESTUDIANTE | |
| GET | `` | ADMIN/DOCENTE/ESTUDIANTE | Lista todos |
| GET | `/{id}/exists` | **Público** (permitAll) | |
| POST | `` | Solo ADMIN | Crea usuario con contraseña codificada en BCrypt |
| PUT | `/{id}` | ADMIN/DOCENTE/ESTUDIANTE | Actualización parcial: solo campos no nulos y no vacíos |
| DELETE | `/{id}` | Solo ADMIN | **Borrado lógico** — establece `enabled=false` |

## Arquitectura

- Paquete base `com.proyecto.user_service`
- `Usuario` implementa `UserDetails` — username = **email**
- Roles: `ADMIN`, `DOCENTE`, `ESTUDIANTE`, `APODERADO` — cada uno con los 4 privilegios
- Manejo de errores: `GlobalExceptionHandler` (`@RestControllerAdvice`) mapea a DTO `ErrorResponse`
- Excepciones: `BusinessRuleException` → 400, `ResourceNotFoundException` → 404, `TokenException` → 403, `BadCredentialsException` → 401
- `deleteById()`: establece `enabled=false` (NO elimina de la BD)
- `RutUtils.formatearRut()`: elimina puntos, agrega guión antes del DV
- `GlobalExceptionHandler` maneja `MethodArgumentNotValidException` con mensajes de error a nivel de campo

## Reglas de dominio (en registro)

| Rol | El email debe terminar en |
|-----|---------------------------|
| ADMIN / DOCENTE | `@colegioohiggins.cl` |
| ESTUDIANTE | `@alumnos.colegioohiggins.cl` |
| APODERADO | `@gmail.com` o `@gmail.cl` |

Email o RUT duplicado → `BusinessRuleException`.

## JWT

- Claims: `roles` (lista de cadenas de autoridad), `userId` (Long)
- Expiración: **15 min** (access), **15 días** (refresh)
- Cookies: `jwt-cookie` (access), `refresh-jwt-cookie` (refresh) — httpOnly, sameSite=Lax, NO secure
- Clave secreta: codificada en Base64 en `application.properties` (hardcodeada para desarrollo)

## Configuración

Toda la configuración está en `src/main/resources/application.properties` — **credenciales de BD hardcodeadas, clave secreta JWT**. No hay archivos específicos por perfil, ni `.env`. La BD apunta a una instancia PostgreSQL en AWS RDS.

## Tests

Solo existe un smoke test (`UserServiceApplicationTests.contextLoads()`). Requiere PostgreSQL en ejecución (sin testcontainers, sin BD en memoria configurada).

## Docker

```bash
docker build -t user-service user-service/
```

Compilación multi-etapa: Maven 3.9.6-eclipse-temurin-21 (build) → eclipse-temurin:21-jre-alpine (run). Expone puerto 9091.

## Datos semilla (V2)

| Rol | Email | Contraseña |
|-----|-------|------------|
| ADMIN | admin@colegioohiggins.cl | Admin123$ |
| APODERADO | juan@gmail.cl | Password123! |
| ESTUDIANTE | benja@alumnos.colegioohiggins.cl | Estudiante456@ |
| ESTUDIANTE | susana@alumnos.colegioohiggins.cl | Estudiante789@ |
| DOCENTE | carlosab@colegioohiggins.cl | Docente321$ |
