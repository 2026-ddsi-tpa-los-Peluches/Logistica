package ar.edu.utn.dds.k3003.model;


public class NecesidadLogistica {

    String id;
    String entidadId;
    Integer urgencia;
    Integer cantidadObjetivo;
    Integer cantidadRecibida;

    public NecesidadLogistica( String id, String entidadId,  Integer urgencia, Integer cantidadObjetivo, Integer cantidadRecibida){
        this.id = id;
        this.entidadId = entidadId;
        this.urgencia = urgencia;
        this.cantidadObjetivo = cantidadObjetivo;
        this.cantidadRecibida = cantidadRecibida;
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

    public Integer getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(Integer cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public Integer getCantidadFaltante() {
        return cantidadObjetivo - cantidadRecibida;
    }
}








