#!/bin/sh
# Hospital Frontend with DuckDNS SSL 초기화 스크립트
set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}🏥 Hospital Frontend with DuckDNS SSL 시작 중...${NC}"

# 환경변수 기본값 설정
export SERVER_IP=${SERVER_IP:-localhost}
export BACKEND_HOST=${BACKEND_HOST:-hospital-backend}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export ENVIRONMENT=${ENVIRONMENT:-production}
export DUCKDNS_DOMAIN=${DUCKDNS_DOMAIN:-}
export DUCKDNS_TOKEN=${DUCKDNS_TOKEN:-}
export ACME_EMAIL=${ACME_EMAIL:-admin@example.com}

# DuckDNS 설정 검증
if [ -z "$DUCKDNS_DOMAIN" ] || [ -z "$DUCKDNS_TOKEN" ]; then
    echo -e "${RED}❌ DuckDNS 설정이 누락되었습니다!${NC}"
    echo -e "  필수 환경변수:"
    echo -e "  - DUCKDNS_DOMAIN: yourapp.duckdns.org"
    echo -e "  - DUCKDNS_TOKEN: your_duckdns_token"
    echo -e "  - ACME_EMAIL: your_email@domain.com (선택사항)"
    exit 1
fi

# DuckDNS IP 업데이트
echo -e "${YELLOW}🔄 DuckDNS IP 업데이트 중...${NC}"
CURRENT_IP=$(curl -s https://ipv4.icanhazip.com || curl -s https://api.ipify.org)
if [ -n "$CURRENT_IP" ]; then
    echo -e "  현재 IP: ${CURRENT_IP}"
    # DuckDNS 업데이트
    DUCKDNS_SUBDOMAIN=$(echo $DUCKDNS_DOMAIN | cut -d'.' -f1)
    UPDATE_RESULT=$(curl -s "https://www.duckdns.org/update?domains=${DUCKDNS_SUBDOMAIN}&token=${DUCKDNS_TOKEN}&ip=${CURRENT_IP}")
    
    if [ "$UPDATE_RESULT" = "OK" ]; then
        echo -e "${GREEN}✅ DuckDNS IP 업데이트 완료${NC}"
    else
        echo -e "${YELLOW}⚠️ DuckDNS 업데이트 응답: ${UPDATE_RESULT}${NC}"
    fi
else
    echo -e "${RED}❌ 현재 IP를 가져올 수 없습니다${NC}"
fi

# 환경변수 출력
echo -e "${BLUE}📋 환경 설정:${NC}"
echo -e "  Environment: ${ENVIRONMENT}"
echo -e "  DuckDNS Domain: ${DUCKDNS_DOMAIN}"
echo -e "  Backend: ${BACKEND_HOST}:${BACKEND_PORT}"
echo -e "  ACME Email: ${ACME_EMAIL}"

# 로그 디렉토리 생성
mkdir -p /var/log/caddy

# 템플릿 파일 존재 확인
if [ ! -f "/etc/caddy/Caddyfile.template" ]; then
    echo -e "${RED}❌ Caddyfile.template이 없습니다!${NC}"
    exit 1
fi

# 환경변수를 사용해 Caddyfile 생성
echo -e "${YELLOW}📝 Caddyfile 생성 중...${NC}"
envsubst '${DUCKDNS_DOMAIN} ${DUCKDNS_TOKEN} ${BACKEND_HOST} ${BACKEND_PORT} ${ENVIRONMENT} ${ACME_EMAIL}' \
  < /etc/caddy/Caddyfile.template > /etc/caddy/Caddyfile

# 생성된 Caddyfile 검증
if [ ! -f "/etc/caddy/Caddyfile" ]; then
    echo -e "${RED}❌ Caddyfile 생성 실패!${NC}"
    exit 1
fi

# Caddy 설정 문법 검사
echo -e "${YELLOW}🔍 Caddy 설정 검증 중...${NC}"
if caddy validate --config /etc/caddy/Caddyfile > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Caddy 설정 검증 완료${NC}"
else
    echo -e "${RED}❌ Caddy 설정 오류 발견${NC}"
    echo -e "${YELLOW}생성된 Caddyfile:${NC}"
    cat /etc/caddy/Caddyfile
    exit 1
fi

# 정적 파일 확인
if [ -f "/usr/share/caddy/index.html" ]; then
    echo -e "${GREEN}✅ 정적 파일 확인 완료${NC}"
    FILE_COUNT=$(find /usr/share/caddy -type f | wc -l)
    echo -e "  총 파일 수: ${FILE_COUNT}개"
else
    echo -e "${YELLOW}⚠️ index.html 파일이 없습니다!${NC}"
    ls -la /usr/share/caddy/
fi

# SSL 설정 안내
echo -e "${GREEN}🔐 SSL 설정 정보:${NC}"
echo -e "  📱 브라우저 접속: https://${DUCKDNS_DOMAIN}"
echo -e "  🔒 Let's Encrypt 인증서 자동 발급"
echo -e "  🔄 HTTP → HTTPS 자동 리다이렉트"

# 데이터 디렉토리 권한 설정
mkdir -p /data/caddy /config/caddy
chown -R caddy:caddy /data/caddy /config/caddy 2>/dev/null || true

# DuckDNS 백그라운드 업데이트 스크립트 생성
cat > /usr/local/bin/duckdns-update.sh << EOF
#!/bin/sh
# DuckDNS IP 주기적 업데이트
while true; do
    sleep 300  # 5분마다 실행
    CURRENT_IP=\$(curl -s https://ipv4.icanhazip.com 2>/dev/null || curl -s https://api.ipify.org 2>/dev/null)
    if [ -n "\$CURRENT_IP" ]; then
        SUBDOMAIN=\$(echo ${DUCKDNS_DOMAIN} | cut -d'.' -f1)
        curl -s "https://www.duckdns.org/update?domains=\${SUBDOMAIN}&token=${DUCKDNS_TOKEN}&ip=\${CURRENT_IP}" > /dev/null
    fi
done
EOF

chmod +x /usr/local/bin/duckdns-update.sh

# 백그라운드에서 DuckDNS 업데이트 실행
/usr/local/bin/duckdns-update.sh &

echo -e "${GREEN}🎉 초기화 완료! Caddy 서버 시작...${NC}"
echo -e "${BLUE}🌐 접속 URL: https://${DUCKDNS_DOMAIN}${NC}"

# 전달받은 명령어 실행
exec "$@"
