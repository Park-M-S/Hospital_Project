FROM node:18-alpine as builder

WORKDIR /app

# 시스템 업데이트 및 필수 도구 설치
RUN apk update && apk add --no-cache git

# 환경 변수 설정
ENV NODE_ENV=production
ENV CI=false
ENV NODE_OPTIONS="--max-old-space-size=2048"

# 패키지 설치
COPY package*.json ./
RUN npm cache clean --force
RUN npm install --verbose

# 설치 확인
RUN npm list --depth=0
RUN which npm
RUN which node

# 소스 복사
COPY . .

# 빌드 실행 (더 자세한 로그)
RUN echo "Starting build process..."
RUN npm run build --verbose

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
