# ---- Bước 1: build file jar bằng Maven (máy chủ không cần cài Java) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
ENV MAVEN_OPTS="-Xmx512m"
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Bước 2: image chạy gọn nhẹ, chỉ có JRE ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
COPY --from=build /src/target/aicoach-api.jar app.jar
USER spring
EXPOSE 8080
ENV JAVA_OPTS="-Xms128m -Xmx384m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
