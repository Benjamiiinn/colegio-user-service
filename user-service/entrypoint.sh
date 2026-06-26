#!/bin/sh
set -e

# Detecta la IP del task en Fargate (awsvpc) y la exporta como
# EUREKA_INSTANCE_IP_ADDRESS para que Spring Cloud Eureka la registre
# en lugar de la IP link-local que auto-detecta.

# Funcion helper: descarga una URL y devuelve el body (usa curl o wget)
fetch() {
  url="$1"
  if command -v curl >/dev/null 2>&1; then
    curl -s --max-time 5 "$url" 2>/dev/null
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O - --timeout=5 "$url" 2>/dev/null
  fi
}

# Funcion helper: extrae la primera IPv4 de un JSON
extract_ip() {
  echo "$1" | grep -oE "\"IPv4Addresses\"[[:space:]]*:[[:space:]]*\[[^]]*\]" | head -1 | grep -oE "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}" | head -1
}

# 1) Intentar con la env var que Fargate suele setear
TASK_METADATA=""
if [ -n "$ECS_CONTAINER_METADATA_URI" ]; then
  TASK_METADATA="${ECS_CONTAINER_METADATA_URI}/task"
fi

# 2) Fallback: endpoint directo de Fargate (V3 -> V2 -> V4)
if [ -z "$TASK_METADATA" ]; then
  for url in "http://169.254.170.2/v3/task" "http://169.254.170.2/v2/task" "http://169.254.170.4/v4/task"; do
    if [ -n "$(fetch $url)" ]; then
      TASK_METADATA="$url"
      break
    fi
  done
fi

if [ -n "$TASK_METADATA" ]; then
  RESP=$(fetch "$TASK_METADATA")
  if [ -n "$RESP" ]; then
    IP=$(extract_ip "$RESP")
    if [ -n "$IP" ]; then
      export EUREKA_INSTANCE_IP_ADDRESS="$IP"
      echo "[entrypoint] Eureka instance IP: $EUREKA_INSTANCE_IP_ADDRESS (from $TASK_METADATA)"
    else
      echo "[entrypoint] WARN: no se encontro IP en respuesta de $TASK_METADATA"
      echo "[entrypoint] Response preview: $(echo $RESP | head -c 200)"
    fi
  else
    echo "[entrypoint] WARN: respuesta vacia de $TASK_METADATA"
  fi
else
  echo "[entrypoint] WARN: no se pudo alcanzar metadata endpoint"
fi

exec java -jar app.jar "$@"

