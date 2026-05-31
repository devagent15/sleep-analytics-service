FROM gradle:8.10.2-jdk21 AS build

WORKDIR /workspace

COPY build.gradle.kts settings.gradle.kts gradle.properties /workspace/
COPY src /workspace/src

RUN gradle --no-daemon test bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
