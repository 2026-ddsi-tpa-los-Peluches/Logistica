package ar.edu.utn.dds.k3003.model;

import java.util.List;

public interface AlgoritmoAsignacion {
    NecesidadLogistica elegir(List<NecesidadLogistica> necesidades, Integer cantidadADonar);
}
