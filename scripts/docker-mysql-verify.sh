#!/usr/bin/env bash
set -euo pipefail
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.mysql.yml}"
SERVICE="${MYSQL_SERVICE_NAME:-mysql}"
DB_NAME="${MYSQL_DATABASE:-wisegrade}"
ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root_wisegrade}"
if ! command -v docker >/dev/null 2>&1; then
  echo "docker not found in PATH" >&2
  exit 1
fi
run_query() {
  local query="$1"
  docker compose -f "${COMPOSE_FILE}" exec -T "${SERVICE}" \
    mysql -uroot -p"${ROOT_PASSWORD}" -D "${DB_NAME}" -Nse "${query}"
}
echo "[verify] Checking table count..."
TABLE_COUNT="$(run_query 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE();')"
echo "[verify] tables=${TABLE_COUNT}"
echo "[verify] Checking key rows..."
for table in docentes estudiantes usuarios examenes preguntas intentos_examen flyway_schema_history; do
  value="$(run_query "SELECT COUNT(*) FROM ${table};")"
  echo "[verify] ${table}=${value}"
done
echo "[verify] Checking flyway max version..."
MAX_FLYWAY="$(run_query 'SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE version REGEXP "^[0-9]+$";')"
echo "[verify] flyway_max_version=${MAX_FLYWAY}"
echo "[verify] Sample users from auth table..."
run_query "SELECT documento, rol, activo FROM usuarios ORDER BY id LIMIT 5;" | sed 's/^/[verify] /'
echo "[verify] Done."
