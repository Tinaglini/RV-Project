package app.sistemaclientesrv.controller;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente cliente) {
        Cliente clienteSalvo = clienteService.salvar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id,
                                             @RequestBody @Valid Cliente cliente) {
        Cliente clienteAtualizado = clienteService.atualizar(id, cliente);
        return ResponseEntity.ok(clienteAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Cliente>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(clienteService.buscarPorNome(nome));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Cliente>> buscarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(clienteService.buscarPorCategoria(categoriaId));
    }

    @GetMapping("/cpf")
    public ResponseEntity<Cliente> buscarPorCpf(@RequestParam String cpf) {
        return ResponseEntity.ok(clienteService.buscarPorCpf(cpf));
    }

    @GetMapping("/email")
    public ResponseEntity<Cliente> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(clienteService.buscarPorEmail(email));
    }

    /**
     * POST /clientes/login
     * Realiza login do cliente
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String cpfOuEmail = loginRequest.get("cpfOuEmail");
        String senha = loginRequest.get("senha");

        if (cpfOuEmail == null || senha == null) {
            throw new IllegalArgumentException("CPF/Email e senha são obrigatórios");
        }

        Cliente cliente = clienteService.autenticar(cpfOuEmail, senha);

        // Retornar dados do cliente (sem a senha)
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Login realizado com sucesso",
                "cliente", cliente
        ));
    }

    /**
     * PUT /clientes/{id}/alterar-senha
     * Altera senha do cliente
     */
    @PutMapping("/{id}/alterar-senha")
    public ResponseEntity<Map<String, Object>> alterarSenha(@PathVariable Long id,
                                                            @RequestBody Map<String, String> senhaRequest) {
        String senhaAtual = senhaRequest.get("senhaAtual");
        String novaSenha = senhaRequest.get("novaSenha");

        if (senhaAtual == null || novaSenha == null) {
            throw new IllegalArgumentException("Senha atual e nova senha são obrigatórias");
        }

        clienteService.alterarSenha(id, senhaAtual, novaSenha);

        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Senha alterada com sucesso"
        ));
    }

    /**
     * PUT /clientes/{id}/desbloquear
     * Desbloqueia conta do cliente (função administrativa)
     */
    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<Map<String, Object>> desbloquearConta(@PathVariable Long id) {
        clienteService.desbloquearConta(id);

        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Conta desbloqueada com sucesso"
        ));
    }

    /**
     * GET /clientes/bloqueados
     * Lista clientes com contas bloqueadas
     */
    @GetMapping("/bloqueados")
    public ResponseEntity<List<Cliente>> listarContasBloqueadas() {
        return ResponseEntity.ok(clienteService.buscarContasBloqueadas());
    }

    /**
     * POST /clientes/verificar-cpf-email
     * Verifica se CPF ou email já existem no sistema
     */
    @PostMapping("/verificar-cpf-email")
    public ResponseEntity<Map<String, Object>> verificarCpfEmail(@RequestBody Map<String, String> verificacaoRequest) {
        String cpfOuEmail = verificacaoRequest.get("cpfOuEmail");

        if (cpfOuEmail == null) {
            throw new IllegalArgumentException("CPF ou email é obrigatório");
        }

        // Tenta buscar o cliente - se não encontrar, o Service lança RuntimeException
        // que será capturada pelo GlobalExceptionHandler
        Cliente cliente = clienteService.buscarPorCpfOuEmail(cpfOuEmail);

        return ResponseEntity.ok(Map.of(
                "existe", true,
                "clienteId", cliente.getId(),
                "nome", cliente.getNome()
        ));
    }
}