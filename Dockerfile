# --- STAGE DE BUILD ---
# Usa uma imagem base do Maven e Java 17 fornecida pelo Quarkus
FROM quay.io/quarkus/ubi-quarkus-maven:22.2-java17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /usr/src/app

# Copia o pom.xml e as dependências para que o Maven possa baixá-las em cache
# Isso otimiza o build para que as dependências não sejam baixadas toda vez que o código muda
COPY pom.xml .
RUN mvn dependency:resolve

# Copia todo o resto do código fonte para o container
COPY src /usr/src/app/src

# Compila o projeto Quarkus e cria o JAR executável (Uber JAR para facilitar)
# -Dquarkus.package.type=uber-jar NÃO é necessário aqui pois já estamos copiando a pasta quarkus-app
# mas vamos usar o comando padrão que você já conhece
RUN mvn clean package -Dquarkus.package.type=uber-jar

# --- STAGE DE EXECUÇÃO ---
# Usa uma imagem base leve com OpenJDK 17 Runtime
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime

# Define o diretório de trabalho onde a aplicação será executada
WORKDIR /app

# Copia os arquivos da aplicação compilada do estágio de build
# Note que target/quarkus-app é o padrão para o pacote JVM do Quarkus
COPY --from=build /usr/src/app/target/quarkus-app /app

# Expõe a porta 8080 que é a porta padrão do Quarkus
EXPOSE 8080

# Comando para iniciar a aplicação
CMD ["java", "-jar", "/app/quarkus-run.jar"]