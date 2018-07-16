FROM openjdk:8-jdk-alpine
MAINTAINER benjamin.heasly@gmail.com

VOLUME /tmp

ARG JAR_FILE
COPY ${JAR_FILE} /app/app.jar

WORKDIR /app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
