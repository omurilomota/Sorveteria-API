# 🍦 Sorveteria API

Backend REST para uma sorveteria: catálogo de sabores e produtos, cadastro de
clientes e registro de pedidos.

Construído com **Java 21** e **Spring Boot 4.1.1**, com persistência em JPA/Hibernate.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-C71A1F?logo=apache-maven&logoColor=white)](https://maven.apache.org/)

---

## 📑 Sumário

- [Sobre](#sobre)
- [Status do projeto](#status-do-projeto)
- [Tecnologias](#tecnologias)
- [Como executar](#como-executar)
- [Configuração](#configuração)
- [Endpoints](#endpoints)
- [Modelo de dados](#modelo-de-dados)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Padrões adotados](#padrões-adotados)
- [Testes](#testes)
- [Problemas conhecidos](#problemas-conhecidos)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

---

## Sobre

API REST que dá suporte a uma operação de sorveteria, cobrindo três áreas:

- **Catálogo** — sabores e produtos com preço, estoque e categoria
- **Clientes** — cadastro com email e CPF únicos
- **Pedidos** — composição de itens com cálculo de total

A arquitetura segue o padrão **Controller → Service → Repository**, com uma
camada de **DTOs** que isola o contrato da API das entidades JPA. Nenhuma
entidade do banco é exposta diretamente nos endpoints — a superfície da API é
composta apenas por `record`s de request e response.

---

## Status do projeto

🚧 **Em desenvolvimento ativo.**

A base arquitetural está completa — camada de DTOs, repositories, tratamento
global de exceção e o CRUD de sabores. As próximas etapas são autenticação,
migrações de banco e suíte de testes. Consulte o
[ROADMAP](./ROADMAP.md) para o panorama completo e a ordem sugerida de
implementação.

| Módulo | Estado |
|---|---|
| Sabor | ✅ CRUD completo com paginação |
| Produto | ⚠️ Entidade e service prontos, controller pendente |
| Usuário | ⚠️ Cadastro e busca por ID, autenticação pendente |
| Pedido | ⚠️ CRUD básico, sem cálculo server-side de preço |
| Segurança | 🚧 Estrutura iniciada, não funcional |
| Banco | ⚠️ H2 em memória, sem versionamento de schema |
| Testes | 🚧 Cobertura mínima |

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 4.1.1 | Framework |
| Spring Web MVC | — | Camada HTTP |
| Spring Data JPA | — | Persistência |
| Spring Security | — | Autenticação (em andamento) |
| Spring Validation | — | Validação de entrada |
| H2 Database | — | Banco em memória (dev) |
| Hibernate | 6.x | ORM |
| Lombok | — | Redução de boilerplate |
| JUnit 5 | — | Framework de testes |
| Maven | — | Build |

---

## Como executar

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.9+** (ou use o wrapper `./mvnw`, que já vem no projeto)

### Passo a passo

```bash
# 1. Clone o repositório
git clone https://github.com/omurilomota/Sorveteria-API.git
cd Sorveteria-API

# 2. Suba a aplicação
./mvnw spring-boot:run
```

Ou, se preferir via JAR:

```bash
./mvnw clean package
java -jar target/SorveteriaBackend-0.0.1-SNAPSHOT.jar
```

### Acessos

| Serviço | URL |
|---|---|
| API | http://localhost:8081 |
| Console H2 | http://localhost:8081/h2-console |

Para o console H2, use a JDBC URL `jdbc:h2:mem:sorveteria`, usuário `sa` e
senha em branco.

> ⚠️ O banco é **em memória**: todo o dado é descartado a cada reinício. Isso é
> proposital no estágio atual e será substituído por Postgres.

---

## Configuração

Toda a configuração está em `src/main/resources/application.properties`:

| Propriedade | Valor | Descrição |
|---|---|---|
| `server.port` | `8081` | Porta da aplicação |
| `spring.datasource.url` | `jdbc:h2:mem:sorveteria` | Banco em memória |
| `spring.datasource.username` | `sa` | Usuário do banco |
| `spring.jpa.hibernate.ddl-auto` | `update` | Cria/atualiza schema no boot |
| `spring.h2.console.enabled` | `true` | Habilita o console web |
| `spring.h2.console.path` | `/h2-console` | Caminho do console |
| `spring.jpa.show-sql` | `true` | Loga as queries |
| `spring.jpa.properties.hibernate.format_sql` | `true` | SQL formatado nos logs |

> ⚠️ `ddl-auto=update` e o console H2 são adequados apenas para
> desenvolvimento. Ver [Problemas conhecidos](#problemas-conhecidos).

---

## Endpoints

Base: `http://localhost:8081`

> ⚠️ **No estado atual, todos os endpoints retornam `401 Unauthorized`.** O
> `spring-boot-starter-security` está no classpath, mas o `SecurityConfig` ainda
> não define uma cadeia de filtros, então o Spring aplica a configuração
> padrão e bloqueia tudo. O motivo está detalhado em
> [Problemas conhecidos](#problemas-conhecidos). Enquanto isso, remova o
> starter de segurança do `pom.xml` para testar localmente.

### Sabores

CRUD completo, com paginação e ativação/desativação.

| Método | Rota | Descrição | Retorno |
|---|---|---|---|
| `GET` | `/sabores` | Lista paginada | `200` · `Page<SaborResponse>` |
| `GET` | `/sabores/ativos` | Lista apenas ativos | `200` · `List<SaborResponse>` |
| `GET` | `/sabores/{id}` | Busca por ID | `200` · `SaborResponse` |
| `POST` | `/sabores` | Cria um sabor | `201` · `SaborResponse` |
| `PUT` | `/sabores/{id}` | Atualiza um sabor | `200` · `SaborResponse` |
| `PATCH` | `/sabores/{id}/status?ativo={bool}` | Altera o status | `204` |
| `DELETE` | `/sabores/{id}` | Remove um sabor | `204` |

<details>
<summary>Exemplos de requisição</summary>

Criar sabor:

```bash
curl -X POST http://localhost:8081/sabores \
  -H "Content-Type: application/json" \
  -d '{"nome": "Chocolate Belga"}'
```

```json
{
  "id": 1,
  "nome": "Chocolate Belga",
  "ativo": true
}
```

Listar com paginação:

```bash
curl "http://localhost:8081/sabores?page=0&size=20&sort=nome,asc"
```

Desativar sem remover:

```bash
curl -X PATCH "http://localhost:8081/sabores/1/status?ativo=false"
```

</details>

### Usuários

| Método | Rota | Descrição | Retorno |
|---|---|---|---|
| `GET` | `/usuarios/{id}` | Busca por ID | `200` · `User` |
| `POST` | `/usuarios` | Cria um usuário | `200` · `UserResponse` |

<details>
<summary>Exemplo de requisição</summary>

```bash
curl -X POST http://localhost:8081/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Ana Souza",
    "email": "ana@exemplo.com",
    "senha": "minhasenha123",
    "telefone": "11987654321",
    "cpf": "12345678901",
    "dataNascimento": "1995-03-15"
  }'
```

Campos validados: `nome` e `email` e `senha` são obrigatórios, `senha` exige
mínimo de 8 caracteres, `email` precisa ter formato válido, `cpf` exatamente 11
dígitos, `telefone` 10 ou 11 dígitos e `dataNascimento` no passado.

</details>

### Pedidos

| Método | Rota | Descrição | Retorno |
|---|---|---|---|
| `GET` | `/pedidos` | Lista todos | `200` · `List<Pedido>` |
| `POST` | `/pedidos` | Cria um pedido | `200` · `Pedido` |

> ⚠️ Estes endpoints ainda recebem e devolvem a **entidade JPA**. A migração
> para DTO está especificada no [ROADMAP](./ROADMAP.md#03--pedidocontroller-aceita-a-entidade-jpa-crua).

---

## Modelo de dados

```mermaid
erDiagram
    User ||--o{ Pedido : "realiza"
    Pedido ||--|{ ItemPedido : "contém"
    Produto ||--o{ ItemPedido : "referenciado em"

    User {
        Long id PK
        string nome
        string email UK "único"
        string senhaHash
        string telefone
        string cpf UK
        date dataNascimento
        boolean ativo
        datetime ultimoLogin
    }

    Pedido {
        Long id PK
        string nome
        decimal custoTotal
        StatusPedido status
        int itensTotal
    }

    ItemPedido {
        Long id PK
        int quantidade
        decimal precoUnitario
    }

    Produto {
        Long id PK
        string nome
        string descricao
        decimal preco
        int estoque
        boolean ativo
        Categoria categoria
    }

    Sabor {
        Long id PK
        string nome
        boolean ativo
    }
```

### Entidades

| Entidade | Papel |
|---|---|
| `User` | Cliente, com endereço embutido (`@Embeddable Endereco`) |
| `Pedido` | Pedido, com status e lista de itens |
| `ItemPedido` | Item do pedido, referencia produto e guarda o preço unitário |
| `Produto` | Item do catálogo: preço, estoque e categoria |
| `Sabor` | Sabor do catálogo, hoje apenas `nome` e `ativo` |

### Enums

```java
public enum StatusPedido { PENDENTE, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO }

public enum Categoria    { SORVETE, ACAI, BEBIDA, SOBREMESA, OUTRO }
```

> 📌 Sobre a modelagem: `Sabor` e `Produto` hoje se sobrepõem, e `Pedido`
> mantém `custoTotal` e `itensTotal` em paralelo a `calcularTotal()` e
> `itens.size()`. A consolidação desse modelo está detalhada no
> [ROADMAP](./ROADMAP.md#58-decisão-de-modelagem-pendente-sabor-vs-produto).

---

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/sorveteria/
│   │   ├── SorveteriaBackendApplication.java   # Entry point
│   │   ├── SecurityConfig.java                  # Configuração de segurança
│   │   ├── JwtAuthenticationFilter.java         # Filtro de token JWT
│   │   ├── controller/                          # Camada HTTP
│   │   │   ├── SaborController.java
│   │   │   ├── UserController.java
│   │   │   └── PedidoController.java
│   │   ├── service/                             # Regras de negócio
│   │   │   ├── SaborService.java                # Interface
│   │   │   ├── SaborServiceImpl.java            # Implementação
│   │   │   ├── UserService.java
│   │   │   ├── ProdutoService.java
│   │   │   └── PedidoService.java
│   │   ├── repository/                          # Acesso a dados
│   │   │   ├── SaborRepository.java
│   │   │   ├── UserRepository.java
│   │   │   ├── ProdutoRepository.java
│   │   │   └── PedidoRepository.java
│   │   ├── model/                               # Entidades JPA
│   │   │   ├── User.java
│   │   │   ├── Pedido.java
│   │   │   ├── ItemPedido.java
│   │   │   ├── Produto.java
│   │   │   ├── Sabor.java
│   │   │   ├── StatusPedido.java
│   │   │   └── Categoria.java
│   │   ├── dto/                                 # Requests e responses
│   │   │   ├── SaborRequest.java        SaborResponse.java
│   │   │   ├── ProdutoRequest.java      ProdutoResponse.java
│   │   │   ├── UserRequest.java         UserResponse.java
│   │   │   ├── PedidoRequest.java       ItemPedidoRequest.java
│   │   │   └── UserMapper.java
│   │   └── exception/                           # Exceções e handler global
│   │       ├── GlobalExceptionHandler.java
│   │       ├── UsuarioNaoEncontradoException.java
│   │       ├── EmailJaCadastradoException.java
│   │       └── CpfJaCadastradoException.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/sorveteria/
        └── SorveteriaBackendApplicationTests.java
```

---

## Padrões adotados

### DTOs como `record`

Todo o contrato da API usa `record`, imutável e sem boilerplate. Entidades JPA
nunca são recebidas nem devolvidas, exceto nos endpoints pendentes de migração.

```java
public record SaborRequest(
    @NotBlank String nome
) {}
```

### Bean Validation nas bordas

A entrada é validada com `jakarta.validation` antes de chegar ao service.

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public SaborResponse criar(@RequestBody @Valid SaborRequest dto) {
    return service.criar(dto);
}
```

### Tratamento global de exceção

`GlobalExceptionHandler` traduz exceções de domínio em respostas HTTP
padronizadas.

| Exceção | Status | Situação |
|---|---|---|
| `MethodArgumentNotValidException` | `400` | Payload inválido |
| `EmailJaCadastradoException` | `409` | Email duplicado |
| `CpfJaCadastradoException` | `409` | CPF duplicado |
| `RuntimeException` | `500` | Erro genérico |

```json
{
  "status": 409,
  "erro": "Conflito",
  "mensagem": "Email já cadastrado: ana@exemplo.com",
  "timestamp": "2026-10-05T10:21:56.031Z"
}
```

### Transações explícitas no service

```java
@Transactional(readOnly = true)
public List<SaborResponse> listarAtivos() { ... }

@Transactional
public SaborResponse criar(SaborRequest dto) { ... }
```

### Injeção por construtor

Preferência por `@RequiredArgsConstructor` do Lombok com campos `final`, o que
torna as dependências explícitas e permite injeção imutável.

### Query methods do Spring Data

Nomes de método auto-descritivos dispensam JPQL na maioria dos casos.

```java
public interface SaborRepository extends JpaRepository<Sabor, Long> {
    List<Sabor> findByAtivoTrue();
}
```

---

## Testes

```bash
# Executa a suíte
./mvnw test

# Com relatório de cobertura
./mvnw verify
```

A suíte atual cobre apenas o carregamento do contexto de aplicação. A estratégia
de testes — slices `@WebMvcTest`, `@DataJpaTest`, Testcontainers e limite de
cobertura — está especificada no
[ROADMAP](./ROADMAP.md#p3--testes).

---

## Problemas conhecidos

A lista completa e priorizada está no [ROADMAP](./ROADMAP.md). Os pontos que
afetam quem for usar a API agora:

### 🔴 A API está bloqueada por configuração de segurança

`SecurityConfig` não define nenhuma cadeia de filtros e `JwtAuthenticationFilter`
está vazio. Com o starter de segurança no classpath, o Spring Boot aplica a
política padrão e **todos os endpoints respondem `401`**.

**Workaround para desenvolvimento local:** remova o bloco abaixo do `pom.xml`
até a autenticação ser implementada.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 🟠 Senha armazenada sem hash

`UserMapper.toEntity` grava a senha em texto puro. **Não use dados reais
enquanto isso não for corrigido.**

### 🟠 Pedido aceita preço do cliente

`POST /pedidos` recebe a entidade `Pedido` diretamente, permitindo que o
cliente envie `precoUnitario` arbitrário. **Não use em produção.**

### 🟠 Dados voláteis

Banco H2 em memória: tudo é perdido a cada reinício, e `ddl-auto=update` altera
o schema silenciosamente. Em produção isso causaria perda de dados.

### 🟡 Módulo de produtos inacessível

`ProdutoService` está implementado, mas não há `ProdutoController`. O catálogo
de produtos com preço e estoque não é acessível pela API.

---

## Contribuindo

1. Crie uma branch a partir de `master`:
   ```bash
   git checkout -b feat/minha-feature
   ```
2. Faça suas alterações seguindo os padrões já existentes no código
3. Rode a build e os testes antes de abrir o PR:
   ```bash
   ./mvnw clean verify
   ```
4. Abra o Pull Request descrevendo a mudança

O projeto adota **Conventional Commits**:

```
feat: adiciona baixa de estoque na confirmação do pedido
fix: corrige mapeamento de telefone no response de usuário
refactor: extrai mapeamento de Pedido para PedidoMapper
chore: configura profiles de dev e prod
```

---

## Licença

Projeto em desenvolvimento. Defina a licença antes de publicar.

---

<div align="center">

**[⬆ Voltar ao topo](#-sorveteria-api)**

Feito com ☕ para quem prefere sorvete a mais

</div>
