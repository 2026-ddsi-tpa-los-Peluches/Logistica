package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
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
    public ResponseEntity<?> registrarEntrega (@PathVariable String id) {

        try {
            PaqueteDTO paqueteDTO = fachada.buscarPaquetePorID(id);
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