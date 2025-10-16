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
 * Testes de integração do CategoriaService.
 * Valida interação com o repository mockado.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceIntegrationTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaPF;
    private Categoria categoriaPJ;

    @BeforeEach
    void setUp() {
        categoriaPF = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        categoriaPF.setId(1L);
        categoriaPF.setBeneficios("Taxas reduzidas");
        categoriaPF.setAtivo(true);

        categoriaPJ = new Categoria("PESSOA_JURIDICA", "Clientes pessoa jurídica");
        categoriaPJ.setId(2L);
        categoriaPJ.setBeneficios("Desconto por volume");
        categoriaPJ.setAtivo(true);
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Listar todas as categorias do repositório")
    void listarTodasCategoriasDoRepositorio() {
        // Mock do repository retornando múltiplas categorias
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoriaPF, categoriaPJ));

        List<Categoria> resultado = categoriaService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("PESSOA_FISICA", resultado.get(0).getNome());
        assertEquals("PESSOA_JURIDICA", resultado.get(1).getNome());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar categorias por nome no repositório")
    void buscarCategoriasPorNomeNoRepositorio() {
        // Mock de busca parcial no repository
        when(categoriaRepository.findByNomeContainingIgnoreCase("FISICA"))
                .thenReturn(Arrays.asList(categoriaPF));

        List<Categoria> resultado = categoriaService.buscarPorNome("FISICA");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PESSOA_FISICA", resultado.get(0).getNome());
        verify(categoriaRepository, times(1)).findByNomeContainingIgnoreCase("FISICA");
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Buscar apenas ativas no repositório")
    void buscarApenasAtivasNoRepositorio() {
        // Mock de busca por categorias ativas
        when(categoriaRepository.findByAtivoTrue())
                .thenReturn(Arrays.asList(categoriaPF, categoriaPJ));

        List<Categoria> resultado = categoriaService.buscarAtivas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Categoria::getAtivo));
        verify(categoriaRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Atualizar categoria consultando repositório")
    void atualizarCategoriaConsultandoRepositorio() {
        Categoria categoriaAtualizada = new Categoria("PESSOA_FISICA", "Nova descrição");
        categoriaAtualizada.setId(1L);

        // Mock de busca e salvamento
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPF));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);

        Categoria resultado = categoriaService.atualizar(1L, categoriaAtualizada);

        assertNotNull(resultado);
        assertEquals("Nova descrição", resultado.getDescricao());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("TESTE DE INTEGRAÇÃO - Validar nome único no repositório antes de salvar")
    void validarNomeUnicoNoRepositorioAntesDeSalvar() {
        // Mock: nome já existe no repository
        when(categoriaRepository.existsByNome("PESSOA_FISICA")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.salvar(categoriaPF);
        });

        assertEquals("Já existe uma categoria com o nome: PESSOA_FISICA", exception.getMessage());
        verify(categoriaRepository, times(1)).existsByNome("PESSOA_FISICA");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}