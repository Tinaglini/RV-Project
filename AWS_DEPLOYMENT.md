# Guia de Deployment na AWS

Este guia fornece instruções detalhadas para fazer o deploy da aplicação Spring Boot com JWT na AWS.

## Opções de Deployment

### Opção 1: AWS Elastic Beanstalk (Recomendado para iniciantes)

#### Pré-requisitos
- Conta AWS ativa
- AWS CLI instalado e configurado
- EB CLI instalado (`pip install awsebcli`)

#### Passos

1. **Inicializar Elastic Beanstalk**
```bash
eb init -p docker sistema-clientes-rv --region us-east-1
```

2. **Criar arquivo .ebextensions/01_environment.config**
```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
    JWT_SECRET: seu_jwt_secret_em_base64
    JWT_EXPIRATION: 86400000
    SPRING_DATASOURCE_URL: jdbc:mysql://seu-rds-endpoint:3306/clientes_rv_db
    SPRING_DATASOURCE_USERNAME: admin
    SPRING_DATASOURCE_PASSWORD: sua_senha_segura
```

3. **Criar ambiente e fazer deploy**
```bash
eb create rv-production-env
eb deploy
```

4. **Verificar aplicação**
```bash
eb open
```

### Opção 2: AWS EC2 com Docker

#### 1. Criar instância EC2

- AMI: Amazon Linux 2
- Tipo: t2.small ou superior
- Security Group: Permitir portas 22 (SSH), 80 (HTTP), 443 (HTTPS), 8080 (App)

#### 2. Conectar via SSH e instalar Docker

```bash
ssh -i sua-chave.pem ec2-user@seu-ip-publico

# Atualizar sistema
sudo yum update -y

# Instalar Docker
sudo yum install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### 3. Clonar repositório e fazer deploy

```bash
git clone https://github.com/seu-usuario/RV-Project.git
cd RV-Project

# Criar arquivo .env com variáveis de ambiente
cat > .env << EOF
DB_NAME=clientes_rv_db
DB_USER=rvuser
DB_PASSWORD=SuaSenhaSegura123!
JWT_SECRET=$(echo -n "seu-secret-muito-seguro-aqui" | base64)
JWT_EXPIRATION=86400000
EOF

# Iniciar aplicação
docker-compose up -d
```

#### 4. Configurar Nginx como proxy reverso (opcional mas recomendado)

```bash
sudo yum install nginx -y

# Configurar Nginx
sudo tee /etc/nginx/conf.d/rv-app.conf << EOF
server {
    listen 80;
    server_name seu-dominio.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

sudo systemctl start nginx
sudo systemctl enable nginx
```

### Opção 3: AWS RDS + EC2

#### 1. Criar banco de dados RDS

1. Acesse AWS Console > RDS
2. Criar banco de dados MySQL 8.0
3. Configurações:
   - DB Instance: db.t3.micro
   - Storage: 20 GB
   - Username: admin
   - Password: sua_senha_segura
   - Security Group: Permitir porta 3306 do Security Group da EC2

#### 2. Atualizar application.properties

```properties
spring.datasource.url=jdbc:mysql://seu-rds-endpoint.region.rds.amazonaws.com:3306/clientes_rv_db
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}
```

#### 3. Deploy na EC2

Seguir os mesmos passos da Opção 2, mas usando o RDS em vez do MySQL local.

## Configuração de Variáveis de Ambiente

### Obrigatórias

- `JWT_SECRET`: Secret em Base64 para assinatura JWT
- `SPRING_DATASOURCE_URL`: URL de conexão com banco de dados
- `SPRING_DATASOURCE_USERNAME`: Usuário do banco
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco

### Opcionais

- `JWT_EXPIRATION`: Tempo de expiração do token (padrão: 24 horas)
- `SERVER_PORT`: Porta da aplicação (padrão: 8080)
- `SPRING_PROFILES_ACTIVE`: Perfil ativo (prod/dev)

## Gerar JWT Secret Seguro

```bash
# Linux/Mac
echo -n "seu-texto-muito-seguro-com-mais-de-32-caracteres" | base64

# Ou usar OpenSSL
openssl rand -base64 64
```

## Configurar HTTPS com Let's Encrypt

```bash
# Instalar Certbot
sudo yum install certbot python3-certbot-nginx -y

# Obter certificado SSL
sudo certbot --nginx -d seu-dominio.com

# Renovar automaticamente
sudo crontab -e
# Adicionar: 0 0 * * * certbot renew --quiet
```

## Monitoramento e Logs

### CloudWatch Logs (Elastic Beanstalk)

```bash
eb logs --all
```

### Logs Docker (EC2)

```bash
docker-compose logs -f backend
```

### Métricas

Configure CloudWatch Alarms para:
- CPU > 80%
- Memory > 80%
- 5xx errors > 10
- Request latency > 1s

## Backup do Banco de Dados

### RDS Automático
- Habilitar backups automáticos (7-35 dias de retenção)
- Configurar snapshot manual antes de atualizações

### MySQL Manual

```bash
# Backup
docker exec rv-mysql mysqldump -u root -p clientes_rv_db > backup.sql

# Restore
docker exec -i rv-mysql mysql -u root -p clientes_rv_db < backup.sql
```

## Testes de Endpoint

### Login

```bash
curl -X POST https://sua-api.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Endpoint Protegido

```bash
curl -X GET https://sua-api.com/api/users/me \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

### Endpoint Admin

```bash
curl -X GET https://sua-api.com/api/users/admin/all \
  -H "Authorization: Bearer TOKEN_DE_ADMIN"
```

## Troubleshooting

### Erro de conexão com banco

```bash
# Verificar conectividade
telnet seu-rds-endpoint.rds.amazonaws.com 3306

# Verificar Security Groups
# Certifique-se que o SG do RDS permite conexões do SG da EC2
```

### Aplicação não inicia

```bash
# Ver logs detalhados
docker-compose logs backend

# Verificar variáveis de ambiente
docker-compose config
```

### Token JWT inválido

- Verificar se JWT_SECRET está configurado corretamente
- Verificar se o secret é o mesmo em todas as instâncias
- Verificar relógio do servidor (sincronização NTP)

## Segurança

### Checklist de Segurança

- [ ] JWT Secret forte (64+ caracteres base64)
- [ ] HTTPS habilitado
- [ ] Senhas de banco fortes
- [ ] Security Groups restritivos
- [ ] Backups automáticos habilitados
- [ ] CloudWatch Logs habilitado
- [ ] Atualizações de segurança automáticas
- [ ] IAM roles com permissões mínimas
- [ ] Secrets Manager para credenciais (recomendado)

### Usar AWS Secrets Manager

```bash
# Criar secret
aws secretsmanager create-secret \
  --name rv-app-secrets \
  --secret-string '{"db_password":"senha","jwt_secret":"secret"}'

# Recuperar no código
// Implementar no application.properties ou via SDK
```

## Custos Estimados (us-east-1)

- **EC2 t2.small**: ~$17/mês
- **RDS db.t3.micro**: ~$15/mês
- **Data Transfer**: ~$5/mês
- **Load Balancer** (opcional): ~$20/mês
- **Total estimado**: $37-57/mês

## Próximos Passos

1. Configurar CI/CD com GitHub Actions
2. Implementar Auto Scaling
3. Configurar Load Balancer
4. Implementar cache com Redis/ElastiCache
5. Configurar CDN com CloudFront
