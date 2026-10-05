package com.tatucarreta.entidades;

public class Papelera {

    private int idPapelera;
    private String tipoRegistro;
    private int idRegistro;
    private String datos;
    private String fechaEliminacion;

    // =========================
    // CONSTRUCTOR VACÍO
    // =========================

    public Papelera() {
    }

    // =========================
    // CONSTRUCTOR COMPLETO
    // =========================

    public Papelera(
            int idPapelera,
            String tipoRegistro,
            int idRegistro,
            String datos,
            String fechaEliminacion) {

        this.idPapelera = idPapelera;
        this.tipoRegistro = tipoRegistro;
        this.idRegistro = idRegistro;
        this.datos = datos;
        this.fechaEliminacion = fechaEliminacion;
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public int getIdPapelera() {
        return idPapelera;
    }

    public void setIdPapelera(int idPapelera) {
        this.idPapelera = idPapelera;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(String tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

    public String getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(String fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
}
