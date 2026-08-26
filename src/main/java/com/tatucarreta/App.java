package com.tatucarreta;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage escenario) {

        // Crear las tablas necesarias si no existen
        CrearTablas.crearTablaEspecies();
        CrearTablas.crearTablaAnimales();
        CrearTablas.crearTablaIngresos();
        CrearTablas.crearTablaDetalleIngreso();
        CrearTablas.crearTablaHabitaculos();
        CrearTablas.crearTablaPlantelPermanente();
        CrearTablas.crearTablaMovimientos();
        CrearTablas.crearTablaIdentificaciones();
        CrearTablas.crearTablaUsuarios();

        // Abrir Login
        VentanaLogin ventanaLogin = new VentanaLogin();
        ventanaLogin.mostrar(escenario);
    }

    public static void main(String[] args) {
        launch(args);
    }
}