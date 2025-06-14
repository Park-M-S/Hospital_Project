FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
# 빌드 결과 확인
RUN ls -la dist/
RUN echo "Checking if index.html exists:"
RUN test -f dist/index.html && echo "index.html found" || echo "index.html NOT found"

FROM nginx:alpine
# 빌드된 파일 복사 (경로 수정)
COPY --from=builder /app/dist /usr/share/nginx/html
# nginx 설정 복사
COPY nginx.conf /etc/nginx/nginx.conf
# 권한 설정
RUN chmod -R 755 /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
