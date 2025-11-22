package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.entity.PedidoItem;
import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.PedidoItemRepository;
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
 * Testes unitários do PedidoItemService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class PedidoItemServiceTest {

    @Mock
    private PedidoItemRepository pedidoItemRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private PedidoItemService pedidoItemService;

    private PedidoItem pedidoItemValido;
    private Contrato contrato;
    private Servico servico;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1L);

        servico = new Servico("Recarga", "Recarga de celular", 10.0, "RECARGA");
        servico.setId(1L);

        pedidoItemValido = new PedidoItem(2, 10.0, contrato, servico);
        pedidoItemValido.setId(1L);
        pedidoItemValido.setDesconto(0.0);
        pedidoItemValido.calcularValorFinal();
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar item de pedido com dados válidos e calcular valor final")
    void salvarPedidoItemComDadosValidosECalcularValorFinal() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(pedidoItemRepository.save(any(PedidoItem.class))).thenReturn(pedidoItemValido);

        PedidoItem resultado = pedidoItemService.salvar(pedidoItemValido);

        assertNotNull(resultado);
        assertEquals(20.0, resultado.getValorFinal());
        verify(pedidoItemRepository, times(1)).save(any(PedidoItem.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com contrato inexistente")
    void lancarExcecaoAoSalvarComContratoInexistente() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoItemService.salvar(pedidoItemValido);
        });

        assertEquals("Contrato não encontrado", exception.getMessage());
        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com serviço inexistente")
    void lancarExcecaoAoSalvarComServicoInexistente() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoItemService.salvar(pedidoItemValido);
        });

        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar item de pedido por ID existente")
    void buscarPedidoItemPorIdExistente() {
        when(pedidoItemRepository.findById(1L)).thenReturn(Optional.of(pedidoItemValido));

        PedidoItem resultado = pedidoItemService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(20.0, resultado.getValorFinal());
        verify(pedidoItemRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(pedidoItemRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoItemService.buscarPorId(999L);
        });

        assertEquals("Item de pedido não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Calcular valor final com desconto")
    void calcularValorFinalComDesconto() {
        pedidoItemValido.setDesconto(5.0);
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(pedidoItemRepository.save(any(PedidoItem.class))).thenReturn(pedidoItemValido);

        PedidoItem resultado = pedidoItemService.salvar(pedidoItemValido);

        assertNotNull(resultado);
        assertEquals(15.0, resultado.getValorFinal()); // (10.0 * 2) - 5.0 = 15.0
        verify(pedidoItemRepository, times(1)).save(any(PedidoItem.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar itens de pedido por contrato")
    void buscarPedidoItensPorContrato() {
        when(pedidoItemRepository.findByContratoId(1L))
                .thenReturn(Arrays.asList(pedidoItemValido));

        List<PedidoItem> resultado = pedidoItemService.buscarPorContrato(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoItemRepository, times(1)).findByContratoId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar itens de pedido por serviço")
    void buscarPedidoItensPorServico() {
        when(pedidoItemRepository.findByServicoId(1L))
                .thenReturn(Arrays.asList(pedidoItemValido));

        List<PedidoItem> resultado = pedidoItemService.buscarPorServico(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoItemRepository, times(1)).findByServicoId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os itens de pedido")
    void listarTodosPedidoItens() {
        PedidoItem pedidoItem2 = new PedidoItem(1, 5.0, contrato, servico);

        when(pedidoItemRepository.findAll())
                .thenReturn(Arrays.asList(pedidoItemValido, pedidoItem2));

        List<PedidoItem> resultado = pedidoItemService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoItemRepository, times(1)).findAll();
    }
}
