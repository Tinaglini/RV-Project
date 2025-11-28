-- Script de inicialização do banco de dados MySQL
-- Execute este script no MySQL Workbench ou linha de comando MySQL

-- Criar banco de dados se não existir
CREATE DATABASE IF NOT EXISTS clientes_rv_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Usar o banco de dados
USE clientes_rv_db;

-- Verificar se o banco foi criado corretamente
SELECT 'Banco de dados clientes_rv_db criado/verificado com sucesso!' AS status;

-- Mostrar tabelas existentes (será vazio na primeira execução)
SHOW TABLES;

-- O Hibernate irá criar as tabelas automaticamente quando a aplicação iniciar
-- devido à configuração: spring.jpa.hibernate.ddl-auto=update
