package dev.ngrok.akira.bipapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Context deve carregar todos os beans necessários")
    void contextLoads_comTodosOsBeans() {
        assertNotNull(context);
        assertNotNull(context.getBean("beneficioController"));
        assertNotNull(context.getBean("beneficioService"));
        assertNotNull(context.getBean("beneficioRepository"));
    }

    @Test
    @DisplayName("OpenAPI bean deve estar configurado")
    void openApiBean_deveEstarConfigurado() {
        assertNotNull(context.getBean("customOpenAPI"));
    }
}
