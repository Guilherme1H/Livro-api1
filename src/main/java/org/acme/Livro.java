package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Entity
@Schema(description = "Representa um livro no acervo da biblioteca")
public class Livro extends PanacheEntity {

    @NotBlank(message = "O título do livro é obrigatório")
    @Size(min = 2, max = 100, message = "O título deve ter entre 2 e 100 caracteres")
    @Schema(description = "Título do livro", example = "O Senhor dos Anéis")
    public String titulo;

    @NotBlank(message = "O ISBN é obrigatório")
    @Pattern(regexp = "^\\d{13}$", message = "O ISBN deve conter 13 dígitos numéricos")
    @Schema(description = "ISBN do livro (13 dígitos)", example = "9788533613379")
    public String isbn;

    @NotNull(message = "O ano de publicação é obrigatório")
    @Min(value = 1000, message = "O ano de publicação deve ser maior que 1000")
    @Max(value = 2100, message = "O ano de publicação não pode ser maior que 2100")
    @Schema(description = "Ano de publicação", example = "1954")
    public Integer anoPublicacao;

    @ManyToMany
    public List<Autor> autores = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "editora_id")
    public Editora editora;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status atual do livro", example = "DISPONIVEL")
    public StatusLivro status = StatusLivro.DISPONIVEL;

    public enum StatusLivro {
        DISPONIVEL,
        EMPRESTADO,
        EM_MANUTENCAO,
        EXTRAVIADO
    }

    public Livro() {
    }

    public Livro(String titulo, String isbn, Integer anoPublicacao, Editora editora) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.editora = editora;
        this.status = StatusLivro.DISPONIVEL;
    }
}