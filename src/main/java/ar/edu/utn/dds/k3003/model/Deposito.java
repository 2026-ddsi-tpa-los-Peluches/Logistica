package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

import java.util.ArrayList;
import java.util.List;

public class Deposito {
    private String id;
    private String nombre;
    private String direccion;
    private int capacidadMaxima;
    public TipoAlgoritmoEnum tipoAlgoritmo;
    private List<Paquete> stockActual;

    public Deposito(String id, TipoAlgoritmoEnum tipoAlgoritmo ,String nombre, String direccion, int capacidadMaxima, List<Paquete> stockActual) {
        this.id = id;
        this.tipoAlgoritmo = tipoAlgoritmo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual = stockActual;
    }


    // getters
    public String getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public List<Paquete> getStockActual(){
        //return stockActual;
        return new ArrayList<>(stockActual);
    }

    public TipoAlgoritmoEnum getTipoAlgoritmo() {
        return tipoAlgoritmo;
    }

    public void setTipoAlgoritmo(TipoAlgoritmoEnum tipoAlgoritmo) {
        this.tipoAlgoritmo = tipoAlgoritmo;
    }
}