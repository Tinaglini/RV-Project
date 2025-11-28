package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.dto.PagamentoDTO;
import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.service.ContratoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Lista contratos com base no papel do usuário:
     * - ADMIN: retorna todos os contratos
     * - USER: retorna apenas contratos do próprio cliente
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> listarTodos(Authentication authentication) {
        String username = authentication.getName();

        // Se for ADMIN: retorna TODOS os contratos
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(contratoService.listarTodos());
        }

        // Se for USER: retorna apenas contratos do próprio cliente
        Cliente cliente = clienteRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return ResponseEntity.ok(contratoService.buscarPorCliente(cliente.getId()));
    }

    /**
     * Busca contrato por ID:
     * - ADMIN: pode ver qualquer contrato
     * - USER: pode ver apenas seus próprios contratos
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Contrato> buscarPorId(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Contrato contrato = contratoService.buscarPorId(id);

        // Se for USER: verificar se o contrato pertence a ele
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            Cliente cliente = clienteRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            if (!contrato.getCliente().getId().equals(cliente.getId())) {
                throw new AccessDeniedException("Você não tem permissão para visualizar este contrato");
            }
        }

        return ResponseEntity.ok(contrato);
    }

    /**
     * Cria novo contrato - Apenas ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Contrato> criar(@RequestBody @Valid Contrato contrato) {
        Contrato contratoSalvo = contratoService.salvar(contrato);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoSalvo);
    }

    /**
     * Atualiza contrato existente - Apenas ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Contrato> atualizar(@PathVariable Long id,
                                              @RequestBody @Valid Contrato contrato) {
        Contrato contratoAtualizado = contratoService.atualizar(id, contrato);
        return ResponseEntity.ok(contratoAtualizado);
    }

    /**
     * Deleta contrato - Apenas ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        contratoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca contratos por cliente:
     * - ADMIN: pode buscar contratos de qualquer cliente
     * - USER: pode buscar apenas seus próprios contratos
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> buscarPorCliente(@PathVariable Long clienteId,
                                                            Authentication authentication) {
        String username = authentication.getName();

        // Se for USER: verificar se está buscando seus próprios contratos
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            Cliente cliente = clienteRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            if (!cliente.getId().equals(clienteId)) {
                throw new AccessDeniedException("Você não tem permissão para visualizar contratos de outros clientes");
            }
        }

        return ResponseEntity.ok(contratoService.buscarPorCliente(clienteId));
    }

    /**
     * Busca contratos por status - Apenas ADMIN
     * Permite filtrar contratos por status (PENDENTE, PAGO, VENCIDO, CANCELADO)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(contratoService.buscarPorStatus(status));
    }

    /**
     * Endpoint para pagar um contrato
     * - ADMIN: pode pagar qualquer contrato
     * - USER: pode pagar apenas seus próprios contratos
     */
    @PostMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Contrato> pagar(@PathVariable Long id,
                                          @RequestBody @Valid PagamentoDTO pagamentoDTO,
                                          Authentication authentication) {
        String username = authentication.getName();

        // Se for USER: verificar se o contrato pertence a ele
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            Cliente cliente = clienteRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Contrato contrato = contratoService.buscarPorId(id);

            // Verificar se o contrato pertence ao cliente logado
            if (!contrato.getCliente().getId().equals(cliente.getId())) {
                throw new AccessDeniedException("Você não tem permissão para pagar este contrato");
            }
        }

        Contrato contratoPago = contratoService.pagarContrato(id, pagamentoDTO);
        return ResponseEntity.ok(contratoPago);
    }
}