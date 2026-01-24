package dev.ngrok.akira.bipapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferenciaRequestUnitTest {

    @Nested
    @DisplayName("Testes de construtor")
    class ConstrutorTests {

        @Test
        @DisplayName("construtor padrão deve criar objeto vazio")
        void construtorPadrao_deveCriarVazio() {
            TransferenciaRequest request = new TransferenciaRequest();

            assertNull(request.getOrigemId());
            assertNull(request.getDestinoId());
            assertNull(request.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de getters e setters")
    class GetterSetterTests {

        @Test
        @DisplayName("setOrigemId e getOrigemId devem funcionar")
        void setOrigemIdGetOrigemId_devemFuncionar() {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);

            assertEquals(1L, request.getOrigemId());
        }

        @Test
        @DisplayName("setDestinoId e getDestinoId devem funcionar")
        void setDestinoIdGetDestinoId_devemFuncionar() {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setDestinoId(2L);

            assertEquals(2L, request.getDestinoId());
        }

        @Test
        @DisplayName("setValor e getValor devem funcionar")
        void setValorGetValor_devemFuncionar() {
            TransferenciaRequest request = new TransferenciaRequest();
            BigDecimal valor = new BigDecimal("250.00");
            request.setValor(valor);

            assertEquals(valor, request.getValor());
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 100L, Long.MAX_VALUE})
        @DisplayName("setOrigemId deve aceitar diferentes valores")
        void setOrigemId_diferentesValores(Long id) {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(id);

            assertEquals(id, request.getOrigemId());
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 100L, Long.MAX_VALUE})
        @DisplayName("setDestinoId deve aceitar diferentes valores")
        void setDestinoId_diferentesValores(Long id) {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setDestinoId(id);

            assertEquals(id, request.getDestinoId());
        }

        @Test
        @DisplayName("deve aceitar valor zero")
        void setValor_zero_deveAceitar() {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setValor(BigDecimal.ZERO);

            assertEquals(BigDecimal.ZERO, request.getValor());
        }

        @Test
        @DisplayName("deve aceitar valor negativo")
        void setValor_negativo_deveAceitar() {
            TransferenciaRequest request = new TransferenciaRequest();
            BigDecimal negativo = new BigDecimal("-100.00");
            request.setValor(negativo);

            assertEquals(negativo, request.getValor());
        }

        @Test
        @DisplayName("deve aceitar valor grande")
        void setValor_grande_deveAceitar() {
            TransferenciaRequest request = new TransferenciaRequest();
            BigDecimal grande = new BigDecimal("99999999999.99");
            request.setValor(grande);

            assertEquals(grande, request.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de cenários completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("deve permitir configurar todos os campos")
        void configurarTodosCampos_deveFuncionar() {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(2L);
            request.setValor(new BigDecimal("500.00"));

            assertEquals(1L, request.getOrigemId());
            assertEquals(2L, request.getDestinoId());
            assertEquals(new BigDecimal("500.00"), request.getValor());
        }

        @Test
        @DisplayName("deve permitir atualizar campos")
        void atualizarCampos_deveFuncionar() {
            TransferenciaRequest request = new TransferenciaRequest();
            request.setOrigemId(1L);
            request.setDestinoId(2L);
            request.setValor(new BigDecimal("100.00"));

            request.setOrigemId(3L);
            request.setDestinoId(4L);
            request.setValor(new BigDecimal("200.00"));

            assertEquals(3L, request.getOrigemId());
            assertEquals(4L, request.getDestinoId());
            assertEquals(new BigDecimal("200.00"), request.getValor());
        }
    }
}
