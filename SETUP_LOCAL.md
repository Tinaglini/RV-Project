# Setup Local - Guia de Instalação

## Pré-requisitos

- ✅ Java 17 ou superior
- ✅ Maven 3.6+
- ✅ MySQL 8.0 instalado e rodando
- ✅ IntelliJ IDEA (ou outra IDE)

## Passo 1: Verificar MySQL

### Windows

1. Abra o **MySQL Workbench** ou **XAMPP Control Panel**
2. Certifique-se que o serviço MySQL está **rodando**
3. Se usar XAMPP, clique em **Start** no módulo MySQL

### Verificar se MySQL está ativo

Você pode verificar se o MySQL está rodando abrindo o Prompt de Comando e digitando:

```cmd
mysql -u root -p
```

Digite a senha: `Wellsny321@`

Se conectar com sucesso, o MySQL está funcionando!

## Passo 2: Criar o Banco de Dados

### Opção A: MySQL Workbench (Recomendado)

1. Abra o **MySQL Workbench**
2. Conecte com suas credenciais:
   - Host: `localhost`
   - Port: `3306`
   - Username: `root`
   - Password: `Wellsny321@`

3. Abra o arquivo `database_setup.sql` (na raiz do projeto)
4. Execute o script (botão de raio ⚡ ou Ctrl+Shift+Enter)

### Opção B: Linha de Comando

```bash
# Windows Command Prompt
mysql -u root -p < database_setup.sql
# Digite a senha: Wellsny321@
```

### Opção C: Criar manualmente

No MySQL Workbench ou console, execute:

```sql
CREATE DATABASE IF NOT EXISTS clientes_rv_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

## Passo 3: Configuração já está pronta!

O arquivo `application.properties` já está configurado com suas credenciais:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clientes_rv_db
spring.datasource.username=root
spring.datasource.password=Wellsny321@
```

## Passo 4: Executar a Aplicação no IntelliJ

### Método 1: Botão Run (Mais fácil)

1. Abra o projeto no IntelliJ IDEA
2. Localize a classe `ClientesApplication.java` em:
   ```
   src/main/java/app/sistemaclientesrv/ClientesApplication.java
   ```
3. Clique com botão direito → **Run 'ClientesApplication'**
4. Aguarde a aplicação iniciar

### Método 2: Maven (Terminal IntelliJ)

1. Abra o Terminal no IntelliJ (Alt+F12)
2. Execute:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```

### Verificar se Iniciou Corretamente

Quando a aplicação iniciar com sucesso, você verá no console:

```
✓ Roles carregadas: ROLE_USER, ROLE_ADMIN
✓ Usuários de teste criados:
  - Username: user | Password: user123 | Roles: ROLE_USER
  - Username: admin | Password: admin123 | Roles: ROLE_USER, ROLE_ADMIN
Categorias carregadas com sucesso!
Serviços carregados com sucesso!
Clientes de exemplo carregados com sucesso!

Started ClientesApplication in X.XXX seconds
```

## Passo 5: Testar a API

### Teste 1: Verificar se está no ar

Abra o navegador e acesse:
```
http://localhost:8080
```

Se aparecer uma página de erro, está funcionando! (Porque não há rota na raiz)

### Teste 2: Login (Recomendado)

**No Terminal do IntelliJ ou PowerShell:**

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\": \"admin\", \"password\": \"admin123\"}"
```

**Resposta Esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

### Teste 3: Endpoint Protegido

Copie o token recebido e substitua em `<TOKEN>`:

```bash
curl -X GET http://localhost:8080/api/users/me -H "Authorization: Bearer <TOKEN>"
```

### Teste 4: Postman (Mais fácil)

1. Abra o **Postman**
2. Importe o arquivo `postman_collection.json` (na raiz do projeto)
3. Clique em **Import** → Selecione o arquivo
4. Execute a requisição **"Login - Admin"**
5. O token será salvo automaticamente
6. Teste os outros endpoints

## Passo 6: Verificar o Banco de Dados

Após a aplicação iniciar, verifique as tabelas criadas no MySQL Workbench:

```sql
USE clientes_rv_db;
SHOW TABLES;

-- Deve mostrar:
-- - users
-- - roles
-- - user_roles
-- - clientes
-- - categorias
-- - servicos
-- - contratos
-- - enderecos
-- - itens
```

### Verificar usuários criados

```sql
SELECT u.id, u.username, u.email, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

**Resultado esperado:**
| id | username | email | role |
|----|----------|-------|------|
| 1 | user | user@example.com | ROLE_USER |
| 2 | admin | admin@example.com | ROLE_USER |
| 2 | admin | admin@example.com | ROLE_ADMIN |

## Troubleshooting (Problemas Comuns)

### Erro: "Access denied for user 'root'@'localhost'"

**Causa:** Senha incorreta ou usuário não tem permissões

**Solução:**
1. Verifique a senha no MySQL Workbench
2. Se necessário, redefina a senha do root no MySQL

### Erro: "Communications link failure"

**Causa:** MySQL não está rodando

**Solução:**
1. Inicie o MySQL via XAMPP ou Windows Services
2. Verifique se a porta 3306 está livre:
   ```cmd
   netstat -an | findstr 3306
   ```

### Erro: "Unknown database 'clientes_rv_db'"

**Causa:** Banco de dados não foi criado

**Solução:**
Execute o script `database_setup.sql` conforme Passo 2

### Erro: "Port 8080 already in use"

**Causa:** Outra aplicação está usando a porta 8080

**Solução 1:** Pare a aplicação que está usando a porta

**Solução 2:** Mude a porta no `application.properties`:
```properties
server.port=8081
```

### Erro CORS ao testar com curl/Postman

**Causa:** Configuração CORS

**Solução:** Já está corrigido! Apenas reinicie a aplicação.

## Próximos Passos

Após a aplicação estar rodando:

1. ✅ Teste todos os endpoints com Postman
2. ✅ Leia a documentação da API: `API_DOCUMENTATION.md`
3. ✅ Configure o front-end para usar a API
4. ✅ Quando estiver pronto, faça deploy na AWS: `AWS_DEPLOYMENT.md`

## Usuários de Teste Disponíveis

### Usuário Comum
```
Username: user
Password: user123
Email: user@example.com
Roles: ROLE_USER
```

### Administrador
```
Username: admin
Password: admin123
Email: admin@example.com
Roles: ROLE_USER, ROLE_ADMIN
```

## Endpoints Principais

### Autenticação (Público)
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registro

### Usuário (Requer Auth)
- `GET /api/users/me` - Dados do usuário logado
- `GET /api/users/user-access` - Acesso USER

### Admin (Requer ROLE_ADMIN)
- `GET /api/users/admin/all` - Listar usuários
- `DELETE /api/users/admin/{id}` - Deletar usuário

## Suporte

Se tiver problemas:
1. Verifique os logs no console do IntelliJ
2. Consulte `SECURITY_IMPLEMENTATION.md` para detalhes técnicos
3. Veja `API_DOCUMENTATION.md` para documentação completa

---

🎉 **Pronto! Sua aplicação Spring Boot com JWT está rodando!**
