package dev.ngrok.akira.bipapp.service;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.repository.BeneficioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BeneficioService {

    private final BeneficioRepository repository;

    public BeneficioService(BeneficioRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all registered benefits.
     * Used by the controller to list resources.
     */
    public List<Beneficio> listarTodos() {
        return repository.findAll();
    }

    /**
     * Searches for a benefit by identifier.
     * @param id Benefit identifier
     * @return Found benefit or throws RuntimeException if it does not exist
     */
    public Beneficio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benefício não encontrado: " + id));
    }

    /**
     * Persists a new benefit.
     * @param beneficio Entity to be created
     * @return Created entity with `id`
     */
    public Beneficio criar(Beneficio beneficio) {
        return repository.save(beneficio);
    }

    /**
     * Updates an existing benefit.
     * @param id Benefit identifier
     * @param dados Data for update
     * @return Updated benefit
     */
    public Beneficio atualizar(Long id, Beneficio dados) {
        Beneficio existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setAtivo(dados.getAtivo());
        return repository.save(existente);
    }

    /**
     * Removes a benefit by id.
     * @param id Identifier of the benefit to be removed
     */
    public void deletar(Long id) {
        Beneficio existente = buscarPorId(id);
        repository.delete(existente);
    }

    /**
     * Value transfer between benefits.
     * EJB bug fix:
     * - Validates sufficient balance
     * - Uses pessimistic locking to avoid lost update
     * - Transaction ensures rollback on error
     */
    /**
     * Performs value transfer between two benefits.
     * - Validates value and ids
     * - Uses pessimistic lock via repository
     * - Throws IllegalArgumentException for invalid inputs
     * @param origemId origin benefit id
     * @param destinoId destination benefit id
     * @param valor value to be transferred
     */
    @Transactional
    public void transferir(Long origemId, Long destinoId, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Origem e destino devem ser diferentes");
        }

        // Lock pessimista para evitar condições de corrida
        Beneficio origem = repository.findByIdWithLock(origemId)
                .orElseThrow(() -> new RuntimeException("Benefício origem não encontrado: " + origemId));

        Beneficio destino = repository.findByIdWithLock(destinoId)
                .orElseThrow(() -> new RuntimeException("Benefício destino não encontrado: " + destinoId));

        // Validação de saldo (correção do bug)
        if (origem.getValor().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente. Disponível: " + origem.getValor());
        }

        origem.setValor(origem.getValor().subtract(valor));
        destino.setValor(destino.getValor().add(valor));

        repository.save(origem);
        repository.save(destino);
    }
}
