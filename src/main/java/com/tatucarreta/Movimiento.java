package com.tatucarreta;

public class Movimiento {

    private int idMovimiento;

    // Puede venir de un detalle de ingreso
    private int idDetalle;

    // Puede estar relacionado directamente con un registro
    // del Plantel Permanente
    private Integer idPlantel;

    private String fechaMovimiento;
    private String tipoMovimiento;
    private int cantidad;
    private String destino;
    private String observaciones;

    // Datos para mostrar en el historial
    private String nombreAnimal;
    private String especie;
    private String numeroActa;

    public Movimiento() {
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdPlantel() {
        return idPlantel;
    }

    public void setIdPlantel(Integer idPlantel) {
        this.idPlantel = idPlantel;
    }

    public String getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(String fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombreAnimal() {
        return nombreAnimal;
    }

    public void setNombreAnimal(String nombreAnimal) {
        this.nombreAnimal = nombreAnimal;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = numeroActa;
    }

    @Override
    public String toString() {
        return tipoMovimiento + " - " + cantidad;
    }
}