package ar.edu.utn.dds.k3003.worker.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.worker.model.alogoritmos.AlgoritmoPrioridadPorScore;
import ar.edu.utn.dds.k3003.worker.model.alogoritmos.AlgoritmoPrioridadSubatendidos;

public class AlgoritmoFactory {

    public static AlgoritmoAsignacion crear(TipoAlgoritmoEnum tipo) {
        return switch (tipo) {
            case SUB_ATENDIDOS -> new AlgoritmoPrioridadSubatendidos();
            case PRIORIDAD_POR_SCORE -> new AlgoritmoPrioridadPorScore();
        };
    }
}