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

    // OBRIGATÓRIO 1: Busca contratos por status
    List<Contrato> findByStatus(String status);

    // OBRIGATÓRIO 2: Busca contratos por cliente
    List<Contrato> findByClienteId(Long clienteId);

    // Extra: Busca contratos ativos de clientes ativos
    List<Contrato> findByStatusAndClienteAtivoTrue(String status);

    // Extra: Busca contratos por período
    List<Contrato> findByDataInicioBetween(LocalDate inicio, LocalDate fim);

    // Extra: Busca contratos vencidos
    List<Contrato> findByDataFimBeforeAndStatus(LocalDate data, String status);

    // Consulta personalizada JPQL: busca contratos por nome do cliente e status
    @Query("SELECT c FROM Contrato c WHERE c.cliente.nome LIKE %:nomeCliente% AND c.status = :status")
    List<Contrato> buscarPorNomeClienteEStatus(@Param("nomeCliente") String nomeCliente, @Param("status") String status);
}