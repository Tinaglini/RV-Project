package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.entity.Item;
import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.ItemRepository;
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
 * Testes unitários do ItemService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ItemService itemService;

    private Item itemValido;
    private Contrato contrato;
    private Servico servico;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1L);

        servico = new Servico("Recarga", "Recarga de celular", 10.0, "RECARGA");
        servico.setId(1L);

        itemValido = new Item(2, 10.0, contrato, servico);
        itemValido.setId(1L);
        itemValido.setDesconto(0.0);
        itemValido.calcularValorFinal();
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar item com dados válidos e calcular valor final")
    void salvarItemComDadosValidosECalcularValorFinal() {
        // Mock: contrato e serviço existem
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(itemRepository.save(any(Item.class))).thenReturn(itemValido);

        Item resultado = itemService.salvar(itemValido);

        assertNotNull(resultado);
        assertEquals(20.0, resultado.getValorFinal()); // 2 * 10.0
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com contrato inexistente")
    void lancarExcecaoAoSalvarComContratoInexistente() {
        // Mock: contrato não existe
        when(contratoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.salvar(itemValido);
        });

        assertEquals("Contrato não encontrado", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com serviço inexistente")
    void lancarExcecaoAoSalvarComServicoInexistente() {
        // Mock: contrato existe mas serviço não
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.salvar(itemValido);
        });

        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Calcular valor final com desconto")
    void calcularValorFinalComDesconto() {
        itemValido.setDesconto(5.0);

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(itemRepository.save(any(Item.class))).thenReturn(itemValido);

        Item resultado = itemService.salvar(itemValido);

        // (2 * 10.0) - 5.0 = 15.0
        assertNotNull(resultado);
        assertEquals(15.0, resultado.getValorFinal());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar item por ID existente")
    void buscarItemPorIdExistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemValido));

        Item resultado = itemService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.getQuantidade());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.buscarPorId(999L);
        });

        assertEquals("Item não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar item existente")
    void atualizarItemExistente() {
        Item itemAtualizado = new Item(5, 10.0, contrato, servico);
        itemAtualizado.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemValido));
        when(itemRepository.save(any(Item.class))).thenReturn(itemAtualizado);

        Item resultado = itemService.atualizar(1L, itemAtualizado);

        assertNotNull(resultado);
        assertEquals(5, resultado.getQuantidade());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar item existente")
    void deletarItemExistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemValido));
        doNothing().when(itemRepository).delete(any(Item.class));

        assertDoesNotThrow(() -> itemService.deletar(1L));

        verify(itemRepository, times(1)).delete(itemValido);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar itens por contrato")
    void buscarItensPorContrato() {
        when(itemRepository.findByContratoId(1L))
                .thenReturn(Arrays.asList(itemValido));

        List<Item> resultado = itemService.buscarPorContrato(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(itemRepository, times(1)).findByContratoId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar itens por serviço")
    void buscarItensPorServico() {
        when(itemRepository.findByServicoId(1L))
                .thenReturn(Arrays.asList(itemValido));

        List<Item> resultado = itemService.buscarPorServico(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(itemRepository, times(1)).findByServicoId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os itens")
    void listarTodosItens() {
        Item item2 = new Item(1, 5.0, contrato, servico);

        when(itemRepository.findAll())
                .thenReturn(Arrays.asList(itemValido, item2));

        List<Item> resultado = itemService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(itemRepository, times(1)).findAll();
    }
}