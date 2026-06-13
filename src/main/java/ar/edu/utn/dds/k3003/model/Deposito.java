package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "depositos")
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String direccion;
    private int capacidadMaxima;

    @Enumerated(EnumType.STRING)
    public TipoAlgoritmoEnum tipoAlgoritmo;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Paquete> stockActual;

    public Deposito() {}


    public Deposito(TipoAlgoritmoEnum tipoAlgoritmo ,String nombre, String direccion, int capacidadMaxima, List<Paquete> stockActual) {
        this.tipoAlgoritmo = tipoAlgoritmo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual = stockActual;
    }


    // getters
    public Integer getId() {
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

    public void setId(Integer id) {
        this.id = id;
    }
}
