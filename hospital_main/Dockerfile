# 빌드 스테이지
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app

# Maven 설치
RUN apt-get update && apt-get install -y maven

# 소스 코드 복사
COPY pom.xml .
COPY src ./src

# db.properties에서 localhost를 mariadb로 변경
RUN echo "=== db.properties 수정 ===" && \
    (if [ -f "src/main/resources/db.properties" ]; then \
        echo "수정 전:" && cat src/main/resources/db.properties && \
        sed -i 's/localhost:3500/mariadb:3500/g' src/main/resources/db.properties && \
        echo "수정 후:" && cat src/main/resources/db.properties; \
    else \
        echo "db.properties 파일 없음"; \
    fi)

# Maven 빌드
RUN echo "=== Maven 빌드 시작 ===" && \
    mvn clean package -DskipTests

# 빌드 결과 확인
RUN echo "=== 빌드 결과 ===" && \
    ls -la target/ && \
    ls -la target/*.war

# 실행 스테이지
FROM tomcat:10.1-jdk21-openjdk-slim

# 기본 webapps 정리
RUN rm -rf /usr/local/tomcat/webapps/*

# WAR 파일 복사
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat 포트를 8888로 변경
RUN sed -i 's/port="8080"/port="8888"/g' /usr/local/tomcat/conf/server.xml

# 이전의 ENV JAVA_OPTS 줄을 삭제합니다.
# Docker Compose의 환경 변수가 이제 정상적으로 적용됩니다.

EXPOSE 8888

CMD ["catalina.sh", "run"]
