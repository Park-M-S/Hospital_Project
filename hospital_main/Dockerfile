# 빌드 스테이지 
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# 수정 전 파일 확인
RUN echo "=== 수정 전 AppConfig.java 확인 ===" && \
    grep -n "hibernate.hbm2ddl.auto" src/main/java/com/hospital/config/AppConfig.java && \
    grep -n "public ObjectMapper objectMapper" src/main/java/com/hospital/config/AppConfig.java

# AppConfig.java 수정 (더 안전한 방식)
RUN sed -i '/jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");/s/^/\/\/ /' src/main/java/com/hospital/config/AppConfig.java && \
    sed -i 's/localhost:3500/mariadb:3500/g' src/main/resources/db.properties

# 수정 후 파일 확인
RUN echo "=== 수정 후 AppConfig.java 확인 ===" && \
    grep -n "hibernate.hbm2ddl.auto" src/main/java/com/hospital/config/AppConfig.java

# @Primary 어노테이션 추가 (import도 확인)
RUN grep -q "import org.springframework.context.annotation.Primary;" src/main/java/com/hospital/config/AppConfig.java || \
    sed -i '/import org.springframework.context.annotation.Configuration;/a import org.springframework.context.annotation.Primary;' src/main/java/com/hospital/config/AppConfig.java

RUN sed -i '/public ObjectMapper objectMapper()/i\    @Primary' src/main/java/com/hospital/config/AppConfig.java

# 최종 확인
RUN echo "=== @Primary 어노테이션 추가 확인 ===" && \
    grep -A 2 -B 2 "@Primary" src/main/java/com/hospital/config/AppConfig.java

# Maven 빌드 전 컴파일 테스트
RUN apt-get update && apt-get install -y maven
RUN mvn compile || (echo "=== 컴파일 오류 발생 ===" && cat target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst && exit 1)

# Maven 빌드 (WAR 파일 생성)
RUN mvn clean package -DskipTests

# 실행 스테이지 - Tomcat 10.1
FROM tomcat:10.1-jdk21-openjdk-slim

# 기본 webapps 정리
RUN rm -rf /usr/local/tomcat/webapps/*

# WAR 파일을 Tomcat webapps에 복사
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# 포트 8888로 변경 
RUN sed -i 's/port="8080"/port="8888"/g' /usr/local/tomcat/conf/server.xml

# JPA DDL 설정을 확실히 하기 위한 환경변수
ENV JAVA_OPTS="-Dhibernate.hbm2ddl.auto=update"

EXPOSE 8888
CMD ["catalina.sh", "run"]
