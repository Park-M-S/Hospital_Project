#!/bin/bash
# Hospital Project 자체 서명 SSL 배포 스크립트 
set -e

echo "🚀 Hospital Project 자체 서명 SSL 배포 시작..."

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
echo "  SSL 타입: 자체 서명 인증서"

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
echo "📍 HTTPS: https://${SERVER_IP} (자체 서명 인증서 - 보안 경고 발생)"
echo "📍 HTTP:  http://${SERVER_IP} (HTTPS로 리다이렉트)"
echo ""
echo "🔐 브라우저 접속 방법:"
echo "  1. https://${SERVER_IP} 접속"
echo "  2. 보안 경고에서 '고급' → '안전하지 않음을 승인하고 계속'"
