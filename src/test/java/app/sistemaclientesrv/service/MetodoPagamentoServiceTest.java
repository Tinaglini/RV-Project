package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.MetodoPagamento;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.MetodoPagamentoRepository;
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
 * Testes unitários do MetodoPagamentoService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class MetodoPagamentoServiceTest {

    @Mock
    private MetodoPagamentoRepository metodoPagamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private MetodoPagamentoService metodoPagamentoService;

    private MetodoPagamento metodoPagamentoValido;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        metodoPagamentoValido = new MetodoPagamento("PIX", "Meu PIX Principal", "joao@email.com", cliente);
        metodoPagamentoValido.setId(1L);
        metodoPagamentoValido.setPrincipal(true);
        metodoPagamentoValido.setStatus("ATIVO");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar método de pagamento com dados válidos")
    void salvarMetodoPagamentoComDadosValidos() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(metodoPagamentoRepository.save(any(MetodoPagamento.class))).thenReturn(metodoPagamentoValido);

        MetodoPagamento resultado = metodoPagamentoService.salvar(metodoPagamentoValido);

        assertNotNull(resultado);
        assertEquals("PIX", resultado.getTipo());
        verify(metodoPagamentoRepository, times(1)).save(any(MetodoPagamento.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar com cliente inexistente")
    void lancarExcecaoAoSalvarComClienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            metodoPagamentoService.salvar(metodoPagamentoValido);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(metodoPagamentoRepository, never()).save(any(MetodoPagamento.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar método de pagamento por ID existente")
    void buscarMetodoPagamentoPorIdExistente() {
        when(metodoPagamentoRepository.findById(1L)).thenReturn(Optional.of(metodoPagamentoValido));

        MetodoPagamento resultado = metodoPagamentoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("PIX", resultado.getTipo());
        verify(metodoPagamentoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(metodoPagamentoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            metodoPagamentoService.buscarPorId(999L);
        });

        assertEquals("Método de pagamento não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Gerar QR Code PIX com sucesso")
    void gerarQRCodePIXComSucesso() {
        when(metodoPagamentoRepository.findById(1L)).thenReturn(Optional.of(metodoPagamentoValido));
        when(metodoPagamentoRepository.save(any(MetodoPagamento.class))).thenReturn(metodoPagamentoValido);

        MetodoPagamento resultado = metodoPagamentoService.gerarQRCodePIX(1L, 100.0, "Pagamento de serviço");

        assertNotNull(resultado);
        assertNotNull(resultado.getQrCode());
        assertTrue(resultado.getQrCode().contains("br.gov.bcb.pix"));
        verify(metodoPagamentoRepository, times(1)).save(any(MetodoPagamento.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao gerar QR Code para não-PIX")
    void lancarExcecaoAoGerarQRCodeParaNaoPIX() {
        MetodoPagamento metodoBoleto = new MetodoPagamento("BOLETO", "Boleto", "123456", cliente);
        metodoBoleto.setId(2L);

        when(metodoPagamentoRepository.findById(2L)).thenReturn(Optional.of(metodoBoleto));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            metodoPagamentoService.gerarQRCodePIX(2L, 100.0, "Pagamento");
        });

        assertEquals("Geração de QR Code disponível apenas para métodos PIX", exception.getMessage());
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar métodos por cliente")
    void buscarMetodosPorCliente() {
        when(metodoPagamentoRepository.findByClienteId(1L))
                .thenReturn(Arrays.asList(metodoPagamentoValido));

        List<MetodoPagamento> resultado = metodoPagamentoService.buscarPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(metodoPagamentoRepository, times(1)).findByClienteId(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar métodos por tipo")
    void buscarMetodosPorTipo() {
        when(metodoPagamentoRepository.findByTipoIgnoreCase("PIX"))
                .thenReturn(Arrays.asList(metodoPagamentoValido));

        List<MetodoPagamento> resultado = metodoPagamentoService.buscarPorTipo("PIX");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PIX", resultado.get(0).getTipo());
        verify(metodoPagamentoRepository, times(1)).findByTipoIgnoreCase("PIX");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todos os métodos de pagamento")
    void listarTodosMetodosPagamento() {
        MetodoPagamento metodoBoleto = new MetodoPagamento("BOLETO", "Boleto", "123456", cliente);

        when(metodoPagamentoRepository.findAll())
                .thenReturn(Arrays.asList(metodoPagamentoValido, metodoBoleto));

        List<MetodoPagamento> resultado = metodoPagamentoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(metodoPagamentoRepository, times(1)).findAll();
    }
}
