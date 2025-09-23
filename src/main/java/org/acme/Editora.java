package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Entity
@Schema(description = "Representa uma editora")
public class Editora extends PanacheEntity {

    @NotBlank(message = "O nome da editora é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Schema(description = "Nome da editora", example = "Companhia das Letras")
    public String nome;

    @NotBlank(message = "O endereço da editora é obrigatório")
    @Size(min = 5, max = 255, message = "O endereço deve ter entre 5 e 255 caracteres")
    @Schema(description = "Endereço da editora", example = "Rua Bandeira Paulista, 702 - São Paulo")
    public String endereco;

    @OneToOne(mappedBy = "editora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(hidden = true)
    public DetalhesEditora detalhes;

    @OneToMany(mappedBy = "editora", fetch = FetchType.LAZY)
    @Schema(hidden = true)
    public List<Livro> livros;

    public Editora() {
    }

    public Editora(String nome, String endereco) {
        this.nome = nome;
        this.endereco = endereco;
    }
}