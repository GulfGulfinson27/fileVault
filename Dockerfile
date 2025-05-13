FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and source code
COPY pom.xml .
COPY src/ src/

# Build the application (skip tests)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/FileVault-shaded.jar /app/filevault.jar

# Create necessary directories
RUN mkdir -p /root/.filevault/data

# Set volume for FileVault data directory
VOLUME /root/.filevault

# Run the application
CMD ["java", "-jar", "/app/filevault.jar"] 