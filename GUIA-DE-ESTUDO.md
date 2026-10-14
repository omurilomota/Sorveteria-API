# Guia de estudo — Sorveteria-API

Este guia explica **como o projeto funciona por dentro**, do momento em que uma
requisição HTTP chega até a linha que é gravada no banco.

Ele cobre duas coisas:

1. **O código que já existia** — a arquitetura, camada por camada.
2. **O que foi mudado depois** — cada alteração com o motivo, na seção
   [Histórico de mudanças](#histórico-de-mudanças).

A ideia é que você consiga ler o projeto inteiro sem travar em nenhum arquivo.

---

## 1. Visão geral

É uma API REST de sorveteria. Ela expõe quatro recursos:

| Recurso | Rota base | O que faz |
| --- | --- | --- |
| Sabores | `/sabores` | Catálogo de sabores, com ativação/desativação |
| Produtos | `/produtos` | Catálogo com preço, estoque e categoria |
| Usuários | `/usuarios` | Cadastro de clientes |
| Pedidos | `/pedidos` | Pedido com itens, total e status |

Stack: Java 21, Spring Boot 4.1.1, Spring Data JPA (Hibernate), PostgreSQL,
Flyway, Spring Security, springdoc/OpenAPI, Lombok, JUnit 5 + Testcontainers.

Configuração da aplicação (`application.properties`):

- porta `8081`
- banco PostgreSQL, com credenciais vindas de variável de ambiente
- `ddl-auto=validate` — o Hibernate não altera o banco, só confere
- Flyway aplicando as migrações de `src/main/resources/db/migration`

---

## 2. O caminho de uma requisição

Vale decorar este caminho: **toda** requisição passa por estas etapas, na ordem.

```
Cliente (Insomnia, frontend, curl)
        |
        v
  Controller        recebe HTTP, valida formato, delega        (@RestController)
        |
        v
  Service           regra de negócio, transação                 (@Service)
        |
        v
  Repository        acesso a dados                              (JpaRepository)
        |
        v
  Banco (PostgreSQL)
        |
        v
  Entity -> DTO     converte para o formato de resposta
        |
        v
  Cliente recebe JSON
```

Um exemplo concreto, `POST /produtos`:

1. **Spring MVC** converte o JSON do corpo em um `ProdutoRequest` (um `record`).
2. A anotação `@Valid` dispara as validações do `record` (`@NotBlank`,
   `@Positive`). Se falhar, lança `MethodArgumentNotValidException` e nem entra
   no controller.
3. **`ProdutoController.criar`** chama `service.criar(dto)`.
4. **`ProdutoService.criar`** cria um `Produto` (entidade) e chama
   `repository.save(...)`.
5. **`ProdutoRepository`** (interface do Spring Data) gera o `INSERT` sozinho.
6. O service converte a entidade salva em `ProdutoResponse` e devolve.
7. O controller retorna `201 Created` com o JSON.

Se algo der errado em qualquer passo (produto inexistente, e-mail duplicado,
payload inválido), o erro é convertido em resposta HTTP por um único lugar:
o `GlobalExceptionHandler`.

---

## 3. As camadas, uma por uma

### 3.1 Controller — `controller/`

Responsabilidade: **falar HTTP**. Recebe e devolve JSON. Não contém regra de
negócio.

```java
@RestController                       // = @Controller + @ResponseBody (devolve JSON)
@RequestMapping("/produtos")          // prefixo de todas as rotas da classe
@RequiredArgsConstructor             // Lombok: gera o construtor dos campos final
public class ProdutoController {

    private final ProdutoService service;   // injeção por construtor

    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)     // 201 em vez de 200
    public ProdutoResponse criar(@RequestBody @Valid ProdutoRequest dto) {
        return service.criar(dto);
    }
}
```

Conceitos que aparecem aqui:

| Anotação | O que faz |
| --- | --- |
| `@RestController` | Marca a classe como controlador e faz o retorno virar JSON |
| `@RequestMapping` | Prefixo de rota da classe |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@PatchMapping` / `@DeleteMapping` | Método HTTP de cada rota |
| `@PathVariable` | Valor que vem na URL (`/produtos/{id}`) |
| `@RequestBody` | O JSON do corpo vira objeto Java |
| `@Valid` | Dispara as validações declaradas no DTO |
| `@ResponseStatus` | Código HTTP de sucesso daquele método |
| `@RequiredArgsConstructor` | Lombok: gera construtor para os campos `final` |

Por que as dependências são `private final` e injetadas pelo construtor? Porque
assim o objeto não pode ser criado "pela metade" nem trocado em tempo de
execução, e o campo pode ser `final`. A alternativa (`@Autowired` no campo)
deixa a classe instanciável sem as dependências e dificulta o teste.

**Códigos HTTP usados:** `200` leitura/atualização, `201` criação, `204` exclusão,
`400` dado inválido, `404` não encontrado, `409` conflito de regra de negócio,
`500` erro inesperado.

### 3.2 Service — `service/`

Responsabilidade: **regra de negócio e transação**. É aqui que mora a decisão.

```java
@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
        return ProdutoResponse.from(produto);
    }
}
```

Conceitos:

| Anotação | O que faz |
| --- | --- |
| `@Service` | Marca como bean de serviço |
| `@Transactional` | Envolve o método numa transação de banco |
| `@Transactional(readOnly = true)` | Otimização para métodos que só leem |
| `orElseThrow` | Converte "Optional vazio" em exceção de domínio |

**O que é `@Transactional`, na prática:** o Spring abre uma transação antes do
método e faz `commit` no fim. Se o método lançar qualquer exceção não tratada,
ele faz `rollback` — tudo que foi alterado naquele método é desfeito. É por isso
que um erro ao adicionar o segundo item de um pedido não deixa o primeiro
gravado "pela metade".

Detalhe importante: `orElseThrow` aqui lança uma **exceção de domínio**
(`ProdutoNaoEncontradoException`), não uma `RuntimeException` genérica. Isso é o
que permite o handler global devolver `404` em vez de `500`.

> **Observação sobre consistência:** `SaborService` é uma **interface** com
> implementação em `SaborServiceImpl`, enquanto `ProdutoService`,
> `UserService` e `PedidoService` são classes concretas. As duas abordagens
> funcionam; o problema é ter as duas no mesmo projeto. Interface faz sentido
> quando existem várias implementações ou você precisa trocar a implementação.
> Aqui não existe. Escolha uma e padronize — é o tipo de detalhe que um
> entrevistador percebe.

### 3.3 Repository — `repository/`

Responsabilidade: **acesso a dados**. Não tem regra de negócio.

```java
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
```

Só de estender `JpaRepository` você ganha `save`, `findById`, `findAll`,
`deleteById`, `existsById`, `count` e outros. O Spring gera a implementação em
tempo de execução.

**Consultas derivadas do nome do método:** se você escrever

```java
boolean existsByEmail(String email);
Optional<User> findByCpf(String cpf);
List<Produto> findByCategoriaAndAtivoTrue(Categoria categoria);
```

o Spring monta o SQL a partir do nome. Não precisa escrever query nenhuma.
Quando a consulta fica difícil de expressar no nome, use `@Query` com JPQL.

### 3.4 Model — `model/`

Responsabilidade: **representar as tabelas** do banco como classes Java.

```java
@Entity
@Getter @Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;
}
```

| Anotação | O que faz |
| --- | --- |
| `@Entity` | A classe vira uma tabela |
| `@Id` | Marca a chave primária |
| `@GeneratedValue(IDENTITY)` | O banco gera o id (coluna *identity*) |
| `@Column` | Ajusta a coluna: nome, nulo, precisão |
| `@Enumerated(STRING)` | Grava o **nome** do enum como texto |
| `@ManyToOne` / `@JoinColumn` | Relação N-1 e a coluna da chave estrangeira |
| `@OneToMany(mappedBy = ...)` | O lado inverso da relação (não cria coluna) |
| `@Embedded` / `@Embeddable` | Achata um objeto nas colunas da tabela dona |

Pontos que valem entender:

- **`EnumType.STRING` vs `ORDINAL`.** Com `STRING` o banco guarda `"SORVETE"`.
  Com `ORDINAL` guardaria a posição (`0`). Se você reordenar o enum, todo dado
  já gravado passa a significar outra coisa. Sempre `STRING` para dado
  persistido.
- **`@OneToMany(mappedBy = "user")`** é só o espelho. Quem manda na relação é o
  lado que tem a chave estrangeira (`Pedido.user`). Por isso não existe coluna
  em `users` para a lista de pedidos.
- **`@Embedded Endereco`** dentro de `User`: não existe tabela `endereco`. As
  colunas `rua`, `numero`, `cidade`, `estado`, `cep` ficam dentro de `users`.
- **Usar `BigDecimal` para dinheiro.** `double` não representa `0.10`
  exatamente, e o erro se acumula nas somas. Em preço, sempre `BigDecimal`.

### 3.5 DTO e Mapper — `dto/`

**DTO** (*Data Transfer Object*) é o formato do que entra e sai pela API.
Aqui são `record`s:

```java
public record ProdutoRequest(
        @NotBlank String nome,
        @NotNull @Positive BigDecimal preco,
        @NotBlank String descricao,
        @NotNull @Positive int estoque,
        @NotBlank String categoria
) {}

public record ProdutoResponse(Long id, String nome, BigDecimal preco, Integer estoque) {
    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(), produto.getNome(), produto.getPreco(), produto.getEstoque());
    }
}
```

**Por que não devolver a entidade direto?** Porque isso acopla o contrato da API
ao modelo do banco. Se você renomear a coluna `nome`, o JSON muda e todos os
clientes quebram — mesmo que a mudança seja só interna. Com DTOs, o banco pode
mudar desde que o DTO continue igual.

Efeito colateral bom: você controla o que sai. `ProdutoResponse` **não** expõe
`ativo` nem `categoria`, e `UserResponse` devolve o CPF mascarado. Devolver a
entidade entregaria tudo, inclusive campos que não deveriam vazar.

Um `record` é imutável e já vem com construtor, `equals`, `hashCode` e
`toString`. Para DTO, é exatamente o que se quer.

### 3.6 Exceções — `exception/`

Existem dois grupos:

**Exceções de domínio** — cada problema possível tem um nome:
`ProdutoNaoEncontradoException`, `PedidoNaoEncontradoException`,
`UsuarioNaoEncontradoException`, `CpfJaCadastradoException`,
`EmailJaCadastradoException`, `ItemInvalidoException`,
`TransicaoStatusInvalidaException`.

**O tradutor** — `GlobalExceptionHandler`, anotado com `@RestControllerAdvice`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleProdutoNaoEncontrado(
            ProdutoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "erro", "Não encontrado",
                        "mensagem", e.getMessage(),
                        "timestamp", LocalDateTime.now()));
    }
}
```

Sem ele, cada controller teria que montar resposta de erro, e cada um montaria
de um jeito. Com ele, **todo erro da API tem o mesmo formato**:

```json
{ "status": 404, "erro": "Não encontrado", "mensagem": "...", "timestamp": "..." }
```

A ordem importa: o Spring procura o handler mais específico. Como existe também
um `@ExceptionHandler(RuntimeException.class)` para o caso de erro inesperado,
uma exceção de domínio **sem** handler próprio cairia nele e viraria `500` —
mesmo sendo, por exemplo, um simples "não encontrado". Foi exatamente esse tipo
de furo que os testes encontraram (ver seção 8).

---

## 4. O modelo de dados

```
users ─────────┐
  id           │
  nome         │        pedido ──────────────┐
  email UNIQUE │          id                 │
  senha_hash   │          nome               │
  rua/numero/  │          custo_total        │
  cidade/      │          status             │
  estado/cep   │          itens_total        │
  telefone     │          user_id ───────────┘
  cpf          │
  data_nasc.   │              item_pedido
  ativo        │                id
  ultimo_login │                pedido_id ──> pedido
               │                produto_id ─> produto
               └─ 1 : N          quantidade
                                  preco_unitario
produto
  id, nome, descricao, preco, estoque, ativo, categoria

sabor
  id, nome, ativo
```

Decisão que vale notar: **`item_pedido` guarda `preco_unitario`**. Poderia ler o
preço do produto toda vez que precisasse calcular. Não faz isso porque o pedido
de ontem precisa continuar valendo o preço de ontem — se o preço do produto
mudar amanhã, o histórico não pode mudar junto.

---

## 5. Regras de negócio no código

### 5.1 Transição de status do pedido

Em `PedidoService` existe um mapa que define o que pode virar o quê:

```java
private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = Map.of(
        PENDENTE,   Set.of(EM_PREPARO, CANCELADO),
        EM_PREPARO, Set.of(PRONTO, CANCELADO),
        PRONTO,     Set.of(ENTREGUE, CANCELADO),
        ENTREGUE,   Set.of(),          // estado final
        CANCELADO,  Set.of());         // estado final
```

Antes de mudar o status, `validarTransicao` consulta esse mapa e lança
`TransicaoStatusInvalidaException` (que vira `409`) se a mudança não for
permitida. Regra de negócio expressa como dado, em um lugar só, é muito mais
fácil de auditar do que uma sequência de `if`.

### 5.2 Unicidade de e-mail e CPF

`UserService.salvar` verifica se já existe usuário com aquele e-mail ou CPF e
lança `EmailJaCadastradoException` / `CpfJaCadastradoException` (vira `409`).

E-mail **também** tem constraint `UNIQUE` no banco. CPF não tem. A diferença
importa: a checagem no Java tem uma janela de corrida — duas requisições
simultâneas podem consultar ao mesmo tempo, as duas verem "não existe" e as duas
gravem. Só o banco consegue impedir isso de forma confiável. **Adicionar
`UNIQUE` para `cpf` é uma melhoria real e pequena.**

### 5.3 Fluxo do pedido

1. `Pedido` nasce com `PENDENTE`, `custoTotal` zero e `itensTotal` zero.
2. Cada item vem com `produtoId` e `quantidade`. O **servidor** busca o produto e
   usa o preço dele — o cliente nunca escolhe o preço.
3. Depois de mexer nos itens, `recalcularTotal` soma e atualiza `custoTotal` e
   `itensTotal`.

Atenção a uma ambiguidade de nome: **`itensTotal` conta linhas de item, não a
soma das quantidades.** Dois produtos iguais de quantidade 2 dão `itensTotal: 1`
e `custoTotal: 2 × preço`. O comportamento é coerente; o nome é que engana.
`linhasTotal` seria mais claro.

---

## 6. Segurança: o que existe e o que não existe

**Sendo direto: não há autenticação neste projeto.** Isto é importante e é
fácil de errar lendo os nomes dos arquivos.

**O que existe:**

- `spring-boot-starter-security` no classpath.
- `SecurityConfig` com um `SecurityFilterChain` que:
  - desabilita CSRF (`csrf.disable()`),
  - libera **todas** as rotas (`anyRequest().permitAll()`),
  - permite o H2/Swagger em iframe (`frameOptions.sameOrigin()`).
- Um bean `PasswordEncoder` com `BCryptPasswordEncoder`.
- `UserMapper.toEntity` grava `passwordEncoder.encode(req.senha())`. Então
  **senha em banco tem hash** — isso é real.

**O que não existe:**

- `JwtAuthenticationFilter` é uma **classe vazia**. Não tem método, não é
  referenciada por ninguém e não é registrada na cadeia de filtros.
- Não há nenhuma biblioteca JWT no `pom.xml`.
- Nenhum endpoint exige credencial.

Ou seja: hoje, qualquer pessoa que alcançar a API pode ler e escrever. Isso serve
como estado intermediário de estudo, **não** como configuração para publicar.

### Por que liberar tudo? O caso das duas armadilhas

Houve duas configurações diferentes aqui, e as duas valem a lição:

- **Sem nenhuma cadeia de filtros e sem senha configurada**, o Spring Boot aplica
  a política padrão: gera uma senha aleatória no log e exige autenticação em
  tudo. Resultado: **todos os endpoints respondem `401`**, e o motivo não é
  óbvio para quem está começando.
- **Com `permitAll()`**, tudo funciona sem credencial — e tudo fica aberto.

Nenhuma das duas é o que você quer. O caminho é o terceiro: implementar o filtro
JWT de verdade e liberar só o que precisa (login, cadastro, documentação).

---

## 7. Testes

```bash
./mvnw test     # exige Docker rodando
```

Hoje são 30 testes:

- `SorveteriaBackendApplicationTests` (1) — só verifica que o contexto sobe.
- `ApiIntegrationTests` (29) — o trabalho de verdade.

### O que é um teste de integração

Teste de unidade isola uma classe e simula (com *mock*) tudo em volta. Teste de
integração sobe a aplicação inteira e exercita o caminho completo: HTTP →
controller → service → repository → banco. É mais lento e vale muito mais aqui,
porque pega erro de integração que o teste de unidade nunca veria. Os três bugs
da seção 8 são todos desse tipo.

### As peças

```java
@SpringBootTest                  // sobe a aplicação inteira
@AutoConfigureMockMvc            // disponibiliza o MockMvc
@Transactional                   // cada teste roda numa transação revertida
class ApiIntegrationTests extends PostgresIntegrationTest {
```

```java
@Testcontainers
public abstract class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine");
}
```

- **`MockMvc`** dispara uma requisição HTTP sem abrir porta de rede e deixa você
  inspecionar status, corpo e cabeçalhos.
- **`@Transactional` na classe de teste** faz o Spring reverter tudo ao final de
  cada teste. Consequência: os testes não interferem entre si e não dependem de
  ordem. Se você esquecer isso, um teste cria dado que quebra o próximo.
- **`@ServiceConnection`** liga o container ao Spring sozinho: URL, usuário e
  senha são configurados automaticamente. Sem ele você teria que ler a porta do
  container e injetar `@DynamicPropertySource` na mão.

### Por que PostgreSQL de verdade e não H2

H2 é um banco em memória que fala um dialeto parecido, mas não igual. Tipos,
constraints, funções e mensagens de erro divergem. Um teste que passa no H2 pode
falhar no Postgres de produção — e aí o teste deu confiança falsa.

Rodando contra o mesmo banco de produção, o teste vale como teste. E tem um
efeito colateral excelente: **a cada execução o Flyway aplica a migração do zero
e o `validate` confere as entidades contra o schema**. Ou seja, um erro na
migração derruba o teste.

### Exemplo comentado

```java
@Test
void produtoInexistenteRetorna404() throws Exception {
    mockMvc.perform(get("/produtos/" + ID_INEXISTENTE))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404))
           .andExpect(jsonPath("$.erro").value("Não encontrado"));
}
```

Lê-se: faça um GET, espere `404`, e confira que o corpo traz `status` e `erro`.
`jsonPath("$.status")` é o caminho até o campo, com `$` sendo a raiz do JSON.

---

## 8. Migrações de banco com Flyway

### O problema que o Flyway resolve

Antes, `spring.jpa.hibernate.ddl-auto=update` fazia o Hibernate criar e alterar
as tabelas a partir das entidades. Isso tem três defeitos:

1. O schema existe **só dentro do banco**. Não está no Git, ninguém revisa,
   não dá para voltar atrás.
2. Não dá para saber qual versão do schema está aplicada em cada ambiente.
3. O Hibernate altera estruturas existentes de forma silenciosa e às vezes
   destrutiva.

### Como funciona agora

O schema é um arquivo SQL versionado: `src/main/resources/db/migration/V1__baseline.sql`.

O nome não é decorativo — `V<versão>__<descrição>.sql`. Dois underscores separam a
versão da descrição. O Flyway aplica em ordem (`V1`, `V2`, ...) e registra numa
tabela de controle chamada `flyway_schema_history` o que já rodou e o checksum de
cada arquivo.

**Regra que você não pode quebrar:** migração já aplicada não se edita. Se editar,
o checksum muda e a próxima subida falha. Mudança nova é **arquivo novo**:
`V2__adiciona_unique_no_cpf.sql`.

E o `ddl-auto` mudou para `validate`:

| Valor | O que o Hibernate faz |
| --- | --- |
| `create` | Apaga e recria tudo a cada subida |
| `create-drop` | Como `create`, e apaga ao encerrar |
| `update` | Cria/altera o que estiver faltando (perigoso) |
| `validate` | **Não altera nada.** Falha se entidade e tabela divergirem |
| `none` | Não faz nada |

Com `validate`, um erro de digitação na migração vira erro na inicialização, e
não um bug misterioso três semanas depois. Quando o teste falhou com
`missing table [item_pedido]`, era exatamente esse mecanismo funcionando.

---

## 9. Histórico de mudanças

Tudo abaixo foi alterado depois que o projeto já existia. Cada item traz o
motivo, porque o motivo é a parte que se aprende.

### 9.1 Correção de build: `ProdutoService.atualizar`

`ProdutoController` chamava `service.atualizar(id, dto)`, mas o método no service
se chamava `atualizarProdutoResponse`. **O projeto não compilava.**

Lição: renomear método exige atualizar quem chama. O compilador sempre avisa —
o problema é commitar sem compilar.

### 9.2 Exceções de domínio em `ProdutoService`

`buscarPorId` e `atualizar` lançavam `RuntimeException("Produto não encontrado")`.
Como o handler global tem um `@ExceptionHandler(RuntimeException.class)` que
devolve `500`, buscar um produto inexistente retornava **500 em vez de 404**.

Corrigido para lançar `ProdutoNaoEncontradoException`, que tem handler próprio e
devolve `404`. O `deletar` do mesmo arquivo já fazia certo — era inconsistência
dentro do mesmo arquivo.

Lição: exceção genérica apaga a intenção. "Não encontrado" não é erro do
servidor; é resposta legítima da API.

### 9.3 Remoção de código morto

`ProdutoRequest.getQuantidade()` existia e só fazia uma coisa: lançar
`UnsupportedOperationException`. Não era chamado por ninguém (o
`PedidoMapper` usa `item.getQuantidade()`, de outra classe).

Lição: código morto que *quebra* é pior que código morto parado, porque alguém
vai chamar achando que funciona.

### 9.4 `UsuarioNaoEncontradoException` sem handler

A exceção era lançada pelo `UserService`, mas **não tinha `@ExceptionHandler`**.
Resultado: caía no handler genérico e devolvia `500` para usuário inexistente.

Só apareceu porque existe teste chamando `GET /usuarios/{id}` com id inexistente.
Foi o teste que revelou, não a leitura do código.

### 9.5 `SaborResponse` com campo `Id`

```java
public record SaborResponse(Long Id, String nome, Boolean ativo)
```

O campo se chamava `Id`, com maiúscula — único recurso do projeto assim, já que
os outros usam `id`. Isso muda o nome do campo no JSON e quebra qualquer cliente
que trate os quatro recursos do mesmo jeito. Renomeado para `id`.

### 9.6 `timesstamp` no handler de `ItemInvalidoException`

Erro de digitação em `GlobalExceptionHandler`: o handler de
`ItemInvalidoException` gravava a chave `"timesstamp"`, com dois "s". Ou seja,
naquele tipo de erro o campo de data saía com nome diferente dos demais.
Corrigido para `timestamp`.

Lição: os dois últimos itens são o mesmo tipo de problema — inconsistência de
contrato. Nenhum quebra um teste de unidade; os dois quebram um cliente. E os
dois seriam invisíveis sem teste de integração.

### 9.7 `POST /usuarios` devolvendo 200

Os outros três controllers devolviam `201 Created`; usuários devolvia `200`. A
criação agora responde `201`, igual às outras.

### 9.8 Testes de integração + Testcontainers

29 testes cobrindo os quatro controllers. Foi o que revelou os itens 9.4, 9.5 e
9.6. O banco é um PostgreSQL descartável em Docker.

### 9.9 PostgreSQL com Flyway, no lugar do H2

- Removido `spring-boot-h2console` e o banco em memória.
- Adicionado o driver do PostgreSQL.
- `V1__baseline.sql` cria as cinco tabelas, com chaves estrangeiras e índices.
- `ddl-auto` de `update` para `validate`.
- Credenciais por variável de ambiente, para não versionar senha.

### 9.10 Configuração de teste com Testcontainers

`PostgresIntegrationTest` é a classe base que sobe o container e o conecta ao
Spring. As duas classes de teste estendem ela.

---

## 10. Pegadinhas do Spring Boot 4 que apareceram

Estas custam tempo e valem saber de antemão.

### 10.1 A autoconfiguração virou artefato separado

No Spring Boot 4 o framework foi modularizado. Declarar a biblioteca **não**
basta — a autoconfiguração que liga aquela biblioteca ao Spring vive em outro
artefato.

Foi exatamente o que aconteceu com o Flyway: `flyway-core` estava no `pom.xml`,
compilava, e o Flyway simplesmente **nunca rodava**. O sintoma foi um
`missing table` na validação do Hibernate, que aponta para o lugar errado.
A correção foi usar `spring-boot-starter-flyway`.

Vale para `spring-boot-webmvc-test` (MockMvc) e `spring-boot-h2console` também.

**Como não cair nisso:** quando algo "não faz nada", confira no log se a
biblioteca sequer inicializou. E prefira o *starter* à biblioteca crua.

### 10.2 O Testcontainers renomeou os módulos na versão 2

Eram `org.testcontainers:postgresql` e `org.testcontainers:junit-jupiter`.
Agora são `org.testcontainers:testcontainers-postgresql` e
`org.testcontainers:testcontainers-junit-jupiter`.

Prova de que versão de biblioteca importa: um exemplo de 2024 não compila em 2026.

### 10.3 O corpo do erro nem sempre aponta para a causa

`missing table [item_pedido]` não significa "esqueci a tabela item_pedido" —
significava "o Flyway não rodou e o banco está vazio". Quando o erro parecer
estranho, olhe o log inteiro, não só a última linha.

---

## 11. Como rodar tudo

```bash
# 1. Banco
docker run -d --name sorveteria-db \
  -e POSTGRES_DB=sorveteria -e POSTGRES_USER=sorveteria \
  -e POSTGRES_PASSWORD=sorveteria -p 5432:5432 postgres:18-alpine

# 2. Aplicação
./mvnw spring-boot:run
```

- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html

```bash
# 3. Testes (Docker precisa estar rodando)
./mvnw test
```

---

## 12. Exercícios sugeridos

Em ordem de dificuldade, todos pequenos e todos úteis de verdade:

1. **Adicionar `UNIQUE` para `cpf`.** Crie `V2__unique_cpf.sql` — e repare que
   você **não** pode editar o `V1`.
2. **Corrigir a mensagem do `not found`.** Hoje `ProdutoNaoEncontradoException`
   provavelmente monta a mensagem no construtor; veja se ela é útil para quem lê
   a resposta.
3. **Padronizar as listagens.** Faça `/produtos` e `/pedidos` também devolverem
   página, ou `/sabores` voltar a array. Escolha uma e escreva o motivo num
   comentário.
4. **Implementar a autenticação por JWT.** É o item que falta para a v1:
   adicionar a biblioteca, gerar token no login, validar no filtro, registrar o
   filtro com `addFilterBefore`, e trocar `permitAll()` por regras reais.
5. **Marcar `nome` e `categoria` como `NOT NULL`** na entidade (`@Column`) e criar
   a migração correspondente.
6. **Adicionar um teste que reprove de propósito** e ver o que acontece. Entender
   a mensagem de falha vale mais que escrever o teste.

---

## 13. Glossário rápido

| Termo | Significado |
| --- | --- |
| **Bean** | Objeto que o Spring cria e gerencia |
| **Injeção de dependência** | A classe recebe o que precisa em vez de criar |
| **DTO** | Objeto que define o formato de entrada/saída da API |
| **Entidade** | Classe que representa uma tabela |
| **Repository** | Camada de acesso a dados |
| **Transação** | Bloco que faz tudo ou não faz nada |
| **Migração** | Arquivo SQL versionado que altera o schema |
| **Checksum** | Impressão digital do arquivo, usada para detectar edição |
| **Container** | Processo isolado; aqui, um banco descartável nos testes |
| **Mock** | Objeto falso que simula uma dependência |
