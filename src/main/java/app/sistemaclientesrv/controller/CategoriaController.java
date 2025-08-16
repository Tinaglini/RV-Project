package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciar categorias de clientes.
 * Expõe endpoints para CRUD completo das categorias.
 */
@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // GET /categorias - Lista todas as categorias
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodos());
    }

    // GET /categorias/{id} - Busca categoria por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    // POST /categorias - Cria nova categoria
    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria) {
        Categoria categoriaSalva = categoriaService.salvar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
    }

    // PUT /categorias/{id} - Atualiza categoria existente
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id,
                                               @RequestBody @Valid Categoria categoria) {
        Categoria categoriaAtualizada = categoriaService.atualizar(id, categoria);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    // DELETE /categorias/{id} - Remove categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // GET /categorias/ativas - Busca apenas categorias ativas
    @GetMapping("/ativas")
    public ResponseEntity<List<Categoria>> buscarAtivas() {
        return ResponseEntity.ok(categoriaService.buscarAtivas());
    }

    // GET /categorias/buscar?nome=valor - Busca por nome
    @GetMapping("/buscar")
    public ResponseEntity<List<Categoria>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(categoriaService.buscarPorNome(nome));
    }
}