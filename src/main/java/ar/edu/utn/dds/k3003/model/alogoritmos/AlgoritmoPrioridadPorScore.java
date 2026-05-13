package ar.edu.utn.dds.k3003.model.alogoritmos;

import ar.edu.utn.dds.k3003.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.model.NecesidadLogistica;

import java.util.Comparator;
import java.util.List;

public class AlgoritmoPrioridadPorScore implements AlgoritmoAsignacion {

    @Override
    public NecesidadLogistica elegir(List<NecesidadLogistica> necesidades) {
        return necesidades.stream()
                .max(Comparator.comparing(NecesidadLogistica::score))
                .orElseThrow();
    }
}
