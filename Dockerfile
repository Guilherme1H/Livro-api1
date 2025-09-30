# --- STAGE DE BUILD ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /usr/src/app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src /usr/src/app/src
RUN mvn clean package -Dquarkus.package.type=uber-jar

# --- STAGE DE EXECUÇÃO ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /usr/src/app/target/quarkus-app /app

EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]