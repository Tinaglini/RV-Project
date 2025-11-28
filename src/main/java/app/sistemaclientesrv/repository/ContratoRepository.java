package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    // Busca contas por status (PENDENTE, PAGO, VENCIDO, CANCELADO)
    List<Contrato> findByStatus(String status);

    // Busca contas de um cliente específico
    List<Contrato> findByClienteId(Long clienteId);

    // Busca contas pendentes de clientes ativos
    List<Contrato> findByStatusAndClienteAtivoTrue(String status);

    // Busca contas por categoria (ENERGIA, AGUA, TELEFONE, INTERNET, GAS, ALUGUEL)
    List<Contrato> findByCategoria(String categoria);

    // Busca contas por período de vencimento
    List<Contrato> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);

    // Busca contas vencidas (data de vencimento antes de hoje e status PENDENTE)
    List<Contrato> findByDataVencimentoBeforeAndStatus(LocalDate data, String status);

    // Busca contas que vencem hoje
    List<Contrato> findByDataVencimento(LocalDate data);

    // Consulta personalizada JPQL: busca contas por nome do cliente e status
    @Query("SELECT c FROM Contrato c WHERE c.cliente.nome LIKE %:nomeCliente% AND c.status = :status")
    List<Contrato> buscarPorNomeClienteEStatus(@Param("nomeCliente") String nomeCliente, @Param("status") String status);

    // Consulta personalizada JPQL: busca contas próximas ao vencimento (próximos N dias)
    @Query("SELECT c FROM Contrato c WHERE c.dataVencimento BETWEEN :hoje AND :dataLimite AND c.status = 'PENDENTE' ORDER BY c.dataVencimento ASC")
    List<Contrato> buscarContasProximasVencimento(@Param("hoje") LocalDate hoje, @Param("dataLimite") LocalDate dataLimite);

    // Consulta personalizada JPQL: calcula total de contas pendentes por cliente
    @Query("SELECT SUM(c.valor) FROM Contrato c WHERE c.cliente.id = :clienteId AND c.status = 'PENDENTE'")
    Double calcularTotalPendente(@Param("clienteId") Long clienteId);
}