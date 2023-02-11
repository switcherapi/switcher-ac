FROM maven:3.8.5 AS maven

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS builder
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=maven ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]