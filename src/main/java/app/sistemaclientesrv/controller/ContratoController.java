package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.service.ContratoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contratos")
@CrossOrigin(origins = "*")
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<Contrato>> listarTodos() {
        return ResponseEntity.ok(contratoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contrato> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Contrato> criar(@RequestBody @Valid Contrato contrato) {
        Contrato contratoSalvo = contratoService.salvar(contrato);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contrato> atualizar(@PathVariable Long id,
                                              @RequestBody @Valid Contrato contrato) {
        Contrato contratoAtualizado = contratoService.atualizar(id, contrato);
        return ResponseEntity.ok(contratoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        contratoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Contrato>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contratoService.buscarPorCliente(clienteId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Contrato>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(contratoService.buscarPorStatus(status));
    }
}