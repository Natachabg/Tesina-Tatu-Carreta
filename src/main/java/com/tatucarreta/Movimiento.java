package com.tatucarreta;

public class Movimiento {

    private int idMovimiento;

    // Animal al que corresponde el movimiento
    private int idAnimal;

    // Opcional: permite saber de qué ingreso proviene
    private Integer idIngreso;

    private String fechaMovimiento;

    // LIBERACION / TRASLADO / FALLECIMIENTO / PLANTEL PERMANENTE
    private String tipoMovimiento;

    private int cantidad;

    // Se utiliza principalmente para traslados
    private String destino;

    private String observaciones;


    // =========================
    // CONSTRUCTOR VACÍO
    // =========================

    public Movimiento() {
    }


    // =========================
    // CONSTRUCTOR COMPLETO
    // =========================

    public Movimiento(
            int idMovimiento,
            int idAnimal,
            Integer idIngreso,
            String fechaMovimiento,
            String tipoMovimiento,
            int cantidad,
            String destino,
            String observaciones) {

        this.idMovimiento = idMovimiento;
        this.idAnimal = idAnimal;
        this.idIngreso = idIngreso;
        this.fechaMovimiento = fechaMovimiento;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.destino = destino;
        this.observaciones = observaciones;
    }


    // =========================
    // ID MOVIMIENTO
    // =========================

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }


    // =========================
    // ID ANIMAL
    // =========================

    public int getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(int idAnimal) {
        this.idAnimal = idAnimal;
    }


    // =========================
    // ID INGRESO
    // =========================

    public Integer getIdIngreso() {
        return idIngreso;
    }

    public void setIdIngreso(Integer idIngreso) {
        this.idIngreso = idIngreso;
    }


    // =========================
    // FECHA
    // =========================

    public String getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(String fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }


    // =========================
    // TIPO DE MOVIMIENTO
    // =========================

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }


    // =========================
    // CANTIDAD
    // =========================

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }


    // =========================
    // DESTINO
    // =========================

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }


    // =========================
    // OBSERVACIONES
    // =========================

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
