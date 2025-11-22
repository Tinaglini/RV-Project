package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa os métodos de pagamento dos clientes da RV Digital.
 * Suporta diferentes tipos: PIX, BOLETO, CARTAO_CREDITO, CARTAO_DEBITO.
 * Relaciona-se com Cliente (N-1).
 */
@Entity
@Table(name = "metodos_pagamento")
@Getter
@Setter
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O tipo de pagamento é obrigatório")
    @Size(max = 20, message = "Tipo deve ter no máximo 20 caracteres")
    @Column(nullable = false, length = 20)
    private String tipo; // PIX, BOLETO, CARTAO_CREDITO, CARTAO_DEBITO

    @Size(max = 100, message = "Apelido deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String apelido; // Ex: "Meu PIX Principal", "Cartão Pessoal"

    @Size(max = 200, message = "Informação deve ter no máximo 200 caracteres")
    @Column(length = 200)
    private String informacao; // Chave PIX, últimos 4 dígitos do cartão, etc.

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode; // QR Code em Base64 ou texto (para PIX)

    @NotNull(message = "O status é obrigatório")
    @Size(max = 15, message = "Status deve ter no máximo 15 caracteres")
    @Column(nullable = false, length = 15)
    private String status = "ATIVO"; // ATIVO, INATIVO

    @Column(nullable = false)
    private Boolean principal = false; // Método de pagamento principal

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento N-1 com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("metodosPagamento")
    private Cliente cliente;

    public MetodoPagamento() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public MetodoPagamento(String tipo, String apelido, String informacao, Cliente cliente) {
        this();
        this.tipo = tipo;
        this.apelido = apelido;
        this.informacao = informacao;
        this.cliente = cliente;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gera QR Code fake para pagamento PIX
     * @param valor Valor do pagamento
     * @param descricao Descrição do pagamento
     */
    public void gerarQRCodePIX(Double valor, String descricao) {
        if ("PIX".equalsIgnoreCase(this.tipo)) {
            // QR Code fake simulado - em produção seria integrado com API real
            String qrCodeData = String.format(
                "00020126580014br.gov.bcb.pix0136%s52040000530398654%05.2f5802BR5925%s6014BRASILIA62070503***63041234",
                this.informacao != null ? this.informacao : "chave@pix.com",
                valor,
                descricao != null ? descricao : "Pagamento"
            );
            this.qrCode = qrCodeData;
        }
    }

    @Override
    public String toString() {
        return "MetodoPagamento{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", apelido='" + apelido + '\'' +
                ", informacao='" + informacao + '\'' +
                ", status='" + status + '\'' +
                ", principal=" + principal +
                '}';
    }
}
