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
 * Testes unitários do ClienteService.
 * Todos os métodos testados isoladamente com dependências mockadas.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteValido;
    private Categoria categoriaPF;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        categoriaPF = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        categoriaPF.setId(1L);

        clienteValido = new Cliente();
        clienteValido.setId(1L);
        clienteValido.setNome("João Silva");
        clienteValido.setCpf("12345678901");
        clienteValido.setEmail("joao@email.com");
        clienteValido.setTelefone("11999887766");
        clienteValido.setSenha("senha123");
        clienteValido.setDataNascimento(LocalDate.of(1990, 1, 1));
        clienteValido.setCategoria(categoriaPF);
        clienteValido.setAtivo(true);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar cliente com dados válidos")
    void salvarClienteComDadosValidos() {
        // Mock: CPF e email não existem no banco
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        Cliente resultado = clienteService.salvar(clienteValido);

        // Validações
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("COMPLETO", resultado.getStatusCadastro());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com CPF duplicado")
    void lancarExcecaoAoSalvarComCpfDuplicado() {
        // Mock: CPF já existe
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteValido);
        });

        assertEquals("Não é possível cadastrar cliente com CPF já existente", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com email duplicado")
    void lancarExcecaoAoSalvarComEmailDuplicado() {
        Cliente clienteExistente = new Cliente();
        clienteExistente.setEmail("joao@email.com");

        // Mock: Email já existe
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(clienteExistente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteValido);
        });

        assertEquals("Já existe um cliente cadastrado com este email", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Definir status INCOMPLETO quando telefone ausente")
    void definirStatusIncompletoQuandoTelefoneAusente() {
        // Remove telefone para testar regra de negócio
        clienteValido.setTelefone(null);

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        Cliente resultado = clienteService.salvar(clienteValido);

        assertEquals("INCOMPLETO", resultado.getStatusCadastro());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar sem senha")
    void lancarExcecaoAoSalvarSemSenha() {
        clienteValido.setSenha(null);

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteValido);
        });

        assertEquals("A senha é obrigatória para cadastro do cliente", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com senha curta")
    void lancarExcecaoAoSalvarComSenhaCurta() {
        clienteValido.setSenha("123");

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(clienteValido);
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar cliente por ID existente")
    void buscarClientePorIdExistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));

        Cliente resultado = clienteService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId(999L);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar cliente existente")
    void deletarClienteExistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        doNothing().when(clienteRepository).delete(any(Cliente.class));

        assertDoesNotThrow(() -> clienteService.deletar(1L));

        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).delete(clienteValido);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar cliente por CPF válido")
    void buscarClientePorCpfValido() {
        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(clienteValido));

        Cliente resultado = clienteService.buscarPorCpf("12345678901");

        assertNotNull(resultado);
        assertEquals("12345678901", resultado.getCpf());
        verify(clienteRepository, times(1)).findByCpf("12345678901");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar CPF inexistente")
    void lancarExcecaoAoBuscarPorCpfInexistente() {
        when(clienteRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorCpf("99999999999");
        });

        assertTrue(exception.getMessage().contains("Cliente não encontrado com CPF"));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Autenticar cliente com credenciais válidas")
    void autenticarClienteComCredenciaisValidas() {
        // Simula senha criptografada no banco
        String senhaHash = passwordEncoder.encode("senha123");
        clienteValido.setSenhaHash(senhaHash);
        clienteValido.setContaBloqueada(false);

        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(clienteValido));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        Cliente resultado = clienteService.autenticar("12345678901", "senha123");

        // Valida login bem-sucedido
        assertNotNull(resultado);
        assertEquals(0, resultado.getTentativasLogin());
        assertNotNull(resultado.getUltimoLogin());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao autenticar com senha incorreta")
    void lancarExcecaoAoAutenticarComSenhaIncorreta() {
        String senhaHash = passwordEncoder.encode("senha123");
        clienteValido.setSenhaHash(senhaHash);
        clienteValido.setContaBloqueada(false);
        clienteValido.setTentativasLogin(0);

        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(clienteValido));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.autenticar("12345678901", "senhaErrada");
        });

        assertEquals("Senha incorreta", exception.getMessage());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao autenticar conta bloqueada")
    void lancarExcecaoAoAutenticarComContaBloqueada() {
        clienteValido.setContaBloqueada(true);

        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(clienteValido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.autenticar("12345678901", "senha123");
        });

        assertTrue(exception.getMessage().contains("Conta bloqueada"));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Alterar senha com senha atual correta")
    void alterarSenhaComSenhaAtualCorreta() {
        String senhaAtualHash = passwordEncoder.encode("senhaAntiga");
        clienteValido.setSenhaHash(senhaAtualHash);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        assertDoesNotThrow(() -> {
            clienteService.alterarSenha(1L, "senhaAntiga", "novaSenha123");
        });

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao alterar senha com senha atual incorreta")
    void lancarExcecaoAoAlterarSenhaComSenhaAtualIncorreta() {
        String senhaAtualHash = passwordEncoder.encode("senhaAntiga");
        clienteValido.setSenhaHash(senhaAtualHash);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.alterarSenha(1L, "senhaErrada", "novaSenha123");
        });

        assertEquals("Senha atual incorreta", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Desbloquear conta de cliente")
    void desbloquearContaDeCliente() {
        clienteValido.setContaBloqueada(true);
        clienteValido.setTentativasLogin(5);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

        clienteService.desbloquearConta(1L);

        // Valida desbloqueio
        assertFalse(clienteValido.getContaBloqueada());
        assertEquals(0, clienteValido.getTentativasLogin());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}