package ar.edu.utn.dds.k3003.controllers;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.controllers.requests.DepositoRequest;
import ar.edu.utn.dds.k3003.controllers.requests.DonacionRequest;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/depositos")
public class DepositoController {

    private final Fachada fachada;


    public DepositoController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping
    public ResponseEntity<?> getAllDepositos() {

        List<DepositoDTO> depositos = fachada.obtenerDepositos();

        if (depositos.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Depositos no encontrados");
        }

        return ResponseEntity.ok(depositos);
    }

    @PostMapping
    public ResponseEntity<?> createDeposito(@RequestBody DepositoRequest depositoRequest) {
        try {
            DepositoDTO depositoDTO = new DepositoDTO(
                    null,
                    null,
                    depositoRequest.nombre(),
                    depositoRequest.direccion(),
                    depositoRequest.capacidadMaxima(),
                    null
            );

            DepositoDTO creado = fachada.agregarDeposito(depositoDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(creado);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepositoById(@PathVariable String id) {

        try {

            return ResponseEntity.ok(
                    fachada.buscarDepositoPorID(id)
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepositoByID(@PathVariable("id") String depositoID) {
        try {
            DepositoDTO depositoDTO = fachada.borrarDepositoPorID(depositoID);
            return ResponseEntity
                    .noContent()
                    .build();

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{id}/donacion")
    // Este endpoint depende del módulo Donadores y Entidades.
    // Para gestionar la donación se necesitan las necesidades insatisfechas
    // asociadas al producto donado. Como los módulos no están integrados,
    // actualmente este endpoint requiere mocks/fakes para poder testearse.
    public ResponseEntity<?> gestionarDonacion(
            @PathVariable String id,
            @RequestBody DonacionRequest donacionRequest
    ) {
        try {
            DepositoDTO deposito = fachada.gestionarDonacion(
                    id,
                    donacionRequest.donacionID(),
                    donacionRequest.productoID(),
                    donacionRequest.cantidad()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(deposito);

        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}