# Auditoria do ambiente de desenvolvimento

Revisão das três IDEs (IntelliJ, VS Code, Eclipse/Spring Tools) e do ambiente
Java, feita em 13/10/2026 nesta máquina. O objetivo é responder uma pergunta
só: **o ambiente está montado de um jeito que não vai te atrapalhar?**

Vale para todos os projetos Java, não só este.

---

## Resumo do que está certo

| Item | Estado |
| --- | --- |
| JDK 21 instalado (`/usr/lib/jvm/java-21-openjdk-amd64`) | ✅ Bate com o `pom.xml` |
| Maven wrapper no projeto (3.3.4 → Maven 3.9.16) | ✅ Reproduzível |
| VS Code completo para Java + Spring | ✅ Pacote de extensões inteiro |
| `.idea/` fora do Git (contém estado pessoal da IDE) | ✅ Correto |
| `.gitattributes` normalizando fim de linha do `mvnw` | ✅ Correto |
| Docker ativo | ✅ Necessário para os testes |

**Destaque positivo:** o VS Code está muito bem equipado — `redhat.java`,
`vmware.vscode-spring-boot`, `vscode-spring-boot-dashboard`, `spring-initializr`,
`java-test`, `vscode-maven`, `java-debug`, `java-dependency`, `vscode-xml`,
`vscode-yaml`. É o conjunto completo. Falta só configurar as preferências de Java
(ver item 3).

---

## 1. O problema mais grave: o IntelliJ aponta para um JDK que não é o do projeto

Em `.idea/misc.xml`:

```xml
<component name="ProjectRootManager" languageLevel="JDK_25"
           project-jdk-name="ms-25" project-jdk-type="JavaSDK" />
```

Enquanto o `pom.xml` compila para **Java 21**:

```xml
<properties><java.version>21</java.version></properties>
```

E no `/usr/lib/jvm` existe **só** o 21. O JDK chamado `ms-25` (build da Microsoft)
não está lá nem em `~/.jdks`.

**Por que isso é um problema real:** o IntelliJ analisa e destaca erro com a
linguagem do Java 25, e o Maven compila com 21. Se você escrever algo que só
existe no 22+, a IDE mostra tudo verde e o `./mvnw build` quebra. É a origem
clássica do "na minha máquina funciona".

**O que fazer:**

1. No IntelliJ: `File → Project Structure → Project → SDK` → selecione o JDK 21
   (`/usr/lib/jvm/java-21-openjdk-amd64`) e `Language level: 21`.
2. Em `Settings → Build Tools → Maven → Importing`, deixe o JDK do runner também
   em 21.
3. Se quiser o JDK 25 depois, instale de verdade e **suba a versão no `pom.xml`
   junto** — as duas andam juntas, nunca separadas.

> Detalhe: `.idea/` não está versionado, então isso é só a sua máquina. Mas o
> erro que ele causa é bem real e difícil de associar à causa.

---

## 2. Seu IntelliJ está em modo Community — e por isso não tem suporte a Spring

Dois sinais:

- `~/.config/JetBrains/IntelliJIdea2026.2/disabled_plugins.txt` contém
  `com.intellij.modules.ultimate`.
- `~/.local/share/JetBrains/IntelliJIdea2026.2/plugins` está **vazio**.

**O que isso significa:** o suporte a Spring no IntelliJ é exclusivo da edição
Ultimate. Com o módulo `ultimate` desabilitado, você tem o IntelliJ Community —
sem navegação entre beans do Spring, sem entender `@Autowired`, sem run
configuration ciente de profiles, sem painel de endpoints.

**Consequência prática, e é o ponto principal desta auditoria:** para trabalhar
com Spring hoje, **o seu VS Code é melhor que o seu IntelliJ.** As extensões que
você já tem cobrem o que o Community não cobre.

**O que fazer — escolha um, e seja coerente:**

- **Opção A (recomendada, custo zero):** use o **VS Code** para Java/Spring, já
  que está completo. Só configure as preferências do item 3.
- **Opção B:** assine o IntelliJ Ultimate (é gratuito para estudantes — você tem
  e-mail de escola técnica, vale verificar a elegibilidade). Aí você fica com o
  melhor suporte a Spring que existe.
- **Opção C:** continue no IntelliJ Community, aceitando não ter as facilidades
  de Spring. Funciona, desde que você saiba que está sem elas.

O que não faz sentido é **usar as duas ao mesmo tempo no mesmo projeto** sem
motivo — você vai editar em uma, a outra reindexar, e perder tempo.

---

## 3. O VS Code está com indentação errada para Java

Em `~/.config/Code/User/settings.json`:

```json
"editor.tabSize": 2,
"editor.insertSpaces": true,
"editor.formatOnSave": true
```

`tabSize: 2` é o padrão de JavaScript, onde você provavelmente configurou isso.
Para Java, a convenção é **4 espaços** — é o que o código deste projeto usa.

Como `formatOnSave` está ligado, salvar um arquivo Java no VS Code pode
reformatar com 2 espaços e gerar um diff enorme de puro espaço em branco.

**O que fazer:** adicione um bloco específico por linguagem:

```json
"[java]": {
  "editor.tabSize": 4,
  "editor.insertSpaces": true
},
"[xml]": {
  "editor.tabSize": 4
},
"[yaml]": {
  "editor.tabSize": 2
}
```

Bônus que vale muito: peça ao formatter do Java para usar as convenções da
linguagem:

```json
"java.format.settings.profile": "GoogleStyle",
"java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml"
```

---

## 4. Falta um `.editorconfig` — e ele resolve o problema acima de uma vez

Este é o arquivo mais barato e mais útil que falta. Ele define indentação,
codificação e fim de linha **por tipo de arquivo**, e é respeitado por IntelliJ,
VS Code e Eclipse ao mesmo tempo. Com ele, o problema do item 3 desaparece — e
não depende de configuração pessoal de cada máquina.

Falta em todos os seus projetos. Conteúdo sugerido para a raiz:

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{xml,yml,yaml,json}]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.{cmd,bat}]
end_of_line = crlf
```

Note o `trim_trailing_whitespace = false` no Markdown: em Markdown, dois espaços
no fim da linha significam quebra de linha, então removê-los muda o documento.

---

## 5. Falta integração contínua neste projeto

O projeto **não tinha nenhum pipeline**, só um `.github/modernize`. Enquanto
isso, o `petshop-Chic` — uma landing page — tinha CI.

Isso é ao contrário: o projeto que você quer usar como prova de competência
técnica é justamente o que mais se beneficia de execução automática.

Foi criado o `.github/workflows/ci.yml`, que a cada push e pull request:

1. sobe uma máquina limpa com Ubuntu,
2. instala o JDK 21,
3. roda `./mvnw verify` — compilação + os 30 testes, incluindo o PostgreSQL
   descartável do Testcontainers (os runners do GitHub já têm Docker).

O selo verde ficou no topo do README. Ele se torna a prova visível de que o
projeto compila e passa nos testes sem depender da sua máquina.

**Complemento que vale a pena:** proteja a branch `master` para exigir o CI verde
antes de aceitar merge. É aí que o CI passa de enfeite a garantia.

---

## 6. Outros ajustes recomendados

### 6.1 `LICENSE` ausente

O repositório é público e não tem licença. Sem licença, juridicamente ninguém
pode reusar o código — o que costuma surpreender. O `README` inclusive diz
"Defina a licença antes de publicar".

Seus outros projetos (`estacionamento-automatico-godot`, `Olho-Eletronico`) usam
**MIT**. Para portfólio, MIT é a escolha comum: permissiva e simples. É uma
decisão sua — mas decida, porque a ausência não é neutra.

### 6.2 `.gitattributes` incompleto

Hoje:

```
/mvnw text eol=lf
*.cmd text eol=crlf
```

Resolve só os scripts do Maven. O ideal é tratar o repositório inteiro:

```
* text=auto eol=lf
*.cmd text eol=crlf
```

`* text=auto` deixa o Git decidir por arquivo e evita que o fim de linha vire
"alteração" em arquivos que você não tocou.

### 6.3 Dependências redundantes no `pom.xml`

Três declarações que não fazem falta:

| Dependência | Por quê |
| --- | --- |
| `spring-data-jpa` | Já vem dentro de `spring-boot-starter-data-jpa` |
| `jakarta.persistence-api` | Também vem, via Hibernate/JPA |
| `spring-boot-configuration-processor` | Só serve se você usar `@ConfigurationProperties` |

Nenhuma quebra o projeto. Mas `pom.xml` é um arquivo que se lê, e cada linha sem
função é ruído para quem vai avaliar.

Também há blocos vazios no topo — `<licenses><license/></licenses>`,
`<developers><developer/></developers>` e `<scm>` com quatro tags vazias. Eles
publicam metadados quebrados. Ou preencha, ou remova.

### 6.4 Aviso do SpringDoc no log

A cada subida aparecem dois avisos:

```
WARN SpringDoc /v3/api-docs endpoint is enabled by default.
WARN SpringDoc /swagger-ui.html endpoint is enabled by default.
```

Não é erro, é o SpringDoc lembrando que documentação aberta não deveria ir para
produção. Silencie com um profile, não apagando a linha.

### 6.5 Um profile para produção

Hoje existe um `application.properties` só. O padrão do Spring é separar:
`application.properties` (comum) + `application-dev.properties` +
`application-prod.properties`, ativando com `SPRING_PROFILES_ACTIVE`.

O que ganharia: em produção, `spring.jpa.show-sql=false`, os avisos do SpringDoc
desligados e as rotas de documentação fechadas — sem `if` no código.

### 6.6 Não use Eclipse para este projeto

`~/eclipse` está praticamente vazia (sem pasta `plugins`) e `~/.eclipse` tem só
metadados do instalador. **Não há instalação funcional do Spring Tools.**

E não faz falta: o VS Code já tem o pacote Spring completo e o IntelliJ você usa
como principal. Manter três IDEs aumenta a chance de uma delas escrever algo que
a outra não espera. Se quiser experimentar o Eclipse, instale o **Spring Tools 4**
e decida depois — mas sem deixar diretório pela metade.

---

## 7. Ordem sugerida

1. Corrigir o SDK do IntelliJ para 21 (item 1) — é o único que causa bug real.
2. Criar o `.editorconfig` (item 4) — resolve o item 3 junto e vale para todos os
   projetos.
3. Configurar as preferências de Java no VS Code (item 3), mesmo com o
   `.editorconfig`.
4. Decidir a licença (item 6.1).
5. Limpar `pom.xml` (item 6.3) e ajustar `.gitattributes` (item 6.2).
6. Decidir qual IDE é a principal (item 2) e parar de alternar.

Os itens 1 a 3 são de configuração e somam menos de meia hora. O item 6 é o que
mais economiza tempo no longo prazo, porque elimina retrabalho diário.
