package app.sistemaclientesrv.service;

import app.sistemaclientesrv.dto.PagamentoDTO;
import app.sistemaclientesrv.entity.Contrato;
import app.sistemaclientesrv.entity.MetodoPagamento;
import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.MetodoPagamentoRepository;
import app.sistemaclientesrv.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MetodoPagamentoRepository metodoPagamentoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public Contrato salvar(Contrato contrato) {
        if (contrato.getCliente() != null && contrato.getCliente().getId() != null) {
            clienteRepository.findById(contrato.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        }

        // Validação: não pode salvar conta com data de vencimento no passado
        // (removida validação de dataFim/dataInicio que não existem mais)

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

    /**
     * Processa o pagamento de um contrato
     * Cria registro de MetodoPagamento e atualiza status do contrato
     */
    public Contrato pagarContrato(Long contratoId, PagamentoDTO pagamentoDTO) {
        // Buscar contrato
        Contrato contrato = buscarPorId(contratoId);

        // Verificar se o contrato já foi pago
        if ("PAGO".equalsIgnoreCase(contrato.getStatus())) {
            throw new RuntimeException("Este contrato já foi pago");
        }

        // Buscar serviço de pagamento
        Servico servico = servicoRepository.findById(pagamentoDTO.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço de pagamento não encontrado"));

        // Criar registro de pagamento
        MetodoPagamento metodoPagamento = new MetodoPagamento();
        metodoPagamento.setValor(contrato.getValor());
        metodoPagamento.setCliente(contrato.getCliente());
        metodoPagamento.setContrato(contrato);
        metodoPagamento.setServico(servico);
        metodoPagamento.setStatus("CONCLUIDO");
        metodoPagamento.setObservacoes(pagamentoDTO.getObservacoes());

        // Preencher dados específicos do método de pagamento
        if (pagamentoDTO.getChavePix() != null) {
            metodoPagamento.setChavePix(pagamentoDTO.getChavePix());
            metodoPagamento.gerarQRCodePIX("Pagamento de " + contrato.getDescricao());
        }
        if (pagamentoDTO.getCodigoBarras() != null) {
            metodoPagamento.setCodigoBarras(pagamentoDTO.getCodigoBarras());
        }
        if (pagamentoDTO.getNumeroCartao() != null) {
            metodoPagamento.setNumeroCartao(pagamentoDTO.getNumeroCartao());
            metodoPagamento.setBandeira(pagamentoDTO.getBandeira());
        }

        // Calcular valor total com taxa
        metodoPagamento.calcularValorTotal();

        // Salvar método de pagamento
        metodoPagamentoRepository.save(metodoPagamento);

        // Atualizar status do contrato para PAGO
        contrato.setStatus("PAGO");
        contrato.setDataPagamento(LocalDate.now());
        contratoRepository.save(contrato);

        return contrato;
    }
}