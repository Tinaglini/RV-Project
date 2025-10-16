package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.ContratoRepository;
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
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ContratoService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ContratoService contratoService;

    private Contrato contratoValido;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpf("12345678901");

        contratoValido = new Contrato(LocalDate.of(2024, 1, 1), cliente);
        contratoValido.setId(1L);
        contratoValido.setStatus("ATIVO");
        contratoValido.setValorTotal(100.0);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar contrato com dados válidos")
    void salvarContratoComDadosValidos() {
        // Mock: cliente existe
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contratoValido);

        Contrato resultado = contratoService.salvar(contratoValido);

        assertNotNull(resultado);
        assertEquals("ATIVO", resultado.getStatus());
        verify(contratoRepository, times(1)).save(any(Contrato.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com cliente inexistente")
    void lancarExcecaoAoSalvarComClienteInexistente() {
        // Mock: cliente não existe
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contratoService.salvar(contratoValido);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(contratoRepository, never()).save(any(Contrato.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com data fim antes da data início")
    void lancarExcecaoAoSalvarComDataFimAntesDaDataInicio() {
        contratoValido.setDataInicio(LocalDate.of(2024, 12, 31));
        contratoValido.setDataFim(LocalDate.of(2024, 1, 1));

        // Mock: cliente existe
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contratoService.salvar(contratoValido);
        });

        assertEquals("Data fim deve ser maior que data início", exception.getMessage());
        verify(contratoRepository, never()).save(any(Contrato.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar contrato por ID existente")
    void buscarContratoPorIdExistente() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contratoValido));

        Contrato resultado = contratoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("ATIVO", resultado.getStatus());
        verify(contratoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(contratoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contratoService.buscarPorId(999L);
        });

        assertEquals("Contrato não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar contrato existente")
    void atualizarContratoExistente() {
        Contrato contratoAtualizado = new Contrato(LocalDate.of(2024, 1, 1), cliente);
        contratoAtualizado.setId(1L);
        contratoAtualizado.setStatus("CANCELADO");

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contratoValido));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contratoAtualizado);

        Contrato resultado = contratoService.atualizar(1L, contratoAtualizado);

        assertNotNull(resultado);
        assertEquals("CANCELADO", resultado.getStatus());
        verify(contratoRepository, times(1)).save(any(Contrato.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar contrato existente")
    void deletarContratoExistente() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contratoValido));
        doNothing().when(contratoRepository).delete(any(Contrato.class));

        assertDoesNotThrow(() -> contratoService.deletar(1L));

        verify(contratoRepository, times(1)).delete(contratoValido);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar contratos por cliente")
    void buscarContratosPorCliente() {
        when(contratoRepository.findByClienteId(1L))
                .thenReturn(Arrays.asList(contratoValido));

        List<Contrato> resultado = contratoService.buscarPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contratoRepository, times(1)).findByClienteId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar contratos por status")
    void buscarContratosPorStatus() {
        when(contratoRepository.findByStatus("ATIVO"))
                .thenReturn(Arrays.asList(contratoValido));

        List<Contrato> resultado = contratoService.buscarPorStatus("ATIVO");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("ATIVO", resultado.get(0).getStatus());
        verify(contratoRepository, times(1)).findByStatus("ATIVO");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os contratos")
    void listarTodosContratos() {
        Contrato contrato2 = new Contrato(LocalDate.of(2024, 2, 1), cliente);

        when(contratoRepository.findAll())
                .thenReturn(Arrays.asList(contratoValido, contrato2));

        List<Contrato> resultado = contratoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(contratoRepository, times(1)).findAll();
    }
}