package com.sorveteria;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para os testes que precisam do banco.
 *
 * <p>Por que não usar um banco em memória: o H2 tem dialeto, tipos e validações
 * próprios. Um teste que passa nele pode falhar no PostgreSQL de verdade — e é
 * justamente esse o ponto. Aqui o teste roda contra um PostgreSQL real.
 *
 * <p>Como funciona, linha a linha:
 *
 * <ul>
 *   <li>{@code @Testcontainers} liga a extensão do JUnit que sobe e derruba os
 *       containers declarados com {@code @Container}.
 *   <li>{@code static} no campo é obrigatório: o container precisa existir antes
 *       do contexto do Spring ser criado, e é compartilhado entre os testes.
 *   <li>{@code @ServiceConnection} faz o Spring Boot descobrir o container e
 *       configurar a conexão sozinho — sem isso, você teria que ler a porta, o
 *       usuário e a senha no código e injetar via {@code @DynamicPropertySource}.
 *   <li>{@code @SpringBootTest} sobe a aplicação inteira no contexto de teste.
 * </ul>
 *
 * <p>O Spring reaproveita o mesmo contexto entre classes de teste que declaram
 * configurações iguais, então o container é criado uma vez por execução, não uma
 * vez por classe.
 */
@SpringBootTest
@Testcontainers
public abstract class PostgresIntegrationTest {

    /**
     * A imagem é a mesma que você usa localmente, para o teste não rodar num
     * banco diferente do de produção.
     */
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");
}
