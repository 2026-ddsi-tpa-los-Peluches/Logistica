package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.model.alogoritmos.AlgoritmoPrioridadPorScore;
import ar.edu.utn.dds.k3003.model.alogoritmos.AlgoritmoPrioridadSubatendidos;

import java.util.Comparator;
import java.util.List;

import static ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE;
import static ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum.SUB_ATENDIDOS;

public class AlgoritmoFactory {

    public static AlgoritmoAsignacion crear(TipoAlgoritmoEnum tipo) {
        return switch (tipo) {
            case SUB_ATENDIDOS -> new AlgoritmoPrioridadSubatendidos();
            case PRIORIDAD_POR_SCORE -> new AlgoritmoPrioridadPorScore();
        };
    }
}