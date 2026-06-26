#!/bin/sh
set -e

# Detecta la IP del task en Fargate (awsvpc) desde el ECS metadata endpoint v2
# y la exporta como EUREKA_INSTANCE_IP_ADDRESS para que Spring Cloud Eureka
# la registre en lugar de la IP link-local que auto-detecta.
TASK_METADATA="${ECS_CONTAINER_METADATA_URI}/task"

if [ -n "$ECS_CONTAINER_METADATA_URI" ]; then
  RESP=$(curl -s --max-time 5 "$TASK_METADATA" 2>/dev/null || echo "")
  if [ -n "$RESP" ]; then
    # Extrae la primera IPv4 dentro de "IPv4Addresses":[...]
    IP=$(echo "$RESP" | grep -oE '"IPv4Addresses":\[[^]]*\]' \
         | head -1 \
         | grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' \
         | head -1)
    if [ -n "$IP" ]; then
      export EUREKA_INSTANCE_IP_ADDRESS="$IP"
      echo "[entrypoint] Eureka instance IP: $EUREKA_INSTANCE_IP_ADDRESS"
    else
      echo "[entrypoint] WARN: no se pudo extraer IP del metadata, usando default"
    fi
  else
    echo "[entrypoint] WARN: metadata vacio, usando default"
  fi
fi

exec java -jar app.jar "$@"
