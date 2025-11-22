-- ============================================================================
-- Script de Migração do Banco de Dados
-- Refatoração: Sistema de Vendas → Sistema de Pagamento de Contas
-- ============================================================================
-- ATENÇÃO: Este script vai DROPAR as tabelas existentes e recriar com a nova estrutura
-- Certifique-se de fazer backup dos dados importantes antes de executar!
-- ============================================================================

USE clientes_rv_db;

-- Desabilitar verificação de chaves estrangeiras temporariamente
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- DROPAR TABELAS NA ORDEM CORRETA (dependências primeiro)
-- ============================================================================

DROP TABLE IF EXISTS metodos_pagamento;
DROP TABLE IF EXISTS contratos;
DROP TABLE IF EXISTS servicos;
-- Não dropar clientes, categorias, users, roles pois podem ter dados importantes

-- ============================================================================
-- RECRIAR TABELA: servicos
-- ANTES: Produtos/serviços vendidos com valor
-- DEPOIS: Formas de pagamento (PIX, TED, Boleto) com taxa
-- ============================================================================

CREATE TABLE servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    taxa DOUBLE NOT NULL,
    tipo VARCHAR(50),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    tempo_processamento VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_nome (nome),
    INDEX idx_tipo (tipo),
    INDEX idx_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- RECRIAR TABELA: contratos
-- ANTES: Contratos de serviço com data_inicio, data_fim
-- DEPOIS: Contas a pagar (luz, água, etc) com data_vencimento
-- ============================================================================

CREATE TABLE contratos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(200) NOT NULL,
    valor DOUBLE NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    categoria VARCHAR(50),
    codigo_barras VARCHAR(100),
    data_pagamento DATE,
    observacoes VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    cliente_id BIGINT,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    INDEX idx_data_vencimento (data_vencimento),
    INDEX idx_status (status),
    INDEX idx_categoria (categoria),
    INDEX idx_cliente (cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- RECRIAR TABELA: metodos_pagamento
-- ANTES: Endereços de entrega
-- DEPOIS: Transações de pagamento (registro de pagamentos realizados)
-- ============================================================================

CREATE TABLE metodos_pagamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    valor DOUBLE NOT NULL,
    valor_taxa DOUBLE,
    valor_total DOUBLE,
    data_transacao DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    comprovante TEXT,

    -- Dados específicos de PIX
    chave_pix VARCHAR(100),
    qr_code TEXT,

    -- Dados específicos de TED/Transferência
    banco VARCHAR(100),
    agencia VARCHAR(10),
    conta VARCHAR(20),
    tipo_conta VARCHAR(20),

    -- Dados específicos de Boleto
    codigo_barras VARCHAR(100),
    linha_digitavel VARCHAR(100),

    -- Dados específicos de Cartão
    numero_cartao VARCHAR(20),
    bandeira VARCHAR(30),

    observacoes VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    -- Relacionamentos
    cliente_id BIGINT,
    contrato_id BIGINT,
    servico_id BIGINT,

    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id) ON DELETE CASCADE,
    FOREIGN KEY (servico_id) REFERENCES servicos(id) ON DELETE CASCADE,

    INDEX idx_cliente (cliente_id),
    INDEX idx_contrato (contrato_id),
    INDEX idx_servico (servico_id),
    INDEX idx_status (status),
    INDEX idx_data_transacao (data_transacao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reabilitar verificação de chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- INSERIR DADOS INICIAIS: Formas de Pagamento
-- ============================================================================

INSERT INTO servicos (nome, descricao, taxa, tipo, ativo, tempo_processamento, created_at, updated_at) VALUES
('PIX', 'Pagamento instantâneo via PIX', 0.00, 'TRANSFERENCIA', TRUE, 'Instantâneo', NOW(), NOW()),
('TED', 'Transferência Eletrônica Disponível', 5.90, 'TRANSFERENCIA', TRUE, '1 dia útil', NOW(), NOW()),
('DOC', 'Documento de Ordem de Crédito', 3.50, 'TRANSFERENCIA', TRUE, '1-2 dias úteis', NOW(), NOW()),
('Boleto', 'Pagamento via boleto bancário', 2.50, 'BOLETO', TRUE, '2-3 dias úteis', NOW(), NOW()),
('Cartão de Crédito', 'Pagamento via cartão de crédito', 3.99, 'CARTAO', TRUE, 'Instantâneo', NOW(), NOW()),
('Cartão de Débito', 'Pagamento via cartão de débito', 1.99, 'CARTAO', TRUE, 'Instantâneo', NOW(), NOW()),
('Débito em Conta', 'Débito automático em conta corrente', 0.00, 'DEBITO_CONTA', TRUE, 'No vencimento', NOW(), NOW());

-- ============================================================================
-- VERIFICAÇÃO
-- ============================================================================

SELECT 'Migração concluída com sucesso!' AS status;

-- Verificar tabelas criadas
SHOW TABLES;

-- Verificar estrutura da tabela contratos
DESCRIBE contratos;

-- Verificar estrutura da tabela servicos
DESCRIBE servicos;

-- Verificar estrutura da tabela metodos_pagamento
DESCRIBE metodos_pagamento;

-- Verificar dados inseridos em servicos
SELECT * FROM servicos;
