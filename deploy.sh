#!/bin/bash
# Hospital Project 백엔드 전용 배포 스크립트
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

echo "📋 현재 설정:"
echo "  서버 IP: ${SERVER_IP}"
echo "  환경: ${ENVIRONMENT}"
echo "  이미지 태그: ${IMAGE_TAG}"
echo "  백엔드 포트: ${BACKEND_PORT}"
echo "  DB 포트: ${DB_PORT}"

# GitHub Actions가 아닌 로컬 실행 시 경고
if [ -z "$GITHUB_ACTIONS" ]; then
    echo "⚠️  로컬 실행 감지: 기본값을 사용합니다."
    echo "   프로덕션 배포는 GitHub Actions를 사용하세요!"
fi

echo "⏹️ 기존 컨테이너 중지..."
docker-compose -f docker-compose.prod.yml down || true

# 필요한 디렉토리 생성
sudo mkdir -p /opt/hospital/data
sudo chown ec2-user:ec2-user /opt/hospital/data

echo "▶️ 새 컨테이너 시작..."
docker-compose -f docker-compose.prod.yml up -d

echo "🧹 이미지 정리..."
docker system prune -f

echo "🎉 배포 완료!"
echo "📍 백엔드 API: http://${SERVER_IP}:8888"
echo ""
echo "🔧 API 테스트:"
echo "  curl http://${SERVER_IP}:8888/api/list"
echo "  curl -X POST http://${SERVER_IP}:8888/api/pharmacy/save"
