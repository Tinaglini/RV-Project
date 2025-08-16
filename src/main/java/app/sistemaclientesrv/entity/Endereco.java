package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa os endereços dos clientes da RV Digital.
 * Relaciona-se com Cliente (N-1).
 */
@Entity
@Table(name = "enderecos")
@Getter
@Setter
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo rua é obrigatório")
    @Size(max = 150, message = "Rua deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String rua;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    @Column(nullable = false, length = 10)
    private String numero;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 50, message = "Cidade deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String cidade;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 2, message = "Estado deve ter 2 caracteres")
    @Column(nullable = false, length = 2)
    private String estado;

    @NotBlank(message = "O CEP é obrigatório")
    @Size(min = 8, max = 9, message = "CEP deve ter entre 8 e 9 caracteres")
    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false)
    private Boolean principal = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacionamento N-1 com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("enderecos")
    private Cliente cliente;

    public Endereco() {
        this.createdAt = LocalDateTime.now();
    }

    public Endereco(String rua, String numero, String cidade, String estado, String cep) {
        this();
        this.rua = rua;
        this.numero = numero;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "id=" + id +
                ", rua='" + rua + '\'' +
                ", numero='" + numero + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", cep='" + cep + '\'' +
                ", principal=" + principal +
                '}';
    }
}