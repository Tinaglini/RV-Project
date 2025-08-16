package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa os contratos de serviços dos clientes RV Digital.
 * Relaciona-se com Cliente (N-1) e Item (1-N).
 */
@Entity
@Table(name = "contratos")
@Getter
@Setter
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Size(max = 20, message = "Status deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String status = "ATIVO"; // ATIVO, INATIVO, CANCELADO

    @Positive(message = "Valor total deve ser positivo")
    @Column(name = "valor_total", precision = 10, scale = 2)
    private Double valorTotal = 0.0;

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    @Column(length = 255)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacionamento N-1 com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("contratos")
    private Cliente cliente;

    // @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties("contrato")
    // private List<Item> itens;

    public Contrato() {
        this.createdAt = LocalDateTime.now();
    }

    public Contrato(LocalDate dataInicio, Cliente cliente) {
        this();
        this.dataInicio = dataInicio;
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        return "Contrato{" +
                "id=" + id +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", status='" + status + '\'' +
                ", valorTotal=" + valorTotal +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}