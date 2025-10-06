import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ClienteService } from './cliente.service';
import { Cliente } from '../models/cliente.model';

// Definição de dados mock (fictícios) para os testes
const mockClientes: Cliente[] = [
  { id: 1, nome: 'Cliente A', cpf: '111.111.111-11' },
  { id: 2, nome: 'Cliente B', cpf: '222.222.222-22' }
];

const API_URL = 'http://localhost:8080/clientes';

describe('ClienteService', () => {
  let service: ClienteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClienteService]
    });
    service = TestBed.inject(ClienteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verifica se não há requisições pendentes
    httpMock.verify();
  });

  // --- Teste do método findAll ---
  it('deve retornar a lista de clientes (findAll)', () => {
    service.findAll().subscribe(clientes => {
      // Verifica se o resultado do Service é o mockClientes
      expect(clientes.length).toBe(2);
      expect(clientes).toEqual(mockClientes);
    });

    // Espera uma requisição GET para a URL da API
    const req = httpMock.expectOne(`${API_URL}`);
    expect(req.request.method).toBe('GET');
    
    // Fornece a resposta mockada para a requisição
    req.flush(mockClientes);
  });

  // --- Teste do método findById ---
  it('deve retornar um cliente por ID (findById)', () => {
    const clienteId = 1;
    const clienteUnico = mockClientes.find(c => c.id === clienteId);

    service.findById(clienteId).subscribe(cliente => {
      expect(cliente).toEqual(clienteUnico);
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteId}`);
    expect(req.request.method).toBe('GET');
    req.flush(clienteUnico);
  });

  // --- Teste de erro para findById ---
  it('deve tratar o erro ao buscar por ID inexistente', () => {
    const clienteId = 99;
    service.findById(clienteId).subscribe({
      next: () => fail('Deveria ter falhado com 404'),
      error: (error) => {
        expect(error.status).toBe(404);
        expect(error.statusText).toContain('Not Found');
      }
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteId}`);
    req.error(new ErrorEvent('Not Found'), { status: 404, statusText: 'Not Found' });
  });

  // --- Teste do método save (criação) ---
  it('deve salvar um novo cliente (save) e retornar o objeto criado', () => {
    const novoCliente: Cliente = { id: undefined, nome: 'Cliente Novo', cpf: '333.333.333-33' };
    const clienteSalvo: Cliente = { id: 3, ...novoCliente };

    service.save(novoCliente).subscribe(cliente => {
      expect(cliente).toEqual(clienteSalvo);
    });

    const req = httpMock.expectOne(`${API_URL}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(novoCliente);
    req.flush(clienteSalvo);
  });

  // --- Teste do método save (atualização) ---
  it('deve atualizar um cliente existente (save)', () => {
    const clienteAtualizado: Cliente = { id: 1, nome: 'Cliente A Atualizado', cpf: '111.111.111-11' };

    service.save(clienteAtualizado).subscribe(cliente => {
      expect(cliente).toEqual(clienteAtualizado);
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteAtualizado.id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(clienteAtualizado);
    req.flush(clienteAtualizado);
  });

  // --- Teste do método delete ---
  it('deve enviar a requisição DELETE corretamente (delete)', () => {
    const clienteId = 1;

    service.delete(clienteId).subscribe(response => {
      expect(response).toBeUndefined(); // DELETE retorna void
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  // --- Teste do método buscarPorNome ---
  it('deve buscar clientes por nome', () => {
    const nome = 'Cliente A';
    const clientesEncontrados = mockClientes.filter(c => c.nome.includes(nome));

    service.buscarPorNome(nome).subscribe(clientes => {
      expect(clientes).toEqual(clientesEncontrados);
    });

    const req = httpMock.expectOne(`${API_URL}/buscar?nome=${nome}`);
    expect(req.request.method).toBe('GET');
    req.flush(clientesEncontrados);
  });

  // --- Teste do método buscarPorCpf ---
  it('deve buscar cliente por CPF', () => {
    const cpf = '111.111.111-11';
    const clienteEncontrado = mockClientes.find(c => c.cpf === cpf);

    service.buscarPorCpf(cpf).subscribe(cliente => {
      expect(cliente).toEqual(clienteEncontrado);
    });

    const req = httpMock.expectOne(`${API_URL}/cpf?cpf=${cpf}`);
    expect(req.request.method).toBe('GET');
    req.flush(clienteEncontrado);
  });

  // --- Teste do método login ---
  it('deve realizar login do cliente', () => {
    const loginRequest = { cpfOuEmail: '111.111.111-11', senha: 'senha123' };
    const loginResponse = {
      sucesso: true,
      mensagem: 'Login realizado com sucesso',
      cliente: mockClientes[0]
    };

    service.login(loginRequest).subscribe(response => {
      expect(response).toEqual(loginResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(loginResponse);
  });

  // --- Teste do método alterarSenha ---
  it('deve alterar senha do cliente', () => {
    const clienteId = 1;
    const senhaRequest = { senhaAtual: 'senha123', novaSenha: 'novaSenha456' };
    const response = { sucesso: true, mensagem: 'Senha alterada com sucesso' };

    service.alterarSenha(clienteId, senhaRequest).subscribe(resp => {
      expect(resp).toEqual(response);
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteId}/alterar-senha`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(senhaRequest);
    req.flush(response);
  });

  // --- Teste do método desbloquearConta ---
  it('deve desbloquear conta do cliente', () => {
    const clienteId = 1;
    const response = { sucesso: true, mensagem: 'Conta desbloqueada com sucesso' };

    service.desbloquearConta(clienteId).subscribe(resp => {
      expect(resp).toEqual(response);
    });

    const req = httpMock.expectOne(`${API_URL}/${clienteId}/desbloquear`);
    expect(req.request.method).toBe('PUT');
    req.flush(response);
  });

  // --- Teste do método listarContasBloqueadas ---
  it('deve listar contas bloqueadas', () => {
    const contasBloqueadas = mockClientes.filter(c => c.contaBloqueada);

    service.listarContasBloqueadas().subscribe(clientes => {
      expect(clientes).toEqual(contasBloqueadas);
    });

    const req = httpMock.expectOne(`${API_URL}/bloqueados`);
    expect(req.request.method).toBe('GET');
    req.flush(contasBloqueadas);
  });

  // --- Teste do método verificarCpfEmail ---
  it('deve verificar se CPF ou email existem', () => {
    const verificacaoRequest = { cpfOuEmail: '111.111.111-11' };
    const response = { existe: true, clienteId: 1, nome: 'Cliente A' };

    service.verificarCpfEmail(verificacaoRequest).subscribe(resp => {
      expect(resp).toEqual(response);
    });

    const req = httpMock.expectOne(`${API_URL}/verificar-cpf-email`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(verificacaoRequest);
    req.flush(response);
  });

  // --- Teste de tratamento de erro ---
  it('deve tratar erro 500 do servidor', () => {
    service.findAll().subscribe({
      next: () => fail('Deveria ter falhado com erro 500'),
      error: (error) => {
        expect(error.message).toContain('Erro interno do servidor');
      }
    });

    const req = httpMock.expectOne(`${API_URL}`);
    req.error(new ErrorEvent('Server Error'), { status: 500, statusText: 'Internal Server Error' });
  });
});
