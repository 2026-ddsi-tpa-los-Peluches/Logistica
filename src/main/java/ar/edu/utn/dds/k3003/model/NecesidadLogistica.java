package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NecesidadLogistica {
    String id;
    String entidadId;
    Integer urgencia;
    Integer cantidadObjetivo;

    public NecesidadLogistica( String id, String entidadId,  Integer urgencia, Integer cantidadObjetivo){
        this.id = id;
        this.entidadId = entidadId;
        this.urgencia = urgencia;
        this.cantidadObjetivo = cantidadObjetivo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public Integer getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Integer urgencia) {
        this.urgencia = urgencia;
    }

    public Integer getCantidadObjetivo() {
        return cantidadObjetivo;
    }

    public void setCantidadObjetivo(Integer cantidadObjetivo) {
        this.cantidadObjetivo = cantidadObjetivo;
    }
}








