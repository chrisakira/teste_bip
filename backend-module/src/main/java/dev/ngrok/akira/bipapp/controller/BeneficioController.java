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
     * Lists all benefits.
     * Returns a list of `Beneficio` objects.
     */
    @GetMapping
    public List<Beneficio> listar() {
        return service.listarTodos();
    }

    /**
     * Retrieves a benefit by `id`.
     * @param id Benefit identifier
     * @return Corresponding `Beneficio` object
     */
    @GetMapping("/{id}")
    public Beneficio buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    /**
     * Creates a new benefit.
     * @param beneficio `Beneficio` object in the request body
     * @return Created benefit with `id`
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Beneficio criar(@RequestBody Beneficio beneficio) {
        return service.criar(beneficio);
    }

    /**
     * Updates an existing benefit.
     * @param id Identifier of the benefit to be updated
     * @param beneficio Updated data
     * @return Updated benefit
     */
    @PutMapping("/{id}")
    public Beneficio atualizar(@PathVariable Long id, @RequestBody Beneficio beneficio) {
        return service.atualizar(id, beneficio);
    }

    /**
     * Deletes a benefit by `id`.
     * Returns HTTP 204 when successfully removed.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    /**
     * Performs value transfer between two benefits.
     * Body: `TransferenciaRequest { origemId, destinoId, valor }`.
     * Returns 200 with success message or 400/404 with error.
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
