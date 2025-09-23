package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/autores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autores", description = "Operações relacionadas a autores")
public class AutorResource {

    @Context
    UriInfo uriInfo;

    private AutorRepresentation toRepresentation(Autor autor) {
        return AutorRepresentation.fromEntity(autor, uriInfo);
    }

    private List<AutorRepresentation> toRepresentationList(List<Autor> autores) {
        return autores.stream()
                .map(this::toRepresentation)
                .collect(Collectors.toList());
    }

    private Autor toEntity(AutorRepresentation rep) {
        Autor autor = new Autor();
        autor.nome = rep.nome;
        autor.nacionalidade = rep.nacionalidade;
        return autor;
    }

    @GET
    @Operation(summary = "Listar todos os autores", description = "Retorna a lista de todos os autores cadastrados")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de autores obtida com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SearchAutorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Nenhum autor encontrado (lista vazia)"
            )
    })
    public Response getAllAutores() {
        List<Autor> autores = Autor.listAll();

        if (autores.isEmpty()) {
            return Response.noContent().build();
        }

        List<AutorRepresentation> representations = toRepresentationList(autores);
        SearchAutorResponse searchResponse = new SearchAutorResponse();
        searchResponse.results = representations;
        searchResponse.addLink("self", uriInfo.getAbsolutePath().toString());

        return Response.ok(searchResponse).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar autor por ID", description = "Recupera um autor específico pelo ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Autor encontrado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AutorRepresentation.class))),
            @APIResponse(responseCode = "404", description = "Autor não encontrado")
    })
    public Response getAutorById(@PathParam("id") Long id) {
        Autor autor = Autor.findById(id);

        if (autor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Autor com ID " + id + " não encontrado.").build();
        }
        return Response.ok(toRepresentation(autor)).build();
    }

    @POST
    @Operation(summary = "Cadastrar novo autor", description = "Adiciona um novo autor ao sistema")
    @RequestBody(
            description = "Dados do autor a ser criado.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AutorRepresentation.class)
            )
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Autor criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AutorRepresentation.class))),
            @APIResponse(responseCode = "400", description = "Dados do autor inválidos")
    })
    @Transactional
    public Response createAutor(@Valid AutorRepresentation autorRep) {
        try {
            Autor autor = toEntity(autorRep);
            autor.persist();
            URI location = uriInfo.getAbsolutePathBuilder().path(autor.id.toString()).build();
            return Response.created(location).entity(toRepresentation(autor)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar autor", description = "Atualiza os dados de um autor existente")
    @RequestBody(
            description = "Dados do autor a ser atualizado.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AutorRepresentation.class)
            )
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Autor atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AutorRepresentation.class))),
            @APIResponse(responseCode = "400", description = "Dados do autor inválidos"),
            @APIResponse(responseCode = "404", description = "Autor não encontrado")
    })
    @Transactional
    public Response updateAutor(@PathParam("id") Long id, @Valid AutorRepresentation autorRep) {
        return Autor.findByIdOptional(id)
                .map(panacheEntityBase -> {
                    Autor autor = (Autor) panacheEntityBase;
                    autor.nome = autorRep.nome;
                    autor.nacionalidade = autorRep.nacionalidade;

                    return Response.ok(toRepresentation(autor)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Excluir autor", description = "Remove um autor do sistema")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Autor excluído com sucesso"),
            @APIResponse(responseCode = "404", description = "Autor não encontrado")
    })
    @Transactional
    public Response deleteAutor(@PathParam("id") Long id) {
        boolean deleted = Autor.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/busca/nome/{nome}")
    @Operation(summary = "Buscar autores por nome", description = "Pesquisa autores contendo o texto especificado no nome")
    @APIResponse(responseCode = "200", description = "Lista de autores encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AutorRepresentation.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "Nenhum autor encontrado com o nome fornecido")
    public Response buscarPorNome(@PathParam("nome") String nome) {
        List<AutorRepresentation> autores = toRepresentationList(Autor.list("lower(nome) LIKE ?1", "%" + nome.toLowerCase() + "%"));
        if (autores.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(autores).build();
    }

    @GET
    @Path("/busca/nacionalidade/{nacionalidade}")
    @Operation(summary = "Buscar autores por nacionalidade", description = "Pesquisa autores com a nacionalidade especificada")
    @APIResponse(responseCode = "200", description = "Lista de autores encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AutorRepresentation.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "Nenhum autor encontrado com a nacionalidade fornecida")
    public Response buscarPorNacionalidade(@PathParam("nacionalidade") String nacionalidade) {
        List<AutorRepresentation> autores = toRepresentationList(Autor.list("lower(nacionalidade) LIKE ?1", "%" + nacionalidade.toLowerCase() + "%"));
        if (autores.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(autores).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca avançada de autores", description = "Permite buscar autores por texto livre em nome ou nacionalidade, com paginação e ordenação.")
    @APIResponse(responseCode = "200", description = "Resultados da busca",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchAutorResponse.class)))
    public Response search(
            @Parameter(description = "Query de busca por nome ou nacionalidade")
            @RestQuery("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno (ex: id, nome, nacionalidade)")
            @RestQuery("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RestQuery("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Número da página (base 1)")
            @RestQuery("page") @DefaultValue("1") int page,
            @Parameter(description = "Tamanho da página")
            @RestQuery("size") @DefaultValue("10") int size) {

        Set<String> allowedSortFields = Set.of("id", "nome", "nacionalidade");
        if (!allowedSortFields.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Autor> query;
        if (q == null || q.isBlank()) {
            query = Autor.findAll(sortObj);
        } else {
            String searchTerm = "%" + q.toLowerCase() + "%";
            query = Autor.find("lower(nome) like ?1 or lower(nacionalidade) like ?1",
                    sortObj, searchTerm);
        }

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);

        List<Autor> autores = query.page(effectivePage, size).list();

        SearchAutorResponse response = SearchAutorResponse.from(
                toRepresentationList(autores), uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }
}