package dev.ngrok.akira.bipapp.controller;

import dev.ngrok.akira.bipapp.model.Beneficio;
import dev.ngrok.akira.bipapp.service.BeneficioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    /**
     * Lista todos os benefícios.
     * Retorna uma lista de objetos `Beneficio`.
     */
    @GetMapping
    public List<Beneficio> listar() {
        return service.listarTodos();
    }

    /**
     * Busca um benefício por `id`.
     * @param id Identificador do benefício
     * @return Objeto `Beneficio` correspondente
     */
    @GetMapping("/{id}")
    public Beneficio buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    /**
     * Cria um novo benefício.
     * @param beneficio Objeto `Beneficio` no corpo da requisição
     * @return Benefício criado com `id`
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Beneficio criar(@RequestBody Beneficio beneficio) {
        return service.criar(beneficio);
    }

    /**
     * Atualiza um benefício existente.
     * @param id Identificador do benefício a ser atualizado
     * @param beneficio Dados atualizados
     * @return Benefício atualizado
     */
    @PutMapping("/{id}")
    public Beneficio atualizar(@PathVariable Long id, @RequestBody Beneficio beneficio) {
        return service.atualizar(id, beneficio);
    }

    /**
     * Remove um benefício por `id`.
     * Retorna HTTP 204 quando removido com sucesso.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    /**
     * Realiza transferência de valor entre dois benefícios.
     * Corpo: `TransferenciaRequest { origemId, destinoId, valor }`.
     * Retorna 200 com mensagem de sucesso ou 400/404 com erro.
     */
    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(@RequestBody TransferenciaRequest request) {
        try {
            service.transferir(request.getOrigemId(), request.getDestinoId(), request.getValor());
            return ResponseEntity.ok("Transferência realizada com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
