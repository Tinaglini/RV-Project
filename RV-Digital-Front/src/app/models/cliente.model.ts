export interface Cliente {
  id?: number;
  nome: string;
  cpf: string;
  email?: string;
  telefone?: string;
  dataNascimento?: string; // ISO date string
  senha?: string; // Apenas para criação/atualização
  ultimoLogin?: string; // ISO datetime string
  tentativasLogin?: number;
  contaBloqueada?: boolean;
  ativo?: boolean;
  statusCadastro?: string;
  createdAt?: string; // ISO datetime string
  updatedAt?: string; // ISO datetime string
  categoria?: {
    id: number;
    nome: string;
  };
  enderecos?: Endereco[];
  contratos?: Contrato[];
}

export interface Endereco {
  id?: number;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  cliente?: Cliente;
}

export interface Contrato {
  id?: number;
  numero: string;
  dataInicio: string; // ISO date string
  dataFim?: string; // ISO date string
  valor: number;
  status: string;
  cliente?: Cliente;
}

export interface LoginRequest {
  cpfOuEmail: string;
  senha: string;
}

export interface LoginResponse {
  sucesso: boolean;
  mensagem: string;
  cliente: Cliente;
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}

export interface VerificacaoCpfEmailRequest {
  cpfOuEmail: string;
}

export interface VerificacaoCpfEmailResponse {
  existe: boolean;
  clienteId?: number;
  nome?: string;
}
