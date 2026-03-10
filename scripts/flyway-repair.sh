#!/usr/bin/env bash
set -euo pipefail

on_error() {
  exit_code=$?
  echo 1>&2
  echo "[WiseGrade] Flyway repair falló (exit=$exit_code)." 1>&2
  echo "Sugerencias rápidas:" 1>&2
  echo "- Verifica DB_URL/DB_USER/DB_PASSWORD." 1>&2
  echo "- Si tu clave tiene caracteres especiales, usa comillas simples en .env.local: DB_PASSWORD='...'." 1>&2
  echo "- Puedes ver el detalle con: mvn -DskipTests -e flyway:repair" 1>&2
  echo 1>&2
  exit "$exit_code"
}

trap on_error ERR

cd "$(dirname "$0")/.."

# Optional local env file (not committed) for developer convenience.
# Example contents:
#   DB_PASSWORD='$$Lagp2026$$'
if [[ -f .env.local ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env.local
  set +a
fi

: "${DB_URL:=jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true}"
: "${DB_USER:=wisegrade_app}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "DB_PASSWORD no está definida. Ingrésala para ejecutar flyway repair (no se mostrará)." 1>&2
  read -r -s -p "DB_PASSWORD: " DB_PASSWORD
  echo 1>&2
fi

export DB_PASSWORD

export DB_URL DB_USER DB_PASSWORD

echo "[WiseGrade] Ejecutando Flyway repair..." 1>&2
echo "[WiseGrade] DB_URL=$DB_URL" 1>&2
echo "[WiseGrade] DB_USER=$DB_USER" 1>&2

mvn -q -DskipTests \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASSWORD" \
  -Dflyway.locations=filesystem:src/main/resources/db/migration \
  flyway:repair

echo "[WiseGrade] Flyway repair OK." 1>&2
