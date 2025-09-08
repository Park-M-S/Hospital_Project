# 빌드 스테이지
FROM openjdk:21-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 업데이트 및 Maven 설치 (캐시 최적화)
RUN apt-get update && \
    apt-get install -y maven curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Maven 설정 확인
RUN echo "=== 환경 확인 ===" && \
    java -version && \
    mvn -version

# pom.xml 먼저 복사하여 의존성 캐싱 최적화
COPY pom.xml .

# Maven 의존성 미리 다운로드 (빌드 시간 단축)
RUN echo "=== Maven 의존성 다운로드 ===" && \
    mvn dependency:go-offline -B || echo "의존성 다운로드 실패 - 계속 진행"

# 소스 코드 복사
COPY src ./src

# 프로젝트 구조 확인
RUN echo "=== 프로젝트 구조 확인 ===" && \
    ls -la && \
    echo "=== src 구조 ===" && \
    find src -type f -name "*.properties" 2>/dev/null || echo "properties 파일 없음"

# db.properties에서 localhost를 mariadb로 변경 (개선된 버전)
RUN echo "=== db.properties 수정 ===" && \
    find src -name "*.properties" -type f -exec echo "발견된 properties 파일: {}" \; && \
    if [ -f "src/main/resources/db.properties" ]; then \
        echo "=== 수정 전 db.properties ===" && \
        cat src/main/resources/db.properties && \
        sed -i 's/localhost:3500/mariadb:3500/g' src/main/resources/db.properties && \
        echo "=== 수정 후 db.properties ===" && \
        cat src/main/resources/db.properties; \
    else \
        echo "db.properties 파일이 없습니다. 다른 설정 파일을 확인합니다." && \
        find src -name "application*.properties" -o -name "application*.yml" | head -5; \
    fi

# JVM 메모리 설정 및 Maven 빌드
ENV MAVEN_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

RUN echo "=== Maven 빌드 시작 ===" && \
    mvn clean package -DskipTests \
        --batch-mode \
        --errors \
        --fail-at-end \
        --show-version \
        -Dmaven.compiler.fork=true

# 빌드 결과 확인 (개선된 버전)
RUN echo "=== 빌드 결과 확인 ===" && \
    ls -la target/ && \
    echo "=== WAR/JAR 파일 확인 ===" && \
    find target/ -name "*.war" -o -name "*.jar" | head -10 && \
    echo "=== 빌드된 파일 크기 ===" && \
    du -h target/*.war 2>/dev/null || du -h target/*.jar 2>/dev/null || echo "빌드 파일 없음"

# 실행 스테이지
FROM tomcat:10.1-jdk21-openjdk-slim

# 메타데이터 추가
LABEL maintainer="Hospital Project Team"
LABEL version="1.0"
LABEL description="Hospital Management System Backend"

# 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 기본 webapps 정리
RUN rm -rf /usr/local/tomcat/webapps/*

# WAR 파일 복사 (더 안전한 방법)
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat 설정 최적화
RUN echo "=== Tomcat 설정 ===" && \
    # 포트를 8888로 변경
    sed -i 's/port="8080"/port="8888"/g' /usr/local/tomcat/conf/server.xml && \
    # 커넥션 풀 최적화
    sed -i 's/maxThreads="200"/maxThreads="400"/g' /usr/local/tomcat/conf/server.xml && \
    # 메모리 사용량 최적화
    echo 'CATALINA_OPTS="$CATALINA_OPTS -Xmx1024m -Xms512m -XX:MetaspaceSize=256m"' >> /usr/local/tomcat/bin/setenv.sh

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /usr/local/tomcat/logs && \
    chmod 755 /usr/local/tomcat/logs

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8888/ || exit 1

# 포트 노출
EXPOSE 8888

# 실행 명령
CMD ["catalina.sh", "run"]
