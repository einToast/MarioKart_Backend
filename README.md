# MarioKart Tournament Backend

Backend service for managing Mario Kart tournaments. Built with Spring Boot 3 (Java 21). It includes registration, scheduling, surveys, settings, notifications, user login, security, and WebSockets.

See [MarioKart_Tournament](https://github.com/einToast/MarioKart_Tournament)

## Tech Stack
- Language: Java 21
- Framework: Spring Boot 3
- Build: Maven
- Security: Spring Security + JWT
- Websockets: STOMP over SockJS
- Database: H2 (dev), PostgreSQL (prod)
- Deployment: Docker + Docker Compose
- PWA support

## Project Structure

```
src/
  main/
    java/de/fsr/mariokart_backend/
      controller/     # controller configuration
      exception/      # custom exceptions
      healthcheck/    # health check endpoints for Docker
      notification/   # PWA push notification
      registration/   # team registration and management
      schedule/       # tournament schedule creation and management
      security/       # security configuration
      settings/       # general tournament settings
      survey/         # survey management
      user/           # admin user management
      websocket/      # websocket configuration
```
## Getting Started

### Prerequisites
- Java 21
- Maven

### Environment

Create a `.env` in the project root:

```
USER_NAME=YOUR_ADMIN_USERNAME
USER_PASSWORD=YOUR_ADMIN_PASSWORD
SECRET_KEY=YOUR_SECRET_KEY
DB_USER=YOUR_DB_USER
DB_PASSWORD=YOUR_DB_PASSWORD
MATCHPLAN_PROTOCOL=http
MATCHPLAN_URL=localhost
MATCHPLAN_PORT=8000
DOMAIN=http://localhost:8100/
VAPID_PUBLIC_KEY=YOUR_VAPID_PUBLIC_KEY_FOR_PWA
VAPID_PRIVATE_KEY=YOUR_VAPID_PRIVATE_KEY_FOR_PWA
```
### Run
- `mvn spring-boot:run`

### Build
- `mvn clean package` (outputs `target/*.jar`)

### Run Production
- `SPRING_PROFILES_ACTIVE=prod java -jar target/*.jar`
- To create a tournament schedule, you must also run [MarioKart_Schedule](https://github.com/einToast/MarioKart_Schedule)

## Docker
- See [MarioKart_Deployment](https://github.com/einToast/MarioKart_Deployment)
