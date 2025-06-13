# 빌드 스테이지 
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# AppConfig.java 수정: validate 설정 제거, db.properties 수정, @Primary 어노테이션 추가
RUN sed -i 's/jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");/\/\/ jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate"); \/\/ 중복 제거/g' src/main/java/com/hospital/config/AppConfig.java && \
    sed -i 's/localhost:3500/mariadb:3500/g' src/main/resources/db.properties && \
    sed -i 's/@Bean$/&\n    @Primary/g' src/main/java/com/hospital/config/AppConfig.java

# Maven 빌드 (WAR 파일 생성)
RUN apt-get update && apt-get install -y maven
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
