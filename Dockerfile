FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE $APP_PORT

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f ${APP_DOMAIN:-localhost}:${APP_PORT:-8080}/actuator/health/readiness || exit 1