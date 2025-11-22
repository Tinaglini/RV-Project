package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.MetodoPagamento;
import app.sistemaclientesrv.service.MetodoPagamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metodos-pagamento")
public class MetodoPagamentoController {

    @Autowired
    private MetodoPagamentoService metodoPagamentoService;

    @GetMapping
    public ResponseEntity<List<MetodoPagamento>> listarTodos() {
        return ResponseEntity.ok(metodoPagamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagamento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(metodoPagamentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MetodoPagamento> criar(@RequestBody @Valid MetodoPagamento metodoPagamento) {
        MetodoPagamento metodoPagamentoSalvo = metodoPagamentoService.salvar(metodoPagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(metodoPagamentoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagamento> atualizar(@PathVariable Long id,
                                                      @RequestBody @Valid MetodoPagamento metodoPagamento) {
        MetodoPagamento metodoPagamentoAtualizado = metodoPagamentoService.atualizar(id, metodoPagamento);
        return ResponseEntity.ok(metodoPagamentoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        metodoPagamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MetodoPagamento>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(metodoPagamentoService.buscarPorCliente(clienteId));
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<MetodoPagamento>> buscarPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(metodoPagamentoService.buscarPorContrato(contratoId));
    }

    @GetMapping("/servico/{servicoId}")
    public ResponseEntity<List<MetodoPagamento>> buscarPorServico(@PathVariable Long servicoId) {
        return ResponseEntity.ok(metodoPagamentoService.buscarPorServico(servicoId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MetodoPagamento>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(metodoPagamentoService.buscarPorStatus(status));
    }

    @GetMapping("/pix-com-qrcode")
    public ResponseEntity<List<MetodoPagamento>> buscarPIXComQRCode() {
        return ResponseEntity.ok(metodoPagamentoService.buscarPIXComQRCode());
    }

    /**
     * Endpoint para gerar QR Code PIX fake
     *
     * Body esperado:
     * {
     *   "valor": 100.00,
     *   "descricao": "Pagamento de serviço"
     * }
     */
    @PostMapping("/{id}/gerar-qrcode-pix")
    public ResponseEntity<MetodoPagamento> gerarQRCodePIX(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Double valor = request.get("valor") != null ?
                       Double.parseDouble(request.get("valor").toString()) : 0.0;
        String descricao = request.get("descricao") != null ?
                          request.get("descricao").toString() : "Pagamento";

        MetodoPagamento metodoPagamento = metodoPagamentoService.gerarQRCodePIX(id, valor, descricao);
        return ResponseEntity.ok(metodoPagamento);
    }
}
