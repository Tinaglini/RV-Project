package app.sistemaclientesrv.service;

import app.sistemaclientesrv.entity.Item;
import app.sistemaclientesrv.repository.ItemRepository;
import app.sistemaclientesrv.repository.ContratoRepository;
import app.sistemaclientesrv.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public Item salvar(Item item) {
        if (item.getContrato() != null && item.getContrato().getId() != null) {
            contratoRepository.findById(item.getContrato().getId())
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
        }

        if (item.getServico() != null && item.getServico().getId() != null) {
            servicoRepository.findById(item.getServico().getId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        }

        // Calcular valor final automaticamente
        item.calcularValorFinal();

        return itemRepository.save(item);
    }

    public Item buscarPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
    }

    public List<Item> listarTodos() {
        return itemRepository.findAll();
    }

    public Item atualizar(Long id, Item item) {
        buscarPorId(id);
        item.setId(id);
        item.calcularValorFinal();
        return itemRepository.save(item);
    }

    public void deletar(Long id) {
        Item item = buscarPorId(id);
        itemRepository.delete(item);
    }

    public List<Item> buscarPorContrato(Long contratoId) {
        return itemRepository.findByContratoId(contratoId);
    }

    public List<Item> buscarPorServico(Long servicoId) {
        return itemRepository.findByServicoId(servicoId);
    }
}