package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.PedidoItem;
import app.sistemaclientesrv.repository.PedidoItemRepository;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PedidoItemService {

    @Autowired
    private PedidoItemRepository pedidoItemRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public PedidoItem salvar(PedidoItem pedidoItem) {
        if (pedidoItem.getContrato() != null && pedidoItem.getContrato().getId() != null) {
            contratoRepository.findById(pedidoItem.getContrato().getId())
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
        }

        if (pedidoItem.getServico() != null && pedidoItem.getServico().getId() != null) {
            servicoRepository.findById(pedidoItem.getServico().getId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        }

        // Calcular valor final automaticamente
        pedidoItem.calcularValorFinal();

        return pedidoItemRepository.save(pedidoItem);
    }

    public PedidoItem buscarPorId(Long id) {
        return pedidoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de pedido não encontrado"));
    }

    public List<PedidoItem> listarTodos() {
        return pedidoItemRepository.findAll();
    }

    public PedidoItem atualizar(Long id, PedidoItem pedidoItem) {
        buscarPorId(id);
        pedidoItem.setId(id);
        pedidoItem.calcularValorFinal();
        return pedidoItemRepository.save(pedidoItem);
    }

    public void deletar(Long id) {
        PedidoItem pedidoItem = buscarPorId(id);
        pedidoItemRepository.delete(pedidoItem);
    }

    public List<PedidoItem> buscarPorContrato(Long contratoId) {
        return pedidoItemRepository.findByContratoId(contratoId);
    }

    public List<PedidoItem> buscarPorServico(Long servicoId) {
        return pedidoItemRepository.findByServicoId(servicoId);
    }
}