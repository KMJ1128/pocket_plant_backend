# 1. 빌드 단계 (Node.js와 Gradle이 모두 필요한 환경)
FROM gradle:8.5-jdk21-jammy AS build

# Node.js 설치 (Ubuntu 기반 이미지이므로 apt 사용)
USER root
RUN apt-get update && \
    apt-get install -y nodejs npm

# 프로젝트 파일 복사
COPY . .

# React 빌드 (pocket_plant_web 디렉토리로 이동하여 빌드)
# build.gradle에서 이미 설정을 마쳤다면 아래 명령만으로도 충분합니다.
RUN chmod +x gradlew && ./gradlew build -x test

# 2. 실행 단계 (경량화된 자바 런타임)
FROM eclipse-temurin:21-jdk-jammy

# 빌드 단계에서 생성된 jar 파일 복사
COPY --from=build /home/gradle/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app.jar"]