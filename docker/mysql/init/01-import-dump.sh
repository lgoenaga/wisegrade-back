#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${MYSQL_DATABASE:-}" ]]; then
  echo "[init] MYSQL_DATABASE is required to import dump." >&2
  exit 1
fi

echo "[init] Importing /opt/bootstrap/dump-wisegrade.sql into database '${MYSQL_DATABASE}'..."
mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" --database="${MYSQL_DATABASE}" < /opt/bootstrap/dump-wisegrade.sql
echo "[init] Dump import completed."

