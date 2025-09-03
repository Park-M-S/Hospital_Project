#!/bin/bash
# Hospital Project 백엔드 전용 배포 스크립트 (DuckDNS 지원)
set -e

echo "🚀 Hospital Project 백엔드 배포 시작..."

# GitHub Actions가 아닌 로컬 실행 시 IP 가져오기
if [ -z "$GITHUB_ACTIONS" ]; then
    PUBLIC_IP=$(curl -s --connect-timeout 5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")
    export SERVER_IP=${SERVER_IP:-$PUBLIC_IP}
fi

# 환경 변수 기본값 설정
export IMAGE_TAG=${IMAGE_TAG:-latest}
export DB_PASSWORD=${DB_PASSWORD:-1234}
export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD:-1234}
export ENVIRONMENT=${ENVIRONMENT:-production}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export DB_PORT=${DB_PORT:-3500}

# DuckDNS 설정 (GitHub Actions에서 전달받거나 기본값 사용)
export DUCKDNS_DOMAIN=${DUCKDNS_DOMAIN:-""}
export DUCKDNS_TOKEN=${DUCKDNS_TOKEN:-""}

# DuckDNS 도메인에서 subdomain만 추출
if [ -n "$DUCKDNS_DOMAIN" ]; then
    export DUCKDNS_SUBDOMAIN=$(echo "$DUCKDNS_DOMAIN" | cut -d'.' -f1)
fi

echo "📋 현재 설정:"
echo "  서버 IP: ${SERVER_IP}"
echo "  환경: ${ENVIRONMENT}"
echo "  이미지 태그: ${IMAGE_TAG}"
echo "  백엔드 포트: ${BACKEND_PORT}"
echo "  DB 포트: ${DB_PORT}"

# DuckDNS 설정 확인 및 표시
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "  🦆 DuckDNS 도메인: ${DUCKDNS_DOMAIN}"
    echo "  🦆 DuckDNS 자동 업데이트: 활성화"
    DOMAIN_URL="http://${DUCKDNS_DOMAIN}"
else
    echo "  🔗 접속 방식: 직접 IP 접근"
    DOMAIN_URL="http://${SERVER_IP}"
fi

# GitHub Actions가 아닌 로컬 실행 시 경고
if [ -z "$GITHUB_ACTIONS" ]; then
    echo "⚠️  로컬 실행 감지: 기본값을 사용합니다."
    echo "   프로덕션 배포는 GitHub Actions를 사용하세요!"
fi

echo "⏹️ 기존 컨테이너 중지..."
docker-compose -f docker-compose.prod.yml down || true

# 필요한 디렉토리 생성
sudo mkdir -p /opt/hospital/data
sudo mkdir -p /opt/hospital/logs/backend
sudo chown -R ec2-user:ec2-user /opt/hospital/

echo "▶️ 새 컨테이너 시작..."
docker-compose -f docker-compose.prod.yml up -d

# 컨테이너 시작 대기
echo "⏳ 컨테이너 시작 대기..."
sleep 10

# 서비스 상태 확인
echo "📊 서비스 상태 확인:"
docker-compose -f docker-compose.prod.yml ps

echo "🧹 이미지 정리..."
docker system prune -f

echo "🎉 배포 완료!"
echo ""
echo "📍 접속 정보:"
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "  🦆 DuckDNS 백엔드 API: ${DOMAIN_URL}:8888"
    echo "  🌍 도메인: ${DUCKDNS_DOMAIN}"
else
    echo "  🔗 백엔드 API: ${DOMAIN_URL}:8888"
fi
echo ""
echo "🔧 API 테스트:"
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "  curl ${DOMAIN_URL}:8888/api/proDoc/status"
    echo "  curl ${DOMAIN_URL}:8888/api/list"
else
    echo "  curl http://${SERVER_IP}:8888/api/proDoc/status"
    echo "  curl http://${SERVER_IP}:8888/api/list"
fi

# DuckDNS 서비스 상태 확인
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo ""
    echo "🦆 DuckDNS 상태 확인:"
    if docker ps | grep hospital-duckdns > /dev/null; then
        echo "  ✅ DuckDNS 자동 업데이트 서비스: 실행 중"
        echo "  📝 DuckDNS 로그 (최근 5줄):"
        docker logs hospital-duckdns --tail=5 | sed 's/^/    /'
    else
        echo "  ⚠️ DuckDNS 자동 업데이트 서비스: 실행되지 않음"
    fi
fi
