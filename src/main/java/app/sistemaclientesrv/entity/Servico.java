package app.sistemaclientesrv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa os serviços oferecidos pela RV Digital.
 * Tipos: RECARGA, FINANCEIRO, DIGITAL
 */
@Entity
@Table(name = "servicos")
@Getter
@Setter
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O campo descrição é obrigatório")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private Double valor;

    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String categoria; // RECARGA, FINANCEIRO, DIGITAL

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties("servico")
    // private List<Item> itens;

    public Servico() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Servico(String nome, String descricao, Double valor, String categoria) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Servico{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", categoria='" + categoria + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}