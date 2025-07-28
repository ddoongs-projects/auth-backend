FROM bellsoft/liberica-openjdk-alpine:21

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY core/core-api/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]