package ar.edu.utn.dds.k3003.model.alogoritmos;

import ar.edu.utn.dds.k3003.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.model.NecesidadLogistica;

import java.util.Comparator;
import java.util.List;

public class AlgoritmoPrioridadPorScore implements AlgoritmoAsignacion {

    @Override
    public NecesidadLogistica elegir(List<NecesidadLogistica> necesidades, Integer cantidadDonada) {
        return necesidades.stream()
                .max(Comparator.comparing(n-> calcularScore(n,cantidadDonada)))
                .orElseThrow();
    }


    private double calcularScore(NecesidadLogistica necesidad, Integer cantidadDonada) {

        double urgencia = necesidad.getUrgencia();
        double cantidadProducto = cantidadDonada;
        double cantidadObjetivo = necesidad.getCantidadObjetivo();

        return urgencia / (cantidadProducto / cantidadObjetivo);
    }
}
