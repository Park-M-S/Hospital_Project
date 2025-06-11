FROM node:18-alpine as builder

WORKDIR /app
COPY package*.json ./

# 설치 과정 확인
RUN npm install
RUN npm list --depth=0

COPY . .

# 파일 구조 확인
RUN ls -la
RUN ls -la src/ || echo "No src directory found"

# vite 설치 확인
RUN npx vite --version

# 빌드 전 환경 확인
RUN echo "Node version: $(node --version)"
RUN echo "NPM version: $(npm --version)"

# 상세한 빌드 로그
RUN npm run build 2>&1 || (echo "Build failed, showing package.json scripts:" && cat package.json | grep -A 10 scripts && exit 1)

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
