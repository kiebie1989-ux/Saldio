# Multi-Stage: Build mit Maven, Laufzeit auf schlankem JRE.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# Nicht als root laufen
RUN useradd -r -u 1001 bwa
COPY --from=build /app/target/controlling-backend-*.jar app.jar
USER bwa
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
