# 1. 빌드 단계
FROM gradle:8.5-jdk21-jammy AS build

USER root

# Node.js 20 LTS 공식 설치
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# 프로젝트 소스 복사
COPY . .

# React 의존성 설치
RUN cd pocket_plant_web && npm install

# Gradle 빌드 실행
RUN chmod +x gradlew && ./gradlew build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jdk-jammy

COPY --from=build /home/gradle/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]