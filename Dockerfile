FROM openjdk:18
MAINTAINER docker@fsr.de

COPY ./target/MarioKart_Backend-0.0.1-SNAPSHOT.jar /service.jar

WORKDIR /

EXPOSE 8080

CMD ["java", "-jar", "/service.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
