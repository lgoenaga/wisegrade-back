FROM mysql:8.0.45

# Keep the dump outside docker-entrypoint-initdb.d so we can explicitly import it
# into MYSQL_DATABASE and avoid assumptions about default DB selection.
COPY Documents/dump-wisegrade-202604201853.sql /opt/bootstrap/dump-wisegrade.sql
COPY docker/mysql/init/01-import-dump.sh /docker-entrypoint-initdb.d/01-import-dump.sh

RUN chmod +x /docker-entrypoint-initdb.d/01-import-dump.sh

