package com.sorveteria;

import org.junit.jupiter.api.Test;

/**
 * Teste de contexto: verifica que a aplicação consegue subir.
 *
 * Parece bobo, mas ele reprova por vários motivos que não têm nada de bobo:
 * bean que não consegue ser criado, configuração ausente, e — desde que o
 * schema passou a ser validado — entidade que não bate com a migração.
 */
class SorveteriaBackendApplicationTests extends PostgresIntegrationTest {

    @Test
    void contextLoads() {
    }

}
