#Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

ENV SECRET_KEY=${SECRET_KEY}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV USER_NAME=${USER_NAME}
ENV USER_PASSWORD=${USER_PASSWORD}


RUN mvn clean package

#Stage 2: Run
FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /service.jar

RUN apk add --no-cache curl

EXPOSE 8080

CMD ["java", "-jar", "/service.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}"]
