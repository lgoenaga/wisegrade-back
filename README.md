## WiseGrade (Backend)

Backend en Spring Boot (Java 21) + MySQL + Flyway.

Documentación de snapshot del estado actual: ver `../Documents/backend-summary.md`.

### Requisitos

- Java 21
- Maven
- MySQL local (DB `wisegrade`)

### Setup de Base de Datos (local)

El backend usa Flyway para crear/evolucionar el schema al arrancar.

- Fuente de verdad: migraciones en `src/main/resources/db/migration/`.
- En dev, Hibernate está en `ddl-auto: validate` (no crea tablas automáticamente).

Si necesitas crear DB/usuario local, existe un script de ayuda en el workspace:

- `../db/mysql-local-setup.sql`

### Flujo local típico (end-to-end)

1. Levanta MySQL y crea la DB/usuario.
2. Levanta el backend (ver sección siguiente).
3. Ejecuta el seed demo (imprime IDs para el frontend).
4. Levanta el frontend y pega los IDs.

### Variables de entorno (dev)

- `DB_URL` (default: `jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true`)
- `DB_USER` (default: `wisegrade_app`)
- `DB_PASSWORD` (requerida)

Tip: también puedes crear `backend/.env.local` (no se sube a git) con:

```bash
DB_PASSWORD='tu_clave'
```

CORS (frontend):

- `APP_CORS_ALLOWED_ORIGINS` (default: `http://localhost:5173`)

### Levantar en desarrollo

Opción recomendada (script):

```bash
cd backend
./scripts/run-dev.sh
```

- Si `DB_PASSWORD` no está definida, el script la pedirá sin mostrarla.
- Requiere Java 21 (el script valida la versión). Si tienes Java 21 instalada en otra ruta, define `JAVA_HOME` antes de ejecutar.

### Seed de datos demo (examen + intento)

Con el backend corriendo, ejecuta:

```bash
cd backend
./scripts/seed-demo-exam.sh
```

Esto crea:

- Docente demo
- Estudiante demo
- Usuarios de autenticación para docente/estudiante (tabla `usuarios`)
- Asociación docente↔materia
- Banco de preguntas para (periodo/materia/momento/docente)
- Inicia un intento y te imprime los IDs para pegarlos en el frontend

### Autenticación (sesión) y roles

El backend usa **Spring Security** con **sesión** (cookie `JSESSIONID`).

- Público:
  - `GET /api/health`
  - `POST /api/auth/login`
- Requiere login (cookie de sesión): el resto de endpoints bajo `/api/**`.

Roles:

- **ADMIN**: acceso completo.
- **DOCENTE**: puede consultar `GET /api/examenes/resultados` (docente fijo por sesión).
- **ESTUDIANTE**: puede iniciar/ver/enviar intentos (`/api/intentos/*`) solo de su propio estudiante.

Usuario admin (bootstrap en dev):

- Documento: `ADMIN`
- Clave: `Wisegrade2026`

Se puede configurar con:

- `wisegrade.auth.admin.documento`
- `wisegrade.auth.admin.password`

### Endpoints útiles

- Health: `GET /api/health`
- Auth:
  - Login: `POST /api/auth/login`
  - Sesión actual: `GET /api/auth/me`
  - Logout: `POST /api/auth/logout`
  - Crear usuarios (ADMIN): `POST /api/auth/users`
  - Crear usuarios en bulk desde Docentes (ADMIN): `POST /api/auth/users/bulk/docentes`
  - Crear usuarios en bulk desde Estudiantes (ADMIN): `POST /api/auth/users/bulk/estudiantes`
- Iniciar intento: `POST /api/intentos/iniciar`
- Enviar intento: `POST /api/intentos/enviar`
- Detalle de intento: `GET /api/intentos/{intentoId}`
- Exportar intento a PDF: `GET /api/intentos/{intentoId}/export/pdf`
- Resultados (docente): `GET /api/examenes/resultados?periodoId=...&materiaId=...&momentoId=...&docenteResponsableId=...`

Notas:

- `POST /api/intentos/iniciar` es idempotente por (examen, estudiante): si ya existe un intento, devuelve el existente para reanudar.
- `POST /api/intentos/enviar` acepta `respuestas` vacías para permitir cierre automático por antitrampa.
- `GET /api/intentos/{intentoId}/export/pdf` solo está disponible si el intento está en estado `SUBMITTED`.

#### Bulk de usuarios (ADMIN)

Para crear usuarios (rol + vínculo) de forma masiva a partir de las tablas académicas:

- `POST /api/auth/users/bulk/estudiantes`
- `POST /api/auth/users/bulk/docentes`

El body es opcional. Si lo envías, soporta:

- `soloActivos` (default `false`): si `true`, solo considera personas activas.
- `activoUsuario` (default `true`): valor de `activo` en el usuario creado.
- `skipExisting` (default `true`): si existe usuario con ese `documento`, lo omite.

Ejemplo:

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"soloActivos":false,"activoUsuario":true,"skipExisting":true}' \
  http://localhost:8080/api/auth/users/bulk/estudiantes
```

### Tests

Ejecutar tests:

```bash
cd backend
mvn test
```

Si vas a correr integración contra MySQL local, existe el script:

```bash
cd backend
./scripts/run-localmysql-it.sh
```

### Troubleshooting

- **Flyway / schema mismatch**: revisa que MySQL esté apuntando a la DB correcta (`DB_URL`) y que el usuario tenga permisos.
- **Access denied (DB)**: confirma `DB_USER` y `DB_PASSWORD`.
- **CORS**: ajusta `APP_CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`).
