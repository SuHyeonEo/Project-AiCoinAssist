FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
COPY src src

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Seoul

COPY --from=builder /workspace/build/libs/batch-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
