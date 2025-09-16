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

    // Busca cliente por CPF
    Optional<Cliente> findByCpf(String cpf);

    // Verifica se CPF já existe
    boolean existsByCpf(String cpf);

    // NOVO: Busca cliente por email
    Optional<Cliente> findByEmail(String email);

    // NOVO: Verifica se email já existe
    boolean existsByEmail(String email);

    // Busca clientes por status do cadastro
    List<Cliente> findByStatusCadastro(String statusCadastro);

    // Busca clientes ativos
    List<Cliente> findByAtivoTrue();

    // NOVO: Busca contas bloqueadas
    List<Cliente> findByContaBloqueadaTrue();

    // NOVO: Busca clientes inativos
    List<Cliente> findByAtivoFalse();

    // NOVO: Busca clientes por CPF ou email
    @Query("SELECT c FROM Cliente c WHERE c.cpf = :cpfOuEmail OR c.email = :cpfOuEmail")
    Optional<Cliente> findByCpfOrEmail(@Param("cpfOuEmail") String cpfOuEmail);

    // Consulta personalizada JPQL: busca clientes por nome da categoria
    @Query("SELECT c FROM Cliente c WHERE c.categoria.nome = :nomeCategoria")
    List<Cliente> buscarPorNomeCategoria(@Param("nomeCategoria") String nomeCategoria);

    // NOVO: Busca clientes ativos por categoria
    @Query("SELECT c FROM Cliente c WHERE c.categoria.id = :categoriaId AND c.ativo = true")
    List<Cliente> findByCategoriaIdAndAtivoTrue(@Param("categoriaId") Long categoriaId);

    // NOVO: Busca clientes com cadastro completo
    @Query("SELECT c FROM Cliente c WHERE c.statusCadastro = 'COMPLETO' AND c.ativo = true")
    List<Cliente> findClientesAtivosComCadastroCompleto();

    // NOVO: Busca clientes com muitas tentativas de login
    @Query("SELECT c FROM Cliente c WHERE c.tentativasLogin >= :tentativas")
    List<Cliente> findByTentativasLoginGreaterThanEqual(@Param("tentativas") Integer tentativas);
}