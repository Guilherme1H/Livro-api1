package org.acme;

import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Schema(name = "DetalhesEditoraRepresentation", description = "Representação dos detalhes da editora com links HATEOAS")
public class DetalhesEditoraRepresentation {

    public Long id;
    public String telefone;
    public String email;

    @Schema(name = "_links", description = "Links HATEOAS para a representação dos detalhes da editora")
    public Map<String, String> _links = new HashMap<>();

    public void addLink(String rel, String href) {
        this._links.put(rel, href);
    }

    public static DetalhesEditoraRepresentation fromEntity(DetalhesEditora entity, UriInfo uriInfo) {
        if (entity == null) {
            return null;
        }
        DetalhesEditoraRepresentation rep = new DetalhesEditoraRepresentation();
        rep.id = entity.id;
        rep.telefone = entity.telefone;
        rep.email = entity.email;

        if (entity.editora != null && entity.editora.id != null) {
            UriBuilder baseUri = uriInfo.getBaseUriBuilder()
                    .path(EditoraResource.class)
                    .path(entity.editora.id.toString())
                    .path("detalhes");

            rep.addLink("self", baseUri.build().toString());

            rep.addLink("editora", uriInfo.getBaseUriBuilder()
                    .path(EditoraResource.class)
                    .path(entity.editora.id.toString())
                    .build().toString());

            rep.addLink("update", baseUri.build().toString());
            rep.addLink("delete", baseUri.build().toString());
        }

        return rep;
    }
}