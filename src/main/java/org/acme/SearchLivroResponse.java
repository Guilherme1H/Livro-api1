package org.acme;

import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URI;
import java.util.List;

@Schema(name = "SearchLivroResponse", description = "Resposta paginada para a busca de livros com links HATEOAS")
public class SearchLivroResponse {

    public List<LivroRepresentation> livros;
    public String query;
    public String sort;
    public String direction;
    public int page;
    public int size;
    public long totalElements;
    public long totalPages;

    @Schema(name = "_links", description = "Links HATEOAS para navegação da paginação")
    public Links _links;

    public static SearchLivroResponse from(
            List<LivroRepresentation> livros,
            UriInfo uriInfo,
            String query,
            String sort,
            String direction,
            int page,
            int size,
            long totalElements,
            long totalPages) {

        SearchLivroResponse response = new SearchLivroResponse();
        response.livros = livros;
        response.query = query;
        response.sort = sort;
        response.direction = direction;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = totalPages;

        response._links = new Links();
        UriBuilder baseUri = uriInfo.getRequestUriBuilder();

        response._links.self = baseUri.replaceQueryParam("page", page).build();
        response._links.first = baseUri.replaceQueryParam("page", 1).build();
        response._links.last = baseUri.replaceQueryParam("page", totalPages).build();

        if (page > 1) {
            response._links.prev = baseUri.replaceQueryParam("page", page - 1).build();
        }
        if (page < totalPages) {
            response._links.next = baseUri.replaceQueryParam("page", page + 1).build();
        }

        return response;
    }

    @Schema(name = "LinksSearch", description = "Coleção de links HATEOAS para paginação da busca")
    public static class Links {
        public URI self;
        public URI first;
        public URI prev;
        public URI next;
        public URI last;
    }
}