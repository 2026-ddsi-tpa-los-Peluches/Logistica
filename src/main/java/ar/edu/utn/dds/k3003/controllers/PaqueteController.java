package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.controllers.requests.PaqueteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/paquetes")
public class PaqueteController {

    private final Fachada fachada;

    public PaqueteController(Fachada fachada) {
        this.fachada = fachada;
    }



    // GET /paquete -> Devuelve todos los paquetes almacenados
    @GetMapping
    public ResponseEntity<List<PaqueteDTO>> buscarTodos() {
        try {
            List<PaqueteDTO> paquetes = fachada.obtenerPaquetes();
            return ResponseEntity.ok(paquetes);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // GET /paquete/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPaquetePorId(@PathVariable Integer id) {
        try {
            PaqueteDTO paqueteDTO = fachada.buscarPaquetePorID(id);
            return ResponseEntity.ok(paqueteDTO);

        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> registrarEntrega (@RequestBody Integer asignacionId) {
        // Este endpoint depende de los módulos (Donadores y Entidades) y Donaciones
        // Como los módulos no están integrados,
        // actualmente este endpoint requiere mocks/fakes para poder testearse.

        try {

            fachada.reportarEntrega(asignacionId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}