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
 * Controller para endpoints de autenticação
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
     * Endpoint de login
     * @param loginRequest Credenciais de login
     * @return JWT token e informações do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("🔐 Tentando login para usuário: {}", loginRequest.getUsername());

        try {
            // Autenticar usuário (Spring Security valida senha automaticamente)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            log.info("✅ Autenticação bem-sucedida para: {}", loginRequest.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            // Carregar usuário do banco com JOIN FETCH para garantir que as roles sejam carregadas
            User user = userRepository.findByUsernameWithRoles(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList())
                : List.of();

            log.info("🎫 Token JWT gerado para: {}", loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    roles
            ));
        } catch (BadCredentialsException e) {
            log.warn("❌ Credenciais inválidas para usuário: {}", loginRequest.getUsername());
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Usuário ou senha incorretos"));
        } catch (Exception e) {
            log.error("❌ Erro inesperado no login para usuário: {} - {}",
                      loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Erro ao processar login: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de registro de novo usuário + cliente
     * Cria AMBOS: User (autenticação) e Cliente (dados pessoais)
     *
     * @param signupRequest Dados do novo usuário/cliente
     * @return Mensagem de sucesso ou erro
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("📝 Tentando registrar novo usuário: {}", signupRequest.getEmail());

        // Validar se email já existe
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("⚠️ Email já cadastrado: {}", signupRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro: Email já está em uso!"));
        }

        // Validar se CPF já existe
        if (clienteRepository.findByCpf(signupRequest.getCpf()).isPresent()) {
            log.warn("⚠️ CPF já cadastrado: {}", signupRequest.getCpf());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro: CPF já está cadastrado!"));
        }

        try {
            // 1. Criar User (para autenticação Spring Security)
            User user = new User();
            user.setUsername(signupRequest.getEmail()); // Email como username
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getSenha()));

            // Definir roles
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Erro: Role ROLE_USER não encontrada"));
            roles.add(userRole);
            user.setRoles(roles);

            // Salvar User primeiro
            User savedUser = userRepository.save(user);
            log.info("✅ User criado com ID: {}", savedUser.getId());

            // 2. Criar Cliente (dados pessoais completos)
            Cliente cliente = new Cliente();
            cliente.setNome(signupRequest.getNome());
            cliente.setEmail(signupRequest.getEmail());
            cliente.setCpf(signupRequest.getCpf());
            cliente.setDataNascimento(signupRequest.getDataNascimento());
            cliente.setTelefone(signupRequest.getTelefone());
            cliente.setSenhaHash(passwordEncoder.encode(signupRequest.getSenha())); // Mesma senha criptografada
            cliente.setAtivo(true);
            cliente.setStatusCadastro("COMPLETO");

            // Salvar Cliente
            Cliente savedCliente = clienteRepository.save(cliente);
            log.info("✅ Cliente criado com ID: {}", savedCliente.getId());

            log.info("🎉 Registro completo! User ID: {}, Cliente ID: {}",
                     savedUser.getId(), savedCliente.getId());

            return ResponseEntity.ok(new MessageResponse(
                "Conta criada com sucesso! Você já pode fazer login."
            ));

        } catch (Exception e) {
            log.error("❌ Erro ao registrar usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Erro ao criar conta: " + e.getMessage()));
        }
    }
}
