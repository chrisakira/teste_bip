package dev.ngrok.akira.bipapp.repository;

import dev.ngrok.akira.bipapp.model.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BeneficioRepositoryIntegrationTest {

    @Autowired
    private BeneficioRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        entityManager.flush();

        beneficioA = new Beneficio("Beneficio A", "Descrição A", new BigDecimal("1000.00"));
        beneficioA = entityManager.persistAndFlush(beneficioA);

        beneficioB = new Beneficio("Beneficio B", "Descrição B", new BigDecimal("500.00"));
        beneficioB = entityManager.persistAndFlush(beneficioB);
    }

    @Nested
    @DisplayName("Testes de findAll")
    class FindAllTests {

        @Test
        @DisplayName("deve retornar todos os benefícios")
        void findAll_deveRetornarTodos() {
            List<Beneficio> result = repository.findAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há registros")
        void findAll_semRegistros_deveRetornarVazio() {
            repository.deleteAll();
            entityManager.flush();

            List<Beneficio> result = repository.findAll();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes de findById")
    class FindByIdTests {

        @Test
        @DisplayName("deve retornar benefício quando existe")
        void findById_existente_deveRetornar() {
            Optional<Beneficio> result = repository.findById(beneficioA.getId());

            assertTrue(result.isPresent());
            assertEquals("Beneficio A", result.get().getNome());
        }

        @Test
        @DisplayName("deve retornar vazio quando não existe")
        void findById_naoExistente_deveRetornarVazio() {
            Optional<Beneficio> result = repository.findById(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes de findByIdWithLock")
    class FindByIdWithLockTests {

        @Test
        @DisplayName("deve retornar benefício com lock pessimista")
        void findByIdWithLock_existente_deveRetornar() {
            Optional<Beneficio> result = repository.findByIdWithLock(beneficioA.getId());

            assertTrue(result.isPresent());
            assertEquals("Beneficio A", result.get().getNome());
        }

        @Test
        @DisplayName("deve retornar vazio quando não existe")
        void findByIdWithLock_naoExistente_deveRetornarVazio() {
            Optional<Beneficio> result = repository.findByIdWithLock(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes de save")
    class SaveTests {

        @Test
        @DisplayName("deve persistir novo benefício")
        void save_novo_devePersistir() {
            Beneficio novo = new Beneficio("Novo", "Descrição", new BigDecimal("200.00"));

            Beneficio saved = repository.save(novo);

            assertNotNull(saved.getId());
            assertEquals("Novo", saved.getNome());
        }

        @Test
        @DisplayName("deve atualizar benefício existente")
        void save_existente_deveAtualizar() {
            beneficioA.setNome("Nome Atualizado");

            Beneficio updated = repository.save(beneficioA);

            assertEquals("Nome Atualizado", updated.getNome());
            assertEquals(beneficioA.getId(), updated.getId());
        }

        @Test
        @DisplayName("deve persistir com valor zero")
        void save_valorZero_devePersistir() {
            Beneficio novo = new Beneficio("Zero", "Desc", BigDecimal.ZERO);

            Beneficio saved = repository.save(novo);

            assertEquals(BigDecimal.ZERO.setScale(2), saved.getValor().setScale(2));
        }

        @Test
        @DisplayName("deve persistir com valor grande")
        void save_valorGrande_devePersistir() {
            BigDecimal valorGrande = new BigDecimal("9999999999999.99");
            Beneficio novo = new Beneficio("Grande", "Desc", valorGrande);

            Beneficio saved = repository.save(novo);

            assertEquals(valorGrande, saved.getValor());
        }

        @Test
        @DisplayName("deve incrementar version após atualização")
        void save_deveIncrementarVersion() {
            Long versionInicial = beneficioA.getVersion();
            beneficioA.setNome("Atualizado");
            
            repository.saveAndFlush(beneficioA);
            entityManager.clear();
            
            Beneficio reloaded = repository.findById(beneficioA.getId()).get();
            
            assertTrue(reloaded.getVersion() >= versionInicial);
        }
    }

    @Nested
    @DisplayName("Testes de delete")
    class DeleteTests {

        @Test
        @DisplayName("deve deletar benefício existente")
        void delete_existente_deveRemover() {
            long countBefore = repository.count();

            repository.delete(beneficioA);
            entityManager.flush();

            long countAfter = repository.count();
            assertEquals(countBefore - 1, countAfter);
        }

        @Test
        @DisplayName("deve deletar por id")
        void deleteById_existente_deveRemover() {
            long countBefore = repository.count();

            repository.deleteById(beneficioA.getId());
            entityManager.flush();

            long countAfter = repository.count();
            assertEquals(countBefore - 1, countAfter);
        }
    }

    @Nested
    @DisplayName("Testes de count")
    class CountTests {

        @Test
        @DisplayName("deve retornar contagem correta")
        void count_deveRetornarCorreto() {
            long count = repository.count();

            assertEquals(2, count);
        }

        @Test
        @DisplayName("deve retornar zero quando vazio")
        void count_vazio_deveRetornarZero() {
            repository.deleteAll();
            entityManager.flush();

            long count = repository.count();

            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("Testes de existsById")
    class ExistsByIdTests {

        @Test
        @DisplayName("deve retornar true quando existe")
        void existsById_existente_deveRetornarTrue() {
            boolean exists = repository.existsById(beneficioA.getId());

            assertTrue(exists);
        }

        @Test
        @DisplayName("deve retornar false quando não existe")
        void existsById_naoExistente_deveRetornarFalse() {
            boolean exists = repository.existsById(999L);

            assertFalse(exists);
        }
    }
}
