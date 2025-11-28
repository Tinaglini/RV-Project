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

/**
 * Service layer for managing contracts (bills to pay).
 * Handles business logic for contract operations including creation, updates, and payment processing.
 *
 * @author Sistema Clientes RV
 * @version 1.0
 */
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

    /**
     * Saves a new contract or updates an existing one.
     * Validates that the associated client exists before saving.
     *
     * @param contrato contract to save
     * @return the saved contract
     * @throws RuntimeException if the associated client is not found
     */
    public Contrato salvar(Contrato contrato) {
        if (contrato.getCliente() != null && contrato.getCliente().getId() != null) {
            clienteRepository.findById(contrato.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        }

        return contratoRepository.save(contrato);
    }

    /**
     * Retrieves a contract by its ID.
     *
     * @param id contract identifier
     * @return the found contract
     * @throws RuntimeException if contract is not found
     */
    public Contrato buscarPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
    }

    /**
     * Retrieves all contracts from the database.
     *
     * @return list of all contracts
     */
    public List<Contrato> listarTodos() {
        return contratoRepository.findAll();
    }

    /**
     * Updates an existing contract.
     *
     * @param id contract identifier
     * @param contrato updated contract data
     * @return the updated contract
     * @throws RuntimeException if contract is not found
     */
    public Contrato atualizar(Long id, Contrato contrato) {
        buscarPorId(id);
        contrato.setId(id);
        return contratoRepository.save(contrato);
    }

    /**
     * Deletes a contract from the database.
     *
     * @param id contract identifier to delete
     * @throws RuntimeException if contract is not found
     */
    public void deletar(Long id) {
        Contrato contrato = buscarPorId(id);
        contratoRepository.delete(contrato);
    }

    /**
     * Retrieves all contracts for a specific client.
     *
     * @param clienteId client identifier
     * @return list of contracts belonging to the client
     */
    public List<Contrato> buscarPorCliente(Long clienteId) {
        return contratoRepository.findByClienteId(clienteId);
    }

    /**
     * Retrieves all contracts with a specific status.
     *
     * @param status contract status (PENDENTE, PAGO, VENCIDO, CANCELADO)
     * @return list of contracts with the specified status
     */
    public List<Contrato> buscarPorStatus(String status) {
        return contratoRepository.findByStatus(status);
    }

    /**
     * Processes payment for a contract by creating a payment record and updating contract status.
     * Supports multiple payment methods: PIX, Boleto, Credit Card, Debit Card, TED.
     * Automatically calculates total amount including service fees.
     *
     * @param contratoId contract identifier to pay
     * @param pagamentoDTO payment details including service ID and method-specific data
     * @return the updated contract with status changed to PAGO
     * @throws RuntimeException if contract is already paid, contract not found, or service not found
     */
    public Contrato pagarContrato(Long contratoId, PagamentoDTO pagamentoDTO) {
        Contrato contrato = buscarPorId(contratoId);

        if ("PAGO".equalsIgnoreCase(contrato.getStatus())) {
            throw new RuntimeException("Este contrato já foi pago");
        }

        Servico servico = servicoRepository.findById(pagamentoDTO.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço de pagamento não encontrado"));

        MetodoPagamento metodoPagamento = new MetodoPagamento();
        metodoPagamento.setValor(contrato.getValor());
        metodoPagamento.setCliente(contrato.getCliente());
        metodoPagamento.setContrato(contrato);
        metodoPagamento.setServico(servico);
        metodoPagamento.setStatus("CONCLUIDO");
        metodoPagamento.setObservacoes(pagamentoDTO.getObservacoes());

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

        metodoPagamento.calcularValorTotal();
        metodoPagamentoRepository.save(metodoPagamento);

        contrato.setStatus("PAGO");
        contrato.setDataPagamento(LocalDate.now());
        contratoRepository.save(contrato);

        return contrato;
    }
}