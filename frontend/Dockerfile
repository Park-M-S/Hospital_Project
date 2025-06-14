# Multi-stage build for Hospital Frontend with Self-Signed SSL
FROM node:18-alpine as builder

# 작업 디렉토리 설정
WORKDIR /app

# package.json 복사 및 의존성 설치
COPY package*.json ./
RUN npm install

# 소스 코드 복사
COPY . .

# Vue.js 빌드
RUN npm run build

# 빌드 결과 확인
RUN ls -la dist/
RUN echo "Checking if index.html exists:"
RUN test -f dist/index.html && echo "✅ index.html found" || echo "❌ index.html NOT found"

# Production stage with Caddy
FROM caddy:alpine

# 필요한 패키지 설치
RUN apk add --no-cache \
    curl \
    envsubst

# 환경변수 설정
ENV SERVER_IP=""
ENV BACKEND_HOST="hospital-backend"
ENV BACKEND_PORT="8888"
ENV ENVIRONMENT="production"

# 빌드된 Vue.js 파일들을 Caddy의 웹 루트로 복사
COPY --from=builder /app/dist /usr/share/caddy

# Caddyfile 템플릿 복사
COPY Caddyfile.template /etc/caddy/Caddyfile.template

# 초기화 스크립트 복사
COPY docker-entrypoint.sh /docker-entrypoint.sh

# 권한 설정
RUN chmod +x /docker-entrypoint.sh
RUN chmod -R 755 /usr/share/caddy

# 로그 디렉토리 생성
RUN mkdir -p /var/log/caddy

# 포트 노출
EXPOSE 80 443

# 헬스체크 (자체 서명 인증서 고려)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f -k https://localhost/ || curl -f http://localhost/ || exit 1

# 커스텀 엔트리포인트 사용
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
