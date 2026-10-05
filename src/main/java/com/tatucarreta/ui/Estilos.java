package com.tatucarreta.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.converter.IntegerStringConverter;
import java.util.function.UnaryOperator;

/** Estilo visual único de toda la aplicación. */
public final class Estilos {
    public static final String VERDE = "#2F7A46";
    public static final String VERDE_OSCURO = "#123D26";
    public static final String VERDE_MEDIO = "#4D9660";
    public static final String VERDE_CLARO = "#D9ECDD";
    public static final String VERDE_SUAVE = "#EAF4EC";
    public static final String FONDO = "#EAF1EC";
    public static final String BLANCO = "#FFFFFF";
    public static final String BORDE = "#C9D9CE";
    public static final String TEXTO = "#203228";
    public static final String GRIS = "#66766C";
    public static final String ROJO = "#B3261E";
    public static final String AMARILLO = "#A66A00";

    private Estilos() {}

    public static void aplicarVentana(Region r) {
        r.setStyle("-fx-background-color:" + FONDO + ";-fx-font-family:'Segoe UI';");
    }

    public static Button boton(String texto) {
        Button b = new Button(texto);
        b.setPrefHeight(46); b.setMinHeight(46);
        b.setStyle(pri(false));
        b.setOnMouseEntered(e -> b.setStyle(pri(true)));
        b.setOnMouseExited(e -> b.setStyle(pri(false)));
        return b;
    }

    public static Button botonSec(String texto) {
        Button b = new Button(texto);
        b.setPrefHeight(46); b.setMinHeight(46);
        b.setStyle(sec(false));
        b.setOnMouseEntered(e -> b.setStyle(sec(true)));
        b.setOnMouseExited(e -> b.setStyle(sec(false)));
        return b;
    }

    public static Button botonPeligro(String texto) {
        Button b = new Button(texto);
        b.setPrefHeight(46); b.setMinHeight(46);
        String normal = "-fx-background-color:#FFF5F4;-fx-text-fill:"+ROJO+";-fx-border-color:#E9C5C2;-fx-border-radius:9;-fx-background-radius:9;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:10 16;-fx-cursor:hand;";
        String hover = "-fx-background-color:#FDE7E4;-fx-text-fill:"+ROJO+";-fx-border-color:#DFA19C;-fx-border-radius:9;-fx-background-radius:9;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:10 16;-fx-cursor:hand;";
        b.setStyle(normal); b.setOnMouseEntered(e->b.setStyle(hover)); b.setOnMouseExited(e->b.setStyle(normal));
        return b;
    }

    private static String pri(boolean h) { return "-fx-background-color:"+(h?VERDE_MEDIO:VERDE)+";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:12px;-fx-background-radius:10;-fx-padding:10 18;-fx-cursor:hand;"; }
    private static String sec(boolean h) { return "-fx-background-color:"+(h?VERDE_SUAVE:BLANCO)+";-fx-text-fill:"+VERDE_OSCURO+";-fx-border-color:"+(h?VERDE:BORDE)+";-fx-border-radius:10;-fx-background-radius:10;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:10 18;-fx-cursor:hand;"; }

    public static TextField campo(String prompt) {
        TextField t = new TextField(); t.setPromptText(prompt); t.setPrefHeight(44); t.setMinHeight(44); aplicarCampo(t); limitar(t, 120); return t;
    }
    public static PasswordField password(String prompt) {
        PasswordField t = new PasswordField(); t.setPromptText(prompt); t.setPrefHeight(44); t.setMinHeight(44); aplicarCampo(t); limitar(t, 80); return t;
    }
    public static TextArea area(String prompt) {
        TextArea t = new TextArea(); t.setPromptText(prompt); t.setPrefRowCount(3); t.setWrapText(true); t.setStyle(estiloCampo(false)); limitar(t, 500); t.focusedProperty().addListener((o,a,f)->t.setStyle(estiloCampo(f))); return t;
    }
    public static <T> ComboBox<T> combo() {
        ComboBox<T> c = new ComboBox<>(); c.setPrefHeight(44); c.setMinHeight(44); c.setMaxWidth(Double.MAX_VALUE); c.setStyle(estiloCampo(false)); c.focusedProperty().addListener((o,a,f)->c.setStyle(estiloCampo(f))); return c;
    }
    public static DatePicker fecha() {
        DatePicker d = new DatePicker(); d.setPrefHeight(44); d.setMinHeight(44); d.setMaxWidth(Double.MAX_VALUE); d.setStyle(estiloCampo(false)); d.focusedProperty().addListener((o,a,f)->d.setStyle(estiloCampo(f))); return d;
    }
    public static TextField entero(String prompt, int maxDigits) {
        TextField t = campo(prompt);
        UnaryOperator<TextFormatter.Change> filter = ch -> ch.getControlNewText().matches("\\d{0," + maxDigits + "}") ? ch : null;
        t.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), null, filter));
        return t;
    }

    private static void aplicarCampo(Control c) { c.setStyle(estiloCampo(false)); c.focusedProperty().addListener((o,a,f)->c.setStyle(estiloCampo(f))); }
    private static String estiloCampo(boolean f) { return "-fx-background-color:white;-fx-border-color:"+(f?VERDE:BORDE)+";-fx-border-width:"+(f?"2":"1")+";-fx-border-radius:9;-fx-background-radius:9;-fx-padding:9 12;-fx-font-size:13px;-fx-text-fill:"+TEXTO+";"; }

    public static void limitar(TextInputControl c, int max) {
        c.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= max ? change : null));
    }

    public static void campoObligatorio(Control c, boolean ok) {
        c.setStyle("-fx-background-color:white;-fx-border-color:"+(ok?BORDE:ROJO)+";-fx-border-width:"+(ok?"1":"2")+";-fx-border-radius:9;-fx-background-radius:9;-fx-padding:9 12;");
    }

    public static Label titulo(String s) { Label l=new Label(s); l.setStyle("-fx-font-size:25px;-fx-font-weight:bold;-fx-text-fill:"+VERDE_OSCURO+";"); return l; }
    public static Label subtitulo(String s) { Label l=new Label(s); l.setWrapText(true); l.setStyle("-fx-font-size:13px;-fx-text-fill:"+GRIS+";"); return l; }
    public static Label etiqueta(String s) { Label l=new Label(s); l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+GRIS+";"); return l; }

    public static VBox tarjeta(Node... n) {
        VBox v=new VBox(9,n); v.setPadding(new Insets(16));
        v.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:"+BORDE+";-fx-border-radius:14;-fx-effect:dropshadow(gaussian,rgba(18,61,38,.06),12,0,0,3);");
        return v;
    }

    public static VBox tarjetaIndicador(String icono, String titulo, String valor, String detalle) {
        Label ico = new Label(icono); ico.setAlignment(javafx.geometry.Pos.CENTER); ico.setMinSize(42,42); ico.setMaxSize(42,42);
        ico.setStyle("-fx-background-color:"+VERDE_CLARO+";-fx-background-radius:21;-fx-text-fill:"+VERDE_OSCURO+";-fx-font-size:20px;");
        Label t=new Label(titulo); t.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:"+GRIS+";");
        Label n=new Label(valor); n.setStyle("-fx-font-size:27px;-fx-font-weight:bold;-fx-text-fill:"+VERDE_OSCURO+";");
        Label d=new Label(detalle); d.setWrapText(true); d.setStyle("-fx-font-size:10px;-fx-text-fill:"+GRIS+";");
        HBox top=new HBox(10,ico,new VBox(2,t,n)); top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox box=new VBox(8,top,d); box.setPadding(new Insets(14)); box.setPrefHeight(122); box.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:"+BORDE+";-fx-border-radius:14;"); HBox.setHgrow(box,Priority.ALWAYS); return box;
    }

    public static void tablaCompacta(TableView<?> t) {
        t.setFixedCellSize(30); t.setPrefHeight(330); t.setMinHeight(280);
        t.setStyle("-fx-background-color:white;-fx-border-color:"+BORDE+";-fx-border-radius:10;-fx-background-radius:10;-fx-font-family:'Segoe UI';-fx-font-size:12px;");
        t.setFocusTraversable(false); t.setPlaceholder(new Label("Todavía no hay registros para mostrar."));
    }

    public static void estilizarDialog(DialogPane p) {
        p.setStyle("-fx-background-color:"+FONDO+";"); p.setPadding(new Insets(18));
        for(ButtonType bt:p.getButtonTypes()) { Node n=p.lookupButton(bt); if(n instanceof Button b){b.setPrefHeight(40);b.setMinHeight(40);b.setStyle(bt==ButtonType.OK?pri(false):sec(false));} }
    }
}
