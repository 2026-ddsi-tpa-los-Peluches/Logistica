package ar.edu.utn.dds.k3003.controllers;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.controllers.requests.DepositoRequest;
import ar.edu.utn.dds.k3003.controllers.requests.DonacionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
                    depositoRequest.tipoAlgoritmo(),
                    depositoRequest.nombre(),
                    depositoRequest.direccion(),
                    depositoRequest.capacidadMaxima(),
                    new ArrayList<>()
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
    public ResponseEntity<?> getDepositoById(@PathVariable Integer id) {

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
    public ResponseEntity<?> deleteDepositoByID(@PathVariable("id") Integer depositoID) {
        try {
            fachada.borrarDepositoPorID(depositoID);
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

    public ResponseEntity<?> gestionarDonacion(
            @PathVariable Integer id,
            @RequestBody DonacionRequest donacionRequest
    ) {
        try {
            AsignacionDTO asignacionDTO =
                    fachada.gestionarDonacion(
                        id,
                        donacionRequest.donacionID(),
                        donacionRequest.productoID(),
                        donacionRequest.cantidad()
                );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(asignacionDTO);

        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}