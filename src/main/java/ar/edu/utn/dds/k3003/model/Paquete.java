package ar.edu.utn.dds.k3003.model;

public class Paquete{

    String paqueteID;
    String donacionID;
    String productoID;
    int cantidad;

    public Paquete(String paqueteID, String donacionID,String productoID, int cantidad) {
        this.paqueteID = paqueteID;
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;

    }

    public String getPaqueteID() {
        return paqueteID;
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

    public void setPaqueteID(String paqueteID){
        this.paqueteID = paqueteID;
    }
}
