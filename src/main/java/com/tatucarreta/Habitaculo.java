package com.tatucarreta;

public class Habitaculo {

    private int idHabitaculo;
    private String nombre;
    private String sector;
    private Integer capacidad;
    private String estado;
    private String observaciones;

    public Habitaculo() {
    }

    public Habitaculo(
            int idHabitaculo,
            String nombre,
            String sector,
            Integer capacidad,
            String estado,
            String observaciones) {

        this.idHabitaculo = idHabitaculo;
        this.nombre = nombre;
        this.sector = sector;
        this.capacidad = capacidad;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public int getIdHabitaculo() {
        return idHabitaculo;
    }

    public void setIdHabitaculo(int idHabitaculo) {
        this.idHabitaculo = idHabitaculo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // Esto se mostrará en el ComboBox
    @Override
    public String toString() {
        return nombre + " - " + sector;
    }
}