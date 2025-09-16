package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Cliente salvar(Cliente cliente) {
        // REGRA OBRIGATÓRIA 1: Exception - CPF único
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Não é possível cadastrar cliente com CPF já existente");
        }

        // NOVA REGRA: Email único (se informado)
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            Optional<Cliente> clienteExistente = clienteRepository.findByEmail(cliente.getEmail());
            if (clienteExistente.isPresent()) {
                throw new RuntimeException("Já existe um cliente cadastrado com este email");
            }
        }

        // NOVA REGRA: Validação e criptografia da senha
        if (cliente.getSenha() == null || cliente.getSenha().trim().isEmpty()) {
            throw new RuntimeException("A senha é obrigatória para cadastro do cliente");
        }

        if (cliente.getSenha().length() < 6) {
            throw new RuntimeException("A senha deve ter no mínimo 6 caracteres");
        }

        // Criptografar a senha antes de salvar
        String senhaHash = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenhaHash(senhaHash);
        // Não limpar a senha aqui pois pode causar problemas de validação

        // REGRA OBRIGATÓRIA 2: Modificação do objeto - Status automático
        if (cliente.getTelefone() == null || cliente.getTelefone().trim().isEmpty()) {
            cliente.setStatusCadastro("INCOMPLETO");
        } else {
            cliente.setStatusCadastro("COMPLETO");
        }

        // Categoria padrão se não informada
        if (cliente.getCategoria() == null) {
            Categoria categoriaPF = categoriaRepository.findByNome("PESSOA_FISICA").orElse(null);
            if (categoriaPF != null) {
                cliente.setCategoria(categoriaPF);
            }
        }

        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente atualizar(Long id, Cliente cliente) {
        Cliente clienteExistente = buscarPorId(id);
        cliente.setId(id);
        cliente.setUpdatedAt(LocalDateTime.now());

        // Se uma nova senha foi fornecida, criptografar
        if (cliente.getSenha() != null && !cliente.getSenha().trim().isEmpty()) {
            if (cliente.getSenha().length() < 6) {
                throw new RuntimeException("A senha deve ter no mínimo 6 caracteres");
            }
            String senhaHash = passwordEncoder.encode(cliente.getSenha());
            cliente.setSenhaHash(senhaHash);
        } else {
            // Manter a senha existente se não foi fornecida nova senha
            cliente.setSenhaHash(clienteExistente.getSenhaHash());
        }

        // Aplicar regra de status na atualização
        if (cliente.getTelefone() == null || cliente.getTelefone().trim().isEmpty()) {
            cliente.setStatusCadastro("INCOMPLETO");
        } else {
            cliente.setStatusCadastro("COMPLETO");
        }

        return clienteRepository.save(cliente);
    }

    public void deletar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Cliente> buscarPorCategoria(Long categoriaId) {
        return clienteRepository.findByCategoriaId(categoriaId);
    }

    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com CPF: " + cpf));
    }

    // NOVOS MÉTODOS PARA AUTENTICAÇÃO

    /**
     * Realiza login do cliente com CPF/Email e senha
     */
    public Cliente autenticar(String loginCpfOuEmail, String senha) {
        Cliente cliente = buscarPorCpfOuEmail(loginCpfOuEmail);

        // Verificar se a conta não está bloqueada
        if (cliente.getContaBloqueada()) {
            throw new RuntimeException("Conta bloqueada devido a muitas tentativas de login incorretas. Entre em contato com o suporte.");
        }

        // Verificar se o cliente está ativo
        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo. Entre em contato com o suporte.");
        }

        // Verificar a senha
        if (!passwordEncoder.matches(senha, cliente.getSenhaHash())) {
            cliente.registrarTentativaLogin();
            clienteRepository.save(cliente);
            throw new RuntimeException("Senha incorreta");
        }

        // Login bem-sucedido
        cliente.resetarTentativasLogin();
        clienteRepository.save(cliente);

        return cliente;
    }

    /**
     * Busca cliente por CPF ou Email
     */
    public Cliente buscarPorCpfOuEmail(String cpfOuEmail) {
        // Primeiro tenta buscar por CPF
        Optional<Cliente> clientePorCpf = clienteRepository.findByCpf(cpfOuEmail);
        if (clientePorCpf.isPresent()) {
            return clientePorCpf.get();
        }

        // Se não encontrou por CPF, tenta por email
        Optional<Cliente> clientePorEmail = clienteRepository.findByEmail(cpfOuEmail);
        if (clientePorEmail.isPresent()) {
            return clientePorEmail.get();
        }

        throw new RuntimeException("Cliente não encontrado com CPF/Email: " + cpfOuEmail);
    }

    /**
     * Altera senha do cliente
     */
    public void alterarSenha(Long clienteId, String senhaAtual, String novaSenha) {
        Cliente cliente = buscarPorId(clienteId);

        // Verificar senha atual
        if (!passwordEncoder.matches(senhaAtual, cliente.getSenhaHash())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        // Validar nova senha
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            throw new RuntimeException("A nova senha é obrigatória");
        }

        if (novaSenha.length() < 6) {
            throw new RuntimeException("A nova senha deve ter no mínimo 6 caracteres");
        }

        // Criptografar e salvar nova senha
        String novaSenhaHash = passwordEncoder.encode(novaSenha);
        cliente.setSenhaHash(novaSenhaHash);
        cliente.setUpdatedAt(LocalDateTime.now());

        clienteRepository.save(cliente);
    }

    /**
     * Desbloqueia conta de cliente (função administrativa)
     */
    public void desbloquearConta(Long clienteId) {
        Cliente cliente = buscarPorId(clienteId);
        cliente.desbloquearConta();
        clienteRepository.save(cliente);
    }

    /**
     * Busca clientes com contas bloqueadas
     */
    public List<Cliente> buscarContasBloqueadas() {
        return clienteRepository.findByContaBloqueadaTrue();
    }

    /**
     * Busca cliente por email
     */
    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com email: " + email));
    }
}