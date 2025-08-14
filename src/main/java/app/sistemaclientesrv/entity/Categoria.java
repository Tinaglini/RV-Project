package app.sistemaclientesrv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa as categorias de clientes (Pessoa Física, Pessoa Jurídica).
 * Implementa relacionamento 1-N com Cliente.
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50, unique = true)
    private String nome;

    @NotBlank(message = "O campo descrição é obrigatório")
    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String descricao;

    @Size(max = 100, message = "Benefícios deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String beneficios;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** TODO: Ativar após criar Cliente.java
     * @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     * @JsonIgnoreProperties("categoria")
     * private List<Cliente> clientes;
     */

    public Categoria() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Categoria(String nome, String descricao) {
        this();
        this.nome = nome;
        this.descricao = descricao;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", beneficios='" + beneficios + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}