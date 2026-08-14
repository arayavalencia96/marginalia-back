FROM maven:3.9.16-eclipse-temurin-21-noble AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

RUN groupadd --system marginalia \
    && useradd --system --gid marginalia --home-dir /app --shell /usr/sbin/nologin marginalia

COPY --from=build --chown=marginalia:marginalia /workspace/target/marginalia-api-*.jar /app/app.jar

USER marginalia

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
