# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
# This caches the dependencies so successive builds are faster
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image using a lightweight JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Expose port 8080 (the default port for Spring Boot)
EXPOSE 8080

# Copy the built JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
