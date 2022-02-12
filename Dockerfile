FROM maven:3.6.3 AS maven
LABEL COMPANY="Trackerforce"
LABEL MAINTAINER="trackerforce.project@gmail.com"
LABEL APPLICATION="Switcher AC"

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn package -DskipTests

FROM openjdk:14-jdk-alpine AS builder
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=maven ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]