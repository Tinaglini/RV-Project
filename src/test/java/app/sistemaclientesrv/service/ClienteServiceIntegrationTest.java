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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de integração do ClienteService.
 * Valida interação entre Service e Repositories mockados.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceIntegrationTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente1;
    private Cliente cliente2;
    private Categoria categoriaPF;

    @BeforeEach
    void setUp() {
        categoriaPF = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        categoriaPF.setId(1L);

        cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");
        cliente1.setCpf("12345678901");
        cliente1.setEmail("joao@email.com");
        cliente1.setTelefone("11999887766");
        cliente1.setSenha("senha123");
        cliente1.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente1.setCategoria(categoriaPF);

        cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNome("Maria Santos");
        cliente2.setCpf("98765432100");
        cliente2.setEmail("maria@email.com");
        cliente2.setSenha("senha456");
        cliente2.setDataNascimento(LocalDate.of(1995, 5, 15));
        cliente2.setCategoria(categoriaPF);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Listar todos os clientes do repositório")
    void listarTodosOsClientes() {
        // Mock do repository retornando múltiplos clientes
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente1, cliente2));

        List<Cliente> resultado = clienteService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Santos", resultado.get(1).getNome());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar clientes por nome parcial no repositório")
    void buscarClientesPorNomeParcial() {
        // Mock de busca parcial no repository
        when(clienteRepository.findByNomeContainingIgnoreCase("Silva"))
                .thenReturn(Arrays.asList(cliente1));

        List<Cliente> resultado = clienteService.buscarPorNome("Silva");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        verify(clienteRepository, times(1)).findByNomeContainingIgnoreCase("Silva");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar clientes por categoria no repositório")
    void buscarClientesPorCategoria() {
        // Mock de busca por categoria no repository
        when(clienteRepository.findByCategoriaId(1L))
                .thenReturn(Arrays.asList(cliente1, cliente2));

        List<Cliente> resultado = clienteService.buscarPorCategoria(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getCategoria().getId().equals(1L)));
        verify(clienteRepository, times(1)).findByCategoriaId(1L);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Atualizar cliente consultando repositório")
    void atualizarClienteConsultandoRepositorio() {
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("João Silva Atualizado");
        clienteAtualizado.setCpf("12345678901");
        clienteAtualizado.setEmail("joao.novo@email.com");
        clienteAtualizado.setTelefone("11988776655");
        clienteAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));
        clienteAtualizado.setCategoria(categoriaPF);

        // Mock de busca e salvamento no repository
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteAtualizado);

        Cliente resultado = clienteService.atualizar(1L, clienteAtualizado);

        assertNotNull(resultado);
        assertEquals("João Silva Atualizado", resultado.getNome());
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar por CPF ou Email no repositório")
    void buscarPorCpfOuEmailNoRepositorio() {
        // Mock de busca por CPF
        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(cliente1));

        Cliente resultadoPorCpf = clienteService.buscarPorCpfOuEmail("12345678901");

        assertNotNull(resultadoPorCpf);
        assertEquals("João Silva", resultadoPorCpf.getNome());
        verify(clienteRepository, times(1)).findByCpf("12345678901");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar por email quando CPF não existe")
    void buscarPorEmailQuandoCpfNaoExiste() {
        // Mock: CPF não encontrado, email encontrado
        when(clienteRepository.findByCpf("email@teste.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("email@teste.com")).thenReturn(Optional.of(cliente1));

        Cliente resultado = clienteService.buscarPorCpfOuEmail("email@teste.com");

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        verify(clienteRepository, times(1)).findByCpf("email@teste.com");
        verify(clienteRepository, times(1)).findByEmail("email@teste.com");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Salvar cliente com categoria padrão do repositório")
    void salvarClienteComCategoriaPadraoDoRepositorio() {
        cliente1.setCategoria(null);

        // Mock: busca categoria padrão no repository
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.findByNome("PESSOA_FISICA")).thenReturn(Optional.of(categoriaPF));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente1);

        Cliente resultado = clienteService.salvar(cliente1);

        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).findByNome("PESSOA_FISICA");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar contas bloqueadas no repositório")
    void buscarContasBloqueadasNoRepositorio() {
        cliente1.setContaBloqueada(true);
        cliente2.setContaBloqueada(true);

        // Mock de busca por contas bloqueadas
        when(clienteRepository.findByContaBloqueadaTrue())
                .thenReturn(Arrays.asList(cliente1, cliente2));

        List<Cliente> resultado = clienteService.buscarContasBloqueadas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Cliente::getContaBloqueada));
        verify(clienteRepository, times(1)).findByContaBloqueadaTrue();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar cliente por email no repositório")
    void buscarClientePorEmailNoRepositorio() {
        // Mock de busca por email
        when(clienteRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(cliente1));

        Cliente resultado = clienteService.buscarPorEmail("joao@email.com");

        assertNotNull(resultado);
        assertEquals("joao@email.com", resultado.getEmail());
        verify(clienteRepository, times(1)).findByEmail("joao@email.com");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Lançar exceção ao buscar email inexistente no repositório")
    void lancarExcecaoAoBuscarEmailInexistenteNoRepositorio() {
        // Mock: email não encontrado
        when(clienteRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorEmail("inexistente@email.com");
        });

        assertTrue(exception.getMessage().contains("Cliente não encontrado com email"));
        verify(clienteRepository, times(1)).findByEmail("inexistente@email.com");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Validar CPF e email integrados no repositório")
    void validarCpfEmailIntegradosNoRepositorio() {
        // Mock: CPF válido mas email duplicado
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(cliente2));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(cliente1);
        });

        assertEquals("Já existe um cliente cadastrado com este email", exception.getMessage());
        verify(clienteRepository, times(1)).existsByCpf("12345678901");
        verify(clienteRepository, times(1)).findByEmail("joao@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}