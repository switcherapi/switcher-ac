FROM maven:3.9.5-eclipse-temurin-21-alpine AS maven

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS builder
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=maven ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]