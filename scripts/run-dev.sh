#!/usr/bin/env bash
set -euo pipefail

on_error() {
  exit_code=$?
  echo 1>&2
  echo "[WiseGrade] Backend no pudo iniciar (exit=$exit_code)." 1>&2
  echo "Sugerencias rápidas:" 1>&2
  echo "- Verifica credenciales MySQL (usuario/clave) y permisos." 1>&2
  echo "- Si tu clave tiene caracteres especiales (ej: '$'), exporta con comillas simples: DB_PASSWORD='...'" 1>&2
  echo "- Re-ejecuta con stacktrace: mvn -DskipTests spring-boot:run -e" 1>&2
  echo "- Confirma que tu DB_URL tenga allowPublicKeyRetrieval=true si MySQL lo requiere." 1>&2
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
  echo "DB_PASSWORD no está definida. Ingrésala para iniciar el backend (no se mostrará)." 1>&2
  read -r -s -p "DB_PASSWORD: " DB_PASSWORD
  echo 1>&2
fi

export DB_PASSWORD

export DB_URL DB_USER DB_PASSWORD

echo "[WiseGrade] DB_URL=$DB_URL" 1>&2
echo "[WiseGrade] DB_USER=$DB_USER" 1>&2

if [[ "$DB_URL" != *"allowPublicKeyRetrieval="* ]]; then
  echo "[WiseGrade] Aviso: DB_URL no incluye allowPublicKeyRetrieval=..." 1>&2
  echo "           Si tu MySQL usa caching_sha2_password sin SSL, agrega allowPublicKeyRetrieval=true." 1>&2
fi

if [[ "$DB_URL" == *"allowPublicKeyRetrieval=false"* ]]; then
  echo "[WiseGrade] Aviso: allowPublicKeyRetrieval=false en DB_URL. Si ves 'Public Key Retrieval is not allowed', cámbialo a true." 1>&2
fi

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

JAVA_VERSION_LINE="$(java -version 2>&1 | head -n 1 || true)"
JAVA_MAJOR="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java\.specification\.version/ {print $2}' | head -n 1)"

if [[ -z "$JAVA_MAJOR" ]]; then
  echo "No se pudo detectar la versión de Java. Salida: $JAVA_VERSION_LINE" 1>&2
  exit 1
fi

if [[ "$JAVA_MAJOR" != "21" ]]; then
  echo "Java 21 requerido. Detectado: $JAVA_VERSION_LINE (spec=$JAVA_MAJOR)" 1>&2
  exit 1
fi

mvn -DskipTests spring-boot:run
