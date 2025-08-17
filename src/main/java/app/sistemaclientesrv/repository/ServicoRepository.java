package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // OBRIGATÓRIO 1: Busca serviços por nome (ignora maiúsculas/minúsculas)
    List<Servico> findByNomeContainingIgnoreCase(String nome);

    // OBRIGATÓRIO 2: Retorna apenas os serviços ativos
    List<Servico> findByAtivoTrue();

    // Extra: Busca serviços por categoria
    List<Servico> findByCategoria(String categoria);

    // Extra: Busca serviços ativos por categoria
    List<Servico> findByCategoriaAndAtivoTrue(String categoria);

    // Consulta personalizada JPQL: busca serviços com valor até determinado limite
    @Query("SELECT s FROM Servico s WHERE s.valor <= :valorMaximo AND s.ativo = true")
    List<Servico> buscarPorValorMaximo(@Param("valorMaximo") Double valorMaximo);
}