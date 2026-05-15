package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.controllers.requests.PaqueteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final Fachada fachada;

    public EntregaController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<?> registrarEntrega (@RequestBody PaqueteRequest paqueteRequest) {
        // Este endpoint depende de los módulos (Donadores y Entidades) y Donaciones
        // Como los módulos no están integrados,
        // actualmente este endpoint requiere mocks/fakes para poder testearse.

        try {
                PaqueteDTO paqueteDTO = fachada.buscarPaquetePorID(paqueteRequest.paqueteId());
            fachada.reportarEntrega(paqueteDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}