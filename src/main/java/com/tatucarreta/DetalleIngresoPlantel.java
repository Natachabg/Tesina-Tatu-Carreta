package com.tatucarreta;

public class DetalleIngresoPlantel {

    private int idDetalle;
    private String nombreAnimal;
    private int idIngreso;
    private int cantidad;

    public DetalleIngresoPlantel(
            int idDetalle,
            String nombreAnimal,
            int idIngreso,
            int cantidad) {

        this.idDetalle = idDetalle;
        this.nombreAnimal = nombreAnimal;
        this.idIngreso = idIngreso;
        this.cantidad = cantidad;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public String getNombreAnimal() {
        return nombreAnimal;
    }

    public int getIdIngreso() {
        return idIngreso;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {

        return nombreAnimal
                + " - Cantidad: "
                + cantidad
                + " - Ingreso N° "
                + idIngreso;
    }
}