#!/bin/sh
# Equivalente en Linux/macOS de test-all.bat (usa el mirror de configuracion-maven/settings.xml)
set -e
RAIZ="$(cd "$(dirname "$0")" && pwd)"
for s in api-gateway ms-admin ms-contenidos ms-vinculacion; do
  echo "== $s =="
  (cd "$RAIZ/$s" && ./mvnw clean test -s ../configuracion-maven/settings.xml)
done
