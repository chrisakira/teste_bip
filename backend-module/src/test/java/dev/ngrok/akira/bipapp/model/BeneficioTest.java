package dev.ngrok.akira.bipapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BeneficioTest {

    @Test
    @DisplayName("Construtor padrão deve criar benefício vazio")
    void construtorPadrao_deveCriarVazio() {
        Beneficio beneficio = new Beneficio();

        assertNull(beneficio.getId());
        assertNull(beneficio.getNome());
        assertNull(beneficio.getDescricao());
        assertNull(beneficio.getValor());
        assertNull(beneficio.getVersion());
    }

    @Test
    @DisplayName("Construtor com parâmetros deve inicializar corretamente")
    void construtorComParametros_deveInicializarCorretamente() {
        Beneficio beneficio = new Beneficio("Nome", "Descrição", new BigDecimal("100.00"));

        assertEquals("Nome", beneficio.getNome());
        assertEquals("Descrição", beneficio.getDescricao());
        assertEquals(new BigDecimal("100.00"), beneficio.getValor());
        assertTrue(beneficio.getAtivo());
    }

    @Test
    @DisplayName("Setters e getters devem funcionar corretamente")
    void settersGetters_devemFuncionar() {
        Beneficio beneficio = new Beneficio();

        beneficio.setId(1L);
        beneficio.setNome("Nome Teste");
        beneficio.setDescricao("Descrição Teste");
        beneficio.setValor(new BigDecimal("500.00"));
        beneficio.setAtivo(false);
        beneficio.setVersion(5L);

        assertEquals(1L, beneficio.getId());
        assertEquals("Nome Teste", beneficio.getNome());
        assertEquals("Descrição Teste", beneficio.getDescricao());
        assertEquals(new BigDecimal("500.00"), beneficio.getValor());
        assertFalse(beneficio.getAtivo());
        assertEquals(5L, beneficio.getVersion());
    }

    @Test
    @DisplayName("Ativo deve ser true por padrão no construtor com parâmetros")
    void ativo_deveSerTruePorPadrao() {
        Beneficio beneficio = new Beneficio("Nome", "Desc", BigDecimal.ONE);

        assertTrue(beneficio.getAtivo());
    }
}
