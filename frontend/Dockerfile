FROM node:22-alpine as builder

WORKDIR /app

# 메모리 증가 및 환경변수 설정
ENV NODE_OPTIONS="--max-old-space-size=4096"
ENV NODE_ENV=production
ENV CI=false

# 패키지 복사 및 설치
COPY package*.json ./
RUN npm install --force

# 소스 복사
COPY . .

# Vite 빌드 (더 자세한 로그)
RUN npm run build -- --mode production

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
