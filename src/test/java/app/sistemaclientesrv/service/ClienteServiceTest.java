package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ClienteService
 * Cobre cenários de sucesso e erro para os métodos principais
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteTeste;
    private Categoria categoriaTeste;

    @BeforeEach
    void setUp() {
        // Setup da categoria
        categoriaTeste = new Categoria();
        categoriaTeste.setId(1L);
        categoriaTeste.setNome("PESSOA_FISICA");

        // Setup do cliente
        clienteTeste = new Cliente();
        clienteTeste.setId(1L);
        clienteTeste.setNome("João Silva");
        clienteTeste.setCpf("123.456.789-00");
        clienteTeste.setEmail("joao@email.com");
        clienteTeste.setTelefone("11999999999");
        clienteTeste.setDataNascimento(LocalDate.of(1990, 1, 1));
        clienteTeste.setSenha("senha123");
        clienteTeste.setSenhaHash("hash_da_senha");
        clienteTeste.setAtivo(true);
        clienteTeste.setContaBloqueada(false);
        clienteTeste.setTentativasLogin(0);
        clienteTeste.setCategoria(categoriaTeste);
        clienteTeste.setCreatedAt(LocalDateTime.now());
        clienteTeste.setUpdatedAt(LocalDateTime.now());
    }

    // ========== TESTES PARA findById ==========

    @Test
    void findById_DeveRetornarCliente_QuandoIdExiste() {
        // Arrange
        Long id = 1L;
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));

        // Act
        Cliente resultado = clienteService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteTeste.getId(), resultado.getId());
        assertEquals(clienteTeste.getNome(), resultado.getNome());
        verify(clienteRepository).findById(id);
    }

    @Test
    void findById_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Arrange
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId(id);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteRepository).findById(id);
    }

    // ========== TESTES PARA findAll ==========

    @Test
    void findAll_DeveRetornarListaDeClientes_QuandoExistemClientes() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(clienteTeste);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(clienteTeste.getId(), resultado.get(0).getId());
        verify(clienteRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarListaVazia_QuandoNaoExistemClientes() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findAll();
    }

    // ========== TESTES PARA save ==========

    @Test
    void save_DeveSalvarCliente_QuandoDadosValidos() {
        // Arrange
        Cliente novoCliente = new Cliente();
        novoCliente.setNome("Maria Santos");
        novoCliente.setCpf("987.654.321-00");
        novoCliente.setEmail("maria@email.com");
        novoCliente.setTelefone("11888888888");
        novoCliente.setDataNascimento(LocalDate.of(1985, 5, 15));
        novoCliente.setSenha("senha456");

        when(clienteRepository.existsByCpf(novoCliente.getCpf())).thenReturn(false);
        when(categoriaRepository.findByNome("PESSOA_FISICA")).thenReturn(Optional.of(categoriaTeste));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(novoCliente);

        // Act
        Cliente resultado = clienteService.salvar(novoCliente);

        // Assert
        assertNotNull(resultado);
        assertEquals("COMPLETO", novoCliente.getStatusCadastro());
        assertNotNull(novoCliente.getSenhaHash());
        verify(clienteRepository).existsByCpf(novoCliente.getCpf());
        verify(clienteRepository).save(novoCliente);
    }

    @Test
    void save_DeveLancarExcecao_QuandoCpfJaExiste() {
        // Arrange
        when(clienteRepository.existsByCpf(clienteTeste.getCpf())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteTeste);
        });

        assertEquals("Não é possível cadastrar cliente com CPF já existente", exception.getMessage());
        verify(clienteRepository).existsByCpf(clienteTeste.getCpf());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void save_DeveLancarExcecao_QuandoEmailJaExiste() {
        // Arrange
        when(clienteRepository.existsByCpf(clienteTeste.getCpf())).thenReturn(false);
        when(clienteRepository.findByEmail(clienteTeste.getEmail())).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteTeste);
        });

        assertEquals("Já existe um cliente cadastrado com este email", exception.getMessage());
        verify(clienteRepository).existsByCpf(clienteTeste.getCpf());
        verify(clienteRepository).findByEmail(clienteTeste.getEmail());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void save_DeveLancarExcecao_QuandoSenhaNula() {
        // Arrange
        clienteTeste.setSenha(null);
        when(clienteRepository.existsByCpf(clienteTeste.getCpf())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteTeste);
        });

        assertEquals("A senha é obrigatória para cadastro do cliente", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void save_DeveLancarExcecao_QuandoSenhaMuitoCurta() {
        // Arrange
        clienteTeste.setSenha("123");
        when(clienteRepository.existsByCpf(clienteTeste.getCpf())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteTeste);
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void save_DeveDefinirStatusIncompleto_QuandoTelefoneNulo() {
        // Arrange
        clienteTeste.setTelefone(null);
        when(clienteRepository.existsByCpf(clienteTeste.getCpf())).thenReturn(false);
        when(categoriaRepository.findByNome("PESSOA_FISICA")).thenReturn(Optional.of(categoriaTeste));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteTeste);

        // Act
        Cliente resultado = clienteService.salvar(clienteTeste);

        // Assert
        assertEquals("INCOMPLETO", clienteTeste.getStatusCadastro());
        verify(clienteRepository).save(clienteTeste);
    }

    // ========== TESTES PARA update ==========

    @Test
    void update_DeveAtualizarCliente_QuandoDadosValidos() {
        // Arrange
        Long id = 1L;
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("João Silva Atualizado");
        clienteAtualizado.setCpf("123.456.789-00");
        clienteAtualizado.setEmail("joao.novo@email.com");
        clienteAtualizado.setTelefone("11777777777");
        clienteAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteAtualizado);

        // Act
        Cliente resultado = clienteService.atualizar(id, clienteAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, clienteAtualizado.getId());
        assertNotNull(clienteAtualizado.getUpdatedAt());
        verify(clienteRepository).findById(id);
        verify(clienteRepository).save(clienteAtualizado);
    }

    @Test
    void update_DeveLancarExcecao_QuandoClienteNaoExiste() {
        // Arrange
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.atualizar(id, clienteTeste);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void update_DeveCriptografarNovaSenha_QuandoSenhaFornecida() {
        // Arrange
        Long id = 1L;
        String novaSenha = "novaSenha123";
        clienteTeste.setSenha(novaSenha);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteTeste);

        // Act
        Cliente resultado = clienteService.atualizar(id, clienteTeste);

        // Assert
        assertNotNull(resultado);
        assertNotNull(clienteTeste.getSenhaHash());
        assertNotEquals(novaSenha, clienteTeste.getSenhaHash());
        verify(clienteRepository).save(clienteTeste);
    }

    @Test
    void update_DeveLancarExcecao_QuandoNovaSenhaMuitoCurta() {
        // Arrange
        Long id = 1L;
        clienteTeste.setSenha("123");
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.atualizar(id, clienteTeste);
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    // ========== TESTES PARA delete ==========

    @Test
    void delete_DeveDeletarCliente_QuandoIdExiste() {
        // Arrange
        Long id = 1L;
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));
        doNothing().when(clienteRepository).delete(clienteTeste);

        // Act
        assertDoesNotThrow(() -> {
            clienteService.deletar(id);
        });

        // Assert
        verify(clienteRepository).findById(id);
        verify(clienteRepository).delete(clienteTeste);
    }

    @Test
    void delete_DeveLancarExcecao_QuandoClienteNaoExiste() {
        // Arrange
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.deletar(id);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).delete(any());
    }

    // ========== TESTES PARA buscarPorCpf ==========

    @Test
    void buscarPorCpf_DeveRetornarCliente_QuandoCpfExiste() {
        // Arrange
        String cpf = "123.456.789-00";
        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.of(clienteTeste));

        // Act
        Cliente resultado = clienteService.buscarPorCpf(cpf);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteTeste.getCpf(), resultado.getCpf());
        verify(clienteRepository).findByCpf(cpf);
    }

    @Test
    void buscarPorCpf_DeveLancarExcecao_QuandoCpfNaoExiste() {
        // Arrange
        String cpf = "999.999.999-99";
        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorCpf(cpf);
        });

        assertEquals("Cliente não encontrado com CPF: " + cpf, exception.getMessage());
        verify(clienteRepository).findByCpf(cpf);
    }

    // ========== TESTES PARA autenticar ==========

    @Test
    void autenticar_DeveRetornarCliente_QuandoCredenciaisValidas() {
        // Arrange
        String cpf = "123.456.789-00";
        String senha = "senha123";
        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.of(clienteTeste));

        // Act
        Cliente resultado = clienteService.autenticar(cpf, senha);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteTeste.getId(), resultado.getId());
        verify(clienteRepository).findByCpf(cpf);
        verify(clienteRepository).save(clienteTeste);
    }

    @Test
    void autenticar_DeveLancarExcecao_QuandoContaBloqueada() {
        // Arrange
        String cpf = "123.456.789-00";
        String senha = "senha123";
        clienteTeste.setContaBloqueada(true);
        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.autenticar(cpf, senha);
        });

        assertEquals("Conta bloqueada devido a muitas tentativas de login incorretas. Entre em contato com o suporte.", exception.getMessage());
        verify(clienteRepository).findByCpf(cpf);
    }

    @Test
    void autenticar_DeveLancarExcecao_QuandoClienteInativo() {
        // Arrange
        String cpf = "123.456.789-00";
        String senha = "senha123";
        clienteTeste.setAtivo(false);
        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.autenticar(cpf, senha);
        });

        assertEquals("Cliente inativo. Entre em contato com o suporte.", exception.getMessage());
        verify(clienteRepository).findByCpf(cpf);
    }

    // ========== TESTES PARA alterarSenha ==========

    @Test
    void alterarSenha_DeveAlterarSenha_QuandoSenhaAtualCorreta() {
        // Arrange
        Long id = 1L;
        String senhaAtual = "senha123";
        String novaSenha = "novaSenha456";
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteTeste);

        // Act
        assertDoesNotThrow(() -> {
            clienteService.alterarSenha(id, senhaAtual, novaSenha);
        });

        // Assert
        verify(clienteRepository).findById(id);
        verify(clienteRepository).save(clienteTeste);
    }

    @Test
    void alterarSenha_DeveLancarExcecao_QuandoSenhaAtualIncorreta() {
        // Arrange
        Long id = 1L;
        String senhaAtual = "senhaIncorreta";
        String novaSenha = "novaSenha456";
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.alterarSenha(id, senhaAtual, novaSenha);
        });

        assertEquals("Senha atual incorreta", exception.getMessage());
        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void alterarSenha_DeveLancarExcecao_QuandoNovaSenhaMuitoCurta() {
        // Arrange
        Long id = 1L;
        String senhaAtual = "senha123";
        String novaSenha = "123";
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteTeste));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.alterarSenha(id, senhaAtual, novaSenha);
        });

        assertEquals("A nova senha deve ter no mínimo 6 caracteres", exception.getMessage());
        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).save(any());
    }
}
