#Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package

#Stage 2: Run
FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /service.jar

EXPOSE 8080

CMD ["java", "-jar", "/service.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
