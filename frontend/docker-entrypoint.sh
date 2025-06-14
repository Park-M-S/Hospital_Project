#!/bin/sh
# Hospital Frontend SSL 초기화 스크립트
set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}🏥 Hospital Frontend 시작 중...${NC}"

# 환경변수 기본값 설정
export SERVER_IP=${SERVER_IP:-localhost}
export BACKEND_HOST=${BACKEND_HOST:-hospital-backend}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export ENVIRONMENT=${ENVIRONMENT:-production}
export SSL_TYPE=${SSL_TYPE:-internal}  # internal, letsencrypt, custom, none

# SSL 타입 결정 로직
if [ "$SSL_TYPE" = "none" ] || [ "$SERVER_IP" = "localhost" ]; then
    SSL_MODE="HTTP Only"
    USE_SSL=false
elif [ "$SSL_TYPE" = "letsencrypt" ]; then
    SSL_MODE="Let's Encrypt"
    USE_SSL=true
elif [ "$SSL_TYPE" = "custom" ]; then
    SSL_MODE="Custom Certificate"
    USE_SSL=true
else
    SSL_MODE="Self-Signed Certificate"
    USE_SSL=true
fi

# 환경변수 출력
echo -e "${BLUE}📋 환경 설정:${NC}"
echo -e "  Environment: ${ENVIRONMENT}"
echo -e "  Server IP: ${SERVER_IP}"
echo -e "  Backend: ${BACKEND_HOST}:${BACKEND_PORT}"
echo -e "  SSL Type: ${SSL_MODE}"

# 로그 디렉토리 생성
mkdir -p /var/log/caddy

# 템플릿 파일 존재 확인
if [ ! -f "/etc/caddy/Caddyfile.template" ]; then
    echo -e "${RED}❌ Caddyfile.template이 없습니다!${NC}"
    exit 1
fi

# 환경변수를 사용해 Caddyfile 생성
echo -e "${YELLOW}📝 Caddyfile 생성 중...${NC}"
envsubst '${SERVER_IP} ${BACKEND_HOST} ${BACKEND_PORT} ${ENVIRONMENT} ${SSL_TYPE}' \
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

# SSL 인증서 관련 확인
if [ "$USE_SSL" = true ]; then
    echo -e "${YELLOW}🔐 SSL 설정 확인 중...${NC}"
    
    # 사용자 정의 인증서 확인
    if [ "$SSL_TYPE" = "custom" ]; then
        if [ ! -f "/etc/ssl/certs/server.crt" ] || [ ! -f "/etc/ssl/private/server.key" ]; then
            echo -e "${RED}❌ 사용자 정의 SSL 인증서 파일이 없습니다!${NC}"
            echo -e "  필요 파일: /etc/ssl/certs/server.crt, /etc/ssl/private/server.key"
            exit 1
        fi
        echo -e "${GREEN}✅ 사용자 정의 SSL 인증서 확인 완료${NC}"
    fi
    
    # 자체 서명 인증서 안내
    if [ "$SSL_TYPE" = "internal" ]; then
        echo -e "${YELLOW}🔐 자체 서명 인증서 사용 - 브라우저 보안 경고가 표시됩니다${NC}"
        echo -e "${BLUE}  브라우저 접속 방법:${NC}"
        echo -e "  1. https://${SERVER_IP} 접속"
        echo -e "  2. '고급' → '안전하지 않음을 승인하고 계속' 클릭"
    fi
else
    echo -e "${BLUE}🌐 HTTP 전용 모드 - SSL 없음${NC}"
    echo -e "  브라우저에서 http://${SERVER_IP} 접속"
fi

# 데이터 디렉토리 권한 설정
mkdir -p /data/caddy /config/caddy
chown -R caddy:caddy /data/caddy /config/caddy 2>/dev/null || true

echo -e "${GREEN}🎉 초기화 완료! Caddy 서버 시작...${NC}"

# 전달받은 명령어 실행
exec "$@"
