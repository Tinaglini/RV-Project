package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public Contrato salvar(Contrato contrato) {
        if (contrato.getCliente() != null && contrato.getCliente().getId() != null) {
            clienteRepository.findById(contrato.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        }

        if (contrato.getDataFim() != null &&
                contrato.getDataFim().isBefore(contrato.getDataInicio())) {
            throw new RuntimeException("Data fim deve ser maior que data início");
        }

        return contratoRepository.save(contrato);
    }

    public Contrato buscarPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
    }

    public List<Contrato> listarTodos() {
        return contratoRepository.findAll();
    }

    public Contrato atualizar(Long id, Contrato contrato) {
        buscarPorId(id);
        contrato.setId(id);
        return contratoRepository.save(contrato);
    }

    public void deletar(Long id) {
        Contrato contrato = buscarPorId(id);
        contratoRepository.delete(contrato);
    }

    public List<Contrato> buscarPorCliente(Long clienteId) {
        return contratoRepository.findByClienteId(clienteId);
    }

    public List<Contrato> buscarPorStatus(String status) {
        return contratoRepository.findByStatus(status);
    }
}