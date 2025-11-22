package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.MetodoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {

    List<MetodoPagamento> findAll();

    // OBRIGATÓRIO 1: Busca métodos de pagamento por tipo
    List<MetodoPagamento> findByTipoIgnoreCase(String tipo);

    // OBRIGATÓRIO 2: Busca métodos de pagamento por cliente
    List<MetodoPagamento> findByClienteId(Long clienteId);

    // Extra: Busca métodos de pagamento principais
    List<MetodoPagamento> findByPrincipalTrue();

    // Extra: Busca métodos de pagamento por status
    List<MetodoPagamento> findByStatus(String status);

    // Extra: Busca métodos de pagamento ativos de um cliente
    List<MetodoPagamento> findByClienteIdAndStatus(Long clienteId, String status);

    // Extra: Busca método de pagamento principal do cliente
    List<MetodoPagamento> findByClienteIdAndPrincipalTrue(Long clienteId);

    // Consulta personalizada JPQL: busca métodos de pagamento por nome do cliente
    @Query("SELECT m FROM MetodoPagamento m WHERE m.cliente.nome LIKE %:nomeCliente%")
    List<MetodoPagamento> buscarPorNomeCliente(@Param("nomeCliente") String nomeCliente);

    // Consulta personalizada JPQL: busca métodos de pagamento PIX com QR Code gerado
    @Query("SELECT m FROM MetodoPagamento m WHERE m.tipo = 'PIX' AND m.qrCode IS NOT NULL")
    List<MetodoPagamento> buscarPIXComQRCode();
}
