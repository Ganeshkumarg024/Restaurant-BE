# Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY --from=build ${JAR_FILE} app.jar

EXPOSE 8881
ENTRYPOINT ["java","-jar","/app.jar"]