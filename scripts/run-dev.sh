#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

: "${DB_URL:=jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true}"
: "${DB_USER:=wisegrade_app}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "DB_PASSWORD no está definida. Ingrésala para iniciar el backend (no se mostrará)." 1>&2
  read -r -s -p "DB_PASSWORD: " DB_PASSWORD
  echo 1>&2
  export DB_PASSWORD
fi

export DB_URL DB_USER

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
