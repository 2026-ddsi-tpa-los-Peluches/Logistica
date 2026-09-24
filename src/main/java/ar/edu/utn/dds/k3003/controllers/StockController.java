package ar.edu.utn.dds.k3003.controllers;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoStockDTO;
import ar.edu.utn.dds.k3003.controllers.requests.PaqueteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final Fachada fachada;

    public StockController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<Void> guardarEnStock(@RequestBody DonacionMensajeDTO donacion) {
        try {
            fachada.guardarEnStock(
                    donacion.depositoID(),
                    donacion.donacionID(),
                    donacion.productoID(),
                    donacion.cantidadDonada()
            );
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DepositoStockDTO>> obtenerTodoElStock() {
        try {
            List<DepositoStockDTO> stock = fachada.obtenerTodoElStock();
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
