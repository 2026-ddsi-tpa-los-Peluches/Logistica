package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;

import static ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum.COMPLETADA;

public class Asignacion {
    String id;
    Paquete paquete;
    String necesidadID;
    LocalDateTime fecha;
    EstadoAsginacionEnum estado;


    public Asignacion(String id, Paquete paquete, String necesidadID, LocalDateTime fecha, EstadoAsginacionEnum estado) {
        this.id = id;
        this.paquete = paquete;
        this.necesidadID = necesidadID;
        this.fecha = fecha;
        this.estado = estado;

    }


    //getters


    public String getId() {
        return id;
    }

    public Paquete getPaquete() {
        return paquete;
    }

    public String getNecesidadID() {
        return necesidadID;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoAsginacionEnum getEstado() {
        return estado;
    }



    public void completada(){
        this.estado = EstadoAsginacionEnum.COMPLETADA;
    }
}


