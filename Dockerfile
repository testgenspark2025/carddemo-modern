# syntax=docker/dockerfile:1.6
# Multi-stage build: node (frontend) -> maven (backend+bundled UI) -> jre runtime

FROM node:22-alpine AS frontend
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci || npm install
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /src
COPY pom.xml ./
COPY carddemo-domain/pom.xml carddemo-domain/
COPY carddemo-batch/pom.xml carddemo-batch/
COPY carddemo-api/pom.xml carddemo-api/
RUN mvn -B -ntp -q dependency:go-offline -DskipTests || true
COPY carddemo-domain/src carddemo-domain/src
COPY carddemo-batch/src carddemo-batch/src
COPY carddemo-api/src carddemo-api/src
# Bundle the React build output as static resources served by Spring Boot
COPY --from=frontend /fe/dist/ carddemo-api/src/main/resources/static/
RUN mvn -B -ntp -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /src/carddemo-api/target/*.jar /app/app.jar
COPY data/ /app/data/
ENV CARDDEMO_DATA_DIR=file:/app/data \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
