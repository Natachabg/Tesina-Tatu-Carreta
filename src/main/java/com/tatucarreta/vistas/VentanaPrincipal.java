package com.tatucarreta.vistas;

import com.tatucarreta.config.CrearTablas;
import com.tatucarreta.ui.Estilos;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.Supplier;

public class VentanaPrincipal extends Application {
    private BorderPane root; private VBox menu; private Button seleccionado; private String rolActual="Administrador";
    @Override public void start(Stage stage){CrearTablas.crearTablas();mostrar(stage,"Administrador");}
    public void mostrar(Stage stage){mostrar(stage,"Administrador");}
    public void mostrar(Stage stage,String rol){
        rolActual=rol==null?"Usuario":rol; root=new BorderPane(); Estilos.aplicarVentana(root);
        menu=new VBox(6); menu.setPadding(new Insets(14,12,12,12)); menu.setPrefWidth(235); menu.setStyle("-fx-background-color:"+Estilos.VERDE_OSCURO+";");

        ImageView logo=new ImageView(); try{logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));logo.setFitWidth(155);logo.setFitHeight(125);logo.setPreserveRatio(true);}catch(Exception ignored){}
        Label marca=new Label("Gestión de Reserva Natural"); marca.setStyle("-fx-text-fill:#DCEBDD;-fx-font-size:11px;-fx-font-weight:bold;");
        VBox marcaBox=new VBox(3,logo,marca); marcaBox.setAlignment(Pos.CENTER); marcaBox.setPadding(new Insets(0,0,10,0));
        Label estado=new Label("●  Sesión activa · "+rolActual); estado.setStyle("-fx-text-fill:#DDE9DF;-fx-font-size:11px;-fx-font-weight:bold;");
        menu.getChildren().addAll(marcaBox,estado,new Separator());
        add("⌂  Panel general",VistaDashboard::new); add("🐾  Animales",VistaAnimales::new); add("📋  Ingresos / Actas",VistaIngresos::new); add("↔  Movimientos",VistaMovimientos::new); add("🌿  Plantel Permanente",VistaPlantel::new); add("🏠  Habitáculos",VistaHabitaculos::new); add("🧬  Especies",VistaEspecies::new);
        if(esAdministrador())add("👤  Usuarios",VistaUsuarios::new);
        Region spacer=new Region();VBox.setVgrow(spacer,Priority.ALWAYS);menu.getChildren().add(spacer);
        Button salir=menuBtn("⇦  Cerrar sesión");salir.setOnAction(e->new VentanaLogin().mostrar(stage));menu.getChildren().add(salir);
        root.setLeft(menu); root.setCenter(new VistaDashboard());
        Scene scene=new Scene(root); stage.setScene(scene); stage.setTitle("Tatú Carreta · Gestión de Reserva Natural"); stage.setResizable(false); stage.setMaximized(true); stage.show();
    }
    private boolean esAdministrador(){return "administrador".equalsIgnoreCase(rolActual)||"admin".equalsIgnoreCase(rolActual);}
    private void add(String texto,Supplier<javafx.scene.Node>view){Button b=menuBtn(texto);b.setOnAction(e->{if(seleccionado!=null)seleccionado.setStyle(menuNormal());seleccionado=b;b.setStyle(menuActivo());root.setCenter(view.get());});menu.getChildren().add(b);}
    private Button menuBtn(String t){Button b=new Button(t);b.setMaxWidth(Double.MAX_VALUE);b.setAlignment(Pos.CENTER_LEFT);b.setPrefHeight(43);b.setMinHeight(43);b.setStyle(menuNormal());b.setOnMouseEntered(e->{if(b!=seleccionado)b.setStyle(menuHover());});b.setOnMouseExited(e->{if(b!=seleccionado)b.setStyle(menuNormal());});return b;}
    private String menuNormal(){return "-fx-background-color:transparent;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:9;-fx-padding:9 12;-fx-cursor:hand;";}
    private String menuHover(){return "-fx-background-color:#315E3A;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:9;-fx-padding:9 12;-fx-cursor:hand;";}
    private String menuActivo(){return "-fx-background-color:#4D9660;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:9;-fx-padding:9 12;-fx-cursor:hand;";}
}
