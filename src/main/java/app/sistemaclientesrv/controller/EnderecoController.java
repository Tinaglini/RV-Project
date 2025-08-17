package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.Endereco;
import app.sistemaclientesrv.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enderecos")
@CrossOrigin(origins = "*")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    @GetMapping
    public ResponseEntity<List<Endereco>> listarTodos() {
        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(enderecoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Endereco> criar(@RequestBody @Valid Endereco endereco) {
        Endereco enderecoSalvo = enderecoService.salvar(endereco);
        return ResponseEntity.status(HttpStatus.CREATED).body(enderecoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endereco> atualizar(@PathVariable Long id,
                                              @RequestBody @Valid Endereco endereco) {
        Endereco enderecoAtualizado = enderecoService.atualizar(id, endereco);
        return ResponseEntity.ok(enderecoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Endereco>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(enderecoService.buscarPorCliente(clienteId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Endereco>> buscarPorCidade(@RequestParam String cidade) {
        return ResponseEntity.ok(enderecoService.buscarPorCidade(cidade));
    }
}