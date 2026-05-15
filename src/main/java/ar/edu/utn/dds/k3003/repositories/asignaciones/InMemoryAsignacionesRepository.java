package ar.edu.utn.dds.k3003.repositories.asignaciones;

import ar.edu.utn.dds.k3003.model.Asignacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
@Repository
public class InMemoryAsignacionesRepository implements AsignacionesRepository {
    private List<Asignacion> asignaciones = new ArrayList<>();
    private AtomicLong idSecuencial = new AtomicLong(1);

    @Override
    public Optional<Asignacion> findById(String id) {
        return asignaciones.stream().filter(a -> a.getId().equals(id)).findFirst();
    }
    @Override
    public Asignacion save(Asignacion asignacion) {
        asignacion.setId(String.valueOf(idSecuencial.getAndIncrement()));
        asignaciones.add(asignacion);
        return asignacion;
    }
    @Override
    public Asignacion deleteById(String id) {
        Asignacion asignacion = findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("No existe asignación")
                );
        asignaciones.remove(asignacion);
        return asignacion;
    }

    @Override
    public Optional<Asignacion> findAsignacionByPaqueteId(String paqueteId) {
        return asignaciones.stream().filter(a -> a.getPaquete().getPaqueteID().equals(paqueteId)).findFirst();
    }


}