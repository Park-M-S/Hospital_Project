#!/bin/bash

# 배포 스크립트
set -e

echo "🚀 Starting Hospital Project deployment..."

# 환경 변수 설정
export IMAGE_TAG=${IMAGE_TAG:-latest}
export DB_PASSWORD=${DB_PASSWORD:-hospitalpass}
export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD:-rootpassword}

# 기존 컨테이너 중지
echo "⏹️ Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down || true

# 데이터 디렉토리 생성
sudo mkdir -p /opt/hospital/data
sudo chown ec2-user:ec2-user /opt/hospital/data

# 새 컨테이너 시작
echo "▶️ Starting new containers..."
docker-compose -f docker-compose.prod.yml up -d

# 헬스 체크
echo "🔍 Performing health check..."
sleep 30

# Backend 헬스 체크
echo "Checking backend health..."
for i in {1..10}; do
  if curl -f http://localhost:8888/health > /dev/null 2>&1; then
    echo "✅ Backend is healthy"
    break
  else
    echo "⏳ Waiting for backend... ($i/10)"
    sleep 10
  fi
  
  if [ $i -eq 10 ]; then
    echo "❌ Backend health check failed"
    docker-compose -f docker-compose.prod.yml logs backend
    exit 1
  fi
done

# Frontend 헬스 체크
echo "Checking frontend health..."
if curl -f http://localhost:80 > /dev/null 2>&1; then
    echo "✅ Frontend is healthy"
else
    echo "❌ Frontend health check failed"
    docker-compose -f docker-compose.prod.yml logs frontend
    exit 1
fi

# MariaDB 헬스 체크
echo "Checking database health..."
if docker exec hospital-mariadb mysqladmin ping -h localhost -u root -p${DB_ROOT_PASSWORD} --port=3500 > /dev/null 2>&1; then
    echo "✅ Database is healthy"
else
    echo "❌ Database health check failed"
    docker-compose -f docker-compose.prod.yml logs mariadb
fi

# 사용하지 않는 이미지 정리
echo "🧹 Cleaning up unused images..."
docker system prune -f

echo "🎉 Hospital Project deployment completed successfully!"
echo "📍 Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"
echo "📍 Backend API: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8888"
