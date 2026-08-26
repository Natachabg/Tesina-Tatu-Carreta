package com.tatucarreta;

public class Ingreso {

    private int idIngreso;
    private String numeroActa;
    private String fechaIngreso;
    private String organismoProcedencia;
    private String responsableEntrega;
    private String procedencia;
    private String motivoIngreso;
    private String documentacion;
    private String observaciones;

    public Ingreso() {
    }

    public Ingreso(
            int idIngreso,
            String numeroActa,
            String fechaIngreso,
            String organismoProcedencia,
            String responsableEntrega,
            String procedencia,
            String motivoIngreso,
            String documentacion,
            String observaciones) {

        this.idIngreso = idIngreso;
        this.numeroActa = numeroActa;
        this.fechaIngreso = fechaIngreso;
        this.organismoProcedencia = organismoProcedencia;
        this.responsableEntrega = responsableEntrega;
        this.procedencia = procedencia;
        this.motivoIngreso = motivoIngreso;
        this.documentacion = documentacion;
        this.observaciones = observaciones;
    }

    public int getIdIngreso() {
        return idIngreso;
    }

    public void setIdIngreso(int idIngreso) {
        this.idIngreso = idIngreso;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = numeroActa;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getOrganismoProcedencia() {
        return organismoProcedencia;
    }

    public void setOrganismoProcedencia(String organismoProcedencia) {
        this.organismoProcedencia = organismoProcedencia;
    }

    public String getResponsableEntrega() {
        return responsableEntrega;
    }

    public void setResponsableEntrega(String responsableEntrega) {
        this.responsableEntrega = responsableEntrega;
    }

    public String getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(String procedencia) {
        this.procedencia = procedencia;
    }

    public String getMotivoIngreso() {
        return motivoIngreso;
    }

    public void setMotivoIngreso(String motivoIngreso) {
        this.motivoIngreso = motivoIngreso;
    }

    public String getDocumentacion() {
        return documentacion;
    }

    public void setDocumentacion(String documentacion) {
        this.documentacion = documentacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}