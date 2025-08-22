package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findAll();
    // OBRIGATÓRIO 1: Busca endereços por cidade (ignora maiúsculas/minúsculas)
    List<Endereco> findByCidadeContainingIgnoreCase(String cidade);

    // OBRIGATÓRIO 2: Busca endereços por cliente
    List<Endereco> findByClienteId(Long clienteId);

    // Extra: Busca endereços principais
    List<Endereco> findByPrincipalTrue();

    // Extra: Busca endereços por estado
    List<Endereco> findByEstado(String estado);

    // Extra: Busca endereços por CEP
    List<Endereco> findByCep(String cep);

    // Consulta personalizada JPQL: busca endereços por nome do cliente
    @Query("SELECT e FROM Endereco e WHERE e.cliente.nome LIKE %:nomeCliente%")
    List<Endereco> buscarPorNomeCliente(@Param("nomeCliente") String nomeCliente);
}