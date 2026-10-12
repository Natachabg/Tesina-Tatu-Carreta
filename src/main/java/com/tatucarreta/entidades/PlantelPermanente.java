package com.tatucarreta.entidades;

public class PlantelPermanente {

    private int idPlantel;

    // Animal que pertenece al plantel permanente
    private int idAnimal;

    // Para mostrar el nombre del animal
    private String nombreAnimal;

    // Cantidad actual en este registro
    private int cantidad;

    // Tipo de ubicación
    private String tipoUbicacion;

    // Habitáculo
    private Integer idHabitaculo;

    // Para mostrar el nombre del habitáculo
    private String nombreHabitaculo;

    // Fecha en que ingresó al plantel
    private String fechaIngresoPlantel;

    // Estado del registro
    private String estado;

    // Observaciones
    private String observaciones;


    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    // Puede ser null o estar vacío.
    // El animal NO está obligado a tener identificación.

    private Integer idIdentificacion;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String fechaAsignacionIdentificacion;
    private String observacionesIdentificacion;


    // =========================================================
    // CONSTRUCTOR VACÍO
    // =========================================================

    public PlantelPermanente() {
    }


    // =========================================================
    // CONSTRUCTOR COMPLETO ORIGINAL
    // =========================================================
    //
    // Se mantiene para no romper las ventanas que ya utilizan
    // el constructor anterior.
    //
    // =========================================================

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


    // =========================================================
    // CONSTRUCTOR COMPLETO CON IDENTIFICACIÓN
    // =========================================================

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
            String observaciones,
            Integer idIdentificacion,
            String tipoIdentificacion,
            String numeroIdentificacion,
            String fechaAsignacionIdentificacion,
            String observacionesIdentificacion) {

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

        this.idIdentificacion = idIdentificacion;
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.fechaAsignacionIdentificacion =
                fechaAsignacionIdentificacion;
        this.observacionesIdentificacion =
                observacionesIdentificacion;
    }


    // =========================================================
    // ID PLANTEL
    // =========================================================

    public int getIdPlantel() {
        return idPlantel;
    }

    public void setIdPlantel(int idPlantel) {
        this.idPlantel = idPlantel;
    }


    // =========================================================
    // ID ANIMAL
    // =========================================================

    public int getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(int idAnimal) {
        this.idAnimal = idAnimal;
    }


    // =========================================================
    // NOMBRE ANIMAL
    // =========================================================

    public String getNombreAnimal() {
        return nombreAnimal;
    }

    public void setNombreAnimal(String nombreAnimal) {
        this.nombreAnimal = nombreAnimal;
    }


    // =========================================================
    // CANTIDAD
    // =========================================================

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }


    // =========================================================
    // TIPO UBICACIÓN
    // =========================================================

    public String getTipoUbicacion() {
        return tipoUbicacion;
    }

    public void setTipoUbicacion(String tipoUbicacion) {
        this.tipoUbicacion = tipoUbicacion;
    }


    // =========================================================
    // ID HABITÁCULO
    // =========================================================

    public Integer getIdHabitaculo() {
        return idHabitaculo;
    }

    public void setIdHabitaculo(Integer idHabitaculo) {
        this.idHabitaculo = idHabitaculo;
    }


    // =========================================================
    // NOMBRE HABITÁCULO
    // =========================================================

    public String getNombreHabitaculo() {
        return nombreHabitaculo;
    }

    public void setNombreHabitaculo(String nombreHabitaculo) {
        this.nombreHabitaculo = nombreHabitaculo;
    }


    // =========================================================
    // FECHA INGRESO PLANTEL
    // =========================================================

    public String getFechaIngresoPlantel() {
        return fechaIngresoPlantel;
    }

    public void setFechaIngresoPlantel(
            String fechaIngresoPlantel) {

        this.fechaIngresoPlantel =
                fechaIngresoPlantel;
    }


    // =========================================================
    // ESTADO
    // =========================================================

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    // =========================================================
    // OBSERVACIONES
    // =========================================================

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones) {

        this.observaciones = observaciones;
    }


    // =========================================================
    // ID IDENTIFICACIÓN
    // =========================================================

    public Integer getIdIdentificacion() {
        return idIdentificacion;
    }

    public void setIdIdentificacion(
            Integer idIdentificacion) {

        this.idIdentificacion =
                idIdentificacion;
    }


    // =========================================================
    // TIPO IDENTIFICACIÓN
    // =========================================================

    public String getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(
            String tipoIdentificacion) {

        this.tipoIdentificacion =
                tipoIdentificacion;
    }


    // =========================================================
    // NÚMERO IDENTIFICACIÓN
    // =========================================================

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(
            String numeroIdentificacion) {

        this.numeroIdentificacion =
                numeroIdentificacion;
    }


    // =========================================================
    // FECHA ASIGNACIÓN IDENTIFICACIÓN
    // =========================================================

    public String getFechaAsignacionIdentificacion() {
        return fechaAsignacionIdentificacion;
    }

    public void setFechaAsignacionIdentificacion(
            String fechaAsignacionIdentificacion) {

        this.fechaAsignacionIdentificacion =
                fechaAsignacionIdentificacion;
    }


    // =========================================================
    // OBSERVACIONES IDENTIFICACIÓN
    // =========================================================

    public String getObservacionesIdentificacion() {
        return observacionesIdentificacion;
    }

    public void setObservacionesIdentificacion(
            String observacionesIdentificacion) {

        this.observacionesIdentificacion =
                observacionesIdentificacion;
    }
}