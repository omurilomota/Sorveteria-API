package com.sorveteria;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração dos endpoints: sobem o contexto completo do Spring e
 * exercitam a pilha inteira (controller → service → repository → banco).
 *
 * Cada teste roda dentro de uma transação que é revertida no final, então eles
 * não dependem de ordem de execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("API REST — testes de integração")
class ApiIntegrationTests {

    private static final long ID_INEXISTENTE = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    // ==================== utilitários ====================

    private long criarProduto(long estoque) throws Exception {
        String corpo = """
                {"nome":"Casquinha","preco":4.50,"descricao":"Casquinha simples","estoque":%d,"categoria":"SORVETE"}
                """.formatted(estoque);
        String resposta = mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long criarSabor() throws Exception {
        String resposta = mockMvc.perform(post("/sabores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Chocolate"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long criarPedido() throws Exception {
        String resposta = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Pedido do balcão"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    // ==================== produtos ====================

    @Test
    @DisplayName("POST /produtos cria e devolve 201 com id")
    void criaProdutoRetorna201() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Açaí 300ml","preco":15.00,"descricao":"Açaí com granola",
                                 "estoque":10,"categoria":"ACAI"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Açaí 300ml"));
    }

    @Test
    @DisplayName("GET /produtos lista o que foi criado")
    void listaProdutos() throws Exception {
        criarProduto(5);

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("GET /produtos/{id} devolve o produto")
    void buscaProdutoPorId() throws Exception {
        long id = criarProduto(5);

        mockMvc.perform(get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("GET /produtos/{id} inexistente devolve 404 no formato padronizado")
    void produtoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/produtos/" + ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Não encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("PUT /produtos/{id} atualiza os campos")
    void atualizaProduto() throws Exception {
        long id = criarProduto(5);

        mockMvc.perform(put("/produtos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Casquinha grande","preco":6.00,"descricao":"Casquinha dupla",
                                 "estoque":3,"categoria":"SORVETE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Casquinha grande"))
                .andExpect(jsonPath("$.estoque").value(3));
    }

    @Test
    @DisplayName("PUT /produtos/{id} inexistente devolve 404, não 500")
    void atualizaProdutoInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/produtos/" + ID_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"X","preco":1.00,"descricao":"X","estoque":1,"categoria":"OUTRO"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /produtos/{id} remove e o recurso passa a devolver 404")
    void excluiProduto() throws Exception {
        long id = criarProduto(5);

        mockMvc.perform(delete("/produtos/" + id)).andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/produtos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /produtos com preço negativo devolve 400")
    void produtoComPrecoNegativoRetorna400() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Inválido","preco":-1.00,"descricao":"x","estoque":1,"categoria":"OUTRO"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /produtos sem nome devolve 400")
    void produtoSemNomeRetorna400() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","preco":1.00,"descricao":"x","estoque":1,"categoria":"OUTRO"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /produtos com JSON malformado devolve 400, não 500")
    void produtoComJsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest());
    }

    // ==================== sabores ====================

    @Test
    @DisplayName("POST /sabores cria e devolve 201")
    void criaSaborRetorna201() throws Exception {
        mockMvc.perform(post("/sabores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Morango"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Morango"));
    }

    @Test
    @DisplayName("GET /sabores lista os sabores (resposta paginada)")
    void listaSabores() throws Exception {
        criarSabor();

        // Diferente de /produtos e /pedidos, /sabores devolve um Page do Spring:
        // a lista vem dentro de `content`, com os metadados ao lado.
        mockMvc.perform(get("/sabores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nome").value("Chocolate"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /sabores/{id} devolve o sabor")
    void buscaSaborPorId() throws Exception {
        long id = criarSabor();

        mockMvc.perform(get("/sabores/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("PUT /sabores/{id} atualiza o nome")
    void atualizaSabor() throws Exception {
        long id = criarSabor();

        mockMvc.perform(put("/sabores/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Flocos"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Flocos"));
    }

    @Test
    @DisplayName("DELETE /sabores/{id} remove o sabor")
    void excluiSabor() throws Exception {
        long id = criarSabor();

        mockMvc.perform(delete("/sabores/" + id)).andExpect(status().is2xxSuccessful());
    }

    // ==================== usuários ====================

    @Test
    @DisplayName("POST /usuarios cria usuário com dados válidos")
    void criaUsuario() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Murilo","email":"murilo@exemplo.com","senha":"senhaSegura1",
                                 "telefone":"18998265409","cpf":"12345678901","dataNascimento":"2000-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("murilo@exemplo.com"));
    }

    @Test
    @DisplayName("A resposta do usuário não devolve o CPF completo")
    void respostaDeUsuarioMascaraCpf() throws Exception {
        String resposta = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Murilo","email":"mascara@exemplo.com","senha":"senhaSegura1",
                                 "telefone":"18998265409","cpf":"12345678901","dataNascimento":"2000-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String cpfDevolvido = JsonPath.read(resposta, "$.cpf");
        org.assertj.core.api.Assertions.assertThat(cpfDevolvido)
                .as("o CPF deve sair mascarado, não em claro")
                .isEqualTo("123.***.***-01");
    }

    @Test
    @DisplayName("POST /usuarios com senha curta devolve 400")
    void usuarioComSenhaCurtaRetorna400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Murilo","email":"curta@exemplo.com","senha":"123",
                                 "telefone":"18998265409","cpf":"12345678901","dataNascimento":"2000-01-01"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios com CPF fora do padrão devolve 400")
    void usuarioComCpfInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Murilo","email":"cpf@exemplo.com","senha":"senhaSegura1",
                                 "telefone":"18998265409","cpf":"123","dataNascimento":"2000-01-01"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /usuarios/{id} inexistente devolve 404")
    void usuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/usuarios/" + ID_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    // ==================== pedidos ====================

    @Test
    @DisplayName("POST /pedidos cria pedido com status PENDENTE e total zerado")
    void criaPedido() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Pedido 1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.itensTotal").value(0))
                .andExpect(jsonPath("$.custoTotal").value(0));
    }

    @Test
    @DisplayName("POST /pedidos/{id}/itens adiciona item e recalcula o total")
    void adicionaItemAoPedido() throws Exception {
        long produtoId = criarProduto(10);
        long pedidoId = criarPedido();

        mockMvc.perform(post("/pedidos/" + pedidoId + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":%d,"quantidade":2}
                                """.formatted(produtoId)))
                .andExpect(status().is2xxSuccessful())
                // `itensTotal` conta linhas de item, não a soma das quantidades: são 2
                // unidades de um único produto, então 1 linha. Quem soma as quantidades
                // é o `custoTotal` (2 x 4.50).
                .andExpect(jsonPath("$.itensTotal").value(1))
                .andExpect(jsonPath("$.custoTotal").value(9.00));
    }

    @Test
    @DisplayName("POST /pedidos/{id}/itens com produto inexistente devolve 404")
    void adicionaItemComProdutoInexistente() throws Exception {
        long pedidoId = criarPedido();

        mockMvc.perform(post("/pedidos/" + pedidoId + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":%d,"quantidade":1}
                                """.formatted(ID_INEXISTENTE)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /pedidos/{id}/itens com quantidade zero devolve 400")
    void adicionaItemComQuantidadeInvalida() throws Exception {
        long produtoId = criarProduto(10);
        long pedidoId = criarPedido();

        mockMvc.perform(post("/pedidos/" + pedidoId + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":%d,"quantidade":0}
                                """.formatted(produtoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Transição válida: PENDENTE para EM_PREPARO")
    void transicaoValidaEhAceita() throws Exception {
        long pedidoId = criarPedido();

        mockMvc.perform(patch("/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"EM_PREPARO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    @DisplayName("Transição inválida: PENDENTE para ENTREGUE devolve 409")
    void transicaoInvalidaRetorna409() throws Exception {
        long pedidoId = criarPedido();

        mockMvc.perform(patch("/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ENTREGUE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Status terminal não aceita nova transição")
    void statusTerminalNaoAceitaTransicao() throws Exception {
        long pedidoId = criarPedido();

        mockMvc.perform(patch("/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CANCELADO"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PRONTO"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /pedidos/{id} inexistente devolve 404")
    void pedidoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/pedidos/" + ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /pedidos/{id} remove o pedido")
    void excluiPedido() throws Exception {
        long pedidoId = criarPedido();

        mockMvc.perform(delete("/pedidos/" + pedidoId)).andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/pedidos/" + pedidoId)).andExpect(status().isNotFound());
    }
}
