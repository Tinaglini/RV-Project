package app.sistemaclientesrv.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa as contas a pagar dos clientes (luz, água, telefone, etc).
 * Cada contrato é uma conta que o cliente precisa pagar.
 */
@Entity
@Table(name = "contratos")
@Getter
@Setter
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String descricao; // Ex: "Conta de Luz - CPFL", "Conta de Água - SABESP"

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false)
    private Double valor;

    @NotNull(message = "A data de vencimento é obrigatória")
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Size(max = 20, message = "Status deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String status = "PENDENTE"; // PENDENTE, PAGO, VENCIDO, CANCELADO

    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String categoria; // ENERGIA, AGUA, TELEFONE, INTERNET, GAS, ALUGUEL, OUTROS

    @Size(max = 100, message = "Código de barras deve ter no máximo 100 caracteres")
    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras; // Para boletos

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento; // Quando foi pago

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    @Column(length = 255)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento N-1 com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonBackReference("cliente-contratos")
    private Cliente cliente;

    // Relacionamento 1-N com MetodoPagamento (histórico de tentativas de pagamento)
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("contrato-metodos")
    private List<MetodoPagamento> metodosPagamento;

    public Contrato() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Contrato(String descricao, Double valor, LocalDate dataVencimento, String categoria, Cliente cliente) {
        this();
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.categoria = categoria;
        this.cliente = cliente;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Contrato{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", dataVencimento=" + dataVencimento +
                ", status='" + status + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}
