# --- STAGE DE BUILD ---
# Alterado para uma imagem oficial do Maven com Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /usr/src/app

# Copia o pom.xml e as dependências para que o Maven possa baixá-las em cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia todo o resto do código fonte para o container
COPY src /usr/src/app/src

# Compila o projeto Quarkus e cria o JAR executável
RUN mvn clean package -Dquarkus.package.type=uber-jar

# --- STAGE DE EXECUÇÃO ---
# Alterado para uma imagem oficial de execução com Java 21
FROM eclipse-temurin:21-jre

# Define o diretório de trabalho onde a aplicação será executada
WORKDIR /app

# Copia os arquivos da aplicação compilada do estágio de build
COPY --from=build /usr/src/app/target/quarkus-app /app

# Expõe a porta 8080 que é a porta padrão do Quarkus
EXPOSE 8080

# Comando para iniciar a aplicação
CMD ["java", "-jar", "/app/quarkus-run.jar"]