package ar.edu.utn.dds.k3003.controllers.requests;

import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionesRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositosRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IntegracionBaseDeDatos {

    private final AsignacionesRepository asignacionesRepository;
    private final PaquetesRepository paquetesRepository;
    private final DepositosRepository depositosRepository;

    public IntegracionBaseDeDatos(
            AsignacionesRepository asignacionesRepository,
            PaquetesRepository paquetesRepository,
            DepositosRepository depositosRepository
    ) {
        this.asignacionesRepository = asignacionesRepository;
        this.paquetesRepository = paquetesRepository;
        this.depositosRepository = depositosRepository;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetDatabase() {

        asignacionesRepository.deleteAll();
        paquetesRepository.deleteAll();
        depositosRepository.deleteAll();

        return ResponseEntity.ok("Base de datos reseteada correctamente");
    }
}