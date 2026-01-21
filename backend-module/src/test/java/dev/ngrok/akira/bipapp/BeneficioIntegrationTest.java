package dev.ngrok.akira.bipapp;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.repository.BeneficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeneficioIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BeneficioRepository repository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/beneficios";
        repository.deleteAll();

        Beneficio a = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        Beneficio b = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        repository.save(a);
        repository.save(b);
    }

    @Test
    @DisplayName("Integração: GET /api/v1/beneficios deve listar todos")
    void listar_deveRetornarTodos() {
        ResponseEntity<Beneficio[]> response = restTemplate.getForEntity(baseUrl, Beneficio[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    @DisplayName("Integração: GET /api/v1/beneficios/{id} deve retornar benefício")
    void buscar_deveRetornarBeneficio() {
        Beneficio saved = repository.findAll().get(0);

        ResponseEntity<Beneficio> response = restTemplate.getForEntity(
                baseUrl + "/" + saved.getId(), Beneficio.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getNome(), response.getBody().getNome());
    }

    @Test
    @DisplayName("Integração: POST /api/v1/beneficios deve criar benefício")
    void criar_deveCriarBeneficio() {
        Beneficio novo = new Beneficio("Novo", "Desc Novo", new BigDecimal("200.00"));

        ResponseEntity<Beneficio> response = restTemplate.postForEntity(baseUrl, novo, Beneficio.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Novo", response.getBody().getNome());
    }

    @Test
    @DisplayName("Integração: PUT /api/v1/beneficios/{id} deve atualizar benefício")
    void atualizar_deveAtualizarBeneficio() {
        Beneficio saved = repository.findAll().get(0);
        saved.setNome("Atualizado");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Beneficio> request = new HttpEntity<>(saved, headers);

        ResponseEntity<Beneficio> response = restTemplate.exchange(
                baseUrl + "/" + saved.getId(), HttpMethod.PUT, request, Beneficio.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Atualizado", response.getBody().getNome());
    }

    @Test
    @DisplayName("Integração: DELETE /api/v1/beneficios/{id} deve remover benefício")
    void deletar_deveRemoverBeneficio() {
        Beneficio saved = repository.findAll().get(0);
        long countBefore = repository.count();

        restTemplate.delete(baseUrl + "/" + saved.getId());

        long countAfter = repository.count();
        assertEquals(countBefore - 1, countAfter);
    }

    @Test
    @DisplayName("Integração: POST /api/v1/beneficios/transferir deve realizar transferência")
    void transferir_deveRealizarTransferencia() {
        Beneficio origem = repository.findAll().get(0);
        Beneficio destino = repository.findAll().get(1);
        BigDecimal valorOriginal = origem.getValor();
        BigDecimal valorDestinoOriginal = destino.getValor();

        Map<String, Object> request = Map.of(
                "origemId", origem.getId(),
                "destinoId", destino.getId(),
                "valor", 100.00
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/transferir", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Transferência realizada com sucesso"));

        Beneficio origemAtualizado = repository.findById(origem.getId()).get();
        Beneficio destinoAtualizado = repository.findById(destino.getId()).get();

        assertEquals(valorOriginal.subtract(new BigDecimal("100.00")), origemAtualizado.getValor());
        assertEquals(valorDestinoOriginal.add(new BigDecimal("100.00")), destinoAtualizado.getValor());
    }

    @Test
    @DisplayName("Integração: POST /api/v1/beneficios/transferir deve falhar com saldo insuficiente")
    void transferir_saldoInsuficiente_deveFalhar() {
        Beneficio origem = repository.findAll().get(0);
        Beneficio destino = repository.findAll().get(1);

        Map<String, Object> request = Map.of(
                "origemId", origem.getId(),
                "destinoId", destino.getId(),
                "valor", 999999.00
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/transferir", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Saldo insuficiente"));
    }
}
