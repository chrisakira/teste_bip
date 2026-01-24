package dev.ngrok.akira.bipapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private OpenApiConfig config;

    @BeforeEach
    void setUp() {
        config = new OpenApiConfig();
    }

    @Test
    @DisplayName("customOpenAPI deve retornar instância de OpenAPI")
    void customOpenAPI_deveRetornarInstancia() {
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
    }

    @Test
    @DisplayName("customOpenAPI deve conter informações corretas")
    void customOpenAPI_deveConterInfoCorreta() {
        OpenAPI openAPI = config.customOpenAPI();
        Info info = openAPI.getInfo();

        assertNotNull(info);
        assertEquals("BIP App API", info.getTitle());
        assertEquals("0.0.1", info.getVersion());
        assertEquals("API documentation for the BIP application", info.getDescription());
    }
}
