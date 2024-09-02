FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:17-alpine

WORKDIR /app

COPY --from=build /app/target/MarioKart_Backend-0.0.1-SNAPSHOT.jar /service.jar

# Port (falls erforderlich) freigeben
EXPOSE 8080

CMD ["java", "-jar", "/service.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
