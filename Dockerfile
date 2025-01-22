LABEL authors="HackintoshSA Team - Benjamin Modimokwane"

# Source & Version of our Docker image
FROM quay.io/quarkus/ubi-quarkus-native-image:22.3.0-java17

# Set the working directory inside the container
WORKDIR /src

# Copy the jar file from the target directory into the container
COPY target/*-runner.jar /src/application.jar

# Expose the application port
EXPOSE 8080

# Set the default command to run the Quarkus application
CMD ["java", "-jar", "/work/application.jar"]
