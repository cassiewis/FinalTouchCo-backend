# ----- Stage 1: Build the application -----
FROM gradle:8.5.0-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy everything to the container
COPY . .

# Build the application (creates a fat JAR)
RUN gradle build --no-daemon

# ----- Stage 2: Run the application -----
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (change if your app uses a different one)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
