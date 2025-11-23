Setup inicial – Keycloak / API stonemark

Este ficheiro documenta os passos feitos neste branch para:

garantir que a API corre em local (Java + Postgres),

preparar o projeto para futura integração com Keycloak (dependências de OAuth2),

registar os comandos principais usados durante o processo.

Neste documento estão descritos:

Ambiente Java (JDK 21)

Base de dados PostgreSQL

Ficheiro .env e configuração de credenciais

Alterações no projeto relacionadas com Keycloak / Security

Como arrancar a API em ambiente local

Comandos usados (resumo)

Estado atual do setup / Nota final

1. Ambiente Java (JDK 21)

Foi instalado o JDK 21 (por exemplo em C:\Programas\Java\jdk-21) e configuradas as variáveis de ambiente no Windows para o sistema usar esta versão.

Foram feitos os seguintes passos:

Definição da variável de ambiente JAVA_HOME a apontar para a pasta do JDK:

C:\Programas\Java\jdk-21


Adição de uma entrada à variável Path:

%JAVA_HOME%\bin


Para verificar se a configuração ficou correta, foram usados no terminal (cmd/PowerShell):

echo %JAVA_HOME%
java -version
javac -version


O objetivo é garantir que:

JAVA_HOME aponta para o JDK 21;

java e javac reportam a versão 21 (e não a 1.8).

2. Base de dados PostgreSQL

A API utiliza PostgreSQL 16 como base de dados. No pgAdmin foi confirmada ou criada a base de dados stonemark através de:

Servers → PostgreSQL 16 → Databases → Create → Database… (nome: stonemark)

Durante o processo, a aplicação falhava com o erro:

FATAL: password authentication failed for user "postgres"


o que indicava que a password usada pela aplicação não correspondia à password real do utilizador postgres. Para corrigir, foi aberta a Query Tool numa das bases de dados (postgres ou stonemark) e executado o comando:

ALTER USER postgres WITH PASSWORD 'SUA_PASSWORD_AQUI';


ficando assim definida uma nova password conhecida, a usar depois no ficheiro .env da aplicação.

3. Ficheiro .env e configuração de credenciais

Para evitar expor credenciais no código fonte, foi criado/atualizado um ficheiro .env na raiz do projeto, contendo as variáveis usadas pela aplicação para se ligar à base de dados PostgreSQL.

Conteúdo relevante:

DB_USERNAME=postgres
DB_PASSWORD=SUA_PASSWORD_AQUI


DB_USERNAME e DB_PASSWORD são lidos pela aplicação através da biblioteca spring-dotenv.

Estes valores são usados em spring.datasource.username e spring.datasource.password no application.yml.

O ficheiro .env foi também colocado no .gitignore para garantir que não é incluído nos commits e que as credenciais permanecem apenas em ambiente local.
Exemplo de entrada no .gitignore:

.env

4. Alterações no projeto relacionadas com Keycloak / Security

Para preparar a API stonemark para futura integração com Keycloak, foi necessário ajustar as dependências de segurança no pom.xml.

Além das dependências já existentes, nomeadamente:

spring-boot-starter-security

spring-boot-starter-oauth2-client

foi adicionada a dependência de OAuth2 Resource Server, necessária para validar tokens JWT emitidos por um Identity Provider (como o Keycloak):

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>


Esta dependência foi colocada dentro da secção <dependencies>, logo após:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>


Neste branch foi feito apenas o setup técnico (dependências e ambiente) para suportar Keycloak.
A configuração de segurança concreta — como criação de realm/client no Keycloak, definição de scopes/roles e adaptação do SecurityConfig para usar oauth2ResourceServer().jwt() — será feita numa fase posterior.

5. Como arrancar a API em ambiente local

Antes de correr a API, é necessário garantir:

JDK 21 instalado e configurado (JAVA_HOME e Path atualizados);

Servidor PostgreSQL a correr;

Base de dados stonemark criada no PostgreSQL;

Ficheiro .env com DB_USERNAME e DB_PASSWORD válidos (compatíveis com o utilizador postgres).

5.1 Arranque via Maven Wrapper

Na pasta do projeto (stonemark-api), a aplicação pode ser arrancada com:

mvnw spring-boot:run

5.2 Arranque via IntelliJ IDEA

Em alternativa, no IntelliJ IDEA:

Abrir o projeto stonemark-api.

Confirmar que o Project SDK está definido para o JDK 21.

Correr a classe principal:

pt.estga.stonemark.StonemarkApplication


Com os pré-requisitos cumpridos, a aplicação arranca sem erros críticos (podem surgir apenas alguns WARN normais de arranque/logging).

6. Comandos usados (resumo)

Durante o setup foram usados vários comandos para testar ambiente, correr a aplicação e gerir o repositório git. Os principais foram:

# Ver variáveis e versões de Java
echo %JAVA_HOME%
java -version
javac -version

# Navegar para a pasta do projeto
cd C:\Users\74ped\IdeaProjects\stonemark-api

# Correr a aplicação com Maven Wrapper
mvnw spring-boot:run

# Git – verificar estado do repositório
git status

# Git – preparar e registar alterações
git add pom.xml keycloack.md
git commit -m "Add OAuth2 resource server dependency and document Keycloak setup"

# Git – enviar o branch para o remoto
git push -u origin feature/keycloak-setup

7. Estado atual do setup / Nota final

Com os passos anteriores:

o ambiente local está configurado com JDK 21 e PostgreSQL 16;

a base de dados stonemark está acessível com as credenciais definidas no .env;

o projeto inclui as dependências necessárias para trabalhar com OAuth2:

spring-boot-starter-oauth2-client

spring-boot-starter-oauth2-resource-server.

A API arranca corretamente em local.
A partir deste ponto, o próximo trabalho será:

configurar o Keycloak (realm, client, scopes/roles);

adaptar a configuração de segurança da API (por exemplo o SecurityConfig e a proteção de endpoints) para validar tokens emitidos pelo Keycloak e aplicar as regras de autorização pretendidas.