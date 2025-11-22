package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Busca serviço por nome exato (necessário para DataLoader)
    Optional<Servico> findByNome(String nome);

    // Busca serviços por nome parcial (ignora maiúsculas/minúsculas)
    List<Servico> findByNomeContainingIgnoreCase(String nome);

    // Retorna apenas as formas de pagamento ativas
    List<Servico> findByAtivoTrue();

    // Busca por tipo de forma de pagamento (TRANSFERENCIA, BOLETO, CARTAO, DEBITO_CONTA)
    List<Servico> findByTipo(String tipo);

    // Busca formas de pagamento ativas por tipo
    List<Servico> findByTipoAndAtivoTrue(String tipo);

    // Consulta personalizada JPQL: busca formas de pagamento com taxa até determinado limite
    @Query("SELECT s FROM Servico s WHERE s.taxa <= :taxaMaxima AND s.ativo = true")
    List<Servico> buscarPorTaxaMaxima(@Param("taxaMaxima") Double taxaMaxima);

    // Busca formas de pagamento gratuitas (taxa = 0)
    @Query("SELECT s FROM Servico s WHERE s.taxa = 0.0 AND s.ativo = true")
    List<Servico> buscarFormasPagamentoGratuitas();
}