# Revisão do ambiente de desenvolvimento

Auditoria das três IDEs instaladas nesta máquina — **IntelliJ IDEA**,
**VS Code** e **Spring Tools / Eclipse** — e do ambiente Java em volta delas.
Feita em 13/10/2026, direto nos arquivos de configuração, não de memória.

O objetivo é responder uma pergunta: **o ambiente está montado de um jeito que
não vai te atrapalhar — e que se sustenta se outra pessoa clonar o projeto?**

Vale para os projetos Java; a seção 2 cobre o site.

---

## 0. Antes de tudo: cinco coisas que a versão anterior deste documento errava

Este arquivo já existia. Ao reauditar com os dados reais na mão, encontrei cinco
afirmações minhas que não se sustentam. Corrijo todas abaixo, porque um documento
de estudo que ensina errado é pior que documento nenhum.

| # | O que eu havia afirmado | O que é verdade |
| --- | --- | --- |
| 1 | "O JDK `ms-25` aponta para um caminho que **não existe**" | **Falso.** `/usr/lib/jvm/java-21-openjdk-amd64` existe; o outro, `java-1.21.0-openjdk-amd64`, é só um *symlink* para ele. As duas entradas apontam para **o mesmo JDK 21**. O erro é outro: o *nível de linguagem* (`JDK_25`). |
| 2 | "Seu IntelliJ está em modo **Community** e por isso não tem Spring" | **Meia verdade enganosa.** A instalação **é Ultimate** (`idea-IU-262`, `productCode: IU`, com o plugin `Spring` empacotado). O que existe é o módulo `com.intellij.modules.ultimate` **desligado**. A conclusão correta muda: **reativar**, não trocar de IDE. |
| 3 | "O VS Code está com indentação de **2 espaços para Java** e vai reformatar" | **Falso.** As preferências já têm `"[java]": { "editor.tabSize": 4, "editor.formatOnSave": false }`. Já estava certo. |
| 4 | "**Não há instalação funcional** do Spring Tools" | **Falso.** O STS **5.4.0** (Eclipse 4.41.0) está em `~/opt/spring-tools`, com `SorveteriaBackend` importado e log gerado **hoje**. O que está vazio é `~/eclipse` — que é outro diretório. |
| 5 | Recomendação de usar o **Google Java Style** no VS Code | **Prejudicial.** O Google Style usa **2 espaços**; este projeto usa **4**. Aplicá-lo geraria um diff de puro espaço em todo arquivo Java tocado. |

**A lição, que vale mais que a correção:** as cinco tinham a mesma origem —
eu inferi o estado real a partir de um sinal indireto (`~/eclipse` existir,
`disabled_plugins.txt` existir, o `tabSize` global). É exatamente o erro que
gera "na minha máquina funciona". Os itens 1 e 4 foram corrigidos por olhar
**o arquivo que importa**, não o que parecia importar.

---

## 1. O que está instalado (fatos)

| Ferramenta | Versão real | Onde | Estado |
| --- | --- | --- | --- |
| **IntelliJ IDEA Ultimate** | 2026.2.3 (`IU-262.10968.63`) | Toolbox | Módulo Ultimate **desligado** |
| **VS Code** | 1.141.1 | `/usr/bin/code` | 43 extensões, bem configurado |
| **Spring Tools** | 5.4.0 (Eclipse **4.41.0**) | `~/opt/spring-tools` | Instalado e usado |
| **JDK** | OpenJDK **21.0.12.1** | `/usr/lib/jvm/java-21-openjdk-amd64` | Correto |
| **Maven (sistema)** | 3.9.12 | `/usr/bin/mvn` | — |
| **Maven (wrapper)** | 3.9.16 (wrapper 3.3.4) | `.mvn/wrapper` | Correto |
| **Node / npm** | v24.20.0 / 12.2.0 | — | — |
| **zsh** | `/usr/bin/zsh` (189 linhas de `.zshrc`) | shell do terminal | Correto |
| **Docker** | ativo | — | Necessário ao Testcontainers |

> **Nota de nomenclatura:** o STS moderno se chama
> **"Spring Tools for Eclipse"** e o executável é `SpringToolsForEclipse` —
> não `SpringToolSuite4`. Foi por procurar pelo nome antigo que eu tinha
> concluído que ele não existia.

---

## 2. O ponto central: o nível de Java está certo em três lugares e errado em um

Este é o achado que importa, e ele só aparece quando se olha cada ferramenta
individualmente — todas resolvem "qual Java usar" no seu próprio arquivo:

| Ferramenta | Arquivo que decide | Nível | Estado |
| --- | --- | --- | --- |
| `pom.xml` / `./mvnw` | `<java.version>21</java.version>` | **21** | ✅ Fonte da verdade |
| **Eclipse / STS** | `.settings/org.eclipse.jdt.core.prefs` | **21** | ✅ `compliance=21`, `source=21`, `target=21` |
| **VS Code** | `java.configuration.runtimes` | *ausente* | ⚠️ Adivinhava pela máquina → **corrigido agora** |
| **IntelliJ** | `.idea/misc.xml` | **JDK_25** ❌ | ❌ Analisava como Java 25 |

**Consequência real do erro do IntelliJ:** a IDE analisa com a linguagem do
Java 25 e o Maven compila com 21. Se você escrever algo que só existe do 22 em
diante (por exemplo um *pattern* de tipo novo, ou `SequencedCollection`), a IDE
mostra tudo verde, o `./mvnw verify` quebra — e o erro aparece longe da causa.

**Já corrigido** em `.idea/misc.xml`:

```xml
<!-- antes -->
languageLevel="JDK_25"  project-jdk-name="ms-25"
<!-- depois -->
languageLevel="JDK_21"  project-jdk-name="21"
```

> ⚠️ **O IntelliJ estava aberto quando eu editei o arquivo.** Ele pode reescrever
> esse XML com o estado em memória ao fechar. Então **confirme no GUI**:
> `File → Project Structure → Project` → SDK **21** e *Language level* **21**;
> e em `Settings → Build Tools → Maven → Importing`, o *JDK for importer* em 21.
> O nome `ms-25` também é enganoso e pode ser apagado em
> `File → Project Structure → SDKs`.

O `.idea/` está fora do Git (correto), então isso é só a sua máquina — mas o
efeito do erro é bem real.

---

## 3. IntelliJ IDEA

### 3.1 O módulo Ultimate está desligado — e o Spring está lá, parado

Evidência:

```
~/.config/JetBrains/IntelliJIdea2026.2/disabled_plugins.txt   (01/10/2026)
  com.intellij.modules.ultimate

install: idea-IU-262.10968.63  (productCode: IU)
  plugins/Spring/   ← presente, 162 plugins empacotados
```

Ou seja: **a edição certa está instalada.** Desligar
`com.intellij.modules.ultimate` é o jeito conhecido de rodar o Ultimate como se
fosse Community — normalmente para não receber o pedido de licença. Funciona,
mas você abre mão justamente do que a licença paga: navegação entre beans,
entendimento de `@Autowired`, run configuration ciente de profiles, painel de
endpoints, análise de banco de dados.

**Recomendação, em ordem de retorno:**

1. Verifique a elegibilidade ao **licenciamento gratuito para estudantes**
   (JetBrains Student Pack). Se você tem e-mail de escola técnica, é o melhor
   custo-benefício possível: você já tem o produto instalado, só falta a licença.
2. Se a licença sair, **remova a linha** de `disabled_plugins.txt` (ou pelo GUI:
   `Settings → Plugins → ⚙ → Enable All Downloaded Plugins`). O Spring volta.
3. Se preferir seguir sem licença, aceite conscientemente: com o módulo off, para
   Spring o seu **VS Code é de fato melhor** — ele tem o pacote completo. É uma
   escolha legítima, só não pode ser acidental.

### 3.2 Codificação UTF-8 incompleta (corrigido)

O `.idea/encodings.xml` declarava UTF-8 **só** para `src/main/java`.
Testes e recursos ficavam no padrão da plataforma. O STS, que é a IDE menos
usada, já fazia certo — `src/main`, `src/test` e `resources`, todos em UTF-8.

**Corrigido** agora, com `PROJECT` + os três diretórios. Isso importa porque o
código tem acentuação em strings de negócio ("Sabor do dia", mensagens de erro).

### 3.3 Nenhuma run configuration salva

`workspace.xml` tem **0** ocorrências de `RunManager`. O IntelliJ até gera run
configurations de Spring Boot automaticamente ao detectar `@SpringBootApplication`,
então não é defeito — mas deixar uma configuração nomeada versionada é melhor
para quem for te avaliar e abrir o projeto.

### 3.4 Plugins instalados que você talvez não saiba

`~/.local/share/JetBrains/IntelliJIdea2026.2/`:

| Plugin | Comentário |
| --- | --- |
| `clouds-docker-gateway`, `clouds-docker-impl`, `clouds-kubernetes` | Empacotados no Ultimate |
| `mcpserver` | **Servidor MCP do JetBrains** — expõe a IDE a agentes de IA externos |
| `swagger`, `nodeJS`, `protoeditor`, `vuejs-plugin` | Úteis se você mexer com front/API |

O `mcpserver` merece ciência: ele abre a sua IDE para um cliente de IA
(reversível, mas é uma superfície a mais).

### 3.5 Detalhes menores

- **`.idea/.name` contém `JwtAuthenticationFilter.java`.** Esse arquivo guarda o
  *nome do projeto*; um nome de classe Java ali é lixo de alguma operação
  antiga. Cosmético, mas é o tipo de coisa que aparece numa inspeção.
- Só **`SorveteriaBackend`** está em `recentProjects.xml` e em `trusted-paths.xml`.
  O **site nunca foi aberto no IntelliJ** (e não tem `.idea/`) — coerente com ele
  ser um projeto de VS Code, mas anote que a stack não é uniforme.

---

## 4. VS Code

### 4.1 O que está muito bom

Sem exagero: o `settings.json` deste VS Code é melhor que o de muita gente
sênior. Vale como referência de estudo. Destaques e **por quê**:

- `files.autoSave: afterDelay` + `autoSaveDelay: 1000` — salva sozinho sem
  virar bagunça.
- `files.watcherExclude` com `node_modules`, `target`, `dist` — sem isso, um
  projeto Maven grande travar a IDE.
- `editor.codeActionsOnSave` com `source.organizeImports` — import na ordem,
  automático.
- `telemetry.telemetryLevel: off` — nada de telemetria.
- `cSpell.userWords` com termos do domínio (`Sorveteria`, `Macarrão`, `Feijão`).
- `terminal.integrated.shellIntegration.enabled` + `persistentSessions` —
  histórico de terminal sobrevive a reinício.
- `editor.stickyScroll` e `bracketPairColorization` — leitura de código aninhado.

### 4.2 O buraco: o Java não estava fixado (corrigido)

Não havia `java.configuration.runtimes` nem `java.jdt.ls.java.home`. O *language
server* do Java (`redhat.java`) escolhia o JDK por conta própria — o mesmo risco
do `JDK_25` do IntelliJ, em outra IDE.

**Corrigido** em `.vscode/settings.json` do backend (esse arquivo é ignorado pelo
Git, então é local):

```json
"java.jdt.ls.java.home": "/usr/lib/jvm/java-21-openjdk-amd64",
"java.configuration.runtimes": [
  { "name": "JavaSE-21", "path": "/usr/lib/jvm/java-21-openjdk-amd64", "default": true }
]
```

### 4.3 O Prettier e o ESLint que **não rodam** no site

Aqui há uma contradição silenciosa. No VS Code:

```json
"editor.formatOnSave": true,
"editor.defaultFormatter": "esbenp.prettier-vscode",
"prettier.requireConfig": true,
"editor.codeActionsOnSave": { "source.fixAll.eslint": "explicit" }
```

E no projeto do site:

```
"lint": "oxlint"
.prettierrc        → não existe
config de ESLint   → não existe
dependências       → não têm prettier nem eslint
```

Conclusão, item por item:

- **`prettier.requireConfig: true` + nenhum `.prettierrc`** ⇒ o Prettier
  **recusa-se a formatar**. O `formatOnSave` está ligado e **não faz nada** no
  site. Silencioso: não há erro, simplesmente não formata.
- **`source.fixAll.eslint`** ⇒ a extensão ESLint não tem configuração nenhuma
  para ler, então também não faz nada.
- **O lint real é o `oxlint`**, configurado em `.oxlintrc.json`
  (`react/rules-of-hooks`, `react/only-export-components`) — e **não há extensão
  do oxlint instalada**, então os avisos dele só aparecem no terminal, nunca no
  editor enquanto você digita.

**Como resolver (escolha uma e seja coerente):**

- **Opção A — adotar o Prettier no site:** `npm i -D prettier`, criar
  `.prettierrc` (`{"semi": false, "singleQuote": true, "printWidth": 100}`),
  e o `formatOnSave` passa a funcionar de verdade.
- **Opção B — seguir só com o oxlint:** remover `prettier.requireConfig` e a
  formatação automática, e instalar a extensão `oxc.oxc-vscode` para o lint
  aparecer no editor.

O `extensions.json` do site (criado agora) já recomenda `oxc.oxc-vscode`,
`editorconfig.editorconfig` e `esbenp.prettier-vscode`.

### 4.4 O `.editorconfig` que faltava (criado agora)

Era o arquivo mais barato e mais útil que faltava nos dois projetos. Ele faz a
regra de formatação **pertencer ao repositório** em vez de morar nas preferências
pessoais de cada máquina — é lido nativamente pelo IntelliJ, pelo VS Code (via
extensão, já instalada) e pelo Eclipse.

Criados:

- `SorveteriaBackend/.editorconfig` — **Java em 4 espaços**, YAML/JSON em 2,
  Markdown sem remover espaço no fim da linha (`trim_trailing_whitespace = false`,
  porque dois espaços finais são quebra de linha em Markdown).
- `SiteProfissional/.editorconfig` — 2 espaços em tudo, mesma regra de Markdown.

Detalhe que eu tinha errado antes: **não** forçar XML para 2 espaços. O `pom.xml`
deste projeto usa 4 (convenção do Maven), e uma regra de 2 geraria diff de puro
espaço em cada `mvn` mexido.

### 4.5 Atualizações desligadas

```json
"extensions.autoUpdate": "off",
"extensions.autoCheckUpdates": false,
"update.mode": "none"
```

É uma escolha defensável (estabilidade, controle), mas o efeito colateral é que
**correções de segurança de extensões não chegam**. Sugestão de meio-termo:
mantenha `update.mode: none`, mas faça `code --update-extensions` de vez em quando
— ou reative só `extensions.autoCheckUpdates` (avisa, não instala).

### 4.6 Agentes de IA instalados

`codeium.codeium`, `kilocode.kilo-code`, `saoudrizwan.claude-dev` (Cline) e o
Copilot — este último **globalmente desligado** (`github.copilot.enable: {"*": false}`).

Ponto de atenção concreto: `"kilo-code.new.autoApprove.enabled": true`. Aprovação
automática significa que o agente aplica edições e roda comandos **sem pedir
confirmação**. É a configuração mais perigosa do seu VS Code — sem revisão, um
agente pode alterar arquivos ou executar comandos no seu repositório. Vale
desligar, ou pelo menos restringir por workspace.

### 4.7 Extensões fora do stack, e uma que falta

Instaladas para coisas que seus projetos atuais não usam: `rust-analyzer`,
`jjkim.gdscript`, `ms-dotnettools.*` (C#), `ms-azuretools.*` (Azure),
`ms-mssql.mssql` (+2 do SQL Server). Não atrapalham, mas cada uma consome
memória e tempo de inicialização.

**A que falta e importa:** você tem o cliente de **SQL Server** — e o projeto usa
**PostgreSQL**. Não existe, hoje, nenhuma extensão para falar com o seu banco.
Recomendadas:

| Extensão | ID | Para quê |
| --- | --- | --- |
| PostgreSQL | `ms-ossdata.vscode-pgsql` | Explorar tabelas, rodar query — o que o seu projeto precisa |
| SonarQube for IDE | `SonarSource.sonarlint-vscode` | Análise estática de Java: caça bug e code smell como o SonarLint |

O SonarLint merece destaque para você especificamente: é a ferramenta que pega
justamente a classe de bug que os seus testes de integração acharam (handler
faltando, campo com nome divergente) **antes** de rodar.

### 4.8 Outros detalhes

- `terminal.integrated.defaultProfile.linux: zsh` — **ok**, o zsh está instalado.
- `files.exclude` esconde `**/.idea` no Explorer. Efeito colateral: você não vê
  a config do IntelliJ pelo VS Code. Sem problema — só saiba.
- Nenhum `.vscode/` no site até agora; criei o `extensions.json` (o próprio
  `.gitignore` do projeto já liberava esse arquivo explicitamente, ou seja,
  ele era **previsto** e nunca existiu).

---

## 5. Spring Tools / Eclipse — o mais bem configurado, e o mais esquecido

### 5.1 Está instalado, funciona, e tem o projeto dentro

```
~/opt/spring-tools/            SpringToolsForEclipse (executável)
  .eclipseproduct              → Eclipse Platform 4.41.0
  plugins/org.springframework.boot.ide.branding_*  → Spring Tools 5.4.0
  611 plugins (27 da família Spring; m2e: 34; EGit: 7; Buildship: 5)

~/eclipse-workspace/.metadata  → workspace ativo, log de 13/10
  .projects/  → SorveteriaBackend   (URI: /home/murilo/IdeaProjects/SorveteriaBackend)
             → Logica-programacao-basica
```

O `SorveteriaBackend` está importado com as *natures* certas: `javanature` +
`maven2Nature`, e os *builders* do JDT, do m2e **e do Spring Boot**. Não é
resíduo: é um workspace em uso.

### 5.2 Ele configura o projeto **melhor** que o IntelliJ

Vale registrar, porque inverte o que se espera. Em
`.settings/org.eclipse.jdt.core.prefs`:

```
compliance=21  source=21  target=21  release=enabled
previewFeatures=disabled
forbiddenReference=error
nullanalysis=enabled                      ← análise de null ligada
nonnull=jakarta.annotation.Nonnull        ← anotações do projeto, não genéricas
nullable=org.springframework.lang.Nullable
```

Mais `.settings/org.eclipse.core.resources.prefs` com **UTF-8 em main, test,
resources e no projeto inteiro** — cobertura maior que a que o IntelliJ tinha.

Ou seja: a IDE que você menos usa é a que estava mais alinhada com o `pom.xml`.

### 5.3 Lombok — e aqui está o que mudou hoje

O `.ini` do launcher tem, e o `.ini.bak` não:

```
-javaagent:/home/murilo/opt/spring-tools/lombok.jar
```

Alguém (o instalador do Lombok) rodou **hoje às 12:24**, acrescentou essa linha e
guardou o backup. Isso está **correto**: o projeto usa Lombok
(`pom.xml` → `org.projectlombok:lombok`, e `.factorypath` aponta para a
`lombok-1.18.46.jar`). No Eclipse, sem esse `-javaagent` o `@Getter`/`@Builder`
não existem para o editor e o arquivo fica cheio de falso erro. Está bem feito.

### 5.4 O que falta no Eclipse

- **Sem heap definido.** O `.ini` não tem `-Xmx`. Ele vai rodar no padrão da JVM
  (1/4 da RAM). Para indexar um projeto Maven com frequência, `-Xmx2g` evita
  travamento:
  ```
  -vmargs
  -Xmx2g
  ```
- **Sem ferramenta de cobertura:** nenhum plugin `eclemma` (0 no diretório
  `plugins`). Não é problema se a cobertura vier pelo Maven (ver §7.2).
- **Roda em Java 25** (`-vm` aponta para o JRE *bundled* `justj ... 25.0.4`),
  enquanto o projeto compila em 21. Não é erro — a IDE pode rodar numa versão
  e compilar em outra, e `-Dosgi.requiredJavaVersion=21` está certo. Só anote
  para não confundir "Java da IDE" com "Java do projeto".

### 5.5 O cascão: `~/eclipse` está vazio de verdade

```
find ~/eclipse -type f  →  0 arquivos
```

Um diretório vazio (só `.` e `..`). É resto de uma tentativa de instalação.
Como o STS real está em `~/opt/spring-tools`, esse diretório **não serve para
nada** e só induz ao erro — foi ele que me fez afirmar, errado, que o Spring
Tools não existia.

```bash
rmdir ~/eclipse        # só funciona se estiver vazio; melhor confirmar antes
```

---

## 6. Convivência: três IDEs na mesma pasta

Os três abrem `/home/murilo/IdeaProjects/SorveteriaBackend`. O Eclipse grava
`.project`, `.classpath`, `.settings/` e `.factorypath` **dentro do projeto**.
A pergunta que importa: isso sujou o repositório?

**Não.** O `.gitignore` já cobria tudo. Medido **antes** das adições da seção 7,
os dois comandos voltaram vazios:

```
git status --porcelain  →  vazio
git ls-files --others   →  vazio
```

`.gitignore` do backend, seção `### STS ###`: `.apt_generated`, `.classpath`,
`.factorypath`, `.project`, `.settings`, `.springBeans`, `.sts4-cache` — e
`.idea` na seção do IntelliJ. Está correto e completo.

**O risco real não é o Git, é o tempo:** três IDEs indexando o mesmo projeto
significam três *language servers* disputando CPU e disco, e você editando em
lugares diferentes sem lembrar qual tem o estado mais novo. Foi exatamente assim
que o IntelliJ ficou com `JDK_25` e o Eclipse com `21` no mesmo código.

---

## 7. O que eu já corrigi agora

| Arquivo | Mudança | Rastreável? |
| --- | --- | --- |
| `.idea/misc.xml` | `JDK_25`/`ms-25` → **`JDK_21`/`21`** | Não (`.idea` é ignorado) |
| `.idea/encodings.xml` | UTF-8 para `PROJECT` + `main/resources` + `test/java` | Não |
| `.vscode/settings.json` (backend) | Fixa o JDK 21 no `redhat.java` | Não (`.vscode/` é ignorado) |
| **`SorveteriaBackend/.editorconfig`** | **novo** — Java 4 espaços, charset/EOL | **Sim (novo)** |
| **`SiteProfissional/.editorconfig`** | **novo** — 2 espaços | **Sim (novo)** |
| **`SiteProfissional/.vscode/extensions.json`** | **novo** — extensões recomendadas | **Sim (novo)** |

Os três arquivos novos estão **`?? não rastreados`** nos dois repositórios,
esperando o seu commit. Nada foi commitado ou enviado.

---

## 8. Recomendações

### 8.1 Decidir **uma** IDE principal (o de maior retorno)

O custo de manter três em pé não é o disco: é a config divergir sem você notar.
Critério de escolha:

- **IntelliJ** — se você reativar o módulo Ultimate (melhor suporte a Spring que
  existe). Só vale com a licença resolvida.
- **VS Code** — já está completo e é a escolha pragmática hoje, sem licença.
  Você perde navegação profunda de Spring, ganha zero configuração.
- **Spring Tools** — é o mais alinhado ao Maven/Spring *na configuração*, mas é o
  mais lento e o menos usado. Deixe-o de lado por ora.

Seja qual for: **feche as outras duas** ao mexer no backend.

### 8.2 Curto prazo (menos de 30 minutos)

1. **Confirmar o SDK 21 no IntelliJ** pelo GUI (§2) — é o único bug em aberto.
2. **Decidir a licença do IntelliJ** (§3.1): reativar o Ultimate ou assumir o
   VS Code como principal.
3. **Desligar o auto-aprovar** do Kilo Code (§4.6).
4. **Instalar** `ms-ossdata.vscode-pgsql` e `SonarSource.sonarlint-vscode` (§4.7).
5. **Decidir Prettier ou oxlint** no site (§4.3) — hoje os dois estão inativos.
6. Revisar e commitar os três arquivos novos (§7).

### 8.3 Médio prazo

7. **Cobertura de testes unificada — JaCoCo no `pom.xml`.** Hoje o IntelliJ tem
   o plugin `java-coverage`, o STS não tem nenhum, e o CI não mede nada. JaCoCo
   resolve nos três e no GitHub Actions **com uma configuração só**, e ainda
   permite *quebrar o build* se a cobertura cair de um limite. Como você acabou
   de escrever 30 testes, é o próximo passo natural: transformar "tenho testes"
   em "tenho **este** percentual, verificado em cada push".
8. **SonarLint em todas as IDEs** — o ganho é no VS Code e no IntelliJ, onde você
   passa mais tempo. Ele pega o que o compilador não pega.
9. **Proteger a branch `master`** para exigir o CI verde (o selo já está no
   README; falta torná-lo obrigatório).
10. **`.gitattributes` completo** no backend — hoje só trata o `mvnw`. Trocar por
    `* text=auto eol=lf` evita que fim de linha apareça como alteração.

### 8.4 Hábitos que economizam mais tempo que qualquer plugin

11. **Quando mudar a versão do Java, mude nos lugares juntos** — e são quatro:
    `pom.xml`, `.idea/misc.xml` (ou GUI), `.settings/org.eclipse.jdt.core.prefs`,
    e `java.configuration.runtimes`. Enquanto isso for manual, vai divergir.
    O `.editorconfig` reduz o problema de formatação; a versão do Java continua
    sendo o ponto frágil.
12. **Não commitar `.idea/` completo.** O `.gitignore` já está certo. Se um dia
    quiser compartilhar config do IntelliJ, versione só `codeStyles/` e
    `inspectionProfiles/` — nunca `workspace.xml`.
13. **Uma run configuration nomeada** de Spring Boot, versionada, para o avaliador
    abrir e rodar em um clique.

---

## 9. Achados de bônus (fora de IDE, mas são bugs)

### 9.1 `.gitignore` do backend: uma linha que não faz nada

Nas últimas linhas:

```
## maven ##
.mvn/ .kilo/
```

O Git lê o `.gitignore` **linha por linha**. `.mvn/ .kilo/` é **um único padrão
contendo um espaço** — ou seja, ele só casaria com um arquivo literalmente
chamado `.mvn/ .kilo/`. Na prática, **não casa com nada**, e a intenção
(ignorar `.mvn/`) nunca aconteceu.

Na prática o projeto não sofre, porque `.mvn/wrapper/maven-wrapper.jar` é
ignorado numa linha própria e o `maven-wrapper.properties` **precisa** ser
versionado. Mas a linha está errada e engana quem lê. O correto seria uma
linha por padrão:

```
.mvn/
.kilo/
```

> E vale pensar: você **quer** o `.mvn/wrapper/maven-wrapper.properties` no
> repositório. Ignorar `.mvn/` inteiro removeria a reprodutibilidade do build.

### 9.2 Ainda não existe `LICENSE`

Repositório público, sem licença. Juridicamente, sem licença ninguém pode reusar
o código. Seus outros projetos usam **MIT**; para portfólio é a escolha natural.
Decida — a ausência não é neutra.

### 9.3 `pom.xml` com ruído

Confirmei que continuam presentes:

- Três dependências redundantes: `spring-data-jpa`,
  `jakarta.persistence-api` (já vêm via starters) e
  `spring-boot-configuration-processor` (só serve com `@ConfigurationProperties`).
- Blocos **vazios** nas linhas 18–24: `<licenses><license/></licenses>`,
  `<developers><developer/></developers>` e `<scm>` com tags vazias.
  Eles publicam metadados quebrados no repositório Maven.

Nada disso quebra. Mas `pom.xml` é um arquivo que se **lê**, e cada linha sem
função é ruído para quem vai te avaliar.

---

## 10. Ordem sugerida, do maior para o menor retorno

| # | Ação | Tempo | Por quê |
| --- | --- | --- | --- |
| 1 | Confirmar SDK 21 no IntelliJ (GUI) | 2 min | Único bug que causa erro real |
| 2 | Decidir a licença do IntelliJ | 15 min | Muda qual IDE é a principal |
| 3 | Revisar e commitar os 3 arquivos novos | 5 min | Deixa o ambiente reproduzível |
| 4 | Desligar auto-aprovar do agente de IA | 1 min | Segurança |
| 5 | JaCoCo no `pom.xml` | 30 min | Cobertura nos 3 IDEs e no CI |
| 6 | Instalar Postgres + SonarLint no VS Code | 10 min | Ferramentas que faltam para o stack |
| 7 | Resolver o lint/format do site | 20 min | Hoje é promessa que não roda |
| 8 | `LICENSE`, `pom` limpo, `.gitattributes` | 30 min | Polimento de portfólio |

Os itens 1 a 4 somam menos de meia hora e resolvem tudo que **quebra hoje**. Os
demais são o que transforma "ambiente bom" em "ambiente profissional".
