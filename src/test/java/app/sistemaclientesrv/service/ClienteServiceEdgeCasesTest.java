package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.repository.CategoriaRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de edge cases do ClienteService para aumentar cobertura
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceEdgeCasesTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Categoria categoria;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        categoria = new Categoria("PESSOA_FISICA", "PF");
        categoria.setId(1L);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Test");
        cliente.setCpf("12345678901");
        cliente.setEmail("test@test.com");
        cliente.setTelefone("11999999999");
        cliente.setSenha("senha123");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente.setCategoria(categoria);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar cliente com email null")
    void salvarClienteComEmailNull() {
        cliente.setEmail(null);

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.salvar(cliente);

        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar cliente com email vazio")
    void salvarClienteComEmailVazio() {
        cliente.setEmail("   ");

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.salvar(cliente);

        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar cliente sem categoria atribui PESSOA_FISICA")
    void salvarClienteSemCategoriaAtribuiPessoaFisica() {
        cliente.setCategoria(null);

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.findByNome("PESSOA_FISICA")).thenReturn(Optional.of(categoria));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.salvar(cliente);

        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).findByNome("PESSOA_FISICA");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar cliente sem categoria quando PESSOA_FISICA não existe")
    void salvarClienteSemCategoriaQuandoPessoaFisicaNaoExiste() {
        cliente.setCategoria(null);

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.findByNome("PESSOA_FISICA")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.salvar(cliente);

        assertNotNull(resultado);
        assertNull(resultado.getCategoria());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar cliente sem fornecer nova senha mantém senha existente")
    void atualizarClienteSemFornecerNovaSenhaMantemSenhaExistente() {
        String senhaHashOriginal = passwordEncoder.encode("senhaAntiga");
        cliente.setSenhaHash(senhaHashOriginal);
        cliente.setSenha(null); // Não fornece nova senha

        Cliente clienteExistente = new Cliente();
        clienteExistente.setSenhaHash(senhaHashOriginal);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizar(1L, cliente);

        assertNotNull(resultado);
        assertEquals(senhaHashOriginal, resultado.getSenhaHash());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar cliente com senha vazia mantém senha existente")
    void atualizarClienteComSenhaVaziaMantemSenhaExistente() {
        String senhaHashOriginal = passwordEncoder.encode("senhaAntiga");
        cliente.setSenhaHash(senhaHashOriginal);
        cliente.setSenha("   "); // Senha vazia/espaços

        Cliente clienteExistente = new Cliente();
        clienteExistente.setSenhaHash(senhaHashOriginal);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizar(1L, cliente);

        assertNotNull(resultado);
        assertEquals(senhaHashOriginal, resultado.getSenhaHash());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar cliente com nova senha criptografa corretamente")
    void atualizarClienteComNovaSenhaCriptografaCorretamente() {
        cliente.setSenha("novaSenha123");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setSenhaHash("hashAntigo");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizar(1L, cliente);

        assertNotNull(resultado);
        assertNotNull(resultado.getSenhaHash());
        assertNotEquals("hashAntigo", resultado.getSenhaHash());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar cliente com telefone null define status INCOMPLETO")
    void atualizarClienteComTelefoneNullDefineStatusIncompleto() {
        cliente.setTelefone(null);

        Cliente clienteExistente = new Cliente();
        clienteExistente.setSenhaHash("hashExistente");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizar(1L, cliente);

        assertNotNull(resultado);
        assertEquals("INCOMPLETO", resultado.getStatusCadastro());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar cliente com telefone vazio define status INCOMPLETO")
    void atualizarClienteComTelefoneVazioDefineStatusIncompleto() {
        cliente.setTelefone("   ");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setSenhaHash("hashExistente");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizar(1L, cliente);

        assertEquals("INCOMPLETO", resultado.getStatusCadastro());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Autenticar com cliente inativo lança exceção")
    void autenticarComClienteInativoLancaExcecao() {
        String senhaHash = passwordEncoder.encode("senha123");
        cliente.setSenhaHash(senhaHash);
        cliente.setContaBloqueada(false);
        cliente.setAtivo(false); // Cliente inativo

        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(cliente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.autenticar("12345678901", "senha123");
        });

        assertTrue(exception.getMessage().contains("Cliente inativo"));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Registrar tentativa de login incrementa contador")
    void registrarTentativaLoginIncrementaContador() {
        cliente.setTentativasLogin(2);
        cliente.setContaBloqueada(false);

        cliente.registrarTentativaLogin();

        assertEquals(3, cliente.getTentativasLogin());
        assertFalse(cliente.getContaBloqueada());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Registrar tentativa de login bloqueia após 5 tentativas")
    void registrarTentativaLoginBloqueiaApos5Tentativas() {
        cliente.setTentativasLogin(4);
        cliente.setContaBloqueada(false);

        cliente.registrarTentativaLogin();

        assertEquals(5, cliente.getTentativasLogin());
        assertTrue(cliente.getContaBloqueada());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Alterar senha com nova senha null lança exceção")
    void alterarSenhaComNovaSenhaNullLancaExcecao() {
        String senhaHash = passwordEncoder.encode("senhaAntiga");
        cliente.setSenhaHash(senhaHash);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.alterarSenha(1L, "senhaAntiga", null);
        });

        assertEquals("A nova senha é obrigatória", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Alterar senha com nova senha vazia lança exceção")
    void alterarSenhaComNovaSenhaVaziaLancaExcecao() {
        String senhaHash = passwordEncoder.encode("senhaAntiga");
        cliente.setSenhaHash(senhaHash);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.alterarSenha(1L, "senhaAntiga", "   ");
        });

        assertEquals("A nova senha é obrigatória", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar por CPF ou Email quando ambos não existem")
    void buscarPorCpfOuEmailQuandoAmbosNaoExistem() {
        when(clienteRepository.findByCpf("inexistente")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("inexistente")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorCpfOuEmail("inexistente");
        });

        assertTrue(exception.getMessage().contains("Cliente não encontrado"));
        verify(clienteRepository, times(1)).findByCpf("inexistente");
        verify(clienteRepository, times(1)).findByEmail("inexistente");
    }
}