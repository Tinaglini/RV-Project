package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAll();
    // OBRIGATÓRIO 1: Busca clientes por nome (ignora maiúsculas/minúsculas)
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    // OBRIGATÓRIO 2: Busca clientes por categoria
    List<Cliente> findByCategoriaId(Long categoriaId);

    // Extra: Busca cliente por CPF
    Optional<Cliente> findByCpf(String cpf);

    // Extra: Verifica se CPF já existe
    boolean existsByCpf(String cpf);

    // Extra: Busca clientes por status do cadastro
    List<Cliente> findByStatusCadastro(String statusCadastro);

    // Extra: Busca clientes ativos
    List<Cliente> findByAtivoTrue();

    // Consulta personalizada JPQL: busca clientes por nome da categoria
    @Query("SELECT c FROM Cliente c WHERE c.categoria.nome = :nomeCategoria")
    List<Cliente> buscarPorNomeCategoria(@Param("nomeCategoria") String nomeCategoria);
}