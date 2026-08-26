package com.tatucarreta;

public class PlantelPermanente {

    private int idPlantel;

    private int idAnimal;

    // NUEVO: para mostrar el nombre en la tabla
    private String nombreAnimal;

    private int cantidad;

    private String tipoUbicacion;

    private Integer idHabitaculo;

    // NUEVO: para mostrar el nombre en la tabla
    private String nombreHabitaculo;

    private String fechaIngresoPlantel;

    private String estado;

    private String observaciones;


    // =========================
    // CONSTRUCTOR VACÍO
    // =========================

    public PlantelPermanente() {
    }


    // =========================
    // CONSTRUCTOR
    // =========================

    public PlantelPermanente(
            int idPlantel,
            int idAnimal,
            String nombreAnimal,
            int cantidad,
            String tipoUbicacion,
            Integer idHabitaculo,
            String nombreHabitaculo,
            String fechaIngresoPlantel,
            String estado,
            String observaciones) {

        this.idPlantel = idPlantel;
        this.idAnimal = idAnimal;
        this.nombreAnimal = nombreAnimal;
        this.cantidad = cantidad;
        this.tipoUbicacion = tipoUbicacion;
        this.idHabitaculo = idHabitaculo;
        this.nombreHabitaculo = nombreHabitaculo;
        this.fechaIngresoPlantel = fechaIngresoPlantel;
        this.estado = estado;
        this.observaciones = observaciones;
    }


    // =========================
    // ID PLANTEL
    // =========================

    public int getIdPlantel() {
        return idPlantel;
    }

    public void setIdPlantel(int idPlantel) {
        this.idPlantel = idPlantel;
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
    // NOMBRE ANIMAL
    // =========================

    public String getNombreAnimal() {
        return nombreAnimal;
    }

    public void setNombreAnimal(String nombreAnimal) {
        this.nombreAnimal = nombreAnimal;
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
    // TIPO UBICACIÓN
    // =========================

    public String getTipoUbicacion() {
        return tipoUbicacion;
    }

    public void setTipoUbicacion(String tipoUbicacion) {
        this.tipoUbicacion = tipoUbicacion;
    }


    // =========================
    // ID HABITÁCULO
    // =========================

    public Integer getIdHabitaculo() {
        return idHabitaculo;
    }

    public void setIdHabitaculo(Integer idHabitaculo) {
        this.idHabitaculo = idHabitaculo;
    }


    // =========================
    // NOMBRE HABITÁCULO
    // =========================

    public String getNombreHabitaculo() {
        return nombreHabitaculo;
    }

    public void setNombreHabitaculo(String nombreHabitaculo) {
        this.nombreHabitaculo = nombreHabitaculo;
    }


    // =========================
    // FECHA INGRESO PLANTEL
    // =========================

    public String getFechaIngresoPlantel() {
        return fechaIngresoPlantel;
    }

    public void setFechaIngresoPlantel(
            String fechaIngresoPlantel) {

        this.fechaIngresoPlantel = fechaIngresoPlantel;
    }


    // =========================
    // ESTADO
    // =========================

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    // =========================
    // OBSERVACIONES
    // =========================

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones) {

        this.observaciones = observaciones;
    }
}