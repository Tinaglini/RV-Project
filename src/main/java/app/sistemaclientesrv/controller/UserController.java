package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.User;
import app.sistemaclientesrv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller com endpoints protegidos por autenticação e autorização
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * Endpoint acessível por usuários autenticados (qualquer role)
     * @return Informações do usuário logado
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint acessível apenas por usuários com ROLE_USER
     * @return Mensagem de acesso
     */
    @GetMapping("/user-access")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conteúdo de usuário comum");
        response.put("accessLevel", "USER");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint acessível apenas por administradores
     * @return Lista de todos os usuários
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> usersResponse = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("enabled", user.getEnabled());
            userMap.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
        }).toList();

        return ResponseEntity.ok(usersResponse);
    }

    /**
     * Endpoint acessível apenas por administradores
     * @return Mensagem de acesso administrativo
     */
    @GetMapping("/admin-access")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conteúdo exclusivo de administrador");
        response.put("accessLevel", "ADMIN");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint acessível apenas por administradores
     * Deleta um usuário pelo ID
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuário deletado com sucesso");
        response.put("userId", id.toString());

        return ResponseEntity.ok(response);
    }
}
