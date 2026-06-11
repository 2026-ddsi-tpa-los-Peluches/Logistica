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


    public Paquete(){}

    public Paquete( String donacionID,String productoID, int cantidad) {
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;

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
}
