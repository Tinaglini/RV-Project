# Documentação da API - Sistema RV Digital

## Visão Geral

API REST completa com autenticação JWT, autorização baseada em roles e operações CRUD para gerenciamento de clientes.

**Base URL**: `http://localhost:8080` (desenvolvimento) ou `https://sua-api.com` (produção)

## Autenticação

A API usa JWT (JSON Web Tokens) para autenticação. Todas as rotas protegidas requerem um token válido no header `Authorization`.

### Formato do Header

```
Authorization: Bearer {seu_token_jwt}
```

## Endpoints de Autenticação

### 1. Login

Autentica um usuário e retorna um token JWT.

**Endpoint**: `POST /api/auth/login`

**Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Resposta de Sucesso (200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

**Resposta de Erro (401)**:
```json
{
  "message": "Credenciais inválidas"
}
```

**Exemplo cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. Registro

Registra um novo usuário no sistema.

**Endpoint**: `POST /api/auth/register`

**Body**:
```json
{
  "username": "novouser",
  "email": "novouser@example.com",
  "password": "senha123",
  "roles": ["user"]
}
```

**Resposta de Sucesso (200)**:
```json
{
  "message": "Usuário registrado com sucesso!"
}
```

**Resposta de Erro (400)**:
```json
{
  "message": "Erro: Username já está em uso!"
}
```

**Exemplo cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "novouser",
    "email": "novouser@example.com",
    "password": "senha123",
    "roles": ["user"]
  }'
```

## Endpoints de Usuário

### 3. Obter Usuário Atual

Retorna informações do usuário autenticado.

**Endpoint**: `GET /api/users/me`

**Autenticação**: Requerida

**Headers**:
```
Authorization: Bearer {token}
```

**Resposta de Sucesso (200)**:
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

**Exemplo cURL**:
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### 4. Acesso de Usuário Comum

Endpoint acessível apenas por usuários com ROLE_USER.

**Endpoint**: `GET /api/users/user-access`

**Autenticação**: Requerida (ROLE_USER)

**Resposta de Sucesso (200)**:
```json
{
  "message": "Conteúdo de usuário comum",
  "accessLevel": "USER"
}
```

**Resposta de Erro (403)**:
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 5. Listar Todos os Usuários (Admin)

Lista todos os usuários do sistema. Requer privilégios de administrador.

**Endpoint**: `GET /api/users/admin/all`

**Autenticação**: Requerida (ROLE_ADMIN)

**Resposta de Sucesso (200)**:
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "enabled": true,
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "createdAt": "2025-01-15T10:30:00"
  },
  {
    "id": 2,
    "username": "user",
    "email": "user@example.com",
    "enabled": true,
    "roles": ["ROLE_USER"],
    "createdAt": "2025-01-15T10:31:00"
  }
]
```

**Exemplo cURL**:
```bash
curl -X GET http://localhost:8080/api/users/admin/all \
  -H "Authorization: Bearer TOKEN_DE_ADMIN"
```

### 6. Acesso Administrativo

Endpoint exclusivo para administradores.

**Endpoint**: `GET /api/users/admin-access`

**Autenticação**: Requerida (ROLE_ADMIN)

**Resposta de Sucesso (200)**:
```json
{
  "message": "Conteúdo exclusivo de administrador",
  "accessLevel": "ADMIN"
}
```

### 7. Deletar Usuário (Admin)

Deleta um usuário do sistema. Apenas administradores.

**Endpoint**: `DELETE /api/users/admin/{id}`

**Autenticação**: Requerida (ROLE_ADMIN)

**Parâmetros de URL**:
- `id` (Long): ID do usuário a ser deletado

**Resposta de Sucesso (200)**:
```json
{
  "message": "Usuário deletado com sucesso",
  "userId": "5"
}
```

**Resposta de Erro (404)**:
```json
{
  "error": "Not Found"
}
```

**Exemplo cURL**:
```bash
curl -X DELETE http://localhost:8080/api/users/admin/5 \
  -H "Authorization: Bearer TOKEN_DE_ADMIN"
```

## Roles e Permissões

### ROLE_USER
- Acesso a endpoints básicos
- Visualizar próprio perfil
- Acessar conteúdo de usuário comum

### ROLE_ADMIN
- Todas as permissões de ROLE_USER
- Listar todos os usuários
- Deletar usuários
- Acessar endpoints administrativos

## Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| 200 | Sucesso |
| 201 | Criado com sucesso |
| 400 | Requisição inválida |
| 401 | Não autenticado (token ausente/inválido) |
| 403 | Não autorizado (sem permissão) |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |

## Tratamento de Erros

### Formato de Erro Padrão

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido",
  "path": "/api/users/me"
}
```

## Segurança

### Boas Práticas

1. **Armazene o token de forma segura**:
   - No navegador: Use `localStorage` ou `sessionStorage`
   - Em apps mobile: Use keychain/keystore
   - Nunca armazene em cookies sem HTTPS

2. **Renovação de token**:
   - Tokens expiram em 24 horas (configurável)
   - Implemente lógica de refresh token se necessário

3. **HTTPS em produção**:
   - Sempre use HTTPS em produção
   - Configure certificados SSL/TLS

4. **Validação de entrada**:
   - Todos os endpoints validam entrada
   - Senhas devem ter mínimo 6 caracteres

## Exemplos de Integração

### JavaScript (Fetch API)

```javascript
// Login
async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  });

  const data = await response.json();

  if (response.ok) {
    // Armazenar token
    localStorage.setItem('token', data.token);
    return data;
  } else {
    throw new Error(data.message);
  }
}

// Requisição autenticada
async function getMyProfile() {
  const token = localStorage.getItem('token');

  const response = await fetch('http://localhost:8080/api/users/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  return await response.json();
}
```

### Python (Requests)

```python
import requests

# Login
def login(username, password):
    url = "http://localhost:8080/api/auth/login"
    payload = {
        "username": username,
        "password": password
    }

    response = requests.post(url, json=payload)

    if response.status_code == 200:
        data = response.json()
        return data['token']
    else:
        raise Exception(response.json()['message'])

# Requisição autenticada
def get_my_profile(token):
    url = "http://localhost:8080/api/users/me"
    headers = {
        "Authorization": f"Bearer {token}"
    }

    response = requests.get(url, headers=headers)
    return response.json()

# Uso
token = login("admin", "admin123")
profile = get_my_profile(token)
print(profile)
```

### Java (Spring RestTemplate)

```java
RestTemplate restTemplate = new RestTemplate();

// Login
LoginRequest loginRequest = new LoginRequest("admin", "admin123");
ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/auth/login",
    loginRequest,
    JwtResponse.class
);

String token = response.getBody().getToken();

// Requisição autenticada
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);

HttpEntity<String> entity = new HttpEntity<>(headers);
ResponseEntity<UserProfile> profile = restTemplate.exchange(
    "http://localhost:8080/api/users/me",
    HttpMethod.GET,
    entity,
    UserProfile.class
);
```

## Usuários de Teste

A aplicação vem com dois usuários pré-configurados para testes:

### Usuário Comum
- **Username**: `user`
- **Password**: `user123`
- **Roles**: ROLE_USER
- **Email**: user@example.com

### Administrador
- **Username**: `admin`
- **Password**: `admin123`
- **Roles**: ROLE_USER, ROLE_ADMIN
- **Email**: admin@example.com

## Postman Collection

Importe a collection do Postman para testar todos os endpoints:

[Link para collection] ou use o arquivo `postman_collection.json` no repositório.

## Rate Limiting

Atualmente não há rate limiting implementado. Em produção, considere:
- AWS API Gateway
- Nginx rate limiting
- Spring Cloud Gateway

## Versionamento da API

Versão atual: `v1`

Futuras versões serão acessadas via: `/api/v2/...`

## Suporte

Para questões ou bugs, abra uma issue no GitHub ou entre em contato com a equipe de desenvolvimento.
