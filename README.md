## WiseGrade (Backend)

Backend en Spring Boot (Java 21) + MySQL + Flyway.

### Requisitos

- Java 21
- Maven
- MySQL local (DB `wisegrade`)

### Variables de entorno (dev)

- `DB_URL` (default: `jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true`)
- `DB_USER` (default: `wisegrade_app`)
- `DB_PASSWORD` (requerida)

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
- Asociación docente↔materia
- Banco de preguntas para (periodo/materia/momento/docente)
- Inicia un intento y te imprime los IDs para pegarlos en el frontend

### Endpoints útiles

- Health: `GET /api/health`
- Iniciar intento: `POST /api/intentos/iniciar`
- Enviar intento: `POST /api/intentos/enviar`

Notas:

- `POST /api/intentos/iniciar` es idempotente por (examen, estudiante): si ya existe un intento, devuelve el existente para reanudar.
- `POST /api/intentos/enviar` acepta `respuestas` vacías para permitir cierre automático por antitrampa.
