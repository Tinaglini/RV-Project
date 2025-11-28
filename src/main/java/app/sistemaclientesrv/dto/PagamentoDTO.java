package app.sistemaclientesrv.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para receber dados de pagamento de um contrato
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {

    @NotNull(message = "ID do serviço de pagamento é obrigatório")
    private Long servicoId;

    private String chavePix;
    private String codigoBarras;
    private String numeroCartao;
    private String bandeira;
    private String observacoes;
}
