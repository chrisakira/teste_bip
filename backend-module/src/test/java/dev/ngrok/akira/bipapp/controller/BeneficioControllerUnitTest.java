package dev.ngrok.akira.bipapp.controller;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.service.BeneficioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioControllerUnitTest {

    @Mock
    private BeneficioService service;

    @InjectMocks
    private BeneficioController controller;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        beneficioA = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        beneficioA.setId(1L);
        beneficioA.setAtivo(true);

        beneficioB = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        beneficioB.setId(2L);
        beneficioB.setAtivo(true);
    }

    @Nested
    @DisplayName("Testes de listar")
    class ListarTests {

        @Test
        @DisplayName("deve retornar lista vazia")
        void listar_vazio_deveRetornarListaVazia() {
            when(service.listarTodos()).thenReturn(Collections.emptyList());

            List<Beneficio> result = controller.listar();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("deve retornar lista com benefícios")
        void listar_comItens_deveRetornarLista() {
            when(service.listarTodos()).thenReturn(Arrays.asList(beneficioA, beneficioB));

            List<Beneficio> result = controller.listar();

            assertEquals(2, result.size());
            assertEquals("Beneficio A", result.get(0).getNome());
            assertEquals("Beneficio B", result.get(1).getNome());
        }
    }

    @Nested
    @DisplayName("Testes de buscar")
    class BuscarTests {

        @Test
        @DisplayName("deve retornar benefício por id")
        void buscar_existente_deveRetornar() {
            when(service.buscarPorId(1L)).thenReturn(beneficioA);

            Beneficio result = controller.buscar(1L);

            assertEquals(1L, result.getId());
            assertEquals("Beneficio A", result.getNome());
        }

        @Test
        @DisplayName("deve propagar exceção quando não encontrado")
        void buscar_naoExistente_devePropagar() {
            when(service.buscarPorId(999L)).thenThrow(new RuntimeException("Não encontrado"));

            assertThrows(RuntimeException.class, () -> controller.buscar(999L));
        }
    }

    @Nested
    @DisplayName("Testes de criar")
    class CriarTests {

        @Test
        @DisplayName("deve criar e retornar benefício")
        void criar_valido_deveRetornar() {
            Beneficio novo = new Beneficio("Novo", "Desc", new BigDecimal("100.00"));
            novo.setId(3L);
            when(service.criar(any(Beneficio.class))).thenReturn(novo);

            Beneficio result = controller.criar(novo);

            assertEquals(3L, result.getId());
            assertEquals("Novo", result.getNome());
        }

        @Test
        @DisplayName("deve chamar service.criar uma vez")
        void criar_deveChamarService() {
            Beneficio novo = new Beneficio("Test", "Test", BigDecimal.ONE);
            when(service.criar(any())).thenReturn(novo);

            controller.criar(novo);

            verify(service, times(1)).criar(any(Beneficio.class));
        }
    }

    @Nested
    @DisplayName("Testes de atualizar")
    class AtualizarTests {

        @Test
        @DisplayName("deve atualizar e retornar benefício")
        void atualizar_valido_deveRetornar() {
            Beneficio atualizado = new Beneficio("Atualizado", "Nova Desc", new BigDecimal("999.00"));
            atualizado.setId(1L);
            when(service.atualizar(eq(1L), any(Beneficio.class))).thenReturn(atualizado);

            Beneficio result = controller.atualizar(1L, atualizado);

            assertEquals("Atualizado", result.getNome());
        }

        @Test
        @DisplayName("deve propagar exceção se não encontrado")
        void atualizar_naoExistente_devePropagar() {
            when(service.atualizar(eq(999L), any())).thenThrow(new RuntimeException("Não encontrado"));

            assertThrows(RuntimeException.class, () -> controller.atualizar(999L, beneficioA));
        }
    }

    @Nested
    @DisplayName("Testes de deletar")
    class DeletarTests {

        @Test
        @DisplayName("deve deletar sem retorno")
        void deletar_existente_deveChamarService() {
            doNothing().when(service).deletar(1L);

            controller.deletar(1L);

            verify(service, times(1)).deletar(1L);
        }

        @Test
        @DisplayName("deve propagar exceção se não encontrado")
        void deletar_naoExistente_devePropagar() {
            doThrow(new RuntimeException("Não encontrado")).when(service).deletar(999L);

            assertThrows(RuntimeException.class, () -> controller.deletar(999L));
        }
    }

    @Nested
    @DisplayName("Testes de transferir")
    class TransferirTests {

        @Test
        @DisplayName("deve retornar 200 com sucesso")
        void transferir_valido_deveRetornar200() {
            doNothing().when(service).transferir(eq(1L), eq(2L), any(BigDecimal.class));

            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(2L);
            request.setValor(new BigDecimal("100.00"));

            ResponseEntity<String> response = controller.transferir(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 para IllegalArgumentException")
        void transferir_argumentoInvalido_deveRetornar400() {
            doThrow(new IllegalArgumentException("Saldo insuficiente"))
                    .when(service).transferir(any(), any(), any());

            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(2L);
            request.setValor(new BigDecimal("99999.00"));

            ResponseEntity<String> response = controller.transferir(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Saldo insuficiente"));
        }

        @Test
        @DisplayName("deve retornar 404 para RuntimeException")
        void transferir_naoEncontrado_deveRetornar404() {
            doThrow(new RuntimeException("Benefício não encontrado"))
                    .when(service).transferir(any(), any(), any());

            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(999L);
            request.setDestinoId(2L);
            request.setValor(new BigDecimal("100.00"));

            ResponseEntity<String> response = controller.transferir(request);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().contains("não encontrado"));
        }

        @Test
        @DisplayName("deve retornar 400 para valor zero")
        void transferir_valorZero_deveRetornar400() {
            doThrow(new IllegalArgumentException("Valor deve ser maior que zero"))
                    .when(service).transferir(any(), any(), any());

            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(2L);
            request.setValor(BigDecimal.ZERO);

            ResponseEntity<String> response = controller.transferir(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 400 para origem igual destino")
        void transferir_origemIgualDestino_deveRetornar400() {
            doThrow(new IllegalArgumentException("Origem e destino devem ser diferentes"))
                    .when(service).transferir(any(), any(), any());

            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(1L);
            request.setValor(new BigDecimal("100.00"));

            ResponseEntity<String> response = controller.transferir(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }
}
