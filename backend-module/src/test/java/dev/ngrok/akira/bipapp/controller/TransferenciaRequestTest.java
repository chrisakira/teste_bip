package dev.ngrok.akira.bipapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferenciaRequestTest {

    @Test
    @DisplayName("Construtor padrão deve criar objeto vazio")
    void construtorPadrao_deveCriarVazio() {
        TransferenciaRequest request = new TransferenciaRequest();

        assertNull(request.getOrigemId());
        assertNull(request.getDestinoId());
        assertNull(request.getValor());
    }

    @Test
    @DisplayName("Setters e getters devem funcionar corretamente")
    void settersGetters_devemFuncionar() {
        TransferenciaRequest request = new TransferenciaRequest();

        request.setOrigemId(1L);
        request.setDestinoId(2L);
        request.setValor(new BigDecimal("250.00"));

        assertEquals(1L, request.getOrigemId());
        assertEquals(2L, request.getDestinoId());
        assertEquals(new BigDecimal("250.00"), request.getValor());
    }
}
