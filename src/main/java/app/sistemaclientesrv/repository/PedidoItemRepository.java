package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    List<PedidoItem> findAll();
    // OBRIGATÓRIO 1: Busca itens de pedido por contrato
    List<PedidoItem> findByContratoId(Long contratoId);

    // OBRIGATÓRIO 2: Busca itens de pedido por serviço
    List<PedidoItem> findByServicoId(Long servicoId);

    // Extra: Busca itens de pedido por quantidade mínima
    List<PedidoItem> findByQuantidadeGreaterThanEqual(Integer quantidade);

    // Extra: Busca itens de pedido com desconto
    List<PedidoItem> findByDescontoGreaterThan(Double desconto);

    // Extra: Busca itens de pedido por valor final mínimo
    List<PedidoItem> findByValorFinalGreaterThanEqual(Double valorMinimo);

    // Consulta personalizada JPQL: busca itens de pedido por categoria do serviço
    @Query("SELECT i FROM PedidoItem i WHERE i.servico.categoria = :categoria")
    List<PedidoItem> buscarPorCategoriaServico(@Param("categoria") String categoria);
}