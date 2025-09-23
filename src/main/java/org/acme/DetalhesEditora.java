package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Schema(description = "Detalhes adicionais de uma editora")
public class DetalhesEditora extends PanacheEntity {

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Formato de telefone inválido")
    @Schema(description = "Telefone de contato da editora", example = "(11) 3707-3500")
    public String telefone;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Email de contato da editora", example = "contato@companhiadasletras.com.br")
    public String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editora_id", unique = true, nullable = false)
    @Schema(hidden = true)
    public Editora editora;

    public DetalhesEditora() {
    }

    public DetalhesEditora(String telefone, String email, Editora editora) {
        this.telefone = telefone;
        this.email = email;
        this.editora = editora;
    }
}