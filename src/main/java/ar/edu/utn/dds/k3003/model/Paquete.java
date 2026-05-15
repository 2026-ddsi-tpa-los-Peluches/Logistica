package ar.edu.utn.dds.k3003.model;

public class Paquete{

    String id;
    String donacionID;
    String productoID;
    int cantidad;

    public Paquete(String id, String donacionID,String productoID, int cantidad) {
        this.id = id;
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;

    }

    public String getId() {
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

    public void setId(String id){
        this.id = id;
    }
}
