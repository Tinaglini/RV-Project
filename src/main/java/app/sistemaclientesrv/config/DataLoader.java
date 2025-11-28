package app.sistemaclientesrv.config;

import app.sistemaclientesrv.entity.*;
import app.sistemaclientesrv.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data loader component that initializes the database with sample data on application startup.
 * Creates default roles, test users, categories, services, clients, contracts, and payment records.
 *
 * @author Sistema Clientes RV
 * @version 1.0
 */
@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private MetodoPagamentoRepository metodoPagamentoRepository;

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

        if (contratoRepository.count() == 0) {
            carregarContratos();
        }

        if (metodoPagamentoRepository.count() == 0) {
            carregarMetodosPagamento();
        }
    }

    /**
     * Loads default system roles (ROLE_USER and ROLE_ADMIN).
     */
    private void carregarRoles() {
        Role roleUser = new Role("ROLE_USER", "Usuário comum do sistema");
        Role roleAdmin = new Role("ROLE_ADMIN", "Administrador do sistema");

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);

        log.info("Loaded default roles: ROLE_USER, ROLE_ADMIN");
    }

    /**
     * Creates test users for development and testing purposes.
     * Creates two accounts: 'user' (ROLE_USER) and 'admin' (ROLE_USER + ROLE_ADMIN).
     */
    private void carregarUsuariosTeste() {
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ROLE_ADMIN não encontrada"));

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

        log.info("Test users created successfully");
        log.info("  - Username: user | Password: user123 | Roles: ROLE_USER");
        log.info("  - Username: admin | Password: admin123 | Roles: ROLE_USER, ROLE_ADMIN");
    }

    /**
     * Loads client categories (PESSOA_FISICA and PESSOA_JURIDICA).
     */
    private void carregarCategorias() {
        Categoria pessoaFisica = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        pessoaFisica.setBeneficios("Taxas reduzidas, atendimento personalizado");

        Categoria pessoaJuridica = new Categoria("PESSOA_JURIDICA", "Clientes pessoa jurídica");
        pessoaJuridica.setBeneficios("Desconto por volume, relatórios detalhados");

        categoriaRepository.save(pessoaFisica);
        categoriaRepository.save(pessoaJuridica);

        log.info("Client categories loaded successfully");
    }

    /**
     * Loads available payment services with their respective fees and processing times.
     */
    private void carregarServicos() {
        Servico pix = new Servico("PIX", "Transferência instantânea via PIX", 0.0, "TRANSFERENCIA");
        pix.setTempoProcessamento("Instantâneo");

        Servico ted = new Servico("TED", "Transferência Eletrônica Disponível", 8.50, "TRANSFERENCIA");
        ted.setTempoProcessamento("Mesmo dia útil");

        Servico boleto = new Servico("Boleto", "Pagamento via boleto bancário", 3.90, "BOLETO");
        boleto.setTempoProcessamento("1-2 dias úteis");

        Servico cartaoCredito = new Servico("Cartão de Crédito", "Pagamento com cartão de crédito", 5.0, "CARTAO");
        cartaoCredito.setTempoProcessamento("Instantâneo");

        Servico cartaoDebito = new Servico("Cartão de Débito", "Pagamento com cartão de débito", 2.0, "DEBITO_CONTA");
        cartaoDebito.setTempoProcessamento("Instantâneo");

        servicoRepository.save(pix);
        servicoRepository.save(ted);
        servicoRepository.save(boleto);
        servicoRepository.save(cartaoCredito);
        servicoRepository.save(cartaoDebito);

        log.info("Payment services loaded: PIX, TED, Boleto, Credit Card, Debit Card");
    }

    /**
     * Loads sample clients for testing purposes.
     */
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
            cliente2.setStatusCadastro("INCOMPLETO");

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

        log.info("Sample clients loaded successfully");
    }

    /**
     * Loads sample contracts (bills to pay) for testing purposes.
     */
    private void carregarContratos() {
        Cliente joao = clienteRepository.findByCpf("12345678901").orElse(null);
        Cliente maria = clienteRepository.findByCpf("98765432100").orElse(null);
        Cliente empresaABC = clienteRepository.findByCpf("12345678000195").orElse(null);

        if (joao != null) {
            Contrato contaLuz = new Contrato(
                "Conta de Luz - CPFL",
                185.50,
                LocalDate.now().plusDays(10),
                "ENERGIA",
                joao
            );

            Contrato contaAgua = new Contrato(
                "Conta de Água - SABESP",
                95.30,
                LocalDate.now().plusDays(5),
                "AGUA",
                joao
            );
            contaAgua.setCodigoBarras("23793381286000001953309189270038698410000009530");

            Contrato contaInternet = new Contrato(
                "Internet Fibra 300MB - Vivo",
                119.90,
                LocalDate.now().plusDays(15),
                "INTERNET",
                joao
            );

            contratoRepository.save(contaLuz);
            contratoRepository.save(contaAgua);
            contratoRepository.save(contaInternet);
        }

        if (maria != null) {
            Contrato contaTelefone = new Contrato(
                "Telefone Fixo - Claro",
                65.00,
                LocalDate.now().plusDays(7),
                "TELEFONE",
                maria
            );

            Contrato contaGas = new Contrato(
                "Gás Natural - Comgás",
                78.40,
                LocalDate.now().plusDays(12),
                "GAS",
                maria
            );

            contratoRepository.save(contaTelefone);
            contratoRepository.save(contaGas);
        }

        if (empresaABC != null) {
            Contrato aluguel = new Contrato(
                "Aluguel Escritório - Centro",
                4500.00,
                LocalDate.now().plusDays(3),
                "ALUGUEL",
                empresaABC
            );

            Contrato contaLuzEmpresa = new Contrato(
                "Conta de Luz Empresa - Enel",
                850.00,
                LocalDate.now().plusDays(8),
                "ENERGIA",
                empresaABC
            );

            contratoRepository.save(aluguel);
            contratoRepository.save(contaLuzEmpresa);
        }

        log.info("Sample contracts loaded: electricity, water, internet, phone, gas, rent");
    }

    /**
     * Loads sample payment transactions for testing purposes.
     */
    private void carregarMetodosPagamento() {
        Cliente joao = clienteRepository.findByCpf("12345678901").orElse(null);
        Cliente maria = clienteRepository.findByCpf("98765432100").orElse(null);

        Servico pix = servicoRepository.findByNome("PIX").orElse(null);
        Servico boleto = servicoRepository.findByNome("Boleto").orElse(null);
        Servico cartaoCredito = servicoRepository.findByNome("Cartão de Crédito").orElse(null);

        if (joao != null && pix != null) {
            Contrato contaAgua = contratoRepository.findAll().stream()
                .filter(c -> c.getCliente().equals(joao) && c.getCategoria().equals("AGUA"))
                .findFirst()
                .orElse(null);

            if (contaAgua != null) {
                MetodoPagamento pagamentoAgua = new MetodoPagamento(95.30, joao, contaAgua, pix);
                pagamentoAgua.setChavePix("joao.silva@email.com");
                pagamentoAgua.setStatus("CONCLUIDO");
                pagamentoAgua.setDataTransacao(LocalDateTime.now().minusDays(1));
                pagamentoAgua.calcularValorTotal();
                pagamentoAgua.gerarQRCodePIX("Pagamento conta de água");
                pagamentoAgua.setComprovante("COMPROVANTE-PIX-" + System.currentTimeMillis());

                metodoPagamentoRepository.save(pagamentoAgua);

                contaAgua.setStatus("PAGO");
                contaAgua.setDataPagamento(LocalDate.now().minusDays(1));
                contratoRepository.save(contaAgua);
            }
        }

        if (maria != null && boleto != null) {
            Contrato contaTelefone = contratoRepository.findAll().stream()
                .filter(c -> c.getCliente().equals(maria) && c.getCategoria().equals("TELEFONE"))
                .findFirst()
                .orElse(null);

            if (contaTelefone != null) {
                MetodoPagamento pagamentoTelefone = new MetodoPagamento(65.00, maria, contaTelefone, boleto);
                pagamentoTelefone.setCodigoBarras("34191790010104351004791020150008884360000006500");
                pagamentoTelefone.setLinhaDigitavel("34191.79001 01043.510047 91020.150008 8 84360000006500");
                pagamentoTelefone.setStatus("PENDENTE");
                pagamentoTelefone.calcularValorTotal();

                metodoPagamentoRepository.save(pagamentoTelefone);
            }
        }

        if (joao != null && cartaoCredito != null) {
            Contrato contaInternet = contratoRepository.findAll().stream()
                .filter(c -> c.getCliente().equals(joao) && c.getCategoria().equals("INTERNET"))
                .findFirst()
                .orElse(null);

            if (contaInternet != null) {
                MetodoPagamento pagamentoInternet = new MetodoPagamento(119.90, joao, contaInternet, cartaoCredito);
                pagamentoInternet.setNumeroCartao("**** **** **** 1234");
                pagamentoInternet.setBandeira("VISA");
                pagamentoInternet.setStatus("PROCESSANDO");
                pagamentoInternet.calcularValorTotal();

                metodoPagamentoRepository.save(pagamentoInternet);
            }
        }

        log.info("Sample payment transactions loaded: PIX (completed), Boleto (pending), Credit Card (processing)");
    }
}