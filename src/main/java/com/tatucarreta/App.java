package com.tatucarreta;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage escenario) {

        // =========================
        // CREAR TABLAS
        // =========================

        CrearTablas.crearTablaEspecies();

        CrearTablas.crearTablaAnimales();

        CrearTablas.crearTablaIngresos();

        CrearTablas.crearTablaDetalleIngreso();

        CrearTablas.crearTablaHabitaculos();

        CrearTablas.crearTablaPlantelPermanente();

// Actualizar Plantel Permanente para permitir carga inicial
CrearTablas.actualizarTablaPlantelPermanente();

CrearTablas.crearTablaMovimientos();

// Actualizar movimientos para trabajar con id_detalle
CrearTablas.actualizarTablaMovimientos();

// Agregar referencia opcional al Plantel Permanente
CrearTablas.actualizarTablaMovimientosPlantel();

CrearTablas.crearTablaIdentificaciones();

CrearTablas.crearTablaUsuarios();

        // =========================
        // ABRIR LOGIN
        // =========================

        VentanaLogin ventanaLogin =
                new VentanaLogin();

        ventanaLogin.mostrar(escenario);
    }


    public static void main(String[] args) {

        launch(args);
    }
}