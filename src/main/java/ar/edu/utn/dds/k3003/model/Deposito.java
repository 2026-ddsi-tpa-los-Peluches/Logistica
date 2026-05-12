package ar.edu.utn.dds.k3003.model;


import java.util.ArrayList;
import java.util.List;

public class Deposito {
    private String id;
    private String nombre;
    private String direccion;
    private int capacidadMaxima;
    private int stockActual;
    private List<Paquete> listaDePaquetes;

    public Deposito(String id, String nombre, String direccion, int capacidadMaxima) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual = 0;
        this.listaDePaquetes = new ArrayList<>();
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

    public int getStockActual() {
        return stockActual;
    }

    public List<Paquete> getListaDePaquetes(){
        //return stockActual;
        return new ArrayList<>(listaDePaquetes);
    }

}