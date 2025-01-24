# Stage 1: Build the Quarkus native executable
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /workspace

# Copy the pom.xml and source code to the container
COPY pom.xml /workspace/

RUN mvn dependency:go-offline

COPY src/ /workspace/src/



RUN mvn clean package -DskipTests

# List files in /workspace/target for debugging
RUN ls -al /workspace/target

# Stage 2: Final Image with Native executable
FROM openjdk:17-slim
# Set the working directory inside the container
WORKDIR /src

#COPY --from=build /workspace/target/*-runner.jar /src/application.jar
COPY --from=build /workspace/target/watch-later-biskop-1.0-SNAPSHOT.jar /src/application.jar
COPY --from=build /workspace/target/quarkus-app/lib/ /src/lib/
COPY --from=build /workspace/target/quarkus-app/*.jar /src/
COPY --from=build /workspace/target/quarkus-app/app/ /src/app/
COPY --from=build /workspace/target/quarkus-app/quarkus/ /src/quarkus/
COPY --from=build /workspace/target/quarkus-app/quarkus-run.jar /src/quarkus-run.jar

# Expose the application port
EXPOSE 8080

CMD ["java", "-jar", "/src/quarkus-run.jar"]
