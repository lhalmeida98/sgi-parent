# syntax=docker/dockerfile:1

# Build stage: compile only the executable module (sgi-backend) with Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy source
COPY . .

# Build the runnable module and its dependencies, skipping tests
RUN mvn -B clean package -pl sgi-backend -am -DskipTests

# Normalize the output JAR name for a clean runtime copy
RUN set -e; \
    JAR="$(ls sgi-backend/target/*.jar | grep -v '\\.original$' | head -n 1)"; \
    cp "$JAR" /workspace/app.jar

# Runtime stage: minimal JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Ensure required Java module access for XAdES4j/Guice at runtime
ENV JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED"

# Copy only the built executable JAR
COPY --from=build /workspace/app.jar /app/app.jar

# Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
