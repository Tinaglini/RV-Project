-- Script de verificação dos dados no banco
-- Execute no MySQL Workbench para diagnosticar problemas

USE clientes_rv_db;

-- 1. Verificar se as tabelas foram criadas
SHOW TABLES;

-- 2. Verificar roles
SELECT * FROM roles;

-- 3. Verificar usuários
SELECT id, username, email, enabled FROM users;

-- 4. Verificar relacionamento user_roles (IMPORTANTE!)
SELECT
    ur.user_id,
    u.username,
    ur.role_id,
    r.name as role_name
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
JOIN roles r ON ur.role_id = r.id
ORDER BY u.username, r.name;

-- 5. Verificar se o admin tem as duas roles
SELECT
    u.username,
    GROUP_CONCAT(r.name ORDER BY r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.username;

-- 6. Contar registros
SELECT
    'Usuários' as tabela,
    COUNT(*) as total
FROM users
UNION ALL
SELECT
    'Roles' as tabela,
    COUNT(*) as total
FROM roles
UNION ALL
SELECT
    'User-Roles' as tabela,
    COUNT(*) as total
FROM user_roles;

-- Se user_roles estiver vazia, os dados não foram carregados!
-- Solução: Reinicie a aplicação Spring Boot para executar o DataLoader
