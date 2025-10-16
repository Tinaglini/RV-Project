package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.repository.CategoriaRepository;
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
 * Testes unitários do CategoriaService.
 * Valida regras de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaValida;

    @BeforeEach
    void setUp() {
        categoriaValida = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        categoriaValida.setId(1L);
        categoriaValida.setBeneficios("Taxas reduzidas");
        categoriaValida.setAtivo(true);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Salvar categoria com dados válidos")
    void salvarCategoriaComDadosValidos() {
        // Mock: nome único
        when(categoriaRepository.existsByNome("PESSOA_FISICA")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaValida);

        Categoria resultado = categoriaService.salvar(categoriaValida);

        assertNotNull(resultado);
        assertEquals("PESSOA_FISICA", resultado.getNome());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao salvar categoria com nome duplicado")
    void lancarExcecaoAoSalvarComNomeDuplicado() {
        // Mock: nome já existe
        when(categoriaRepository.existsByNome("PESSOA_FISICA")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.salvar(categoriaValida);
        });

        assertEquals("Já existe uma categoria com o nome: PESSOA_FISICA", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar categoria por ID existente")
    void buscarCategoriaPorIdExistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));

        Categoria resultado = categoriaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("PESSOA_FISICA", resultado.getNome());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Lançar exceção ao buscar ID inexistente")
    void lancarExcecaoAoBuscarPorIdInexistente() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.buscarPorId(999L);
        });

        assertTrue(exception.getMessage().contains("Categoria não encontrada com ID"));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Atualizar categoria existente")
    void atualizarCategoriaExistente() {
        Categoria categoriaAtualizada = new Categoria("PESSOA_FISICA", "Descrição atualizada");
        categoriaAtualizada.setId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);

        Categoria resultado = categoriaService.atualizar(1L, categoriaAtualizada);

        assertNotNull(resultado);
        assertEquals("Descrição atualizada", resultado.getDescricao());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Deletar categoria existente")
    void deletarCategoriaExistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
        doNothing().when(categoriaRepository).delete(any(Categoria.class));

        assertDoesNotThrow(() -> categoriaService.deletar(1L));

        verify(categoriaRepository, times(1)).delete(categoriaValida);
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar apenas categorias ativas")
    void buscarApenasCategoriasAtivas() {
        Categoria categoria2 = new Categoria("PESSOA_JURIDICA", "Empresas");
        categoria2.setAtivo(true);

        when(categoriaRepository.findByAtivoTrue())
                .thenReturn(Arrays.asList(categoriaValida, categoria2));

        List<Categoria> resultado = categoriaService.buscarAtivas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Categoria::getAtivo));
        verify(categoriaRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Buscar categorias por nome parcial")
    void buscarCategoriasPorNomeParcial() {
        when(categoriaRepository.findByNomeContainingIgnoreCase("FISICA"))
                .thenReturn(Arrays.asList(categoriaValida));

        List<Categoria> resultado = categoriaService.buscarPorNome("FISICA");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PESSOA_FISICA", resultado.get(0).getNome());
        verify(categoriaRepository, times(1)).findByNomeContainingIgnoreCase("FISICA");
    }

    @Test
    @DisplayName("TESTE DE UNIDADE - Listar todas as categorias")
    void listarTodasCategorias() {
        Categoria categoria2 = new Categoria("PESSOA_JURIDICA", "Empresas");

        when(categoriaRepository.findAll())
                .thenReturn(Arrays.asList(categoriaValida, categoria2));

        List<Categoria> resultado = categoriaService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }
}