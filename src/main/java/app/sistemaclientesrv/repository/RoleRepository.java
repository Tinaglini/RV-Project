package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de persistência da entidade Role
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca uma role pelo nome
     * @param name Nome da role
     * @return Optional contendo a role se encontrada
     */
    Optional<Role> findByName(String name);

    /**
     * Verifica se existe uma role com o nome informado
     * @param name Nome da role
     * @return true se existe, false caso contrário
     */
    Boolean existsByName(String name);
}
