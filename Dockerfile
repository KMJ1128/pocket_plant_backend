
FROM gradle:8.5-jdk21-jammy AS build
COPY . .
RUN ./gradlew build -x test


FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /home/gradle/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]