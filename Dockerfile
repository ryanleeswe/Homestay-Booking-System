# Build the executable Spring Boot JAR with the project's Java version.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src/main ./src/main
# Database-dependent integration tests run separately, not during image creation.
RUN mvn -B -ntp -Dmaven.test.skip=true package \
    && cp target/Homestay-Booking-System-*.jar /build/app.jar

# The runtime image only contains the JRE and packaged application.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /build/app.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
