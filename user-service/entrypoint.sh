#!/bin/sh
set -e

# Detecta la IP del task en Fargate (awsvpc) y la exporta como
# EUREKA_INSTANCE_IP_ADDRESS para que Spring Cloud Eureka la registre
# en lugar de la IP link-local que auto-detecta.

# 1) Intentar con la env var que Fargate suele setear
TASK_METADATA=""
if [ -n "$ECS_CONTAINER_METADATA_URI" ]; then
  TASK_METADATA="${ECS_CONTAINER_METADATA_URI}/task"
fi

# 2) Fallback: endpoint directo de Fargate (V3 -> V2 -> V4)
if [ -z "$TASK_METADATA" ] || ! curl -s --max-time 3 -o /dev/null -w "%{http_code}" "$TASK_METADATA" 2>/dev/null | grep -q "200"; then
  for url in "http://169.254.170.2/v3/task" "http://169.254.170.2/v2/task" "http://169.254.170.4/v4/task"; do
    if curl -s --max-time 3 -o /dev/null -w "%{http_code}" "$url" 2>/dev/null | grep -q "200"; then
      TASK_METADATA="$url"
      break
    fi
  done
fi

if [ -n "$TASK_METADATA" ]; then
  RESP=$(curl -s --max-time 5 "$TASK_METADATA" 2>/dev/null || echo "")
  if [ -n "$RESP" ]; then
    # Extrae la primera IPv4 dentro de "IPv4Addresses":[...] o "PrivateIp" o "IPv4Address"
    IP=$(echo "$RESP" | grep -oE "\"IPv4Addresses\"\s*:\s*\[[^]]*\]" | head -1 | grep -oE "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}" | head -1)
    if [ -z "$IP" ]; then
      IP=$(echo "$RESP" | grep -oE "\"PrivateIp\"\s*:\s*\"[0-9.]*\"" | head -1 | grep -oE "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}" | head -1)
    fi
    if [ -z "$IP" ]; then
      IP=$(echo "$RESP" | grep -oE "\"IPv4Address\"\s*:\s*\"[0-9.]*\"" | head -1 | grep -oE "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}" | head -1)
    fi
    if [ -n "$IP" ]; then
      export EUREKA_INSTANCE_IP_ADDRESS="$IP"
      echo "[entrypoint] Eureka instance IP: $EUREKA_INSTANCE_IP_ADDRESS (from $TASK_METADATA)"
    else
      echo "[entrypoint] WARN: no se encontro IP en respuesta de $TASK_METADATA, usando default"
    fi
  else
    echo "[entrypoint] WARN: respuesta vacia de $TASK_METADATA, usando default"
  fi
else
  echo "[entrypoint] WARN: no se pudo alcanzar metadata endpoint, usando default"
fi

exec java -jar app.jar "$@"

