import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { 
  Cliente, 
  LoginRequest, 
  LoginResponse, 
  AlterarSenhaRequest, 
  VerificacaoCpfEmailRequest, 
  VerificacaoCpfEmailResponse 
} from '../models/cliente.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  
  // URL base da API - ajuste conforme necessário
  private readonly API_URL = 'http://localhost:8080/clientes';

  constructor(private http: HttpClient) { }

  /**
   * Busca todos os clientes
   * @returns Observable<Cliente[]>
   */
  findAll(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.API_URL)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Busca um cliente por ID
   * @param id ID do cliente
   * @returns Observable<Cliente>
   */
  findById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.API_URL}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Salva um novo cliente ou atualiza um existente
   * @param cliente Dados do cliente
   * @returns Observable<Cliente>
   */
  save(cliente: Cliente): Observable<Cliente> {
    if (cliente.id) {
      // Atualização
      return this.http.put<Cliente>(`${this.API_URL}/${cliente.id}`, cliente)
        .pipe(
          catchError(this.handleError)
        );
    } else {
      // Criação
      return this.http.post<Cliente>(this.API_URL, cliente)
        .pipe(
          catchError(this.handleError)
        );
    }
  }

  /**
   * Deleta um cliente por ID
   * @param id ID do cliente
   * @returns Observable<void>
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Busca clientes por nome
   * @param nome Nome para busca
   * @returns Observable<Cliente[]>
   */
  buscarPorNome(nome: string): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.API_URL}/buscar`, {
      params: { nome }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Busca clientes por categoria
   * @param categoriaId ID da categoria
   * @returns Observable<Cliente[]>
   */
  buscarPorCategoria(categoriaId: number): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.API_URL}/categoria/${categoriaId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Busca cliente por CPF
   * @param cpf CPF do cliente
   * @returns Observable<Cliente>
   */
  buscarPorCpf(cpf: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.API_URL}/cpf`, {
      params: { cpf }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Busca cliente por email
   * @param email Email do cliente
   * @returns Observable<Cliente>
   */
  buscarPorEmail(email: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.API_URL}/email`, {
      params: { email }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Realiza login do cliente
   * @param loginRequest Dados de login (CPF/Email e senha)
   * @returns Observable<LoginResponse>
   */
  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Altera senha do cliente
   * @param id ID do cliente
   * @param senhaRequest Dados da alteração de senha
   * @returns Observable<any>
   */
  alterarSenha(id: number, senhaRequest: AlterarSenhaRequest): Observable<any> {
    return this.http.put(`${this.API_URL}/${id}/alterar-senha`, senhaRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Desbloqueia conta do cliente
   * @param id ID do cliente
   * @returns Observable<any>
   */
  desbloquearConta(id: number): Observable<any> {
    return this.http.put(`${this.API_URL}/${id}/desbloquear`, {})
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Lista clientes com contas bloqueadas
   * @returns Observable<Cliente[]>
   */
  listarContasBloqueadas(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.API_URL}/bloqueados`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Verifica se CPF ou email já existem no sistema
   * @param verificacaoRequest Dados para verificação
   * @returns Observable<VerificacaoCpfEmailResponse>
   */
  verificarCpfEmail(verificacaoRequest: VerificacaoCpfEmailRequest): Observable<VerificacaoCpfEmailResponse> {
    return this.http.post<VerificacaoCpfEmailResponse>(`${this.API_URL}/verificar-cpf-email`, verificacaoRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Tratamento de erros HTTP
   * @param error Erro HTTP
   * @returns Observable<never>
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Erro desconhecido';
    
    if (error.error instanceof ErrorEvent) {
      // Erro do lado do cliente
      errorMessage = `Erro: ${error.error.message}`;
    } else {
      // Erro do lado do servidor
      switch (error.status) {
        case 400:
          errorMessage = 'Dados inválidos fornecidos';
          break;
        case 401:
          errorMessage = 'Não autorizado';
          break;
        case 403:
          errorMessage = 'Acesso negado';
          break;
        case 404:
          errorMessage = 'Cliente não encontrado';
          break;
        case 409:
          errorMessage = 'Conflito: CPF ou email já cadastrado';
          break;
        case 500:
          errorMessage = 'Erro interno do servidor';
          break;
        default:
          errorMessage = `Erro ${error.status}: ${error.statusText}`;
      }
    }
    
    console.error('Erro no ClienteService:', error);
    return throwError(() => new Error(errorMessage));
  }
}
