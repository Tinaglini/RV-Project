package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade intermediária que representa os itens de um contrato RV Digital.
 * Implementa relacionamento N-N entre Contrato e Servico.
 */
@Entity
@Table(name = "itens")
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser positiva")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false)
    private Double valor;

    @Column
    private Double desconto = 0.0;

    @Column(name = "valor_final")
    private Double valorFinal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacionamento N-1 com Contrato
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    @JsonIgnoreProperties("itens")
    private Contrato contrato;

    // Relacionamento N-1 com Servico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    @JsonIgnoreProperties("itens")
    private Servico servico;

    public Item() {
        this.createdAt = LocalDateTime.now();
    }

    public Item(Integer quantidade, Double valor, Contrato contrato, Servico servico) {
        this();
        this.quantidade = quantidade;
        this.valor = valor;
        this.contrato = contrato;
        this.servico = servico;
        this.calcularValorFinal();
    }

    public void calcularValorFinal() {
        if (this.valor != null && this.quantidade != null) {
            this.valorFinal = (this.valor * this.quantidade) - (this.desconto != null ? this.desconto : 0.0);
        }
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", quantidade=" + quantidade +
                ", valor=" + valor +
                ", desconto=" + desconto +
                ", valorFinal=" + valorFinal +
                '}';
    }
}