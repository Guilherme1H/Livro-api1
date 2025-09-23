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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/livros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Livros", description = "Operações relacionadas a livros")
public class LivroResource {

    @Context
    UriInfo uriInfo;

    private LivroRepresentation toRepresentation(Livro livro) {
        return LivroRepresentation.fromEntity(livro, uriInfo);
    }

    private List<LivroRepresentation> toRepresentationList(List<Livro> livros) {
        return livros.stream()
                .map(this::toRepresentation)
                .collect(Collectors.toList());
    }

    private Livro toEntity(LivroRepresentation rep) {
        Livro livro = new Livro();
        livro.titulo = rep.titulo;
        livro.isbn = rep.isbn;
        livro.anoPublicacao = rep.anoPublicacao;
        livro.status = rep.status != null ? rep.status : Livro.StatusLivro.DISPONIVEL;

        if (rep.editora != null && rep.editora.id != null) {
            Editora editora = Editora.findById(rep.editora.id);
            if (editora == null) {
                throw new WebApplicationException("Editora com ID " + rep.editora.id + " não encontrada.", Response.Status.BAD_REQUEST);
            }
            livro.editora = editora;
        }

        if (rep.autores != null && !rep.autores.isEmpty()) {
            livro.autores = new ArrayList<>();
            for (AutorRepresentation autorRep : rep.autores) {
                if (autorRep.id == null) {
                    throw new WebApplicationException("ID do autor é obrigatório na representação.", Response.Status.BAD_REQUEST);
                }
                Autor autor = Autor.findById(autorRep.id);
                if (autor == null) {
                    throw new WebApplicationException("Autor com ID " + autorRep.id + " não encontrado.", Response.Status.BAD_REQUEST);
                }
                livro.autores.add(autor);
            }
        }
        return livro;
    }

    @GET
    @Operation(summary = "Listar todos os livros", description = "Retorna a lista de todos os livros cadastrados")
    @APIResponse(responseCode = "200", description = "Lista de livros obtida com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class, type = SchemaType.ARRAY)))
    public List<LivroRepresentation> listarTodos() {
        return toRepresentationList(Livro.listAll());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar livro por ID", description = "Recupera um livro específico pelo ID")
    @APIResponse(responseCode = "200", description = "Livro encontrado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    public Response buscarPorId(@PathParam("id") Long id) {
        return Livro.findByIdOptional(id)
                .map(livro -> Response.ok(toRepresentation((Livro) livro)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Operation(summary = "Cadastrar novo livro", description = "Adiciona um novo livro ao acervo")
    @RequestBody(
            description = "Dados do livro a ser criado, incluindo ID da editora e IDs dos autores.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)
            )
    )
    @APIResponse(responseCode = "201", description = "Livro criado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)))
    @APIResponse(responseCode = "400", description = "Dados do livro inválidos ou entidades relacionadas não encontradas")
    @Transactional
    public Response criar(@Valid LivroRepresentation livroRep) {
        try {
            Livro livro = toEntity(livroRep);
            livro.persist();
            URI location = uriInfo.getAbsolutePathBuilder().path(livro.id.toString()).build();
            return Response.created(location).entity(toRepresentation(livro)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar livro", description = "Atualiza os dados de um livro existente")
    @RequestBody(
            description = "Dados do livro a ser atualizado. ID da editora e IDs dos autores devem ser fornecidos para atualização dos relacionamentos.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)
            )
    )
    @APIResponse(responseCode = "200", description = "Livro atualizado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)))
    @APIResponse(responseCode = "400", description = "Dados do livro inválidos ou entidades relacionadas não encontradas")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    @Transactional
    public Response atualizar(@PathParam("id") Long id, @Valid LivroRepresentation livroRep) {
        return Livro.findByIdOptional(id)
                .map(panacheEntityBase -> {
                    Livro livro = (Livro) panacheEntityBase;
                    livro.titulo = livroRep.titulo;
                    livro.isbn = livroRep.isbn;
                    livro.anoPublicacao = livroRep.anoPublicacao;
                    livro.status = livroRep.status != null ? livroRep.status : livro.status;

                    try {
                        if (livroRep.editora != null && livroRep.editora.id != null) {
                            Editora editora = Editora.findById(livroRep.editora.id);
                            if (editora == null) {
                                throw new WebApplicationException("Editora com ID " + livroRep.editora.id + " não encontrada.", Response.Status.BAD_REQUEST);
                            }
                            livro.editora = editora;
                        } else {
                            livro.editora = null;
                        }

                        livro.autores.clear();
                        if (livroRep.autores != null && !livroRep.autores.isEmpty()) {
                            for (AutorRepresentation autorRep : livroRep.autores) {
                                if (autorRep.id == null) {
                                    throw new WebApplicationException("ID do autor é obrigatório na representação.", Response.Status.BAD_REQUEST);
                                }
                                Autor autor = Autor.findById(autorRep.id);
                                if (autor == null) {
                                    throw new WebApplicationException("Autor com ID " + autorRep.id + " não encontrado.", Response.Status.BAD_REQUEST);
                                }
                                livro.autores.add(autor);
                            }
                        }
                    } catch (WebApplicationException e) {
                        return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
                    }

                    return Response.ok(toRepresentation(livro)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Excluir livro", description = "Remove um livro do acervo")
    @APIResponse(responseCode = "204", description = "Livro excluído com sucesso")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    @Transactional
    public Response excluir(@PathParam("id") Long id) {
        boolean deleted = Livro.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/busca/titulo/{titulo}")
    @Operation(summary = "Buscar livros por título", description = "Pesquisa livros contendo o texto especificado no título")
    @APIResponse(responseCode = "200", description = "Lista de livros encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class, type = SchemaType.ARRAY)))
    public List<LivroRepresentation> buscarPorTitulo(@PathParam("titulo") String titulo) {
        return toRepresentationList(Livro.list("lower(titulo) LIKE ?1", "%" + titulo.toLowerCase() + "%"));
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Filtrar livros por status", description = "Lista todos os livros com o status especificado")
    @APIResponse(responseCode = "200", description = "Lista de livros filtrada por status",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Status inválido fornecido")
    public List<LivroRepresentation> filtrarPorStatus(@PathParam("status") Livro.StatusLivro status) {
        return toRepresentationList(Livro.list("status", status));
    }

    @PUT
    @Path("/{id}/status")
    @Operation(summary = "Atualizar status do livro", description = "Altera o status de um livro (disponível, emprestado, etc)")
    @APIResponse(responseCode = "200", description = "Status do livro atualizado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LivroRepresentation.class)))
    @APIResponse(responseCode = "400", description = "Status inválido fornecido")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    @Transactional
    public Response atualizarStatus(@PathParam("id") Long id, @QueryParam("status") Livro.StatusLivro novoStatus) {
        return Livro.findByIdOptional(id)
                .map(panacheEntityBase -> {
                    Livro livro = (Livro) panacheEntityBase;
                    livro.status = novoStatus;
                    return Response.ok(toRepresentation(livro)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca avançada de livros", description = "Permite buscar livros por texto livre em título, ISBN, nome do autor ou nome da editora, com paginação e ordenação.")
    @APIResponse(responseCode = "200", description = "Resultados da busca",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchLivroResponse.class)))
    public Response search(
            @Parameter(description = "Query de busca por titulo, ISBN, nome do autor ou nome da editora")
            @QueryParam("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno (ex: id, titulo, anoPublicacao)")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Número da página (base 1)")
            @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Tamanho da página")
            @QueryParam("size") @DefaultValue("10") int size) {

        Set<String> allowedSortFields = Set.of("id", "titulo", "isbn", "anoPublicacao", "status");
        if (!allowedSortFields.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Livro> query;
        if (q == null || q.isBlank()) {
            query = Livro.findAll(sortObj);
        } else {
            String searchTerm = "%" + q.toLowerCase() + "%";
            query = Livro.find("lower(titulo) like ?1 or lower(isbn) like ?1 or lower(editora.nome) like ?1 or exists (select 1 from Autor a join Livro_Autor la on a.id = la.autores_id where la.livros_id = Livro.id and lower(a.nome) like ?1)",
                    sortObj,
                    searchTerm);
        }

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);

        List<Livro> livros = query.page(effectivePage, size).list();

        SearchLivroResponse response = SearchLivroResponse.from(
                toRepresentationList(livros), uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }
}