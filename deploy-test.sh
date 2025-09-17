#!/bin/bash
# Hospital Project 배포 스크립트 (DuckDNS 지원)
set -e
echo "🚀 Hospital Project 백엔드 배포 시작..."

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

# 환경 변수 기본값 설정
export IMAGE_TAG=${IMAGE_TAG:-latest}
export DB_PASSWORD=${DB_PASSWORD:-1234}
export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD:-1234}
export ENVIRONMENT=${ENVIRONMENT:-production}
export BACKEND_PORT=${BACKEND_PORT:-8888}
export DB_PORT=${DB_PORT:-3500}

# DuckDNS 설정 (.env에서 로드된 값 사용)
export DEV_DUCKDNS_DOMAIN=${DEV_DUCKDNS_DOMAIN:-""}
export DEV_DUCKDNS_SUBDOMAIN=${DEV_DUCKDNS_SUBDOMAIN:-""}
export DUCKDNS_TOKEN=${DUCKDNS_TOKEN:-""}

# GitHub Actions가 아닌 로컬 실행 시 IP 가져오기
if [ -z "$GITHUB_ACTIONS" ]; then
    PUBLIC_IP=$(curl -s --connect-timeout 5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")
    export SERVER_IP=${SERVER_IP:-$PUBLIC_IP}
fi

echo "📋 현재 설정:"
echo "  환경: ${ENVIRONMENT}"
echo "  이미지 태그: ${IMAGE_TAG}"
echo "  백엔드 포트: ${BACKEND_PORT}"
echo "  DB 포트: ${DB_PORT}"

# DuckDNS 설정 확인 및 표시 (토큰은 마스킹)
if [ -n "$DEV_DUCKDNS_DOMAIN" ] && [ -n "$DEV_DUCKDNS_SUBDOMAIN" ] && [ -n "$DUCKDNS_TOKEN" ]; then
    echo "  🦆 DuckDNS 도메인: ${DEV_DUCKDNS_DOMAIN}"
    echo "  🦆 DuckDNS 서브도메인: ${DEV_DUCKDNS_SUBDOMAIN}"
    TOKEN_MASKED="${DUCKDNS_TOKEN:0:8}...${DUCKDNS_TOKEN: -4}"
    echo "  🦆 DuckDNS 토큰: ${TOKEN_MASKED}"
    echo "  🦆 DuckDNS 자동 업데이트: 활성화"
    DOMAIN_URL="http://${DEV_DUCKDNS_DOMAIN}"
else
    echo "  🔗 접속 방식: 직접 IP 접근"
    echo "  ⚠️ DuckDNS 설정이 불완전합니다:"
    echo "    - DOMAIN: '${DEV_DUCKDNS_DOMAIN}'"
    echo "    - SUBDOMAIN: '${DEV_DUCKDNS_SUBDOMAIN}'"
    echo "    - TOKEN: '$([ -n "$DUCKDNS_TOKEN" ] && echo "설정됨" || echo "비어있음")'"
    DOMAIN_URL="http://${SERVER_IP:-localhost}"
fi

# GitHub Actions가 아닌 로컬 실행 시 경고
if [ -z "$GITHUB_ACTIONS" ]; then
    echo "⚠️  로컬 실행 감지: 기본값을 사용합니다."
    echo "   프로덕션 배포는 GitHub Actions를 사용하세요!"
fi

echo "⏹️ 기존 컨테이너 중지..."
docker-compose down || true

# 필요한 디렉토리 생성 및 권한 설정
echo "📁 디렉토리 생성 및 권한 설정..."
sudo mkdir -p /opt/hospital/{data,config,logs}/{mariadb,grafana,prometheus,backend,duckdns}

# 각 서비스별 적절한 권한 설정
sudo chown -R 999:999 /opt/hospital/data/mariadb      # MariaDB
sudo chown -R 472:0 /opt/hospital/data/grafana        # Grafana
sudo chown -R 65534:65534 /opt/hospital/data/prometheus # Prometheus
sudo chown -R ec2-user:ec2-user /opt/hospital/logs/backend # Backend logs
sudo chown -R ec2-user:ec2-user /opt/hospital/config   # 설정 파일들
sudo chmod -R 755 /opt/hospital

# Docker 네트워크 생성 (이미 존재하면 무시)
echo "🌐 Docker 네트워크 확인..."
docker network ls | grep hospital-network || docker network create hospital-network

echo "▶️ 새 컨테이너 시작..."
docker-compose up -d

# 컨테이너 시작 대기
echo "⏳ 컨테이너 시작 대기..."
sleep 30

# 서비스 상태 확인
echo "📊 서비스 상태 확인:"
docker-compose ps

# 각 서비스별 상태 확인
echo ""
echo "🔍 서비스 개별 상태 확인:"

# MariaDB 상태 확인
if docker ps | grep hospital-mariadb > /dev/null; then
    echo "  ✅ MariaDB: 실행 중"
    # 헬스체크 결과 확인
    HEALTH_STATUS=$(docker inspect hospital-mariadb --format='{{.State.Health.Status}}' 2>/dev/null || echo "unknown")
    echo "    - 헬스체크: ${HEALTH_STATUS}"
else
    echo "  ❌ MariaDB: 실행되지 않음"
    docker-compose logs mariadb --tail=5
fi

# Backend 상태 확인
if docker ps | grep hospital-backend > /dev/null; then
    echo "  ✅ Backend: 실행 중"
else
    echo "  ❌ Backend: 실행되지 않음"
    echo "  📝 Backend 로그:"
    docker-compose logs backend --tail=10
fi

# DuckDNS 서비스 상태 확인
if [ -n "$DEV_DUCKDNS_DOMAIN" ] && [ -n "$DUCKDNS_TOKEN" ]; then
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

# API 연결 테스트
echo ""
echo "🔍 API 연결 테스트:"
if docker ps | grep hospital-backend > /dev/null; then
    echo "  Backend 컨테이너 발견, API 테스트 시작..."
    sleep 10  # Backend 초기화 대기
    
    # 로컬 테스트 먼저
    if curl -f -s --connect-timeout 10 "http://localhost:8888/api/proDoc/status" > /dev/null 2>&1; then
        echo "  ✅ 로컬 API: 응답 정상"
    else
        echo "  ⚠️ 로컬 API: 응답 없음 (아직 시작 중일 수 있음)"
    fi
    
    # DuckDNS 도메인 테스트 (설정된 경우)
    if [ -n "$DEV_DUCKDNS_DOMAIN" ]; then
        if curl -f -s --connect-timeout 10 "${DOMAIN_URL}:8888/api/proDoc/status" > /dev/null 2>&1; then
            echo "  ✅ DuckDNS API: 응답 정상"
        else
            echo "  ⚠️ DuckDNS API: 응답 없음 (DNS 전파 대기 중일 수 있음)"
        fi
    fi
else
    echo "  ❌ Backend 컨테이너가 실행되지 않음"
fi

echo "🧹 이미지 정리..."
docker system prune -f

echo ""
echo "🎉 배포 완료!"
echo ""
echo "📍 접속 정보:"
if [ -n "$DEV_DUCKDNS_DOMAIN" ]; then
    echo "  🦆 DuckDNS 백엔드 API: ${DOMAIN_URL}:8888"
    echo "  🌍 도메인: ${DEV_DUCKDNS_DOMAIN}"
else
    echo "  🔗 백엔드 API: ${DOMAIN_URL}:8888"
fi

# 모니터링 서비스 정보
echo "  📊 모니터링 서비스:"
if [ -n "$DEV_DUCKDNS_DOMAIN" ]; then
    echo "    - Grafana: ${DOMAIN_URL}:3000"
    echo "    - Prometheus: ${DOMAIN_URL}:9090"
    echo "    - cAdvisor: ${DOMAIN_URL}:8080"
    echo "    - Node Exporter: ${DOMAIN_URL}:9100"
else
    echo "    - Grafana: http://localhost:3000"
    echo "    - Prometheus: http://localhost:9090"
    echo "    - cAdvisor: http://localhost:8080"
    echo "    - Node Exporter: http://localhost:9100"
fi

echo ""
echo "🔧 API 테스트:"
if [ -n "$DEV_DUCKDNS_DOMAIN" ]; then
    echo "  curl ${DOMAIN_URL}:8888/api/proDoc/status"
    echo "  curl ${DOMAIN_URL}:8888/api/list"
else
    echo "  curl http://localhost:8888/api/proDoc/status"
    echo "  curl http://localhost:8888/api/list"
fi
echo ""
echo "✨ 배포가 완료되었습니다!"
