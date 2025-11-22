package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa transações de pagamento de contas (Contratos).
 * Registra o pagamento de uma conta usando um serviço de pagamento (PIX, TED, Boleto).
 * Contém dados específicos do método escolhido (chave PIX, dados bancários, código de barras, etc).
 */
@Entity
@Table(name = "metodos_pagamento")
@Getter
@Setter
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false)
    private Double valor; // Valor da transação (sem taxa)

    @Column(name = "valor_taxa")
    private Double valorTaxa; // Taxa cobrada pelo serviço

    @Column(name = "valor_total")
    private Double valorTotal; // Valor total (valor + valorTaxa)

    @NotNull(message = "A data da transação é obrigatória")
    @Column(name = "data_transacao", nullable = false)
    private LocalDateTime dataTransacao;

    @NotNull(message = "O status é obrigatório")
    @Size(max = 20, message = "Status deve ter no máximo 20 caracteres")
    @Column(nullable = false, length = 20)
    private String status = "PENDENTE"; // PENDENTE, PROCESSANDO, CONCLUIDO, FALHO, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String comprovante; // Comprovante de pagamento (pode ser URL ou texto)

    // Dados específicos de PIX
    @Size(max = 100, message = "Chave PIX deve ter no máximo 100 caracteres")
    @Column(name = "chave_pix", length = 100)
    private String chavePix; // Chave PIX usada na transação

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode; // QR Code PIX gerado

    // Dados específicos de TED/Transferência
    @Size(max = 100, message = "Nome do banco deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String banco; // Nome do banco

    @Size(max = 10, message = "Agência deve ter no máximo 10 caracteres")
    @Column(length = 10)
    private String agencia;

    @Size(max = 20, message = "Conta deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String conta;

    @Size(max = 20, message = "Tipo de conta deve ter no máximo 20 caracteres")
    @Column(name = "tipo_conta", length = 20)
    private String tipoConta; // CORRENTE, POUPANCA

    // Dados específicos de Boleto
    @Size(max = 100, message = "Código de barras deve ter no máximo 100 caracteres")
    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Size(max = 100, message = "Linha digitável deve ter no máximo 100 caracteres")
    @Column(name = "linha_digitavel", length = 100)
    private String linhaDigitavel;

    // Dados específicos de Cartão
    @Size(max = 20, message = "Número do cartão deve ter no máximo 20 caracteres")
    @Column(name = "numero_cartao", length = 20)
    private String numeroCartao; // Últimos 4 dígitos mascarados (ex: **** **** **** 1234)

    @Size(max = 30, message = "Bandeira deve ter no máximo 30 caracteres")
    @Column(length = 30)
    private String bandeira; // VISA, MASTERCARD, ELO, etc.

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    @Column(length = 255)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento N-1 com Cliente (quem fez o pagamento)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties({"metodosPagamento", "contratos"})
    private Cliente cliente;

    // Relacionamento N-1 com Contrato (qual conta foi paga)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    @JsonIgnoreProperties("metodosPagamento")
    private Contrato contrato;

    // Relacionamento N-1 com Servico (qual forma de pagamento foi usada)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    @JsonIgnoreProperties("metodosPagamento")
    private Servico servico;

    public MetodoPagamento() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.dataTransacao = LocalDateTime.now();
    }

    public MetodoPagamento(Double valor, Cliente cliente, Contrato contrato, Servico servico) {
        this();
        this.valor = valor;
        this.cliente = cliente;
        this.contrato = contrato;
        this.servico = servico;
        calcularValorTotal();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula o valor total (valor + taxa do serviço)
     */
    public void calcularValorTotal() {
        if (this.servico != null && this.servico.getTaxa() != null) {
            this.valorTaxa = this.servico.getTaxa();
            this.valorTotal = this.valor + this.valorTaxa;
        } else {
            this.valorTaxa = 0.0;
            this.valorTotal = this.valor;
        }
    }

    /**
     * Gera QR Code para pagamento PIX
     * @param descricao Descrição do pagamento
     */
    public void gerarQRCodePIX(String descricao) {
        if (this.servico != null && "PIX".equalsIgnoreCase(this.servico.getNome())) {
            // QR Code fake simulado - em produção seria integrado com API real do Banco Central
            String qrCodeData = String.format(
                "00020126580014br.gov.bcb.pix0136%s52040000530398654%05.2f5802BR5925%s6014BRASILIA62070503***63041234",
                this.chavePix != null ? this.chavePix : "chave@pix.com",
                this.valorTotal != null ? this.valorTotal : this.valor,
                descricao != null ? descricao : "Pagamento de conta"
            );
            this.qrCode = qrCodeData;
        }
    }

    @Override
    public String toString() {
        return "MetodoPagamento{" +
                "id=" + id +
                ", valor=" + valor +
                ", valorTotal=" + valorTotal +
                ", status='" + status + '\'' +
                ", dataTransacao=" + dataTransacao +
                '}';
    }
}
