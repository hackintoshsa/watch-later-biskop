# Stage 1: Build the Quarkus native executable
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /workspace

# Copy the pom.xml and source code to the container
COPY pom.xml /workspace/

RUN mvn dependency:go-offline

COPY src/ /workspace/src/


# Build the Quarkus application (native image)
RUN mvn clean package -DskipTests

# Stage 2: Final Image with Native executable
FROM openjdk:17-slim
# Set the working directory inside the container
WORKDIR /src

# Copy the native executable from the build stage to the final image
COPY --from=build /workspace/target/*-runner.jar /src/application.jar

# Expose the application port
EXPOSE 8080

# Set the default command to run the native Quarkus application
CMD ["java", "-jar", "/src/application.jar"]
