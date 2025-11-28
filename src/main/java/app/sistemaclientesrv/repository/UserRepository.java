package app.sistemaclientesrv.repository;

import app.sistemaclientesrv.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de persistência da entidade User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca um usuário pelo username
     * @param username Nome de usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca um usuário pelo username com roles carregadas (JOIN FETCH)
     * @param username Nome de usuário
     * @return Optional contendo o usuário com roles se encontrado
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Busca um usuário pelo email
     * @param email Email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o username informado
     * @param username Nome de usuário
     * @return true se existe, false caso contrário
     */
    Boolean existsByUsername(String username);

    /**
     * Verifica se existe um usuário com o email informado
     * @param email Email do usuário
     * @return true se existe, false caso contrário
     */
    Boolean existsByEmail(String email);
}
