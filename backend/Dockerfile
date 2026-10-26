# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/fittrack-backend-*.jar app.jar
EXPOSE 8080
# All config is env-driven (PORT, DB_*, CORS_ALLOWED_ORIGINS) so this image
# deploys anywhere without rebuilds — fully independent of the frontend.
ENTRYPOINT ["java", "-jar", "app.jar"]
