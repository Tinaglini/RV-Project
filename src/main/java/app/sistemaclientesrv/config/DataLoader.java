package app.sistemaclientesrv.config;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Role;
import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.entity.User;
import app.sistemaclientesrv.repository.CategoriaRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.RoleRepository;
import app.sistemaclientesrv.repository.ServicoRepository;
import app.sistemaclientesrv.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Carregando roles primeiro (necessário para usuários)
        if (roleRepository.count() == 0) {
            carregarRoles();
        }

        // Carregando usuários de teste
        if (userRepository.count() == 0) {
            carregarUsuariosTeste();
        }

        // Carregando dados iniciais apenas se o banco estiver vazio
        if (categoriaRepository.count() == 0) {
            carregarCategorias();
        }

        if (servicoRepository.count() == 0) {
            carregarServicos();
        }

        if (clienteRepository.count() == 0) {
            carregarClientesExemplo();
        }
    }

    private void carregarRoles() {
        Role roleUser = new Role("ROLE_USER", "Usuário comum do sistema");
        Role roleAdmin = new Role("ROLE_ADMIN", "Administrador do sistema");

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);

        System.out.println("✓ Roles carregadas: ROLE_USER, ROLE_ADMIN");
    }

    private void carregarUsuariosTeste() {
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ROLE_ADMIN não encontrada"));

        // Usuário comum
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roleUser);

        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRoles(userRoles);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        userRepository.save(user);

        // Usuário administrador
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleUser);
        adminRoles.add(roleAdmin);

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(adminRoles);
        admin.setEnabled(true);
        admin.setAccountNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setCredentialsNonExpired(true);

        userRepository.save(admin);

        System.out.println("✓ Usuários de teste criados:");
        System.out.println("  - Username: user | Password: user123 | Roles: ROLE_USER");
        System.out.println("  - Username: admin | Password: admin123 | Roles: ROLE_USER, ROLE_ADMIN");
    }

    private void carregarCategorias() {
        Categoria pessoaFisica = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        pessoaFisica.setBeneficios("Taxas reduzidas, atendimento personalizado");

        Categoria pessoaJuridica = new Categoria("PESSOA_JURIDICA", "Clientes pessoa jurídica");
        pessoaJuridica.setBeneficios("Desconto por volume, relatórios detalhados");

        categoriaRepository.save(pessoaFisica);
        categoriaRepository.save(pessoaJuridica);

        System.out.println("Categorias carregadas com sucesso!");
    }

    private void carregarServicos() {
        Servico recarga = new Servico("Recarga de Celular", "Recarga para todas as operadoras", 10.0, "RECARGA");
        Servico pagamento = new Servico("Pagamento de Boletos", "Pagamento de contas e boletos", 2.5, "FINANCEIRO");
        Servico transferencia = new Servico("Transferência PIX", "Transferências via PIX", 1.0, "FINANCEIRO");
        Servico cartao = new Servico("Cartão Pré-pago", "Emissão de cartão pré-pago", 15.0, "DIGITAL");

        servicoRepository.save(recarga);
        servicoRepository.save(pagamento);
        servicoRepository.save(transferencia);
        servicoRepository.save(cartao);

        System.out.println("Serviços carregados com sucesso!");
    }

    private void carregarClientesExemplo() {
        Categoria pessoaFisica = categoriaRepository.findByNome("PESSOA_FISICA").orElse(null);
        Categoria pessoaJuridica = categoriaRepository.findByNome("PESSOA_JURIDICA").orElse(null);

        if (pessoaFisica != null) {
            Cliente cliente1 = new Cliente("João Silva", "12345678901", LocalDate.of(1985, 3, 15));
            cliente1.setEmail("joao.silva@email.com");
            cliente1.setTelefone("11999887766");
            cliente1.setCategoria(pessoaFisica);
            cliente1.setStatusCadastro("COMPLETO");

            Cliente cliente2 = new Cliente("Maria Santos", "98765432100", LocalDate.of(1990, 7, 22));
            cliente2.setEmail("maria.santos@email.com");
            cliente2.setCategoria(pessoaFisica);
            cliente2.setStatusCadastro("INCOMPLETO"); // Sem telefone

            clienteRepository.save(cliente1);
            clienteRepository.save(cliente2);
        }

        if (pessoaJuridica != null) {
            Cliente cliente3 = new Cliente("Empresa ABC Ltda", "12345678000195", LocalDate.of(2010, 1, 1));
            cliente3.setEmail("contato@empresaabc.com");
            cliente3.setTelefone("1133334444");
            cliente3.setCategoria(pessoaJuridica);
            cliente3.setStatusCadastro("COMPLETO");

            clienteRepository.save(cliente3);
        }

        System.out.println("Clientes de exemplo carregados com sucesso!");
    }
}