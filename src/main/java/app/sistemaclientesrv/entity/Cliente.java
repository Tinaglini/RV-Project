package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa os clientes da RV Digital.
 * Clientes PF/PJ que utilizam recargas, app RV Point e serviços financeiros.
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 14, message = "CPF deve ter entre 11 e 14 caracteres")
    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
    @Column(length = 15)
    private String telefone;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Size(max = 20, message = "Status do cadastro deve ter no máximo 20 caracteres")
    @Column(name = "status_cadastro", length = 20)
    private String statusCadastro; // COMPLETO, INCOMPLETO

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento N-1 com Categoria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties("clientes")
    private Categoria categoria;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cliente")
    private List<Endereco> enderecos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cliente")
    private List<Contrato> contratos;

    public Cliente() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Cliente(String nome, String cpf, LocalDate dataNascimento) {
        this();
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", statusCadastro='" + statusCadastro + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}