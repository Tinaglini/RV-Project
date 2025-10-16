package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Endereco;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do EnderecoService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private EnderecoService enderecoService;

    private Endereco enderecoValido;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        enderecoValido = new Endereco("Rua Principal", "100", "São Paulo", "SP", "01234567");
        enderecoValido.setId(1L);
        enderecoValido.setCliente(cliente);
        enderecoValido.setPrincipal(true);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar endereço com dados válidos")
    void salvarEnderecoComDadosValidos() {
        // Mock: cliente existe
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoValido);

        Endereco resultado = enderecoService.salvar(enderecoValido);

        assertNotNull(resultado);
        assertEquals("Rua Principal", resultado.getRua());
        verify(enderecoRepository, times(1)).save(any(Endereco.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com cliente inexistente")
    void lancarExcecaoAoSalvarComClienteInexistente() {
        // Mock: cliente não existe
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enderecoService.salvar(enderecoValido);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(enderecoRepository, never()).save(any(Endereco.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar endereço por ID existente")
    void buscarEnderecoPorIdExistente() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoValido));

        Endereco resultado = enderecoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Rua Principal", resultado.getRua());
        verify(enderecoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(enderecoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enderecoService.buscarPorId(999L);
        });

        assertEquals("Endereço não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar endereço existente")
    void atualizarEnderecoExistente() {
        Endereco enderecoAtualizado = new Endereco("Rua Atualizada", "200", "São Paulo", "SP", "01234567");
        enderecoAtualizado.setId(1L);

        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoValido));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoAtualizado);

        Endereco resultado = enderecoService.atualizar(1L, enderecoAtualizado);

        assertNotNull(resultado);
        assertEquals("Rua Atualizada", resultado.getRua());
        verify(enderecoRepository, times(1)).save(any(Endereco.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar endereço existente")
    void deletarEnderecoExistente() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoValido));
        doNothing().when(enderecoRepository).delete(any(Endereco.class));

        assertDoesNotThrow(() -> enderecoService.deletar(1L));

        verify(enderecoRepository, times(1)).delete(enderecoValido);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar endereços por cliente")
    void buscarEnderecosPorCliente() {
        when(enderecoRepository.findByClienteId(1L))
                .thenReturn(Arrays.asList(enderecoValido));

        List<Endereco> resultado = enderecoService.buscarPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(enderecoRepository, times(1)).findByClienteId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar endereços por cidade")
    void buscarEnderecosPorCidade() {
        when(enderecoRepository.findByCidadeContainingIgnoreCase("São Paulo"))
                .thenReturn(Arrays.asList(enderecoValido));

        List<Endereco> resultado = enderecoService.buscarPorCidade("São Paulo");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("São Paulo", resultado.get(0).getCidade());
        verify(enderecoRepository, times(1)).findByCidadeContainingIgnoreCase("São Paulo");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os endereços")
    void listarTodosEnderecos() {
        Endereco endereco2 = new Endereco("Rua Secundária", "50", "Rio de Janeiro", "RJ", "20000000");

        when(enderecoRepository.findAll())
                .thenReturn(Arrays.asList(enderecoValido, endereco2));

        List<Endereco> resultado = enderecoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(enderecoRepository, times(1)).findAll();
    }
}