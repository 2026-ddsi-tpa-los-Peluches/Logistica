package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable Integer id
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

    @PostMapping
    public ResponseEntity<?> AsignarProductoAEntidad(
            @RequestBody NecesidadMaterialDTO request
    ) {
        try {

            Integer cantidadAsignada = fachada.asignarProductoAEntidad(request);

            return ResponseEntity.ok(cantidadAsignada);

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }

    }
}

