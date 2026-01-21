package dev.ngrok.akira.bipapp.service;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.repository.BeneficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

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

    @Test
    @DisplayName("listarTodos deve retornar lista de benefícios")
    void listarTodos_deveRetornarLista() {
        when(repository.findAll()).thenReturn(Arrays.asList(beneficioA, beneficioB));

        List<Beneficio> result = service.listarTodos();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("buscarPorId deve retornar benefício quando existe")
    void buscarPorId_quandoExiste_deveRetornar() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));

        Beneficio result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Beneficio A", result.getNome());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_quandoNaoExiste_deveLancarExcecao() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.buscarPorId(999L);
        });

        assertTrue(exception.getMessage().contains("Benefício não encontrado"));
    }

    @Test
    @DisplayName("criar deve persistir e retornar benefício")
    void criar_devePersistirERetornar() {
        Beneficio novo = new Beneficio("Novo", "Desc", new BigDecimal("100.00"));
        when(repository.save(any(Beneficio.class))).thenReturn(novo);

        Beneficio result = service.criar(novo);

        assertNotNull(result);
        assertEquals("Novo", result.getNome());
        verify(repository, times(1)).save(novo);
    }

    @Test
    @DisplayName("atualizar deve modificar e salvar benefício existente")
    void atualizar_deveModificarESalvar() {
        Beneficio dados = new Beneficio("Atualizado", "Nova Desc", new BigDecimal("200.00"));
        dados.setAtivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));
        when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

        Beneficio result = service.atualizar(1L, dados);

        assertEquals("Atualizado", result.getNome());
        assertEquals("Nova Desc", result.getDescricao());
        assertEquals(new BigDecimal("200.00"), result.getValor());
        assertFalse(result.getAtivo());
        verify(repository, times(1)).save(beneficioA);
    }

    @Test
    @DisplayName("deletar deve remover benefício existente")
    void deletar_deveRemover() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));
        doNothing().when(repository).delete(beneficioA);

        service.deletar(1L);

        verify(repository, times(1)).delete(beneficioA);
    }

    @Test
    @DisplayName("transferir deve mover valor entre benefícios")
    void transferir_deveMoverValor() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));
        when(repository.save(any(Beneficio.class))).thenAnswer(i -> i.getArgument(0));

        service.transferir(1L, 2L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("900.00"), beneficioA.getValor());
        assertEquals(new BigDecimal("600.00"), beneficioB.getValor());
        verify(repository, times(2)).save(any(Beneficio.class));
    }

    @Test
    @DisplayName("transferir deve lançar exceção para valor nulo")
    void transferir_valorNulo_deveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.transferir(1L, 2L, null);
        });

        assertEquals("Valor deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("transferir deve lançar exceção para valor zero")
    void transferir_valorZero_deveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.transferir(1L, 2L, BigDecimal.ZERO);
        });

        assertEquals("Valor deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("transferir deve lançar exceção para valor negativo")
    void transferir_valorNegativo_deveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.transferir(1L, 2L, new BigDecimal("-100.00"));
        });

        assertEquals("Valor deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("transferir deve lançar exceção quando origem igual destino")
    void transferir_origemIgualDestino_deveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.transferir(1L, 1L, new BigDecimal("100.00"));
        });

        assertEquals("Origem e destino devem ser diferentes", exception.getMessage());
    }

    @Test
    @DisplayName("transferir deve lançar exceção quando origem não existe")
    void transferir_origemNaoExiste_deveLancarExcecao() {
        when(repository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.transferir(999L, 2L, new BigDecimal("100.00"));
        });

        assertTrue(exception.getMessage().contains("Benefício origem não encontrado"));
    }

    @Test
    @DisplayName("transferir deve lançar exceção quando destino não existe")
    void transferir_destinoNaoExiste_deveLancarExcecao() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
        when(repository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.transferir(1L, 999L, new BigDecimal("100.00"));
        });

        assertTrue(exception.getMessage().contains("Benefício destino não encontrado"));
    }

    @Test
    @DisplayName("transferir deve lançar exceção para saldo insuficiente")
    void transferir_saldoInsuficiente_deveLancarExcecao() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(beneficioA));
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(beneficioB));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.transferir(1L, 2L, new BigDecimal("9999.00"));
        });

        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
    }
}
