#!/usr/bin/env bash
set -euo pipefail

on_error() {
  exit_code=$?
  echo 1>&2
  echo "[WiseGrade] Backend no pudo iniciar (exit=$exit_code)." 1>&2
  echo "Sugerencias rápidas:" 1>&2
  echo "- Verifica credenciales MySQL (usuario/clave) y permisos." 1>&2
  echo "- Si tu clave tiene caracteres especiales (ej: '\$'), exporta con comillas simples: DB_PASSWORD='...'" 1>&2
  echo "- Re-ejecuta con stacktrace: mvn -DskipTests spring-boot:run -e" 1>&2
  echo "- Confirma que tu DB_URL tenga allowPublicKeyRetrieval=true si MySQL lo requiere." 1>&2
  echo 1>&2
  exit "$exit_code"
}

trap on_error ERR

cd "$(dirname "$0")/.."

# ─── Cargar variables de entorno ─────────────────────────────────────────────
# Prioridad: .env.local (MySQL local propio) > .env.docker (MySQL Docker)
if [[ -f .env.local ]]; then
  echo "[WiseGrade] Cargando .env.local..." 1>&2
  set -a; source .env.local; set +a
elif [[ -f .env.docker ]]; then
  echo "[WiseGrade] Cargando .env.docker..." 1>&2
  set -a; source .env.docker; set +a
fi

: "${DB_URL:=jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true}"
: "${DB_USER:=wisegrade_app}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "DB_PASSWORD no está definida. Ingrésala para iniciar el backend (no se mostrará)." 1>&2
  read -r -s -p "DB_PASSWORD: " DB_PASSWORD
  echo 1>&2
fi

export DB_URL DB_USER DB_PASSWORD

# ─── MySQL Docker ─────────────────────────────────────────────────────────────
IMAGE_NAME="wisegrade-spring-mysql:latest"
CONTAINER_NAME="wisegrade-mysql"
VOLUME_NAME="wisegrade-spring_wisegrade_mysql_data"

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root_wisegrade}"
MYSQL_DATABASE="${MYSQL_DATABASE:-wisegrade}"
MYSQL_USER_VAR="${MYSQL_USER:-wisegrade_app}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-wisegrade_password}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

CONTAINER_STATE="$(docker inspect --format='{{.State.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo 'missing')"

if [[ "$CONTAINER_STATE" == "running" ]]; then
  HEALTH="$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo 'unknown')"
  echo "[WiseGrade] MySQL ya está corriendo (health=$HEALTH)." 1>&2
  if [[ "$HEALTH" != "healthy" ]]; then
    echo "[WiseGrade] Esperando a que MySQL esté healthy..." 1>&2
    RETRIES=30
    until docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null | grep -q "healthy"; do
      RETRIES=$((RETRIES - 1))
      [[ $RETRIES -le 0 ]] && { echo "[WiseGrade] MySQL no respondió. Revisa: docker logs $CONTAINER_NAME" 1>&2; exit 1; }
      sleep 3
    done
  fi

elif [[ "$CONTAINER_STATE" == "exited" || "$CONTAINER_STATE" == "created" ]]; then
  echo "[WiseGrade] Reiniciando contenedor detenido..." 1>&2
  docker start "$CONTAINER_NAME"
  RETRIES=30
  until docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null | grep -q "healthy"; do
    RETRIES=$((RETRIES - 1))
    [[ $RETRIES -le 0 ]] && { echo "[WiseGrade] MySQL no respondió." 1>&2; exit 1; }
    echo "[WiseGrade] Esperando MySQL... ($RETRIES intentos restantes)" 1>&2
    sleep 3
  done
  echo "[WiseGrade] MySQL listo." 1>&2

else
  echo "[WiseGrade] Construyendo imagen MySQL..." 1>&2
  docker build -t "$IMAGE_NAME" -f docker/mysql/Dockerfile .

  echo "[WiseGrade] Creando contenedor MySQL..." 1>&2
  docker run -d \
    --name "$CONTAINER_NAME" \
    --restart unless-stopped \
    -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
    -e MYSQL_DATABASE="$MYSQL_DATABASE" \
    -e MYSQL_USER="$MYSQL_USER_VAR" \
    -e MYSQL_PASSWORD="$MYSQL_PASSWORD" \
    -p "${MYSQL_PORT}:3306" \
    -v "${VOLUME_NAME}:/var/lib/mysql" \
    --health-cmd="mysqladmin ping -h 127.0.0.1 -uroot -p\$MYSQL_ROOT_PASSWORD --silent" \
    --health-interval=10s \
    --health-timeout=5s \
    --health-retries=15 \
    --health-start-period=20s \
    "$IMAGE_NAME"

  echo "[WiseGrade] Esperando a que MySQL esté healthy..." 1>&2
  RETRIES=40
  until docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null | grep -q "healthy"; do
    RETRIES=$((RETRIES - 1))
    [[ $RETRIES -le 0 ]] && { echo "[WiseGrade] MySQL no levantó. Revisa: docker logs $CONTAINER_NAME" 1>&2; exit 1; }
    echo "[WiseGrade] Esperando MySQL... ($RETRIES intentos restantes)" 1>&2
    sleep 3
  done
  echo "[WiseGrade] MySQL listo." 1>&2
fi

echo "[WiseGrade] DB_URL=$DB_URL" 1>&2
echo "[WiseGrade] DB_USER=$DB_USER" 1>&2


# ─── Java 21 ─────────────────────────────────────────────────────────────────
DEFAULT_JAVA_HOME="/home/soporte/.jdk/jdk-21.0.8"

if [[ -z "${JAVA_HOME:-}" && -d "$DEFAULT_JAVA_HOME" ]]; then
  JAVA_HOME="$DEFAULT_JAVA_HOME"
  export JAVA_HOME
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  PATH="${JAVA_HOME}/bin:${PATH}"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "No se encontró 'java' en PATH. Instala Java 21 y/o define JAVA_HOME." 1>&2
  exit 1
fi

JAVA_MAJOR="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java\.specification\.version/ {print $2}' | head -n 1)"

if [[ -z "$JAVA_MAJOR" ]]; then
  echo "No se pudo detectar la versión de Java." 1>&2
  exit 1
fi

if [[ "$JAVA_MAJOR" != "21" ]]; then
  echo "Java 21 requerido. Detectado spec=$JAVA_MAJOR" 1>&2
  exit 1
fi

# ─── Arrancar backend ─────────────────────────────────────────────────────────
echo "[WiseGrade] Iniciando Spring Boot..." 1>&2
mvn -DskipTests spring-boot:run
