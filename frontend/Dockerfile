FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# 빌드 과정 확인
RUN echo "Node version: $(node --version)"
RUN echo "NPM version: $(npm --version)"
RUN npx vite --version
RUN ls -la
RUN npm run build

FROM nginx:alpine
# 빌드된 파일을 올바른 경로로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# 커스텀 nginx 설정 복사 (있다면)
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
