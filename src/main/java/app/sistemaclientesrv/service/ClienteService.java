package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Cliente salvar(Cliente cliente) {
        // REGRA OBRIGATÓRIA 1: Exception - CPF único
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Não é possível cadastrar cliente com CPF já existente");
        }

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
        buscarPorId(id);
        cliente.setId(id);
        cliente.setUpdatedAt(LocalDateTime.now());

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
}