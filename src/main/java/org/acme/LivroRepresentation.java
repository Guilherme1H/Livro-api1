package org.acme;

import org.acme.Livro;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Schema(name = "LivroRepresentation", description = "Representação de um livro com links HATEOAS")
public class LivroRepresentation {

    public Long id;
    public String titulo;
    public String isbn;
    public Integer anoPublicacao;
    public Livro.StatusLivro status;

    @Schema(description = "Autores do livro")
    public List<AutorRepresentation> autores;

    @Schema(description = "Editora do livro")
    public EditoraRepresentation editora;

    @Schema(name = "_links", description = "Links HATEOAS para a representação do livro")
    public Links _links;

    public static LivroRepresentation fromEntity(Livro entity, UriInfo uriInfo) {
        LivroRepresentation rep = new LivroRepresentation();
        rep.id = entity.id;
        rep.titulo = entity.titulo;
        rep.isbn = entity.isbn;
        rep.anoPublicacao = entity.anoPublicacao;
        rep.status = entity.status;

        if (entity.autores != null && !entity.autores.isEmpty()) {
            rep.autores = entity.autores.stream()
                    .map(autor -> AutorRepresentation.fromEntity(autor, uriInfo))
                    .collect(Collectors.toList());
        }

        if (entity.editora != null) {
            rep.editora = EditoraRepresentation.fromEntity(entity.editora, uriInfo);
        }

        UriBuilder baseUri = uriInfo.getBaseUriBuilder().path("livros");
        rep._links = new Links();
        rep._links.self = baseUri.path(entity.id.toString()).build();
        rep._links.all = baseUri.build();
        rep._links.update = baseUri.path(entity.id.toString()).build();
        rep._links.delete = baseUri.path(entity.id.toString()).build();
        if (entity.editora != null) {
            rep._links.editora = uriInfo.getBaseUriBuilder().path("editoras").path(entity.editora.id.toString()).build();
        }
        if (entity.autores != null && !entity.autores.isEmpty()) {
            rep._links.autores = uriInfo.getBaseUriBuilder().path("livros").path(entity.id.toString()).path("autores").build();
        }

        return rep;
    }

    @Schema(name = "LinksLivro", description = "Coleção de links HATEOAS para Livro")
    public static class Links {
        public URI self;
        public URI all;
        public URI update;
        public URI delete;
        public URI editora;
        public URI autores;
    }
}