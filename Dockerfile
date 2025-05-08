# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

ENV SECRET_KEY=${SECRET_KEY}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV USER_NAME=${USER_NAME}
ENV USER_PASSWORD=${USER_PASSWORD}
ENV MATCHPLAN_PROTOCOL=${MATCHPLAN_PROTOCOL}
ENV MATCHPLAN_HOST=${MATCHPLAN_HOST}
ENV MATCHPLAN_PORT=${MATCHPLAN_PORT}
ENV VAPID_PUBLIC_KEY=${VAPID_PUBLIC_KEY}
ENV VAPID_PRIVATE_KEY=${VAPID_PRIVATE_KEY}

RUN mvn clean package

# Stage 2: Run
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /service.jar

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
    CMD curl --fail http://localhost:8080/api/public/healthcheck || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /service.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
