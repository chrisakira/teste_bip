package dev.ngrok.akira.bipapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.service.BeneficioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioControllerTest {

    @Mock
    private BeneficioService service;

    @InjectMocks
    private BeneficioController controller;

    private ObjectMapper objectMapper;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        beneficioA = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        beneficioA.setId(1L);
        beneficioA.setAtivo(true);

        beneficioB = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        beneficioB.setId(2L);
        beneficioB.setAtivo(true);
    }

    @Test
    @DisplayName("listar deve retornar lista de benefícios")
    void listar_deveRetornarLista() {
        when(service.listarTodos()).thenReturn(Arrays.asList(beneficioA, beneficioB));

        List<Beneficio> result = controller.listar();

        assertEquals(2, result.size());
        assertEquals("Beneficio A", result.get(0).getNome());
        verify(service, times(1)).listarTodos();
    }

    @Test
    @DisplayName("buscar deve retornar benefício por id")
    void buscar_deveRetornarBeneficio() {
        when(service.buscarPorId(1L)).thenReturn(beneficioA);

        Beneficio result = controller.buscar(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Beneficio A", result.getNome());
        verify(service, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("criar deve criar e retornar benefício")
    void criar_deveCriarBeneficio() {
        Beneficio novo = new Beneficio("Novo", "Desc", new BigDecimal("100.00"));
        novo.setId(3L);

        when(service.criar(any(Beneficio.class))).thenReturn(novo);

        Beneficio result = controller.criar(novo);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Novo", result.getNome());
        verify(service, times(1)).criar(any(Beneficio.class));
    }

    @Test
    @DisplayName("atualizar deve atualizar e retornar benefício")
    void atualizar_deveAtualizarBeneficio() {
        Beneficio atualizado = new Beneficio("Atualizado", "Nova Desc", new BigDecimal("200.00"));
        atualizado.setId(1L);
        atualizado.setAtivo(false);

        when(service.atualizar(eq(1L), any(Beneficio.class))).thenReturn(atualizado);

        Beneficio result = controller.atualizar(1L, atualizado);

        assertNotNull(result);
        assertEquals("Atualizado", result.getNome());
        verify(service, times(1)).atualizar(eq(1L), any(Beneficio.class));
    }

    @Test
    @DisplayName("deletar deve remover benefício")
    void deletar_deveRemoverBeneficio() {
        doNothing().when(service).deletar(1L);

        controller.deletar(1L);

        verify(service, times(1)).deletar(1L);
    }

    @Test
    @DisplayName("transferir deve realizar transferência com sucesso")
    void transferir_deveRealizarTransferencia() {
        doNothing().when(service).transferir(eq(1L), eq(2L), any(BigDecimal.class));

        TransferenciaRequest request = new TransferenciaRequest();
        request.setOrigemId(1L);
        request.setDestinoId(2L);
        request.setValor(new BigDecimal("100.00"));

        ResponseEntity<String> response = controller.transferir(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Transferência realizada com sucesso"));
        verify(service, times(1)).transferir(eq(1L), eq(2L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("transferir deve retornar 400 para argumentos inválidos")
    void transferir_argumentoInvalido_deveRetornar400() {
        doThrow(new IllegalArgumentException("Saldo insuficiente"))
                .when(service).transferir(eq(1L), eq(2L), any(BigDecimal.class));

        TransferenciaRequest request = new TransferenciaRequest();
        request.setOrigemId(1L);
        request.setDestinoId(2L);
        request.setValor(new BigDecimal("99999.00"));

        ResponseEntity<String> response = controller.transferir(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Saldo insuficiente"));
    }

    @Test
    @DisplayName("transferir deve retornar 404 quando não encontrado")
    void transferir_naoEncontrado_deveRetornar404() {
        doThrow(new RuntimeException("Benefício origem não encontrado"))
                .when(service).transferir(eq(999L), eq(2L), any(BigDecimal.class));

        TransferenciaRequest request = new TransferenciaRequest();
        request.setOrigemId(999L);
        request.setDestinoId(2L);
        request.setValor(new BigDecimal("100.00"));

        ResponseEntity<String> response = controller.transferir(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Benefício origem não encontrado"));
    }
}
