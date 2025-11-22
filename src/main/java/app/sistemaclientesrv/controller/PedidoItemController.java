package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.PedidoItem;
import app.sistemaclientesrv.service.PedidoItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedido-itens")
@CrossOrigin(origins = "*")
public class PedidoItemController {

    @Autowired
    private PedidoItemService pedidoItemService;

    @GetMapping
    public ResponseEntity<List<PedidoItem>> listarTodos() {
        return ResponseEntity.ok(pedidoItemService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoItem> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoItemService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PedidoItem> criar(@RequestBody @Valid PedidoItem pedidoItem) {
        PedidoItem pedidoItemSalvo = pedidoItemService.salvar(pedidoItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoItemSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoItem> atualizar(@PathVariable Long id,
                                          @RequestBody @Valid PedidoItem pedidoItem) {
        PedidoItem pedidoItemAtualizado = pedidoItemService.atualizar(id, pedidoItem);
        return ResponseEntity.ok(pedidoItemAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pedidoItemService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<PedidoItem>> buscarPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(pedidoItemService.buscarPorContrato(contratoId));
    }

    @GetMapping("/servico/{servicoId}")
    public ResponseEntity<List<PedidoItem>> buscarPorServico(@PathVariable Long servicoId) {
        return ResponseEntity.ok(pedidoItemService.buscarPorServico(servicoId));
    }
}