# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

ARG SECRET_KEY
ARG DB_USER
ARG DB_PASSWORD
ARG USER_NAME
ARG USER_PASSWORD
ARG MATCHPLAN_PROTOCOL
ARG MATCHPLAN_HOST
ARG MATCHPLAN_PORT

RUN mvn clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /service.jar

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
    CMD curl --fail http://localhost:8080/api/public/healthcheck || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /service.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
