package dev.ngrok.akira.bipapp.service;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.repository.BeneficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceUnitTest {

    @Mock
    private BeneficioRepository repository;

    @InjectMocks
    private BeneficioService service;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        beneficioA = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        beneficioA.setId(1L);
        beneficioA.setAtivo(true);
        beneficioA.setVersion(0L);

        beneficioB = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        beneficioB.setId(2L);
        beneficioB.setAtivo(true);
        beneficioB.setVersion(0L);
    }

    @Nested
    @DisplayName("Testes de listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("deve retornar lista vazia quando não há benefícios")
        void listarTodos_quandoVazio_deveRetornarListaVazia() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            List<Beneficio> result = service.listarTodos();

            assertTrue(result.isEmpty());
            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("deve retornar lista com um benefício")
        void listarTodos_comUm_deveRetornarLista() {
            when(repository.findAll()).thenReturn(Collections.singletonList(beneficioA));

            List<Beneficio> result = service.listarTodos();

            assertEquals(1, result.size());
            assertEquals("Beneficio A", result.get(0).getNome());
        }

        @Test
        @DisplayName("deve retornar lista com múltiplos benefícios")
        void listarTodos_comMultiplos_deveRetornarLista() {
            when(repository.findAll()).thenReturn(Arrays.asList(beneficioA, beneficioB));

            List<Beneficio> result = service.listarTodos();

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Testes de buscarPorId")
    class BuscarPorIdTests {

        @Test
        @DisplayName("deve retornar benefício quando existe")
        void buscarPorId_quandoExiste_deveRetornar() {
            when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));

            Beneficio result = service.buscarPorId(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Beneficio A", result.getNome());
        }

        @Test
        @DisplayName("deve lançar exceção quando não existe")
        void buscarPorId_quandoNaoExiste_deveLancarExcecao() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                service.buscarPorId(999L);
            });

            assertTrue(exception.getMessage().contains("999"));
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 100L, 999L})
        @DisplayName("deve buscar com diferentes IDs")
        void buscarPorId_comDiferentesIds(Long id) {
            Beneficio b = new Beneficio("Test", "Desc", BigDecimal.TEN);
            b.setId(id);
            when(repository.findById(id)).thenReturn(Optional.of(b));

            Beneficio result = service.buscarPorId(id);

            assertEquals(id, result.getId());
        }
    }

    @Nested
    @DisplayName("Testes de criar")
    class CriarTests {

        @Test
        @DisplayName("deve criar benefício com todos os campos")
        void criar_comTodosCampos_devePersistir() {
            Beneficio novo = new Beneficio("Novo", "Desc", new BigDecimal("100.00"));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> {
                Beneficio b = i.getArgument(0);
                b.setId(3L);
                return b;
            });

            Beneficio result = service.criar(novo);

            assertNotNull(result.getId());
            assertEquals("Novo", result.getNome());
            verify(repository, times(1)).save(novo);
        }

        @Test
        @DisplayName("deve criar benefício com valor zero")
        void criar_comValorZero_devePersistir() {
            Beneficio novo = new Beneficio("Zero", "Desc", BigDecimal.ZERO);
            when(repository.save(any(Beneficio.class))).thenReturn(novo);

            Beneficio result = service.criar(novo);

            assertEquals(BigDecimal.ZERO, result.getValor());
        }

        @Test
        @DisplayName("deve criar benefício com valor grande")
        void criar_comValorGrande_devePersistir() {
            BigDecimal valorGrande = new BigDecimal("9999999999999.99");
            Beneficio novo = new Beneficio("Grande", "Desc", valorGrande);
            when(repository.save(any(Beneficio.class))).thenReturn(novo);

            Beneficio result = service.criar(novo);

            assertEquals(valorGrande, result.getValor());
        }
    }

    @Nested
    @DisplayName("Testes de atualizar")
    class AtualizarTests {

        @Test
        @DisplayName("deve atualizar todos os campos")
        void atualizar_todosCampos_deveSalvar() {
            Beneficio dados = new Beneficio("Atualizado", "Nova Desc", new BigDecimal("200.00"));
            dados.setAtivo(false);

            when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

            Beneficio result = service.atualizar(1L, dados);

            assertEquals("Atualizado", result.getNome());
            assertEquals("Nova Desc", result.getDescricao());
            assertEquals(new BigDecimal("200.00"), result.getValor());
            assertFalse(result.getAtivo());
        }

        @Test
        @DisplayName("deve manter o id original")
        void atualizar_deveManterId() {
            Beneficio dados = new Beneficio("Novo Nome", "Nova Desc", new BigDecimal("999.00"));
            dados.setAtivo(true);

            when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

            Beneficio result = service.atualizar(1L, dados);

            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("deve lançar exceção se id não existe")
        void atualizar_idNaoExiste_deveLancarExcecao() {
            Beneficio dados = new Beneficio("X", "Y", BigDecimal.ONE);
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> {
                service.atualizar(999L, dados);
            });

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de deletar")
    class DeletarTests {

        @Test
        @DisplayName("deve deletar benefício existente")
        void deletar_existente_deveRemover() {
            when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));
            doNothing().when(repository).delete(beneficioA);

            service.deletar(1L);

            verify(repository, times(1)).delete(beneficioA);
        }

        @Test
        @DisplayName("deve lançar exceção se não existe")
        void deletar_naoExiste_deveLancarExcecao() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> {
                service.deletar(999L);
            });

            verify(repository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Testes de transferir")
    class TransferirTests {

        @Test
        @DisplayName("deve transferir valor corretamente")
        void transferir_valorCorreto_deveAtualizar() {
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

            service.transferir(1L, 2L, new BigDecimal("100.00"));

            assertEquals(new BigDecimal("900.00"), beneficioA.getValor());
            assertEquals(new BigDecimal("600.00"), beneficioB.getValor());
        }

        @Test
        @DisplayName("deve transferir todo o saldo")
        void transferir_todoSaldo_deveZerar() {
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

            service.transferir(1L, 2L, new BigDecimal("1000.00"));

            assertEquals(BigDecimal.ZERO.setScale(2), beneficioA.getValor().setScale(2));
            assertEquals(new BigDecimal("1500.00"), beneficioB.getValor());
        }

        @Test
        @DisplayName("deve lançar exceção para valor nulo")
        void transferir_valorNulo_deveLancarExcecao() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 2L, null);
            });
            assertEquals("Valor deve ser maior que zero", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar exceção para valor zero")
        void transferir_valorZero_deveLancarExcecao() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 2L, BigDecimal.ZERO);
            });
            assertEquals("Valor deve ser maior que zero", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar exceção para valor negativo")
        void transferir_valorNegativo_deveLancarExcecao() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 2L, new BigDecimal("-100.00"));
            });
            assertEquals("Valor deve ser maior que zero", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar exceção quando origem igual destino")
        void transferir_origemIgualDestino_deveLancarExcecao() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 1L, new BigDecimal("100.00"));
            });
            assertEquals("Origem e destino devem ser diferentes", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar exceção quando origem não existe")
        void transferir_origemNaoExiste_deveLancarExcecao() {
            when(repository.findByIdWithLock(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                service.transferir(999L, 2L, new BigDecimal("100.00"));
            });
            assertTrue(ex.getMessage().contains("origem não encontrado"));
        }

        @Test
        @DisplayName("deve lançar exceção quando destino não existe")
        void transferir_destinoNaoExiste_deveLancarExcecao() {
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                service.transferir(1L, 999L, new BigDecimal("100.00"));
            });
            assertTrue(ex.getMessage().contains("destino não encontrado"));
        }

        @Test
        @DisplayName("deve lançar exceção para saldo insuficiente")
        void transferir_saldoInsuficiente_deveLancarExcecao() {
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 2L, new BigDecimal("1000.01"));
            });
            assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        }

        @Test
        @DisplayName("deve lançar exceção mostrando saldo disponível")
        void transferir_saldoInsuficiente_deveMostrarDisponivel() {
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.transferir(1L, 2L, new BigDecimal("9999.00"));
            });
            assertTrue(ex.getMessage().contains("1000.00"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.01", "1.00", "99.99", "500.00", "999.99"})
        @DisplayName("deve aceitar diferentes valores válidos")
        void transferir_valoresValidos_deveAceitar(String valorStr) {
            BigDecimal valor = new BigDecimal(valorStr);
            when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));
            when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

            assertDoesNotThrow(() -> service.transferir(1L, 2L, valor));
        }
    }
}
