# syntax=docker/dockerfile:1
FROM gradle:8.12.0-jdk17-alpine AS build
ARG SERVICE_PATH=:services:account-service
WORKDIR /workspace
COPY . /workspace
RUN gradle ${SERVICE_PATH}:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/account-service/build/libs/account-service.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]

