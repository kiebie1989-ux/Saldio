#!/bin/sh
# Rendert die Runtime-Config des SPA beim Containerstart aus Umgebungsvariablen (12-factor).
# nginx:alpine fuehrt /docker-entrypoint.d/*.sh vor dem Start automatisch aus.
set -eu

: "${ISSUER:?ISSUER (OIDC-Issuer-URL, z.B. https://auth.example.com/realms/bwa) muss gesetzt sein}"
CLIENT_ID="${CLIENT_ID:-bwa-app}"
REQUIRE_HTTPS="${REQUIRE_HTTPS:-true}"

cat > /usr/share/nginx/html/config.json <<EOF
{
  "issuer": "${ISSUER}",
  "clientId": "${CLIENT_ID}",
  "requireHttps": ${REQUIRE_HTTPS}
}
EOF

echo "[render-config] config.json erzeugt: issuer=${ISSUER} clientId=${CLIENT_ID} requireHttps=${REQUIRE_HTTPS}"
