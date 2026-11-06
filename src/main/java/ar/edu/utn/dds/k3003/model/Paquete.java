package ar.edu.utn.dds.k3003.model;


import jakarta.persistence.*;

@Entity
@Table(name = "paquetes")
public class Paquete{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String donacionID;
    String productoID;
    int cantidad;
    Integer depositoID;


    public Paquete(){}

    public Paquete( String donacionID,String productoID, int cantidad, Integer depositoID) {
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;
        this.depositoID = depositoID;

    }

    public Integer getId() {
        return id;
    }

    public String getDonacionID() {
        return donacionID;
    }

    public String getProductoID() {
        return productoID;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public void sumarCantidad(int cantidadExtra) {
        this.cantidad += cantidadExtra;
    }

    public void restarCantidad(int cantidadExtra) {
        this.cantidad -= cantidadExtra;
    }

    public Integer getDepositoID() {
        return depositoID;
    }
}
