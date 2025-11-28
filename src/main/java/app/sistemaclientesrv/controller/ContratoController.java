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

/**
 * REST controller for managing contracts (bills to pay).
 * Implements role-based access control where ADMIN users can access all contracts,
 * while regular users can only access their own contracts.
 *
 * @author Sistema Clientes RV
 * @version 1.0
 */
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
     * Retrieves all contracts based on user role.
     * ADMIN users receive all contracts, while regular users only receive their own.
     *
     * @param authentication current authenticated user
     * @return list of contracts accessible to the user
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> listarTodos(Authentication authentication) {
        String username = authentication.getName();

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(contratoService.listarTodos());
        }

        Cliente cliente = clienteRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return ResponseEntity.ok(contratoService.buscarPorCliente(cliente.getId()));
    }

    /**
     * Retrieves a specific contract by ID.
     * ADMIN users can view any contract, while regular users can only view their own.
     *
     * @param id contract identifier
     * @param authentication current authenticated user
     * @return the requested contract
     * @throws AccessDeniedException if a regular user attempts to access another user's contract
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Contrato> buscarPorId(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Contrato contrato = contratoService.buscarPorId(id);

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
     * Creates a new contract. Restricted to ADMIN role only.
     *
     * @param contrato contract data to create
     * @return the created contract
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Contrato> criar(@RequestBody @Valid Contrato contrato) {
        Contrato contratoSalvo = contratoService.salvar(contrato);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoSalvo);
    }

    /**
     * Updates an existing contract. Restricted to ADMIN role only.
     *
     * @param id contract identifier
     * @param contrato updated contract data
     * @return the updated contract
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Contrato> atualizar(@PathVariable Long id,
                                              @RequestBody @Valid Contrato contrato) {
        Contrato contratoAtualizado = contratoService.atualizar(id, contrato);
        return ResponseEntity.ok(contratoAtualizado);
    }

    /**
     * Deletes a contract. Restricted to ADMIN role only.
     *
     * @param id contract identifier to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        contratoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves contracts for a specific client.
     * ADMIN users can search for any client, while regular users can only search their own contracts.
     *
     * @param clienteId client identifier
     * @param authentication current authenticated user
     * @return list of contracts for the specified client
     * @throws AccessDeniedException if a regular user attempts to access another client's contracts
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> buscarPorCliente(@PathVariable Long clienteId,
                                                            Authentication authentication) {
        String username = authentication.getName();

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
     * Retrieves contracts filtered by status. Restricted to ADMIN role only.
     * Valid status values: PENDENTE, PAGO, VENCIDO, CANCELADO.
     *
     * @param status contract status to filter by
     * @return list of contracts with the specified status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Contrato>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(contratoService.buscarPorStatus(status));
    }

    /**
     * Processes payment for a contract.
     * ADMIN users can pay any contract, while regular users can only pay their own.
     *
     * @param id contract identifier to pay
     * @param pagamentoDTO payment method details (service, PIX key, barcode, etc.)
     * @param authentication current authenticated user
     * @return the updated contract with PAGO status
     * @throws AccessDeniedException if a regular user attempts to pay another user's contract
     */
    @PostMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Contrato> pagar(@PathVariable Long id,
                                          @RequestBody @Valid PagamentoDTO pagamentoDTO,
                                          Authentication authentication) {
        String username = authentication.getName();

        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            Cliente cliente = clienteRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Contrato contrato = contratoService.buscarPorId(id);

            if (!contrato.getCliente().getId().equals(cliente.getId())) {
                throw new AccessDeniedException("Você não tem permissão para pagar este contrato");
            }
        }

        Contrato contratoPago = contratoService.pagarContrato(id, pagamentoDTO);
        return ResponseEntity.ok(contratoPago);
    }
}