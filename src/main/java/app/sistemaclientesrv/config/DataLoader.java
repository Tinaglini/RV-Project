package app.sistemaclientesrv.config;

import app.sistemaclientesrv.entity.Categoria;
import app.sistemaclientesrv.entity.Cliente;
import app.sistemaclientesrv.entity.Servico;
import app.sistemaclientesrv.repository.CategoriaRepository;
import app.sistemaclientesrv.repository.ClienteRepository;
import app.sistemaclientesrv.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Carregando dados iniciais apenas se o banco estiver vazio
        if (categoriaRepository.count() == 0) {
            carregarCategorias();
        }

        if (servicoRepository.count() == 0) {
            carregarServicos();
        }

        if (clienteRepository.count() == 0) {
            carregarClientesExemplo();
        }
    }

    private void carregarCategorias() {
        Categoria pessoaFisica = new Categoria("PESSOA_FISICA", "Clientes pessoa física");
        pessoaFisica.setBeneficios("Taxas reduzidas, atendimento personalizado");

        Categoria pessoaJuridica = new Categoria("PESSOA_JURIDICA", "Clientes pessoa jurídica");
        pessoaJuridica.setBeneficios("Desconto por volume, relatórios detalhados");

        categoriaRepository.save(pessoaFisica);
        categoriaRepository.save(pessoaJuridica);

        System.out.println("Categorias carregadas com sucesso!");
    }

    private void carregarServicos() {
        Servico recarga = new Servico("Recarga de Celular", "Recarga para todas as operadoras", 10.0, "RECARGA");
        Servico pagamento = new Servico("Pagamento de Boletos", "Pagamento de contas e boletos", 2.5, "FINANCEIRO");
        Servico transferencia = new Servico("Transferência PIX", "Transferências via PIX", 1.0, "FINANCEIRO");
        Servico cartao = new Servico("Cartão Pré-pago", "Emissão de cartão pré-pago", 15.0, "DIGITAL");

        servicoRepository.save(recarga);
        servicoRepository.save(pagamento);
        servicoRepository.save(transferencia);
        servicoRepository.save(cartao);

        System.out.println("Serviços carregados com sucesso!");
    }

    private void carregarClientesExemplo() {
        Categoria pessoaFisica = categoriaRepository.findByNome("PESSOA_FISICA").orElse(null);
        Categoria pessoaJuridica = categoriaRepository.findByNome("PESSOA_JURIDICA").orElse(null);

        if (pessoaFisica != null) {
            Cliente cliente1 = new Cliente("João Silva", "12345678901", LocalDate.of(1985, 3, 15));
            cliente1.setEmail("joao.silva@email.com");
            cliente1.setTelefone("11999887766");
            cliente1.setCategoria(pessoaFisica);
            cliente1.setStatusCadastro("COMPLETO");

            Cliente cliente2 = new Cliente("Maria Santos", "98765432100", LocalDate.of(1990, 7, 22));
            cliente2.setEmail("maria.santos@email.com");
            cliente2.setCategoria(pessoaFisica);
            cliente2.setStatusCadastro("INCOMPLETO"); // Sem telefone

            clienteRepository.save(cliente1);
            clienteRepository.save(cliente2);
        }

        if (pessoaJuridica != null) {
            Cliente cliente3 = new Cliente("Empresa ABC Ltda", "12345678000195", LocalDate.of(2010, 1, 1));
            cliente3.setEmail("contato@empresaabc.com");
            cliente3.setTelefone("1133334444");
            cliente3.setCategoria(pessoaJuridica);
            cliente3.setStatusCadastro("COMPLETO");

            clienteRepository.save(cliente3);
        }

        System.out.println("Clientes de exemplo carregados com sucesso!");
    }
}