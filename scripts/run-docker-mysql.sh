#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# run-docker-mysql.sh
# Levanta el backend Spring Boot apuntando al contenedor Docker wisegrade-mysql.
# Siempre usa las credenciales de .env.docker (ignora .env.local).
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

on_error() {
  exit_code=$?
  echo 1>&2
  echo "[WiseGrade] Backend no pudo iniciar (exit=$exit_code)." 1>&2
  echo "Sugerencias rápidas:" 1>&2
  echo "- Verifica que el contenedor esté corriendo: docker ps | grep wisegrade-mysql" 1>&2
  echo "- Revisa logs de MySQL: docker logs wisegrade-mysql" 1>&2
  echo "- Re-ejecuta con stacktrace: mvn -DskipTests spring-boot:run -e" 1>&2
  echo 1>&2
  exit "$exit_code"
}

trap on_error ERR

cd "$(dirname "$0")/.."

ENV_FILE=".env.docker"
COMPOSE_FILE="docker-compose.mysql.yml"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[WiseGrade] No se encontró $ENV_FILE. Copia .env.docker.example y configúralo." 1>&2
  exit 1
fi

echo "[WiseGrade] Cargando credenciales desde $ENV_FILE..." 1>&2
set -a; source "$ENV_FILE"; set +a

# ─── MySQL Docker: levantar si no está corriendo ──────────────────────────────
CONTAINER_STATE="$(docker inspect --format='{{.State.Status}}' wisegrade-mysql 2>/dev/null || echo 'missing')"

if [[ "$CONTAINER_STATE" == "running" ]]; then
  HEALTH="$(docker inspect --format='{{.State.Health.Status}}' wisegrade-mysql 2>/dev/null || echo 'unknown')"
  echo "[WiseGrade] MySQL Docker ya está corriendo (health=$HEALTH)." 1>&2
  if [[ "$HEALTH" != "healthy" ]]; then
    echo "[WiseGrade] Esperando a que MySQL esté healthy..." 1>&2
    RETRIES=30
    until docker inspect --format='{{.State.Health.Status}}' wisegrade-mysql 2>/dev/null | grep -q "healthy"; do
      RETRIES=$((RETRIES - 1))
      [[ $RETRIES -le 0 ]] && { echo "[WiseGrade] MySQL no respondió a tiempo. Revisa: docker logs wisegrade-mysql" 1>&2; exit 1; }
      echo "[WiseGrade] Esperando... ($RETRIES intentos restantes)" 1>&2
      sleep 3
    done
  fi
else
  echo "[WiseGrade] Levantando MySQL Docker..." 1>&2
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build

  echo "[WiseGrade] Esperando a que MySQL esté healthy..." 1>&2
  RETRIES=40
  until docker inspect --format='{{.State.Health.Status}}' wisegrade-mysql 2>/dev/null | grep -q "healthy"; do
    RETRIES=$((RETRIES - 1))
    [[ $RETRIES -le 0 ]] && { echo "[WiseGrade] MySQL no levantó a tiempo. Revisa: docker logs wisegrade-mysql" 1>&2; exit 1; }
    echo "[WiseGrade] Esperando MySQL... ($RETRIES intentos restantes)" 1>&2
    sleep 3
  done
  echo "[WiseGrade] MySQL listo." 1>&2
fi

# ─── Variables del backend ────────────────────────────────────────────────────
export DB_URL="${DB_URL:-jdbc:mysql://localhost:${MYSQL_PORT:-3306}/${MYSQL_DATABASE:-wisegrade}?useSSL=false&allowPublicKeyRetrieval=true}"
export DB_USER="${DB_USER:-${MYSQL_USER:-wisegrade_app}}"
export DB_PASSWORD="${DB_PASSWORD:-${MYSQL_PASSWORD:-wisegrade_password}}"

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
echo "[WiseGrade] Iniciando Spring Boot con MySQL Docker..." 1>&2
mvn -DskipTests spring-boot:run

