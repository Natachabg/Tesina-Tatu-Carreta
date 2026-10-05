package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.Estilos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class VistaDashboard extends BorderPane {
    public VistaDashboard(){
        Estilos.aplicarVentana(this);setPadding(new Insets(22));
        VBox main=new VBox(16);
        ImageView title=new ImageView();try{title.setImage(new Image(getClass().getResourceAsStream("/images/secciones/portada.png")));}catch(Exception ignored){}title.setPreserveRatio(true);title.setFitWidth(1080);title.setFitHeight(155);title.setSmooth(true);

        HBox banner=new HBox(18);banner.setAlignment(Pos.CENTER_LEFT);banner.setPadding(new Insets(18));banner.setStyle("-fx-background-color:linear-gradient(to right,"+Estilos.VERDE_OSCURO+","+Estilos.VERDE+");-fx-background-radius:16;");
        VBox bt=new VBox(4);Label b1=new Label("SISTEMA DE FAUNA NATIVA");b1.setStyle("-fx-text-fill:white;-fx-font-size:19px;-fx-font-weight:bold;");Label b2=new Label("Información operativa calculada directamente desde la base de datos.");b2.setStyle("-fx-text-fill:#DCEBDD;-fx-font-size:12px;");bt.getChildren().addAll(b1,b2);Region bg=new Region();HBox.setHgrow(bg,Priority.ALWAYS);Label per=new Label("PERÍODO · "+java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")));per.setStyle("-fx-text-fill:white;-fx-background-color:#315E3A;-fx-background-radius:18;-fx-padding:9 13;-fx-font-size:11px;-fx-font-weight:bold;");banner.getChildren().addAll(bt,bg,per);

        HBox cards=new HBox(12,
            Estilos.tarjetaIndicador("🌿","EJEMPLARES EN PLANTEL",String.valueOf(SistemaDAO.ejemplaresPlantel()),"Cantidad física activa en Plantel Permanente."),
            Estilos.tarjetaIndicador("🐾","ANIMALES EN CATÁLOGO",String.valueOf(SistemaDAO.animalesCatalogados()),"Registros maestros de animales, no ejemplares individuales."),
            Estilos.tarjetaIndicador("🧬","ESPECIES ACTIVAS",String.valueOf(SistemaDAO.especiesActivas()),"Especies disponibles para registrar."),
            Estilos.tarjetaIndicador("📋","ACTAS ACTIVAS",String.valueOf(SistemaDAO.actasActivas()),"Actas / ingresos actualmente activos."),
            Estilos.tarjetaIndicador("🏠","HABITÁCULOS",String.valueOf(SistemaDAO.habitaculosActivos()),"Espacios activos configurados en la Reserva."));

        int capacidad=SistemaDAO.capacidadTotal(),ocup=SistemaDAO.ocupacionTotal();double pct=capacidad<=0?0:Math.min(100,(ocup*100.0)/capacidad);
        VBox capacidadBox=Estilos.tarjeta();Label ct=new Label("Ocupación de habitáculos");ct.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");Label cv=new Label(ocup+" / "+capacidad+" ejemplares");cv.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");javafx.scene.control.ProgressBar barra=new javafx.scene.control.ProgressBar(pct/100.0);barra.setMaxWidth(Double.MAX_VALUE);barra.setPrefHeight(13);barra.setStyle("-fx-accent:"+Estilos.VERDE+";");Label cp=new Label(String.format("%.0f%% de ocupación · La capacidad se valida antes de cada alta o nacimiento.",pct));cp.setWrapText(true);cp.setStyle("-fx-text-fill:"+Estilos.GRIS+";-fx-font-size:11px;");capacidadBox.getChildren().addAll(ct,cv,barra,cp);HBox.setHgrow(capacidadBox,Priority.ALWAYS);

        VBox circuito=Estilos.tarjeta();Label ft=new Label("Trazabilidad");ft.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");circuito.getChildren().addAll(ft,paso("01","Catálogo","Especies y animales forman el registro maestro."),paso("02","Ingreso","Cada acta conserva entrega, recepción y animales asociados."),paso("03","Plantel","Se controla ubicación, identificaciones y capacidad."),paso("04","Historial","Nacimientos, fallecimientos y movimientos no pierden trazabilidad."));HBox.setHgrow(circuito,Priority.ALWAYS);
        HBox info=new HBox(14,capacidadBox,circuito);

        main.getChildren().addAll(title,banner,cards,info);setCenter(main);
    }
    private Label paso(String n,String t,String d){Label l=new Label(n+"   "+t+"\n          "+d);l.setStyle("-fx-background-color:"+Estilos.VERDE_SUAVE+";-fx-background-radius:9;-fx-padding:9;-fx-text-fill:"+Estilos.TEXTO+";-fx-font-size:11px;");l.setWrapText(true);return l;}
}
