package org.acme;

import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Schema(description = "Representação de um autor com links HATEOAS")
public class AutorRepresentation {

    @Schema(readOnly = true)
    public Long id;

    @Schema(example = "J.R.R. Tolkien")
    public String nome;

    @Schema(example = "Britânica")
    public String nacionalidade;

    @Schema(readOnly = true)
    public Map<String, String> _links = new HashMap<>();

    public void addLink(String rel, String href) {
        this._links.put(rel, href);
    }

    public static AutorRepresentation fromEntity(Autor autor, UriInfo uriInfo) {
        if (autor == null) {
            return null;
        }
        AutorRepresentation rep = new AutorRepresentation();
        rep.id = autor.id;
        rep.nome = autor.nome;
        rep.nacionalidade = autor.nacionalidade;

        UriBuilder baseUri = uriInfo.getBaseUriBuilder().path(AutorResource.class);
        rep.addLink("self", baseUri.path(autor.id.toString()).build().toString());
        rep.addLink("all", baseUri.build().toString());
        rep.addLink("update", baseUri.path(autor.id.toString()).build().toString());
        rep.addLink("delete", baseUri.path(autor.id.toString()).build().toString());

        rep.addLink("livros_deste_autor", uriInfo.getBaseUriBuilder().path(LivroResource.class).path("search").queryParam("autorId", autor.id).build().toString());

        return rep;
    }
}