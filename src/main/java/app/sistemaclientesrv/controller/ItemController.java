package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.Item;
import app.sistemaclientesrv.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itens")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> listarTodos() {
        return ResponseEntity.ok(itemService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Item> criar(@RequestBody @Valid Item item) {
        Item itemSalvo = itemService.salvar(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> atualizar(@PathVariable Long id,
                                          @RequestBody @Valid Item item) {
        Item itemAtualizado = itemService.atualizar(id, item);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        itemService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<Item>> buscarPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(itemService.buscarPorContrato(contratoId));
    }

    @GetMapping("/servico/{servicoId}")
    public ResponseEntity<List<Item>> buscarPorServico(@PathVariable Long servicoId) {
        return ResponseEntity.ok(itemService.buscarPorServico(servicoId));
    }
}