package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Aqui ficam todas as consultas relacionadas às categorias de clientes.
 * Permite buscar, filtrar e validar as categorias no banco de dados.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Busca categorias por nome (ignora maiúsculas/minúsculas)
    List<Categoria> findByNomeContainingIgnoreCase(String nome);

    // Retorna apenas as categorias ativas
    List<Categoria> findByAtivoTrue();

    // Busca categoria pelo nome exato
    Optional<Categoria> findByNome(String nome);

    // Verifica se já existe uma categoria com esse nome
    boolean existsByNome(String nome);

    // Consulta personalizada: busca categoria ativa por nome específico
    @Query("SELECT c FROM Categoria c WHERE c.nome = :nome AND c.ativo = true")
    Optional<Categoria> buscarPorNomeAtivo(@Param("nome") String nome);
}