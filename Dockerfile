# Stage 1: Build
FROM maven:3.9.15-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:25.0.4.1_1-jre AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar /service.jar

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home --uid 10001 backend

USER backend

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
    CMD curl --fail http://localhost:8080/api/public/healthcheck || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /service.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
