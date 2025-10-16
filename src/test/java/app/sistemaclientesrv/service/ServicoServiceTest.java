package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.ServicoRepository;
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
 * Testes unitários do ServicoService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servicoValido;

    @BeforeEach
    void setUp() {
        servicoValido = new Servico("Recarga de Celular", "Recarga para todas as operadoras", 10.0, "RECARGA");
        servicoValido.setId(1L);
        servicoValido.setAtivo(true);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar serviço com dados válidos")
    void salvarServicoComDadosValidos() {
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoValido);

        Servico resultado = servicoService.salvar(servicoValido);

        assertNotNull(resultado);
        assertEquals("Recarga de Celular", resultado.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com valor negativo")
    void lancarExcecaoAoSalvarComValorNegativo() {
        servicoValido.setValor(-10.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.salvar(servicoValido);
        });

        assertEquals("Valor do serviço deve ser positivo", exception.getMessage());
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com valor zero")
    void lancarExcecaoAoSalvarComValorZero() {
        servicoValido.setValor(0.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.salvar(servicoValido);
        });

        assertEquals("Valor do serviço deve ser positivo", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar serviço por ID existente")
    void buscarServicoPorIdExistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoValido));

        Servico resultado = servicoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Recarga de Celular", resultado.getNome());
        verify(servicoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.buscarPorId(999L);
        });

        assertEquals("Serviço não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar serviço existente")
    void atualizarServicoExistente() {
        Servico servicoAtualizado = new Servico("Recarga Atualizada", "Nova descrição", 15.0, "RECARGA");
        servicoAtualizado.setId(1L);

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoValido));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoAtualizado);

        Servico resultado = servicoService.atualizar(1L, servicoAtualizado);

        assertNotNull(resultado);
        assertEquals("Recarga Atualizada", resultado.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar serviço existente")
    void deletarServicoExistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoValido));
        doNothing().when(servicoRepository).delete(any(Servico.class));

        assertDoesNotThrow(() -> servicoService.deletar(1L));

        verify(servicoRepository, times(1)).delete(servicoValido);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar apenas serviços ativos")
    void buscarApenasServicosAtivos() {
        Servico servico2 = new Servico("Pagamento PIX", "Pagamentos via PIX", 1.0, "FINANCEIRO");
        servico2.setAtivo(true);

        when(servicoRepository.findByAtivoTrue())
                .thenReturn(Arrays.asList(servicoValido, servico2));

        List<Servico> resultado = servicoService.buscarAtivos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Servico::getAtivo));
        verify(servicoRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar serviços por categoria")
    void buscarServicosPorCategoria() {
        when(servicoRepository.findByCategoria("RECARGA"))
                .thenReturn(Arrays.asList(servicoValido));

        List<Servico> resultado = servicoService.buscarPorCategoria("RECARGA");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("RECARGA", resultado.get(0).getCategoria());
        verify(servicoRepository, times(1)).findByCategoria("RECARGA");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar serviços por nome parcial")
    void buscarServicosPorNomeParcial() {
        when(servicoRepository.findByNomeContainingIgnoreCase("Recarga"))
                .thenReturn(Arrays.asList(servicoValido));

        List<Servico> resultado = servicoService.buscarPorNome("Recarga");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNome().contains("Recarga"));
        verify(servicoRepository, times(1)).findByNomeContainingIgnoreCase("Recarga");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os serviços")
    void listarTodosServicos() {
        Servico servico2 = new Servico("Pagamento PIX", "Pagamentos", 1.0, "FINANCEIRO");

        when(servicoRepository.findAll())
                .thenReturn(Arrays.asList(servicoValido, servico2));

        List<Servico> resultado = servicoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(servicoRepository, times(1)).findAll();
    }
}