# Stage 1: Build
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

ARG SECRET_KEY
ARG DB_USER
ARG DB_PASSWORD
ARG USER_NAME
ARG USER_PASSWORD
ARG VAPID_PUBLIC_KEY
ARG VAPID_PRIVATE_KEY

ENV SECRET_KEY=$SECRET_KEY
ENV DB_USER=$DB_USER
ENV DB_PASSWORD=$DB_PASSWORD
ENV USER_NAME=$USER_NAME
ENV USER_PASSWORD=$USER_PASSWORD
ENV VAPID_PUBLIC_KEY=$VAPID_PUBLIC_KEY
ENV VAPID_PRIVATE_KEY=$VAPID_PRIVATE_KEY


RUN mvn clean package

# Stage 2: Run
FROM eclipse-temurin:24.0.2_12-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /service.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
    CMD curl --fail http://localhost:8080/api/public/healthcheck || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /service.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
