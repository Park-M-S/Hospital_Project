# 빌드 스테이지
FROM node:20-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

# 소스코드 및 환경변수 파일 복사
COPY . .

# 빌드 실행 (.env.production 파일이 있으면 자동으로 사용됨)
RUN npm run build

# 프로덕션 스테이지
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
