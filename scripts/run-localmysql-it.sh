#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

: "${DB_URL:=jdbc:mysql://localhost:3306/wisegrade?useSSL=false&allowPublicKeyRetrieval=true}"
: "${DB_USER:=wisegrade_app}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "DB_PASSWORD no está definida. Ingrésala para ejecutar las pruebas (no se mostrará)." 1>&2
  read -r -s -p "DB_PASSWORD: " DB_PASSWORD
  echo 1>&2
fi

export DB_PASSWORD

export DB_URL DB_USER DB_PASSWORD

JAVA_HOME="/home/soporte/.jdk/jdk-21.0.8"
export JAVA_HOME

PATH="${JAVA_HOME}/bin:${PATH}" \
  mvn -Dwisegrade.localmysql.it=true test
