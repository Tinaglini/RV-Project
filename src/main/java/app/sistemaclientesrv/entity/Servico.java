package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa as formas de pagamento disponíveis (PIX, TED, Boleto, etc).
 * Cada serviço é uma modalidade de pagamento com sua respectiva taxa.
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
    private String nome; // PIX, TED, Boleto, Cartão

    @NotBlank(message = "O campo descrição é obrigatório")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String descricao;

    @NotNull(message = "A taxa é obrigatória")
    @PositiveOrZero(message = "A taxa deve ser zero ou positiva")
    @Column(nullable = false)
    private Double taxa; // Taxa cobrada pela forma de pagamento

    @Size(max = 50, message = "Tipo deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String tipo; // TRANSFERENCIA, BOLETO, CARTAO, DEBITO_CONTA

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "tempo_processamento")
    private String tempoProcessamento; // Ex: "Instantâneo", "1 dia útil", "2-3 dias úteis"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento 1-N com MetodoPagamento (pagamentos feitos com este serviço)
    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("servico")
    private List<MetodoPagamento> metodosPagamento;

    public Servico() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Servico(String nome, String descricao, Double taxa, String tipo) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.taxa = taxa;
        this.tipo = tipo;
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
                ", taxa=" + taxa +
                ", tipo='" + tipo + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
