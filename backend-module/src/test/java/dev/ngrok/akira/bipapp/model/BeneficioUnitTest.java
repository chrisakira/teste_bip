package dev.ngrok.akira.bipapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BeneficioUnitTest {

    @Nested
    @DisplayName("Testes de construtor")
    class ConstrutorTests {

        @Test
        @DisplayName("construtor padrão deve criar objeto vazio")
        void construtorPadrao_deveCriarVazio() {
            Beneficio beneficio = new Beneficio();

            assertNull(beneficio.getId());
            assertNull(beneficio.getNome());
            assertNull(beneficio.getDescricao());
            assertNull(beneficio.getValor());
            assertNull(beneficio.getVersion());
        }

        @Test
        @DisplayName("construtor com parâmetros deve inicializar campos")
        void construtorComParametros_deveInicializar() {
            Beneficio beneficio = new Beneficio("Nome", "Descrição", new BigDecimal("100.00"));

            assertEquals("Nome", beneficio.getNome());
            assertEquals("Descrição", beneficio.getDescricao());
            assertEquals(new BigDecimal("100.00"), beneficio.getValor());
            assertTrue(beneficio.getAtivo());
        }

        @Test
        @DisplayName("construtor com valores nulos deve aceitar")
        void construtorComNulos_deveAceitar() {
            Beneficio beneficio = new Beneficio(null, null, null);

            assertNull(beneficio.getNome());
            assertNull(beneficio.getDescricao());
            assertNull(beneficio.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de getters e setters")
    class GetterSetterTests {

        @Test
        @DisplayName("setId e getId devem funcionar")
        void setIdGetId_devemFuncionar() {
            Beneficio beneficio = new Beneficio();
            beneficio.setId(42L);

            assertEquals(42L, beneficio.getId());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Nome 1", "Nome com espaços", "Ação", "123", ""})
        @DisplayName("setNome deve aceitar diferentes valores")
        void setNome_deveAceitar(String nome) {
            Beneficio beneficio = new Beneficio();
            beneficio.setNome(nome);

            assertEquals(nome, beneficio.getNome());
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("setNome deve aceitar null")
        void setNome_deveAceitarNull(String nome) {
            Beneficio beneficio = new Beneficio();
            beneficio.setNome(nome);

            assertNull(beneficio.getNome());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Desc 1", "Descrição longa com muitos caracteres", "", "Ação"})
        @DisplayName("setDescricao deve aceitar diferentes valores")
        void setDescricao_deveAceitar(String descricao) {
            Beneficio beneficio = new Beneficio();
            beneficio.setDescricao(descricao);

            assertEquals(descricao, beneficio.getDescricao());
        }

        @Test
        @DisplayName("setValor e getValor devem funcionar com BigDecimal")
        void setValorGetValor_devemFuncionar() {
            Beneficio beneficio = new Beneficio();
            BigDecimal valor = new BigDecimal("12345.67");
            beneficio.setValor(valor);

            assertEquals(valor, beneficio.getValor());
        }

        @Test
        @DisplayName("setValor deve aceitar zero")
        void setValor_deveAceitarZero() {
            Beneficio beneficio = new Beneficio();
            beneficio.setValor(BigDecimal.ZERO);

            assertEquals(BigDecimal.ZERO, beneficio.getValor());
        }

        @Test
        @DisplayName("setValor deve aceitar valor negativo")
        void setValor_deveAceitarNegativo() {
            Beneficio beneficio = new Beneficio();
            BigDecimal negativo = new BigDecimal("-100.00");
            beneficio.setValor(negativo);

            assertEquals(negativo, beneficio.getValor());
        }

        @Test
        @DisplayName("setAtivo e getAtivo devem funcionar")
        void setAtivoGetAtivo_devemFuncionar() {
            Beneficio beneficio = new Beneficio();

            beneficio.setAtivo(true);
            assertTrue(beneficio.getAtivo());

            beneficio.setAtivo(false);
            assertFalse(beneficio.getAtivo());
        }

        @Test
        @DisplayName("setVersion e getVersion devem funcionar")
        void setVersionGetVersion_devemFuncionar() {
            Beneficio beneficio = new Beneficio();
            beneficio.setVersion(5L);

            assertEquals(5L, beneficio.getVersion());
        }

        @Test
        @DisplayName("setVersion deve aceitar zero")
        void setVersion_deveAceitarZero() {
            Beneficio beneficio = new Beneficio();
            beneficio.setVersion(0L);

            assertEquals(0L, beneficio.getVersion());
        }
    }

    @Nested
    @DisplayName("Testes de valores de borda")
    class ValoresBordaTests {

        @Test
        @DisplayName("deve aceitar valor máximo de BigDecimal")
        void valor_maximo_deveAceitar() {
            Beneficio beneficio = new Beneficio();
            BigDecimal valorGrande = new BigDecimal("9999999999999.99");
            beneficio.setValor(valorGrande);

            assertEquals(valorGrande, beneficio.getValor());
        }

        @Test
        @DisplayName("deve aceitar valor com muitas casas decimais")
        void valor_muitasCasas_deveAceitar() {
            Beneficio beneficio = new Beneficio();
            BigDecimal valor = new BigDecimal("123.456789");
            beneficio.setValor(valor);

            assertEquals(valor, beneficio.getValor());
        }

        @Test
        @DisplayName("deve aceitar id máximo Long")
        void id_maximo_deveAceitar() {
            Beneficio beneficio = new Beneficio();
            beneficio.setId(Long.MAX_VALUE);

            assertEquals(Long.MAX_VALUE, beneficio.getId());
        }

        @Test
        @DisplayName("deve aceitar nome vazio")
        void nome_vazio_deveAceitar() {
            Beneficio beneficio = new Beneficio();
            beneficio.setNome("");

            assertEquals("", beneficio.getNome());
        }
    }
}
