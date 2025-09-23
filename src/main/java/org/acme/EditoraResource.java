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

@Path("/editoras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Editoras", description = "Operações relacionadas a editoras")
public class EditoraResource {

    @Context
    UriInfo uriInfo;

    private EditoraRepresentation toRepresentation(Editora editora) {
        return EditoraRepresentation.fromEntity(editora, uriInfo);
    }

    private List<EditoraRepresentation> toRepresentationList(List<Editora> editoras) {
        return editoras.stream()
                .map(this::toRepresentation)
                .collect(Collectors.toList());
    }

    private Editora toEntity(EditoraRepresentation rep) {
        Editora editora = new Editora();
        editora.nome = rep.nome;
        editora.endereco = rep.endereco;

        if (rep.detalhes != null) {
            DetalhesEditora detalhes = new DetalhesEditora();
            detalhes.telefone = rep.detalhes.telefone;
            detalhes.email = rep.detalhes.email;
            editora.detalhes = detalhes;
            detalhes.editora = editora;
        }
        return editora;
    }

    @GET
    @Operation(summary = "Listar todas as editoras", description = "Retorna a lista de todas as editoras cadastradas")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de editoras obtida com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SearchEditoraResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Nenhuma editora encontrada (lista vazia)"
            )
    })
    public Response getAllEditoras() {
        List<Editora> editoras = Editora.listAll();

        if (editoras.isEmpty()) {
            return Response.noContent().build();
        }

        List<EditoraRepresentation> representations = toRepresentationList(editoras);
        SearchEditoraResponse searchResponse = new SearchEditoraResponse();
        searchResponse.results = representations;
        searchResponse.addLink("self", uriInfo.getAbsolutePath().toString());

        return Response.ok(searchResponse).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar editora por ID", description = "Recupera uma editora específica pelo ID")
    @APIResponse(responseCode = "200", description = "Editora encontrada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = EditoraRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Editora não encontrada")
    public Response getEditoraById(@PathParam("id") Long id) {
        Editora editora = Editora.findById(id);

        if (editora == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Editora com ID " + id + " não encontrada.").build();
        }
        return Response.ok(toRepresentation(editora)).build();
    }

    @POST
    @Operation(summary = "Cadastrar nova editora", description = "Adiciona uma nova editora ao sistema")
    @RequestBody(
            description = "Dados da editora a ser criada, incluindo detalhes opcionais.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = EditoraRepresentation.class)
            )
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Editora criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EditoraRepresentation.class))),
            @APIResponse(responseCode = "400", description = "Dados da editora inválidos")
    })
    @Transactional
    public Response createEditora(@Valid EditoraRepresentation editoraRep) {
        try {
            Editora editora = toEntity(editoraRep);
            editora.persist();
            URI location = uriInfo.getAbsolutePathBuilder().path(editora.id.toString()).build();
            return Response.created(location).entity(toRepresentation(editora)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar editora", description = "Atualiza os dados de uma editora existente")
    @RequestBody(
            description = "Dados da editora a ser atualizada. Detalhes opcionais podem ser atualizados ou adicionados.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = EditoraRepresentation.class)
            )
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Editora atualizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EditoraRepresentation.class))),
            @APIResponse(responseCode = "400", description = "Dados da editora inválidos"),
            @APIResponse(responseCode = "404", description = "Editora não encontrada")
    })
    @Transactional
    public Response updateEditora(@PathParam("id") Long id, @Valid EditoraRepresentation editoraRep) {
        return Editora.findByIdOptional(id)
                .map(panacheEntityBase -> {
                    Editora editora = (Editora) panacheEntityBase;
                    editora.nome = editoraRep.nome;
                    editora.endereco = editoraRep.endereco;

                    if (editoraRep.detalhes != null) {
                        if (editora.detalhes == null) {
                            DetalhesEditora novosDetalhes = new DetalhesEditora();
                            novosDetalhes.telefone = editoraRep.detalhes.telefone;
                            novosDetalhes.email = editoraRep.detalhes.email;
                            editora.detalhes = novosDetalhes;
                            novosDetalhes.editora = editora;
                        } else {
                            editora.detalhes.telefone = editoraRep.detalhes.telefone;
                            editora.detalhes.email = editoraRep.detalhes.email;
                        }
                    } else if (editora.detalhes != null) {
                        editora.detalhes.delete();
                        editora.detalhes = null;
                    }

                    return Response.ok(toRepresentation(editora)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Excluir editora", description = "Remove uma editora do sistema")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Editora excluída com sucesso"),
            @APIResponse(responseCode = "404", description = "Editora não encontrada")
    })
    @Transactional
    public Response deleteEditora(@PathParam("id") Long id) {
        boolean deleted = Editora.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/busca/nome/{nome}")
    @Operation(summary = "Buscar editoras por nome", description = "Pesquisa editoras contendo o texto especificado no nome")
    @APIResponse(responseCode = "200", description = "Lista de editoras encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = EditoraRepresentation.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "Nenhuma editora encontrada com o nome fornecido")
    public Response buscarPorNome(@PathParam("nome") String nome) {
        List<EditoraRepresentation> editoras = toRepresentationList(Editora.list("lower(nome) LIKE ?1", "%" + nome.toLowerCase() + "%"));
        if (editoras.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(editoras).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca avançada de editoras", description = "Permite buscar editoras por texto livre em nome, endereço, telefone ou email, com paginação e ordenação.")
    @APIResponse(responseCode = "200", description = "Resultados da busca",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchEditoraResponse.class)))
    public Response search(
            @Parameter(description = "Query de busca por nome, endereço, telefone ou email")
            @RestQuery("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno (ex: id, nome)")
            @RestQuery("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RestQuery("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Número da página (base 1)")
            @RestQuery("page") @DefaultValue("1") int page,
            @Parameter(description = "Tamanho da página")
            @RestQuery("size") @DefaultValue("10") int size) {

        Set<String> allowedSortFields = Set.of("id", "nome", "endereco");
        if (!allowedSortFields.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Editora> query;
        if (q == null || q.isBlank()) {
            query = Editora.findAll(sortObj);
        } else {
            String searchTerm = "%" + q.toLowerCase() + "%";
            query = Editora.find("lower(nome) like ?1 or lower(endereco) like ?1 or lower(detalhes.telefone) like ?1 or lower(detalhes.email) like ?1",
                    sortObj, searchTerm);
        }

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);

        List<Editora> editoras = query.page(effectivePage, size).list();

        SearchEditoraResponse response = SearchEditoraResponse.from(
                toRepresentationList(editoras), uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }

    @GET
    @Path("/{editoraId}/detalhes")
    @Operation(summary = "Buscar detalhes da editora por ID", description = "Recupera os detalhes de uma editora específica")
    @APIResponse(responseCode = "200", description = "Detalhes da editora encontrados com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DetalhesEditoraRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Detalhes da editora não encontrados")
    public Response getDetalhesEditora(@PathParam("editoraId") Long editoraId) {
        Editora editora = Editora.findById(editoraId);
        if (editora == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Editora com ID " + editoraId + " não encontrada.").build();
        }
        if (editora.detalhes == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Detalhes para a Editora com ID " + editoraId + " não encontrados.").build();
        }
        return Response.ok(DetalhesEditoraRepresentation.fromEntity(editora.detalhes, uriInfo)).build();
    }
}