FROM openjdk:17
MAINTAINER docker@fsr.de

# Kopiere die JAR-Datei in das Image
COPY ./target/MarioKart_Backend-0.0.1-SNAPSHOT.jar /service.jar

# Arbeitsverzeichnis setzen
WORKDIR /

# Port 8080 freigeben
EXPOSE 8080

# Java-Anwendung ausführen
CMD ["java", "-jar", "/service.jar"]