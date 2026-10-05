package com.tatucarreta;
import com.tatucarreta.config.CrearTablas;import com.tatucarreta.vistas.VentanaLogin;import javafx.application.Application;import javafx.stage.Stage;
public class App extends Application{public void start(Stage stage){CrearTablas.crearTablas();new VentanaLogin().mostrar(stage);}public static void main(String[]args){launch(args);}}
