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
    private int capacidadRestante;

    @Enumerated(EnumType.STRING)
    public TipoAlgoritmoEnum tipoAlgoritmo;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Paquete> stockActual;

    public Deposito() {}


    public Deposito(TipoAlgoritmoEnum tipoAlgoritmo ,String nombre, String direccion, int capacidadMaxima) {
        this.tipoAlgoritmo = tipoAlgoritmo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual = new ArrayList<>();;
        this.capacidadRestante = capacidadMaxima;
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

    public void removerPaquete(Paquete paquete) {
        this.stockActual.remove(paquete);
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

    public int getCapacidadRestante() {return capacidadRestante;}

    public void setCapacidadRestante(int capacidadRestante) {
        this.capacidadRestante = capacidadRestante;
    }

    public void agregarPaquete(Paquete paqueteNuevo) {
        if (paqueteNuevo == null) {
            throw new IllegalArgumentException("El paquete no puede ser nulo.");
        }

        if (!tieneLugar(paqueteNuevo.getCantidad())) {
            throw new IllegalStateException("Capacidad insuficiente en el depósito.");
        }

       //Buscar si ya hay un paquete en la lista con el mismo productoID
        Paquete paqueteExistente = stockActual.stream()
                .filter(p -> p.getProductoID() != null && p.getProductoID().equals(paqueteNuevo.getProductoID()))
                .findFirst()
                .orElse(null);

        if (paqueteExistente != null) {
            // Si existe, le sumamos la cantidad al paquete existente
            paqueteExistente.sumarCantidad(paqueteNuevo.getCantidad());
        } else {
            // Si no existe, agregamos el nuevo paquete a la lista
            this.stockActual.add(paqueteNuevo);
        }

        // 3. Descontamos la capacidad restante
        this.capacidadRestante -= paqueteNuevo.getCantidad();
    }

    public Boolean tieneLugar(int cantidadDonada){
        return cantidadDonada < capacidadRestante;
    }
}
