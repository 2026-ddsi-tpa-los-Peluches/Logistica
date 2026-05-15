package ar.edu.utn.dds.k3003.repositories.asignaciones;

import ar.edu.utn.dds.k3003.model.Asignacion;

import java.util.Optional;

public interface AsignacionesRepository {
    Optional<Asignacion> findById(String id);

    Asignacion save(Asignacion asignacion);

    Asignacion deleteById(String id);

    Optional<Asignacion> findAsignacionByPaqueteId(String paqueteId);


}
