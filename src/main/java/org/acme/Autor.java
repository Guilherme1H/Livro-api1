package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Entity
@Schema(description = "Representa um autor de livros")
public class Autor extends PanacheEntity {

    @NotBlank(message = "O nome do autor é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Schema(description = "Nome completo do autor", example = "J.R.R. Tolkien")
    public String nome;

    @NotBlank(message = "A nacionalidade do autor é obrigatória")
    @Size(min = 2, max = 50, message = "A nacionalidade deve ter entre 2 e 50 caracteres")
    @Schema(description = "Nacionalidade do autor", example = "Britânica")
    public String nacionalidade;

    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    @Schema(hidden = true)
    public List<Livro> livros = new ArrayList<>();

    public Autor() {
    }

    public Autor(String nome, String nacionalidade) {
        this.nome = nome;
        this.nacionalidade = nacionalidade;
    }
}