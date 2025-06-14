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

# DuckDNS 설정 확인 (강제 종료하지 않음)
if [ -z "$DUCKDNS_DOMAIN" ] || [ -z "$DUCKDNS_TOKEN" ]; then
    echo -e "${YELLOW}⚠️ DuckDNS 설정이 없습니다. HTTP 전용 모드로 실행합니다.${NC}"
    USE_DUCKDNS=false
    SSL_MODE="HTTP Only"
else
    echo -e "${GREEN}✅ DuckDNS 설정 발견. HTTPS 모드로 실행합니다.${NC}"
    USE_DUCKDNS=true
    SSL_MODE="DuckDNS + Let's Encrypt"
fi

# DuckDNS IP 업데이트 (DuckDNS 사용시만)
if [ "$USE_DUCKDNS" = true ]; then
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
fi

# 환경변수 출력
echo -e "${BLUE}📋 환경 설정:${NC}"
echo -e "  Environment: ${ENVIRONMENT}"
echo -e "  SSL Mode: ${SSL_MODE}"
if [ "$USE_DUCKDNS" = true ]; then
    echo -e "  DuckDNS Domain: ${DUCKDNS_DOMAIN}"
    echo -e "  ACME Email: ${ACME_EMAIL}"
else
    echo -e "  Server IP: ${SERVER_IP:-'모든 인터페이스'}"
fi
echo -e "  Backend: ${BACKEND_HOST}:${BACKEND_PORT}"

# 로그 디렉토리 생성
mkdir -p /var/log/caddy

# 템플릿 파일 존재 확인
if [ ! -f "/etc/caddy/Caddyfile.template" ]; then
    echo -e "${RED}❌ Caddyfile.template이 없습니다!${NC}"
    exit 1
fi

# 환경변수를 사용해 Caddyfile 생성
echo -e "${YELLOW}📝 Caddyfile 생성 중...${NC}"
if [ "$USE_DUCKDNS" = true ]; then
    # DuckDNS + SSL 모드
    envsubst '${DUCKDNS_DOMAIN} ${DUCKDNS_TOKEN} ${BACKEND_HOST} ${BACKEND_PORT} ${ENVIRONMENT} ${ACME_EMAIL}' \
      < /etc/caddy/Caddyfile.template > /etc/caddy/Caddyfile
else
    # HTTP 전용 모드 - 기본 Caddyfile 생성
    cat > /etc/caddy/Caddyfile << 'CADDY_EOF'
{
    auto_https off
}

:80 {
    root * /usr/share/caddy
    file_server
    try_files {path} /index.html
    
    handle /api/* {
        reverse_proxy ${BACKEND_HOST}:${BACKEND_PORT} {
            header_up Host {upstream_hostport}
            header_up X-Real-IP {remote_host}
        }
    }
    
    header {
        X-Content-Type-Options "nosniff"
        X-Frame-Options "DENY" 
        X-XSS-Protection "1; mode=block"
        Referrer-Policy "strict-origin-when-cross-origin"
        X-Environment "${ENVIRONMENT}"
    }
    
    handle_errors {
        respond "Error: {err.status_code} - {err.status_text}"
    }
}
CADDY_EOF
    
    # 환경변수 치환
    envsubst '${BACKEND_HOST} ${BACKEND_PORT} ${ENVIRONMENT}' \
      < /etc/caddy/Caddyfile > /etc/caddy/Caddyfile.tmp && \
      mv /etc/caddy/Caddyfile.tmp /etc/caddy/Caddyfile
fi

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
if [ "$USE_DUCKDNS" = true ]; then
    echo -e "${GREEN}🔐 HTTPS 모드:${NC}"
    echo -e "  📱 브라우저 접속: https://${DUCKDNS_DOMAIN}"
    echo -e "  🔒 Let's Encrypt 인증서 자동 발급"
    echo -e "  🔄 HTTP → HTTPS 자동 리다이렉트"
    
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
else
    echo -e "${BLUE}🌐 HTTP 모드:${NC}"
    echo -e "  📱 브라우저 접속: http://$(curl -s https://ipv4.icanhazip.com 2>/dev/null || echo 'your-server-ip')"
    echo -e "  ⚠️ SSL 없음 - DuckDNS 설정 후 HTTPS 사용 가능"
fi

# 데이터 디렉토리 권한 설정
mkdir -p /data/caddy /config/caddy
chown -R caddy:caddy /data/caddy /config/caddy 2>/dev/null || true

echo -e "${GREEN}🎉 초기화 완료! Caddy 서버 시작...${NC}"
if [ "$USE_DUCKDNS" = true ]; then
    echo -e "${BLUE}🌐 접속 URL: https://${DUCKDNS_DOMAIN}${NC}"
else
    echo -e "${BLUE}🌐 접속 URL: http://$(curl -s https://ipv4.icanhazip.com 2>/dev/null || echo 'your-server-ip')${NC}"
fi

# 전달받은 명령어 실행
exec "$@"
