# 📚 API de Gerenciamento de Livros

E aí! Essa é uma API RESTful que criei para gerenciar livros, autores e editoras. Ela foi desenvolvida com Quarkus para a disciplina de Web Services e o foco foi fazer uma API moderna: com HATEOAS (aqueles links que te guiam na API), validações nos dados, paginação e ordenação pra organizar tudo.

## �� O que usei pra construir?

*   **Quarkus**: Meu framework Java preferido! Leve, rápido e perfeito para APIs.
*   **Java**: A linguagem por trás de tudo.
*   **Panache**: Ajuda demais a lidar com o banco de dados de um jeito simples.
*   **Hibernate Validator**: Pra garantir que os dados que chegam na API estão sempre certinhos.
*   **Flyway**: Cuida da evolução do banco de dados, tipo um "controle de versão" pra ele.
*   **H2 Database**: Um banco de dados rapidinho pra rodar localmente (mas dá pra trocar fácil!).
*   **HATEOAS**: A cereja do bolo! Links inteligentes que te mostram o que fazer em seguida na API.

## 🌟 O que essa API faz?

Essa API te permite gerenciar tudo de uma biblioteca:

*   📖 **Livros**:
    *   Criar, ver, atualizar, apagar.
    *   Pesquisar por título, ISBN, ano, etc.
    *   Mudar o status (disponível, emprestado, em manutenção, extraviado).
    *   Ver os livros de um autor ou editora específica.
*   ✍️ **Autores**:
    *   Criar, ver, atualizar, apagar.
    *   Pesquisar por nome ou nacionalidade.
*   �� **Editoras**:
    *   Criar, ver, atualizar, apagar.
    *   Pesquisar por nome, endereço, contato.
    *   Ver os detalhes de contato (telefone, e-mail).

## 🧭 Para testar

Pra você ver tudo funcionando e até fazer umas chamadas de teste, use o Swagger UI!

*   **Rodando no seu PC**: `http://localhost:8080/q/swagger-ui/`


## 🛠️ Colocando pra Rodar

1.  **O que você precisa ter**: Java 17+ e Maven 3.8+.
2.  **Pegar o código**:
    ```bash
    git clone https://github.com/Guilherme1H/Livro-api1.git
    cd Livro-api
    ```
3.  **Rodar no modo "desenvolvimento" (com atualizações automáticas)**:
    ```bash
    ./mvnw quarkus:dev
    ```
    A API estará em `http://localhost:8080` e qualquer mudança no código já aparece na hora!
4.  **Pra gerar um arquivo final (tipo pra produção)**:
    ```bash
    ./mvnw clean package
    java -jar target/quarkus-app/quarkus-run.jar
    ```

## 🧠 As Peças Chave do Sistema (Entidades)

*   **Livro**: Onde ficam todas as infos dos livros.
*   **Autor**: Quem escreveu os livros.
*   **Editora**: Quem publica os livros, com dados de contato separados (DetalhesEditora).

## 📄 Licença

Relaxa! Esse projeto é de uso livre.