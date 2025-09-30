# Estágio de Build: Usa o Maven para compilar o projeto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /usr/src/app

# Copia o pom.xml e baixa as dependências (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia todo o resto do código fonte para o container
COPY src /usr/src/app/src

# Compila o projeto Quarkus e cria o JAR executável
RUN mvn clean package -Dquarkus.package.type=uber-jar

# ---

# Estágio Final: Cria a imagem leve final para rodar a aplicação
FROM eclipse-temurin:21-jre
WORKDIR /app

# ----> MUDANÇA 1: Copia o JAR criado no estágio de build, e não a pasta inexistente
COPY --from=build /usr/src/app/target/*-runner.jar /app/app.jar

EXPOSE 8080

# ----> MUDANÇA 2: Define o comando para executar a aplicação via JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]