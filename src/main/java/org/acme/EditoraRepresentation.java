package org.acme;

import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Schema(name = "EditoraRepresentation", description = "Representação de uma editora com links HATEOAS e detalhes")
public class EditoraRepresentation {

    public Long id;
    public String nome;
    public String endereco;

    public DetalhesEditoraRepresentation detalhes;

    @Schema(name = "_links", description = "Links HATEOAS para a representação da editora")
    public Map<String, String> _links = new HashMap<>();

    public void addLink(String rel, String href) {
        this._links.put(rel, href);
    }

    public static EditoraRepresentation fromEntity(Editora entity, UriInfo uriInfo) {
        if (entity == null) {
            return null;
        }
        EditoraRepresentation rep = new EditoraRepresentation();
        rep.id = entity.id;
        rep.nome = entity.nome;
        rep.endereco = entity.endereco;

        if (entity.detalhes != null) {
            rep.detalhes = DetalhesEditoraRepresentation.fromEntity(entity.detalhes, uriInfo);
        }

        UriBuilder baseUri = uriInfo.getBaseUriBuilder().path(EditoraResource.class);

        rep.addLink("self", baseUri.path(entity.id.toString()).build().toString());
        rep.addLink("all", baseUri.build().toString());
        rep.addLink("update", baseUri.path(entity.id.toString()).build().toString());
        rep.addLink("delete", baseUri.path(entity.id.toString()).build().toString());

        if (entity.detalhes != null && entity.detalhes.id != null) {
            rep.addLink("detalhes", uriInfo.getBaseUriBuilder()
                    .path(EditoraResource.class)
                    .path(entity.id.toString())
                    .path("detalhes")
                    .build().toString());
        }

        return rep;
    }
}