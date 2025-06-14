#!/bin/bash

# 배포 스크립트
set -e

echo "🚀 Starting Hospital Project deployment..."

# 환경 변수 설정
export IMAGE_TAG=${IMAGE_TAG:-latest}
export DB_PASSWORD=${DB_PASSWORD:-1234}
export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD:-1234}

# 기존 컨테이너 중지
echo "⏹️ Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down || true

# 데이터 디렉토리 생성
sudo mkdir -p /opt/hospital/data
sudo chown ec2-user:ec2-user /opt/hospital/data

# 새 컨테이너 시작
echo "▶️ Starting new containers..."
docker-compose -f docker-compose.prod.yml up -d

# 사용하지 않는 이미지 정리
echo "🧹 Cleaning up unused images..."
docker system prune -f

echo "🎉 Hospital Project deployment completed successfully!"
echo "📍 Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"
echo "📍 Backend API: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8888"
