#!/bin/sh
# Hospital Frontend 자체 서명 SSL 초기화 스크립트
set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${GREEN}🏥 Hospital Frontend (Self-Signed SSL) 시작 중...${NC}"

# 환경변수 기본값 설정
export SERVER_IP=${SERVER_IP:-localhost}
export BACKEND_HOST=${BACKEND_HOST:-hospital-backend}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export ENVIRONMENT=${ENVIRONMENT:-production}

# 환경변수 출력
echo -e "${BLUE}📋 환경 설정:${NC}"
echo -e "  Environment: ${ENVIRONMENT}"
echo -e "  Server IP: ${SERVER_IP}"
echo -e "  Backend: ${BACKEND_HOST}:${BACKEND_PORT}"
echo -e "  SSL Type: Self-Signed Certificate"

# 로그 디렉토리 생성
mkdir -p /var/log/caddy

# 환경변수를 사용해 Caddyfile 생성
echo -e "${YELLOW}📝 Caddyfile 생성 중...${NC}"
envsubst '${SERVER_IP} ${BACKEND_HOST} ${BACKEND_PORT} ${ENVIRONMENT}' \
  < /etc/caddy/Caddyfile.template > /etc/caddy/Caddyfile

# 정적 파일 확인
if [ -f "/usr/share/caddy/index.html" ]; then
    echo -e "${GREEN}✅ 정적 파일 확인 완료${NC}"
    FILE_COUNT=$(find /usr/share/caddy -type f | wc -l)
    echo -e "  총 파일 수: ${FILE_COUNT}개"
else
    echo -e "${YELLOW}⚠️ index.html 파일이 없습니다!${NC}"
    ls -la /usr/share/caddy/
fi

# SSL 경고 안내
echo -e "${YELLOW}🔐 자체 서명 인증서 사용 - 브라우저 보안 경고가 표시됩니다${NC}"
echo -e "${GREEN}🎉 초기화 완료! Caddy 서버 시작...${NC}"

# 전달받은 명령어 실행
exec "$@"
