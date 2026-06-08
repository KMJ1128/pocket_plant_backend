# 1. 빌드 단계
FROM gradle:8.5-jdk21-jammy AS build

# 루트 권한으로 Node.js와 npm 설치
USER root
RUN apt-get update && \
    apt-get install -y nodejs npm

# 프로젝트 소스 복사
COPY . .

# [핵심 수정] React 의존성 설치 (npm install)
# pocket_plant_web 디렉토리로 이동하여 라이브러리 설치
RUN cd pocket_plant_web && npm install

# Gradle 빌드 실행
RUN chmod +x gradlew && ./gradlew build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jdk-jammy

# 빌드 결과물(jar) 복사
COPY --from=build /home/gradle/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app.jar"]