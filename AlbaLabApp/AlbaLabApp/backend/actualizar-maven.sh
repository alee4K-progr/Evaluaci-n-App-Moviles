#!/bin/sh
# Copia configuracion-maven/maven-wrapper.properties a cada servicio (cambia el mirror del wrapper en un solo lugar).
RAIZ="$(cd "$(dirname "$0")" && pwd)"
for d in "$RAIZ"/*/; do
  if [ -f "$d/pom.xml" ]; then
    mkdir -p "$d/.mvn/wrapper"
    cp "$RAIZ/configuracion-maven/maven-wrapper.properties" "$d/.mvn/wrapper/maven-wrapper.properties"
    echo "Actualizado: $d"
  fi
done
