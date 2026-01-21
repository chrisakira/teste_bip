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
     * Retorna todos os benefícios cadastrados.
     * Usado pelo controller para listar recursos.
     */
    public List<Beneficio> listarTodos() {
        return repository.findAll();
    }

    /**
     * Busca um benefício pelo identificador.
     * @param id Identificador do benefício
     * @return Benefício encontrado ou lança RuntimeException se não existir
     */
    public Beneficio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benefício não encontrado: " + id));
    }

    /**
     * Persiste um novo benefício.
     * @param beneficio Entidade a ser criada
     * @return Entidade criada com `id`
     */
    public Beneficio criar(Beneficio beneficio) {
        return repository.save(beneficio);
    }

    /**
     * Atualiza um benefício existente.
     * @param id Identificador do benefício
     * @param dados Dados para atualização
     * @return Benefício atualizado
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
     * Remove um benefício pelo id.
     * @param id Identificador do benefício a ser removido
     */
    public void deletar(Long id) {
        Beneficio existente = buscarPorId(id);
        repository.delete(existente);
    }

    /**
     * Transferência de valor entre benefícios.
     * Correção do bug do EJB:
     * - Valida saldo suficiente
     * - Usa locking pessimista para evitar lost update
     * - Transação garante rollback em caso de erro
     */
    /**
     * Realiza transferência de valor entre dois benefícios.
     * - Valida valor e ids
     * - Usa lock pessimista via repository
     * - Lança IllegalArgumentException para entradas inválidas
     * @param origemId id do benefício origem
     * @param destinoId id do benefício destino
     * @param valor valor a ser transferido
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
