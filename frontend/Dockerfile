FROM node:22-alpine as builder

WORKDIR /app

# 환경 변수 설정 (빌드 안정성)
ENV NODE_ENV=production
ENV CI=false
ENV NODE_OPTIONS="--max-old-space-size=2048"

# 패키지 설치
COPY package*.json ./
RUN npm install

# 소스 복사 및 빌드
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
