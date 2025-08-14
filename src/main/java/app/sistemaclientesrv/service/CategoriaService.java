package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável pelas regras de negócio das categorias.
 * Controla validações, salvamento e consultas das categorias de clientes.
 */
@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Salva uma nova categoria ou atualiza existente
    public Categoria salvar(Categoria categoria) {
        // Regra: não pode ter duas categorias com o mesmo nome
        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new RuntimeException("Já existe uma categoria com o nome: " + categoria.getNome());
        }

        return categoriaRepository.save(categoria);
    }

    // Busca categoria por ID (lança erro se não encontrar)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
    }

    // Lista todas as categorias cadastradas
    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    // Atualiza uma categoria existente
    public Categoria atualizar(Long id, Categoria categoria) {
        buscarPorId(id); // Verifica se existe
        categoria.setId(id);
        categoria.setUpdatedAt(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    // Remove uma categoria do sistema
    public void deletar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }

    // Retorna apenas as categorias ativas
    public List<Categoria> buscarAtivas() {
        return categoriaRepository.findByAtivoTrue();
    }

    // Busca categorias que contenham o nome pesquisado
    public List<Categoria> buscarPorNome(String nome) {
        return categoriaRepository.findByNomeContainingIgnoreCase(nome);
    }
}