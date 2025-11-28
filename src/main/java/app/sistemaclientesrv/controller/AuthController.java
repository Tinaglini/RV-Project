package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.dto.JwtResponse;
import app.sistemaclientesrv.dto.LoginRequest;
import app.sistemaclientesrv.dto.MessageResponse;
import app.sistemaclientesrv.dto.SignupRequest;
import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Role;
import app.sistemaclientesrv.entity.User;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.RoleRepository;
import app.sistemaclientesrv.repository.UserRepository;
import app.sistemaclientesrv.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller for user login and registration endpoints.
 * Handles JWT token generation and user account creation.
 *
 * @author Sistema Clientes RV
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest user credentials (username and password)
     * @return ResponseEntity containing JWT token and user information, or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            log.info("Authentication successful for user: {}", loginRequest.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = userRepository.findByUsernameWithRoles(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList())
                : List.of();

            log.info("JWT token generated for user: {}", loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    roles
            ));
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Usuário ou senha incorretos"));
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {} - {}",
                      loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Erro ao processar login: " + e.getMessage()));
        }
    }

    /**
     * Registers a new user account by creating both User and Cliente entities.
     * The User entity is used for Spring Security authentication, while Cliente
     * stores complete personal information.
     *
     * @param signupRequest complete registration data including personal information
     * @return ResponseEntity containing success message or validation error
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Attempting user registration for email: {}", signupRequest.getEmail());

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("Email already registered: {}", signupRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro: Email já está em uso!"));
        }

        if (clienteRepository.findByCpf(signupRequest.getCpf()).isPresent()) {
            log.warn("CPF already registered: {}", signupRequest.getCpf());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro: CPF já está cadastrado!"));
        }

        try {
            User user = new User();
            user.setUsername(signupRequest.getEmail());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getSenha()));

            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Erro: Role ROLE_USER não encontrada"));
            roles.add(userRole);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            log.info("User entity created with ID: {}", savedUser.getId());

            Cliente cliente = new Cliente();
            cliente.setNome(signupRequest.getNome());
            cliente.setEmail(signupRequest.getEmail());
            cliente.setCpf(signupRequest.getCpf());
            cliente.setDataNascimento(signupRequest.getDataNascimento());
            cliente.setTelefone(signupRequest.getTelefone());
            cliente.setSenhaHash(passwordEncoder.encode(signupRequest.getSenha()));
            cliente.setAtivo(true);
            cliente.setStatusCadastro("COMPLETO");

            Cliente savedCliente = clienteRepository.save(cliente);
            log.info("Cliente entity created with ID: {}", savedCliente.getId());

            log.info("Registration completed successfully for user ID: {} and cliente ID: {}",
                     savedUser.getId(), savedCliente.getId());

            return ResponseEntity.ok(new MessageResponse(
                "Conta criada com sucesso! Você já pode fazer login."
            ));

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Erro ao criar conta: " + e.getMessage()));
        }
    }
}
