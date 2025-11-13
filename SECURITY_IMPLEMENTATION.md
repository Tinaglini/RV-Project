# Implementação de Segurança com Spring Security e JWT

## Visão Geral

Este projeto implementa autenticação e autorização completas usando Spring Security 6 e JWT (JSON Web Tokens), seguindo as melhores práticas de segurança da indústria.

## Arquitetura de Segurança

### Componentes Principais

```
┌─────────────────┐
│   Cliente       │
│  (Frontend)     │
└────────┬────────┘
         │ 1. POST /login
         │    {username, password}
         ▼
┌─────────────────────────────┐
│   AuthController            │
│   - Valida credenciais      │
│   - Gera JWT token          │
└────────┬────────────────────┘
         │ 2. Token JWT
         ▼
┌─────────────────────────────┐
│   Cliente armazena token    │
└─────────────────────────────┘
         │ 3. GET /api/users/me
         │    Authorization: Bearer {token}
         ▼
┌─────────────────────────────┐
│  JwtAuthenticationFilter    │
│  - Extrai token             │
│  - Valida token             │
│  - Carrega UserDetails      │
└────────┬────────────────────┘
         │ 4. SecurityContext
         ▼
┌─────────────────────────────┐
│   SecurityFilterChain       │
│   - Verifica autorização    │
│   - Permite/Nega acesso     │
└────────┬────────────────────┘
         │ 5. Response
         ▼
┌─────────────────┐
│   Cliente       │
└─────────────────┘
```

## Componentes Implementados

### 1. Entidades

#### User (User.java)
- Implementa `UserDetails` do Spring Security
- Armazena credenciais e informações do usuário
- Relacionamento Many-to-Many com Role
- Senhas criptografadas com BCrypt
- Timestamps automáticos (createdAt, updatedAt)

**Campos**:
- `id`: Identificador único
- `username`: Nome de usuário único
- `email`: Email único
- `password`: Senha criptografada com BCrypt
- `enabled`: Conta ativa/inativa
- `accountNonExpired`: Conta não expirada
- `accountNonLocked`: Conta não bloqueada
- `credentialsNonExpired`: Credenciais não expiradas
- `roles`: Conjunto de roles do usuário

#### Role (Role.java)
- Representa perfis/papéis no sistema
- Relacionamento Many-to-Many com User

**Roles Disponíveis**:
- `ROLE_USER`: Usuário comum
- `ROLE_ADMIN`: Administrador

### 2. Segurança

#### JwtTokenProvider (JwtTokenProvider.java)
Responsável pela geração e validação de tokens JWT.

**Funcionalidades**:
- Gera tokens JWT com claims customizados
- Valida assinatura e expiração
- Extrai informações do token
- Usa secret em Base64 (configurável via environment)

**Configurações**:
```properties
jwt.secret=${JWT_SECRET:defaultSecret}
jwt.expiration=86400000  # 24 horas em millisegundos
```

**Estrutura do Token**:
```json
{
  "sub": "username",
  "iat": 1705315200,
  "exp": 1705401600
}
```

#### JwtAuthenticationFilter (JwtAuthenticationFilter.java)
Filtro que intercepta todas as requisições para validar JWT.

**Fluxo**:
1. Extrai token do header `Authorization: Bearer {token}`
2. Valida token usando JwtTokenProvider
3. Carrega UserDetails do banco
4. Configura SecurityContext com Authentication
5. Permite que a requisição continue

#### UserDetailsServiceImpl (UserDetailsServiceImpl.java)
Implementação customizada para carregar usuários do banco.

**Responsabilidades**:
- Buscar usuário por username
- Converter User em UserDetails
- Lançar UsernameNotFoundException se não encontrado

#### SecurityConfig (SecurityConfig.java)
Configuração central de segurança do Spring Security.

**Configurações**:
- Password encoder (BCrypt)
- Authentication provider
- Security filter chain
- Rotas públicas vs protegidas
- CORS configuration
- Session management (STATELESS)

**Rotas Públicas**:
- `/api/auth/**` - Login e registro
- `/api/public/**` - Conteúdo público
- `/h2-console/**` - Console H2 (apenas dev)
- `/error` - Página de erro

**Rotas Protegidas**:
- Todas as outras rotas requerem autenticação

### 3. Controllers

#### AuthController (AuthController.java)
Gerencia autenticação e registro de usuários.

**Endpoints**:

##### POST /api/auth/login
Autentica usuário e retorna JWT token.

**Validações**:
- Username obrigatório
- Password obrigatório
- Credenciais válidas no banco

**Processo**:
1. Valida credenciais com AuthenticationManager
2. Gera token JWT
3. Retorna token com informações do usuário

##### POST /api/auth/register
Registra novo usuário no sistema.

**Validações**:
- Username único (3-50 caracteres)
- Email único e válido
- Password forte (mínimo 6 caracteres)
- Roles válidas

**Processo**:
1. Verifica username/email duplicados
2. Criptografa senha com BCrypt
3. Atribui roles (padrão: ROLE_USER)
4. Salva usuário no banco

#### UserController (UserController.java)
Endpoints protegidos com autorização baseada em roles.

**Endpoints**:

##### GET /api/users/me
Retorna informações do usuário autenticado.

**Autorização**: `@PreAuthorize("isAuthenticated()")`

##### GET /api/users/user-access
Conteúdo acessível por ROLE_USER.

**Autorização**: `@PreAuthorize("hasRole('ROLE_USER')")`

##### GET /api/users/admin/all
Lista todos os usuários (apenas admin).

**Autorização**: `@PreAuthorize("hasRole('ROLE_ADMIN')")`

##### GET /api/users/admin-access
Conteúdo exclusivo de admin.

**Autorização**: `@PreAuthorize("hasRole('ROLE_ADMIN')")`

##### DELETE /api/users/admin/{id}
Deleta usuário por ID (apenas admin).

**Autorização**: `@PreAuthorize("hasRole('ROLE_ADMIN')")`

## Segurança Implementada

### ✅ Criptografia de Senhas

- **BCryptPasswordEncoder** com strength padrão (10 rounds)
- Senhas NUNCA armazenadas em texto plano
- Cada senha tem salt único automático
- Validação de senha usa hashing seguro

**Exemplo no código**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Ao criar usuário
user.setPassword(passwordEncoder.encode(plainPassword));
```

### ✅ JWT Secret Seguro

- Secret armazenado em `application.properties`
- Suporta variável de ambiente `${JWT_SECRET}`
- Base64 encoded (256+ bits recomendado)
- Nunca hardcoded no código

**Configuração**:
```properties
jwt.secret=${JWT_SECRET:defaultSecretForDevelopment}
```

**Para produção**, defina variável de ambiente:
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

### ✅ Proteção de Rotas

**SecurityFilterChain** configurado para:
- Rotas públicas sem autenticação
- Rotas privadas requerem token válido
- Autorização baseada em roles com `@PreAuthorize`

**Respostas HTTP**:
- `401 Unauthorized`: Token ausente/inválido
- `403 Forbidden`: Token válido mas sem permissão (role)

### ✅ Autorização por Roles

**Implementação**:
```java
@PreAuthorize("hasRole('ROLE_ADMIN')")
@GetMapping("/admin/all")
public ResponseEntity<?> getAllUsers() {
    // Apenas ROLE_ADMIN pode acessar
}
```

**Hierarquia**:
- ROLE_USER: Acesso básico
- ROLE_ADMIN: Acesso total (inclui ROLE_USER)

### ✅ Session Stateless

- Sem sessões server-side
- Cada requisição validada independentemente
- Escalabilidade horizontal facilitada
- Token carrega todas as informações necessárias

**Configuração**:
```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

### ✅ CORS Configurado

- Permite requisições de origens específicas
- Headers de Authorization permitidos
- Métodos HTTP configurados
- Credentials habilitados para desenvolvimento

**Origens permitidas**:
- `http://localhost:*`
- `https://*.amazonaws.com`
- `https://*.cloudfront.net`

## Fluxo de Autenticação Detalhado

### 1. Login (Primeira requisição)

```
Cliente                  Backend
   │                        │
   │  POST /api/auth/login  │
   ├───────────────────────>│
   │  {username, password}  │
   │                        │
   │                        │  AuthenticationManager
   │                        │  valida credenciais
   │                        │
   │                        │  BCrypt compara
   │                        │  password hash
   │                        │
   │                        │  JwtTokenProvider
   │                        │  gera token
   │                        │
   │  {token, user info}    │
   │<───────────────────────┤
   │                        │
   │  Armazena token        │
   │  (localStorage)        │
   │                        │
```

### 2. Requisição Autenticada

```
Cliente                  Backend
   │                        │
   │  GET /api/users/me     │
   ├───────────────────────>│
   │  Authorization: Bearer │
   │         {token}        │
   │                        │
   │                        │  JwtAuthenticationFilter
   │                        │  extrai token
   │                        │
   │                        │  JwtTokenProvider
   │                        │  valida token
   │                        │
   │                        │  UserDetailsService
   │                        │  carrega user
   │                        │
   │                        │  SecurityContext
   │                        │  set authentication
   │                        │
   │                        │  @PreAuthorize
   │                        │  verifica role
   │                        │
   │  {user data}           │
   │<───────────────────────┤
   │                        │
```

## Dados de Teste

### Usuários Pré-configurados

Os usuários abaixo são criados automaticamente no primeiro startup:

#### Usuário Comum
```json
{
  "username": "user",
  "password": "user123",
  "email": "user@example.com",
  "roles": ["ROLE_USER"]
}
```

#### Administrador
```json
{
  "username": "admin",
  "password": "admin123",
  "email": "admin@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

## Testes de Segurança

### Cenários de Teste

#### ✅ Teste 1: Login Bem-sucedido
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Esperado**: Token JWT retornado com status 200

#### ✅ Teste 2: Login com Credenciais Inválidas
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "senhaerrada"}'
```

**Esperado**: Status 401 Unauthorized

#### ✅ Teste 3: Acesso Sem Token
```bash
curl -X GET http://localhost:8080/api/users/me
```

**Esperado**: Status 403 Forbidden

#### ✅ Teste 4: Acesso Com Token Válido
```bash
TOKEN="seu_token_aqui"
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado**: Dados do usuário com status 200

#### ✅ Teste 5: User Tentando Acesso Admin
```bash
# Login como user
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}' \
  | jq -r '.token')

# Tentar acessar endpoint admin
curl -X GET http://localhost:8080/api/users/admin/all \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado**: Status 403 Forbidden

#### ✅ Teste 6: Admin Acessando Endpoint Admin
```bash
# Login como admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' \
  | jq -r '.token')

# Acessar endpoint admin
curl -X GET http://localhost:8080/api/users/admin/all \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado**: Lista de usuários com status 200

## Checklist de Segurança

### Backend

- [x] Senhas criptografadas com BCrypt no banco
- [x] JWT secret em arquivo de configuração (não hardcoded)
- [x] Endpoint `/login` retornando token JWT válido
- [x] Filtro JWT validando tokens em todas requisições protegidas
- [x] SecurityFilterChain bloqueando acesso sem token válido
- [x] Autorização por roles com @PreAuthorize implementada
- [x] Retornos HTTP corretos (401 para auth, 403 para authorization)
- [x] CORS configurado corretamente
- [x] Session management STATELESS
- [x] UserDetailsService customizado
- [x] Variáveis de ambiente para secrets
- [x] Validação de entrada em DTOs
- [x] Tratamento global de exceções

### Deployment

- [x] Dockerfile otimizado com multi-stage build
- [x] Docker Compose para desenvolvimento
- [x] Documentação AWS deployment
- [x] .env.example com variáveis necessárias
- [x] .gitignore protegendo secrets
- [x] Health checks configurados

## Melhorias Futuras

### Sugeridas

1. **Refresh Token**: Implementar refresh token para melhor UX
2. **Rate Limiting**: Limitar tentativas de login
3. **Two-Factor Auth**: Adicionar 2FA opcional
4. **Password Policy**: Regras mais rígidas de senha
5. **Account Lockout**: Bloquear conta após X tentativas
6. **Audit Logging**: Log de todas ações sensíveis
7. **Email Verification**: Verificar email no registro
8. **Password Reset**: Funcionalidade de recuperação de senha
9. **Remember Me**: Token de longa duração opcional
10. **OAuth2**: Integração com Google/Facebook

## Referências

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Spring Boot Security Best Practices](https://spring.io/guides/topicals/spring-security-architecture/)
