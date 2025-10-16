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
 * Testes de integração do ServicoService.
 * Valida interação com repository mockado.
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceIntegrationTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servicoRecarga;
    private Servico servicoPix;

    @BeforeEach
    void setUp() {
        servicoRecarga = new Servico("Recarga de Celular", "Recarga operadoras", 10.0, "RECARGA");
        servicoRecarga.setId(1L);
        servicoRecarga.setAtivo(true);

        servicoPix = new Servico("Pagamento PIX", "Transferências PIX", 1.0, "FINANCEIRO");
        servicoPix.setId(2L);
        servicoPix.setAtivo(true);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Listar todos os serviços do repositório")
    void listarTodosServicosDoRepositorio() {
        // Mock do repository retornando múltiplos serviços
        when(servicoRepository.findAll()).thenReturn(Arrays.asList(servicoRecarga, servicoPix));

        List<Servico> resultado = servicoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Recarga de Celular", resultado.get(0).getNome());
        assertEquals("Pagamento PIX", resultado.get(1).getNome());
        verify(servicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar serviços por nome no repositório")
    void buscarServicosPorNomeNoRepositorio() {
        // Mock de busca parcial no repository
        when(servicoRepository.findByNomeContainingIgnoreCase("Recarga"))
                .thenReturn(Arrays.asList(servicoRecarga));

        List<Servico> resultado = servicoService.buscarPorNome("Recarga");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNome().contains("Recarga"));
        verify(servicoRepository, times(1)).findByNomeContainingIgnoreCase("Recarga");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar serviços por categoria no repositório")
    void buscarServicosPorCategoriaNoRepositorio() {
        // Mock de busca por categoria
        when(servicoRepository.findByCategoria("FINANCEIRO"))
                .thenReturn(Arrays.asList(servicoPix));

        List<Servico> resultado = servicoService.buscarPorCategoria("FINANCEIRO");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("FINANCEIRO", resultado.get(0).getCategoria());
        verify(servicoRepository, times(1)).findByCategoria("FINANCEIRO");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar apenas ativos no repositório")
    void buscarApenasAtivosNoRepositorio() {
        // Mock de busca por ativos
        when(servicoRepository.findByAtivoTrue())
                .thenReturn(Arrays.asList(servicoRecarga, servicoPix));

        List<Servico> resultado = servicoService.buscarAtivos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Servico::getAtivo));
        verify(servicoRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Atualizar serviço consultando repositório")
    void atualizarServicoConsultandoRepositorio() {
        Servico servicoAtualizado = new Servico("Recarga Atualizada", "Nova descrição", 12.0, "RECARGA");
        servicoAtualizado.setId(1L);

        // Mock de busca e salvamento
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoRecarga));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoAtualizado);

        Servico resultado = servicoService.atualizar(1L, servicoAtualizado);

        assertNotNull(resultado);
        assertEquals("Recarga Atualizada", resultado.getNome());
        assertEquals(12.0, resultado.getValor());
        verify(servicoRepository, times(1)).findById(1L);
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }
}