package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    public Servico salvar(Servico servico) {
        if (servico.getValor() <= 0) {
            throw new RuntimeException("Valor do serviço deve ser positivo");
        }
        return servicoRepository.save(servico);
    }

    public Servico buscarPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
    }

    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    public Servico atualizar(Long id, Servico servico) {
        buscarPorId(id);
        servico.setId(id);
        servico.setUpdatedAt(LocalDateTime.now());
        return servicoRepository.save(servico);
    }

    public void deletar(Long id) {
        Servico servico = buscarPorId(id);
        servicoRepository.delete(servico);
    }

    public List<Servico> buscarAtivos() {
        return servicoRepository.findByAtivoTrue();
    }

    public List<Servico> buscarPorCategoria(String categoria) {
        return servicoRepository.findByCategoria(categoria);
    }

    public List<Servico> buscarPorNome(String nome) {
        return servicoRepository.findByNomeContainingIgnoreCase(nome);
    }
}