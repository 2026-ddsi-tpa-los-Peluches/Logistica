package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/asignaciones")
// Este endpoint depende del módulo Donadores y Entidades.
// Para gestionar la donación se necesitan las necesidades insatisfechas
// asociadas al producto donado. Como los módulos no están integrados,
// actualmente este endpoint requiere mocks/fakes para poder testearse.
public class AsignacionController {
    private final Fachada fachada;

    public AsignacionController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarAsignacion(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    fachada.buscarAsignacionPorID(id)
            );
        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}

