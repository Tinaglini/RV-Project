package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.MetodoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {

    // Busca transações de pagamento por cliente
    List<MetodoPagamento> findByClienteId(Long clienteId);

    // Busca transações de pagamento por contrato (conta)
    List<MetodoPagamento> findByContratoId(Long contratoId);

    // Busca transações de pagamento por serviço (forma de pagamento)
    List<MetodoPagamento> findByServicoId(Long servicoId);

    // Busca transações de pagamento por status
    List<MetodoPagamento> findByStatus(String status);

    // Busca transações de pagamento de um cliente com determinado status
    List<MetodoPagamento> findByClienteIdAndStatus(Long clienteId, String status);

    // Busca transações de um contrato com determinado status
    List<MetodoPagamento> findByContratoIdAndStatus(Long contratoId, String status);

    // Consulta personalizada JPQL: busca transações por nome do cliente
    @Query("SELECT m FROM MetodoPagamento m WHERE m.cliente.nome LIKE %:nomeCliente%")
    List<MetodoPagamento> buscarPorNomeCliente(@Param("nomeCliente") String nomeCliente);

    // Consulta personalizada JPQL: busca transações PIX com QR Code gerado
    @Query("SELECT m FROM MetodoPagamento m WHERE m.servico.nome = 'PIX' AND m.qrCode IS NOT NULL")
    List<MetodoPagamento> buscarPIXComQRCode();

    // Consulta personalizada JPQL: busca transações concluídas de um cliente
    @Query("SELECT m FROM MetodoPagamento m WHERE m.cliente.id = :clienteId AND m.status = 'CONCLUIDO' ORDER BY m.dataTransacao DESC")
    List<MetodoPagamento> buscarPagamentosConcluidos(@Param("clienteId") Long clienteId);

    // Consulta personalizada JPQL: busca total pago por cliente
    @Query("SELECT SUM(m.valorTotal) FROM MetodoPagamento m WHERE m.cliente.id = :clienteId AND m.status = 'CONCLUIDO'")
    Double calcularTotalPagoPorCliente(@Param("clienteId") Long clienteId);
}
