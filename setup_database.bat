@echo off
REM Script para criar o banco de dados MySQL no Windows
REM Execute este arquivo clicando duas vezes ou via prompt de comando

echo ========================================
echo Setup do Banco de Dados MySQL
echo ========================================
echo.

REM Verificar se o MySQL está instalado
where mysql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] MySQL nao encontrado no PATH!
    echo.
    echo Por favor:
    echo 1. Use o MySQL Workbench para executar database_setup.sql OU
    echo 2. Adicione o MySQL ao PATH do Windows
    echo.
    echo Caminho comum do MySQL:
    echo C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo ou
    echo C:\xampp\mysql\bin
    pause
    exit /b 1
)

echo MySQL encontrado!
echo.
echo Executando script de criacao do banco de dados...
echo.

REM Executar o script SQL
mysql -u root -pWellsny321@ < database_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCESSO] Banco de dados criado com sucesso!
    echo.
    echo Proximo passo: Execute a aplicacao no IntelliJ
) else (
    echo.
    echo [ERRO] Falha ao criar banco de dados!
    echo.
    echo Verifique:
    echo 1. MySQL esta rodando
    echo 2. Senha esta correta: Wellsny321@
    echo 3. Use MySQL Workbench como alternativa
)

echo.
pause
