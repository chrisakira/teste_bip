package dev.ngrok.akira.bipapp.repository;

import dev.ngrok.akira.bipapp.model.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BeneficioRepositoryTest {

    @Autowired
    private BeneficioRepository repository;

    private Beneficio beneficio;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        beneficio = new Beneficio("Teste", "Descrição", new BigDecimal("1000.00"));
        beneficio = repository.save(beneficio);
    }

    @Test
    @DisplayName("findByIdWithLock deve retornar benefício com lock pessimista")
    void findByIdWithLock_deveRetornarComLock() {
        Optional<Beneficio> result = repository.findByIdWithLock(beneficio.getId());

        assertTrue(result.isPresent());
        assertEquals("Teste", result.get().getNome());
    }

    @Test
    @DisplayName("findByIdWithLock deve retornar vazio quando não existe")
    void findByIdWithLock_quandoNaoExiste_deveRetornarVazio() {
        Optional<Beneficio> result = repository.findByIdWithLock(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("save deve persistir e retornar benefício com id")
    void save_devePersistirComId() {
        Beneficio novo = new Beneficio("Novo", "Desc", new BigDecimal("50.00"));

        Beneficio saved = repository.save(novo);

        assertNotNull(saved.getId());
        assertEquals("Novo", saved.getNome());
    }

    @Test
    @DisplayName("findAll deve retornar todos os benefícios")
    void findAll_deveRetornarTodos() {
        repository.save(new Beneficio("Outro", "Desc", new BigDecimal("200.00")));

        var all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("delete deve remover benefício")
    void delete_deveRemover() {
        long countBefore = repository.count();

        repository.delete(beneficio);

        long countAfter = repository.count();
        assertEquals(countBefore - 1, countAfter);
    }
}
