package org.acme;

import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "Estrutura de resposta para buscas paginadas de autores")
public class SearchAutorResponse {

    @Schema(description = "Resultados da busca de autores")
    public List<AutorRepresentation> results;

    @Schema(description = "Metadados da paginação")
    public PaginationMetadata metadata;

    @Schema(description = "Links HATEOAS para navegação")
    public Map<String, String> _links = new HashMap<>();

    public void addLink(String rel, String href) {
        this._links.put(rel, href);
    }

    public static SearchAutorResponse from(
            List<AutorRepresentation> results,
            UriInfo uriInfo,
            String q,
            String sort,
            String direction,
            int page,
            int size,
            long totalElements,
            long totalPages) {

        SearchAutorResponse response = new SearchAutorResponse();
        response.results = results;
        response.metadata = new PaginationMetadata(totalElements, totalPages, page, size);

        String baseUri = uriInfo.getAbsolutePath().toString();
        StringBuilder queryParams = new StringBuilder();
        if (q != null && !q.isBlank()) queryParams.append("q=").append(q).append("&");
        queryParams.append("sort=").append(sort).append("&");
        queryParams.append("direction=").append(direction).append("&");
        queryParams.append("size=").append(size);

        response.addLink("self", String.format("%s?%s&page=%d", baseUri, queryParams.toString(), page));
        if (page > 1) {
            response.addLink("first", String.format("%s?%s&page=1", baseUri, queryParams.toString()));
            response.addLink("prev", String.format("%s?%s&page=%d", baseUri, queryParams.toString(), page - 1));
        }
        if (page < totalPages) {
            response.addLink("next", String.format("%s?%s&page=%d", baseUri, queryParams.toString(), page + 1));
            response.addLink("last", String.format("%s?%s&page=%d", baseUri, queryParams.toString(), totalPages));
        }

        return response;
    }
}