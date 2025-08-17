package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Endereco;
import app.sistemaclientesrv.repository.EnderecoRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public Endereco salvar(Endereco endereco) {
        if (endereco.getCliente() != null && endereco.getCliente().getId() != null) {
            clienteRepository.findById(endereco.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        }
        return enderecoRepository.save(endereco);
    }

    public Endereco buscarPorId(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
    }

    public List<Endereco> listarTodos() {
        return enderecoRepository.findAll();
    }

    public Endereco atualizar(Long id, Endereco endereco) {
        buscarPorId(id);
        endereco.setId(id);
        return enderecoRepository.save(endereco);
    }

    public void deletar(Long id) {
        Endereco endereco = buscarPorId(id);
        enderecoRepository.delete(endereco);
    }

    public List<Endereco> buscarPorCliente(Long clienteId) {
        return enderecoRepository.findByClienteId(clienteId);
    }

    public List<Endereco> buscarPorCidade(String cidade) {
        return enderecoRepository.findByCidadeContainingIgnoreCase(cidade);
    }
}