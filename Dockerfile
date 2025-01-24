LABEL authors="HackintoshSA Team - Benjamin Modimokwane"

# Source & Version of our Docker image
FROM quay.io/quarkus/ubi-quarkus-native-image:22.3.0-java17

# Set the working directory inside the container
WORKDIR /src


# Copy the native executable from the target directory
COPY target/*-runner /src/application

# Expose the application port
EXPOSE 8080

# Set the default command to run the native Quarkus application
CMD ["/src/application"]
