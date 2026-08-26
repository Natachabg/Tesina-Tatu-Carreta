package com.tatucarreta;

public class Animal {

    private int idAnimal;
    private int idEspecie;
    private String nombreVulgar;
    private String nombreCientifico;

    private int cantidadActual;
    private String origen;

    private String estado;

    public Animal() {
    }

    public Animal(
            int idAnimal,
            int idEspecie,
            String nombreVulgar,
            String nombreCientifico,
            int cantidadActual,
            String origen,
            String estado) {

        this.idAnimal = idAnimal;
        this.idEspecie = idEspecie;
        this.nombreVulgar = nombreVulgar;
        this.nombreCientifico = nombreCientifico;
        this.cantidadActual = cantidadActual;
        this.origen = origen;
        this.estado = estado;
    }

    public int getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(int idAnimal) {
        this.idAnimal = idAnimal;
    }

    public int getIdEspecie() {
        return idEspecie;
    }

    public void setIdEspecie(int idEspecie) {
        this.idEspecie = idEspecie;
    }

    public String getNombreVulgar() {
        return nombreVulgar;
    }

    public void setNombreVulgar(String nombreVulgar) {
        this.nombreVulgar = nombreVulgar;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(int cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Esto es lo que verá el ComboBox
    @Override
    public String toString() {
        return idAnimal
                + " - "
                + nombreVulgar
                + " (Cantidad: "
                + cantidadActual
                + ")";
    }
}