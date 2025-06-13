# 빌드 스테이지 
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Maven 빌드 (WAR 파일 생성)
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# 실행 스테이지 - Tomcat 10.1
FROM tomcat:10.1-jdk21-openjdk-slim

# 기본 webapps 정리
RUN rm -rf /usr/local/tomcat/webapps/*

# WAR 파일을 Tomcat webapps에 복사
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# WAR 파일 압축 해제하여 db.properties 수정
RUN cd /usr/local/tomcat/webapps && \
    unzip ROOT.war -d ROOT && \
    rm ROOT.war && \
    sed -i 's/localhost:3500/mariadb:3500/g' /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/db.properties

# 포트 8888로 변경 
RUN sed -i 's/port="8080"/port="8888"/g' /usr/local/tomcat/conf/server.xml

EXPOSE 8888
CMD ["catalina.sh", "run"]
