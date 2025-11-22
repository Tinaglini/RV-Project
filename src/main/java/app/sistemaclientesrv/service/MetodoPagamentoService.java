package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.MetodoPagamento;
import app.sistemaclientesrv.repository.MetodoPagamentoRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MetodoPagamentoService {

    @Autowired
    private MetodoPagamentoRepository metodoPagamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public MetodoPagamento salvar(MetodoPagamento metodoPagamento) {
        if (metodoPagamento.getCliente() != null && metodoPagamento.getCliente().getId() != null) {
            clienteRepository.findById(metodoPagamento.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        }
        return metodoPagamentoRepository.save(metodoPagamento);
    }

    public MetodoPagamento buscarPorId(Long id) {
        return metodoPagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));
    }

    public List<MetodoPagamento> listarTodos() {
        return metodoPagamentoRepository.findAll();
    }

    public MetodoPagamento atualizar(Long id, MetodoPagamento metodoPagamento) {
        buscarPorId(id);
        metodoPagamento.setId(id);
        return metodoPagamentoRepository.save(metodoPagamento);
    }

    public void deletar(Long id) {
        MetodoPagamento metodoPagamento = buscarPorId(id);
        metodoPagamentoRepository.delete(metodoPagamento);
    }

    public List<MetodoPagamento> buscarPorCliente(Long clienteId) {
        return metodoPagamentoRepository.findByClienteId(clienteId);
    }

    public List<MetodoPagamento> buscarPorStatus(String status) {
        return metodoPagamentoRepository.findByStatus(status);
    }

    public List<MetodoPagamento> buscarPorContrato(Long contratoId) {
        return metodoPagamentoRepository.findByContratoId(contratoId);
    }

    public List<MetodoPagamento> buscarPorServico(Long servicoId) {
        return metodoPagamentoRepository.findByServicoId(servicoId);
    }

    /**
     * Gera QR Code PIX fake para uma transação de pagamento
     * @param id ID da transação de pagamento
     * @param valor Valor do pagamento (não usado, pois já está na transação)
     * @param descricao Descrição do pagamento
     * @return Transação de pagamento com QR Code gerado
     */
    public MetodoPagamento gerarQRCodePIX(Long id, Double valor, String descricao) {
        MetodoPagamento metodoPagamento = buscarPorId(id);

        // Verifica se o serviço usado é PIX
        if (metodoPagamento.getServico() == null ||
            !"PIX".equalsIgnoreCase(metodoPagamento.getServico().getNome())) {
            throw new RuntimeException("Geração de QR Code disponível apenas para métodos PIX");
        }

        metodoPagamento.gerarQRCodePIX(descricao);
        return metodoPagamentoRepository.save(metodoPagamento);
    }

    /**
     * Busca transações de pagamento PIX com QR Code gerado
     * @return Lista de transações PIX com QR Code
     */
    public List<MetodoPagamento> buscarPIXComQRCode() {
        return metodoPagamentoRepository.buscarPIXComQRCode();
    }
}
