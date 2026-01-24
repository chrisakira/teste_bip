package dev.ngrok.akira.bipapp;

import dev.ngrok.akira.bipapp.controller.TransferenciaRequest;
import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.repository.BeneficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class BeneficioApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BeneficioRepository repository;

    private String baseUrl;
    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/beneficios";
        repository.deleteAll();

        beneficioA = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        beneficioA = repository.save(beneficioA);

        beneficioB = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        beneficioB = repository.save(beneficioB);
    }

    @Nested
    @DisplayName("GET /api/v1/beneficios")
    class ListarTests {

        @Test
        @DisplayName("deve retornar 200 e lista de benefícios")
        void listar_deveRetornar200() {
            ResponseEntity<Beneficio[]> response = restTemplate.getForEntity(baseUrl, Beneficio[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().length);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há registros")
        void listar_semRegistros_deveRetornarVazio() {
            repository.deleteAll();

            ResponseEntity<Beneficio[]> response = restTemplate.getForEntity(baseUrl, Beneficio[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(0, response.getBody().length);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/beneficios/{id}")
    class BuscarTests {

        @Test
        @DisplayName("deve retornar 200 e benefício quando existe")
        void buscar_existente_deveRetornar200() {
            ResponseEntity<Beneficio> response = restTemplate.getForEntity(
                    baseUrl + "/" + beneficioA.getId(), Beneficio.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Beneficio A", response.getBody().getNome());
        }

        @Test
        @DisplayName("deve retornar 500 quando não existe")
        void buscar_naoExistente_deveRetornar500() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/999", String.class);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/beneficios")
    class CriarTests {

        @Test
        @DisplayName("deve retornar 201 e criar benefício")
        void criar_valido_deveRetornar201() {
            Beneficio novo = new Beneficio("Novo", "Descrição Novo", new BigDecimal("300.00"));

            ResponseEntity<Beneficio> response = restTemplate.postForEntity(baseUrl, novo, Beneficio.class);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId());
            assertEquals("Novo", response.getBody().getNome());
        }

        @Test
        @DisplayName("deve persistir no banco de dados")
        void criar_devePersistirNoBanco() {
            long countBefore = repository.count();
            Beneficio novo = new Beneficio("Persistido", "Desc", new BigDecimal("100.00"));

            restTemplate.postForEntity(baseUrl, novo, Beneficio.class);

            long countAfter = repository.count();
            assertEquals(countBefore + 1, countAfter);
        }

        @Test
        @DisplayName("deve criar com valor zero")
        void criar_valorZero_deveCriar() {
            Beneficio novo = new Beneficio("Zero", "Desc", BigDecimal.ZERO);

            ResponseEntity<Beneficio> response = restTemplate.postForEntity(baseUrl, novo, Beneficio.class);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/beneficios/{id}")
    class AtualizarTests {

        @Test
        @DisplayName("deve retornar 200 e atualizar benefício")
        void atualizar_existente_deveRetornar200() {
            beneficioA.setNome("Atualizado");
            beneficioA.setDescricao("Nova Descrição");
            beneficioA.setValor(new BigDecimal("999.00"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Beneficio> request = new HttpEntity<>(beneficioA, headers);

            ResponseEntity<Beneficio> response = restTemplate.exchange(
                    baseUrl + "/" + beneficioA.getId(), HttpMethod.PUT, request, Beneficio.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Atualizado", response.getBody().getNome());
            assertEquals("Nova Descrição", response.getBody().getDescricao());
        }

        @Test
        @DisplayName("deve persistir alterações no banco")
        void atualizar_devePersistirNoBanco() {
            beneficioA.setNome("Persistido");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Beneficio> request = new HttpEntity<>(beneficioA, headers);

            restTemplate.exchange(baseUrl + "/" + beneficioA.getId(), HttpMethod.PUT, request, Beneficio.class);

            Beneficio reloaded = repository.findById(beneficioA.getId()).get();
            assertEquals("Persistido", reloaded.getNome());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/beneficios/{id}")
    class DeletarTests {

        @Test
        @DisplayName("deve retornar 204 e remover benefício")
        void deletar_existente_deveRetornar204() {
            long countBefore = repository.count();

            restTemplate.delete(baseUrl + "/" + beneficioA.getId());

            long countAfter = repository.count();
            assertEquals(countBefore - 1, countAfter);
        }

        @Test
        @DisplayName("deve remover do banco de dados")
        void deletar_deveRemoverDoBanco() {
            restTemplate.delete(baseUrl + "/" + beneficioA.getId());

            assertFalse(repository.existsById(beneficioA.getId()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/beneficios/transferir")
    class TransferirTests {

        @Test
        @DisplayName("deve retornar 200 e realizar transferência")
        void transferir_valido_deveRetornar200() {
            BigDecimal valorOriginalOrigem = beneficioA.getValor();
            BigDecimal valorOriginalDestino = beneficioB.getValor();
            BigDecimal valorTransferencia = new BigDecimal("100.00");

            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", valorTransferencia
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("sucesso"));

            Beneficio origemAtualizado = repository.findById(beneficioA.getId()).get();
            Beneficio destinoAtualizado = repository.findById(beneficioB.getId()).get();

            assertEquals(valorOriginalOrigem.subtract(valorTransferencia), origemAtualizado.getValor());
            assertEquals(valorOriginalDestino.add(valorTransferencia), destinoAtualizado.getValor());
        }

        @Test
        @DisplayName("deve retornar 400 para saldo insuficiente")
        void transferir_saldoInsuficiente_deveRetornar400() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", new BigDecimal("99999.00")
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Saldo insuficiente"));
        }

        @Test
        @DisplayName("deve retornar 400 para valor zero")
        void transferir_valorZero_deveRetornar400() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", BigDecimal.ZERO
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 400 para valor negativo")
        void transferir_valorNegativo_deveRetornar400() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", new BigDecimal("-100.00")
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 400 para origem igual destino")
        void transferir_origemIgualDestino_deveRetornar400() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioA.getId(),
                    "valor", new BigDecimal("100.00")
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 404 para origem não existente")
        void transferir_origemNaoExiste_deveRetornar404() {
            Map<String, Object> request = Map.of(
                    "origemId", 999L,
                    "destinoId", beneficioB.getId(),
                    "valor", new BigDecimal("100.00")
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 404 para destino não existente")
        void transferir_destinoNaoExiste_deveRetornar404() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", 999L,
                    "valor", new BigDecimal("100.00")
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("deve transferir todo o saldo disponível")
        void transferir_todoSaldo_deveFuncionar() {
            Map<String, Object> request = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", beneficioA.getValor()
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/transferir", request, String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());

            Beneficio origemAtualizado = repository.findById(beneficioA.getId()).get();
            assertEquals(BigDecimal.ZERO.setScale(2), origemAtualizado.getValor().setScale(2));
        }
    }

    @Nested
    @DisplayName("Testes de fluxo completo")
    class FluxoCompletoTests {

        @Test
        @DisplayName("deve criar, atualizar e deletar benefício")
        void fluxoCRUD_completo() {
            // Create
            Beneficio novo = new Beneficio("Fluxo", "Desc", new BigDecimal("100.00"));
            ResponseEntity<Beneficio> createResponse = restTemplate.postForEntity(baseUrl, novo, Beneficio.class);
            assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
            Long id = createResponse.getBody().getId();

            // Read
            ResponseEntity<Beneficio> readResponse = restTemplate.getForEntity(baseUrl + "/" + id, Beneficio.class);
            assertEquals(HttpStatus.OK, readResponse.getStatusCode());
            assertEquals("Fluxo", readResponse.getBody().getNome());

            // Update
            Beneficio atualizado = readResponse.getBody();
            atualizado.setNome("Fluxo Atualizado");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Beneficio> updateRequest = new HttpEntity<>(atualizado, headers);
            ResponseEntity<Beneficio> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + id, HttpMethod.PUT, updateRequest, Beneficio.class);
            assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
            assertEquals("Fluxo Atualizado", updateResponse.getBody().getNome());

            // Delete
            restTemplate.delete(baseUrl + "/" + id);
            assertFalse(repository.existsById(id));
        }

        @Test
        @DisplayName("deve realizar múltiplas transferências consecutivas")
        void multiplasTransferencias_devemFuncionar() {
            // Primeira transferência: A -> B (100)
            Map<String, Object> request1 = Map.of(
                    "origemId", beneficioA.getId(),
                    "destinoId", beneficioB.getId(),
                    "valor", 100.00
            );
            restTemplate.postForEntity(baseUrl + "/transferir", request1, String.class);

            // Segunda transferência: B -> A (50)
            Map<String, Object> request2 = Map.of(
                    "origemId", beneficioB.getId(),
                    "destinoId", beneficioA.getId(),
                    "valor", 50.00
            );
            restTemplate.postForEntity(baseUrl + "/transferir", request2, String.class);

            Beneficio aFinal = repository.findById(beneficioA.getId()).get();
            Beneficio bFinal = repository.findById(beneficioB.getId()).get();

            // A: 1000 - 100 + 50 = 950
            assertEquals(new BigDecimal("950.00"), aFinal.getValor());
            // B: 500 + 100 - 50 = 550
            assertEquals(new BigDecimal("550.00"), bFinal.getValor());
        }
    }
}
