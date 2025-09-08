#!/bin/bash
# Hospital Project 배포 스크립트 (DuckDNS 지원) - 개발환경
set -e
echo "🚀 Hospital Project 백엔드 배포 시작... (개발환경)"

# .env 파일이 있으면 로드
if [ -f ".env" ]; then
    echo "📄 .env 파일 로드 중..."
    set -a  # 모든 변수를 자동으로 export
    source .env
    set +a
    echo "✅ .env 파일 로드 완료"
else
    echo "⚠️ .env 파일을 찾을 수 없습니다."
fi

# 환경 변수 기본값 설정 (개발환경)
export IMAGE_TAG=${IMAGE_TAG:-latest}
export DB_PASSWORD=${DB_PASSWORD:-1234}
export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD:-1234}
export ENVIRONMENT=${ENVIRONMENT:-development}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export DB_PORT=${DB_PORT:-3500}

# DuckDNS 설정 (.env에서 로드된 값 사용) - 개발환경용
export DUCKDNS_DOMAIN=${DUCKDNS_DOMAIN:-""}
export DUCKDNS_SUBDOMAIN=${DUCKDNS_SUBDOMAIN:-""}
export DUCKDNS_TOKEN=${DUCKDNS_TOKEN:-""}

# GitHub Actions가 아닌 로컬 실행 시 IP 가져오기
if [ -z "$GITHUB_ACTIONS" ]; then
    PUBLIC_IP=$(curl -s --connect-timeout 5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")
    export SERVER_IP=${SERVER_IP:-$PUBLIC_IP}
fi

echo "📋 현재 설정 (개발환경):"
echo "  환경: ${ENVIRONMENT}"
echo "  이미지 태그: ${IMAGE_TAG}"
echo "  백엔드 포트: ${BACKEND_PORT}"
echo "  DB 포트: ${DB_PORT}"

# DuckDNS 설정 확인 및 표시 (토큰은 마스킹)
if [ -n "$DUCKDNS_DOMAIN" ] && [ -n "$DUCKDNS_SUBDOMAIN" ] && [ -n "$DUCKDNS_TOKEN" ]; then
    echo "  🦆 DuckDNS 도메인 (개발): ${DUCKDNS_DOMAIN}"
    echo "  🦆 DuckDNS 서브도메인 (개발): ${DUCKDNS_SUBDOMAIN}"
    TOKEN_MASKED="${DUCKDNS_TOKEN:0:8}...${DUCKDNS_TOKEN: -4}"
    echo "  🦆 DuckDNS 토큰: ${TOKEN_MASKED}"
    echo "  🦆 DuckDNS 자동 업데이트: 활성화"
    DOMAIN_URL="http://${DUCKDNS_DOMAIN}"
else
    echo "  🔗 접속 방식: 직접 IP 접근"
    echo "  ⚠️ DuckDNS 설정이 불완전합니다:"
    echo "    - DOMAIN: '${DUCKDNS_DOMAIN}'"
    echo "    - SUBDOMAIN: '${DUCKDNS_SUBDOMAIN}'"
    echo "    - TOKEN: '$([ -n "$DUCKDNS_TOKEN" ] && echo "설정됨" || echo "비어있음")'"
    DOMAIN_URL="http://${SERVER_IP:-localhost}"
fi

# GitHub Actions가 아닌 로컬 실행 시 경고
if [ -z "$GITHUB_ACTIONS" ]; then
    echo "⚠️  로컬 실행 감지: 기본값을 사용합니다."
    echo "   개발환경 배포는 GitHub Actions를 사용하세요!"
fi

echo "⏹️ 기존 컨테이너 중지 (개발환경)..."
docker-compose -f docker-compose.dev.yml down || true

# 필요한 디렉토리 생성
sudo mkdir -p /opt/hospital/data/mariadb
sudo mkdir -p /opt/hospital/logs/backend
sudo chown -R ec2-user:ec2-user /opt/hospital/

echo "▶️ 새 컨테이너 시작 (개발환경)..."
docker-compose -f docker-compose.dev.yml up -d

# 컨테이너 시작 대기
echo "⏳ 컨테이너 시작 대기..."
sleep 15

# 서비스 상태 확인
echo "📊 서비스 상태 확인:"
docker-compose -f docker-compose.dev.yml ps

# DuckDNS 서비스 상태 확인
if [ -n "$DUCKDNS_DOMAIN" ] && [ -n "$DUCKDNS_TOKEN" ]; then
    echo ""
    echo "🦆 DuckDNS 상태 확인:"
    if docker ps | grep hospital-duckdns > /dev/null; then
        echo "  ✅ DuckDNS 자동 업데이트 서비스: 실행 중"
        
        # DuckDNS 환경변수 확인
        echo "  🔍 DuckDNS 환경변수 확인:"
        CONTAINER_SUBDOMAIN=$(docker exec hospital-duckdns printenv SUBDOMAINS 2>/dev/null || echo "없음")
        CONTAINER_TOKEN=$(docker exec hospital-duckdns printenv TOKEN 2>/dev/null || echo "없음")
        echo "    - SUBDOMAINS: ${CONTAINER_SUBDOMAIN}"
        echo "    - TOKEN: $([ "$CONTAINER_TOKEN" != "없음" ] && [ -n "$CONTAINER_TOKEN" ] && echo "설정됨" || echo "비어있음")"
        
        sleep 5  # DuckDNS 초기화 대기
        echo "  📝 DuckDNS 로그 (최근 10줄):"
        docker logs hospital-duckdns --tail=10 | sed 's/^/    /'
    else
        echo "  ⚠️ DuckDNS 자동 업데이트 서비스: 실행되지 않음"
    fi
fi

echo "🧹 이미지 정리..."
docker system prune -f

echo ""
echo "🎉 개발환경 배포 완료!"
echo ""
echo "📍 접속 정보 (개발환경):"
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "  🦆 DuckDNS 백엔드 API: ${DOMAIN_URL}:8888"
    echo "  🌍 개발 도메인: ${DUCKDNS_DOMAIN}"
else
    echo "  🔗 백엔드 API: ${DOMAIN_URL}:8888"
fi
echo ""
echo "🔧 API 테스트 (개발환경):"
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "  curl ${DOMAIN_URL}:8888/api/proDoc/status"
    echo "  curl ${DOMAIN_URL}:8888/api/list"
else
    echo "  curl http://localhost:8888/api/proDoc/status"
    echo "  curl http://localhost:8888/api/list"
fi
echo ""
echo "✨ 개발환경 배포가 완료되었습니다!"
