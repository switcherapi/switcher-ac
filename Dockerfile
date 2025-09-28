FROM maven:3.9.11-eclipse-temurin-25-alpine AS maven

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS builder
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=maven ${JAR_FILE} app.jar

RUN adduser -D user \
    && mkdir /etc/certs \
    && mkdir -p /data/snapshots \
    && chown -R user:user /etc/certs \
    && chown -R user:user /data/snapshots

USER user

ENTRYPOINT ["java","-jar","/app.jar"]