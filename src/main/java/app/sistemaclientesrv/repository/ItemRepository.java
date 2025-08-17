package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // OBRIGATÓRIO 1: Busca itens por contrato
    List<Item> findByContratoId(Long contratoId);

    // OBRIGATÓRIO 2: Busca itens por serviço
    List<Item> findByServicoId(Long servicoId);

    // Extra: Busca itens por quantidade mínima
    List<Item> findByQuantidadeGreaterThanEqual(Integer quantidade);

    // Extra: Busca itens com desconto
    List<Item> findByDescontoGreaterThan(Double desconto);

    // Extra: Busca itens por valor final mínimo
    List<Item> findByValorFinalGreaterThanEqual(Double valorMinimo);

    // Consulta personalizada JPQL: busca itens por categoria do serviço
    @Query("SELECT i FROM Item i WHERE i.servico.categoria = :categoria")
    List<Item> buscarPorCategoriaServico(@Param("categoria") String categoria);
}