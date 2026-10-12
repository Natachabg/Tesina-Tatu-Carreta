package com.tatucarreta.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class Formulario {
    private Formulario() {}
    public static GridPane grid(){ GridPane g=new GridPane(); g.setHgap(14); g.setVgap(10); g.setPadding(new Insets(4)); return g; }

    public static void add(GridPane g,int row,String label,Node n,boolean req){
        Label l=new Label((req?"* ":"")+label); l.setStyle("-fx-font-weight:bold;-fx-text-fill:"+Estilos.TEXTO+";-fx-font-size:12px;");
        g.add(l,0,row); g.add(n,1,row); GridPane.setHgrow(n,Priority.ALWAYS);
        if(n instanceof Control c) c.setMaxWidth(Double.MAX_VALUE);
        if(n instanceof TextInputControl ti) {
            if(req) { Estilos.campoObligatorio(ti,false); ti.textProperty().addListener((o,a,b)->Estilos.campoObligatorio(ti,b!=null && !b.isBlank())); }
        }
        if(n instanceof ComboBox<?> cb && req) { Estilos.campoObligatorio(cb,false); cb.valueProperty().addListener((o,a,b)->Estilos.campoObligatorio(cb,b!=null)); }
        if(n instanceof DatePicker dp && req) { Estilos.campoObligatorio(dp,false); dp.valueProperty().addListener((o,a,b)->Estilos.campoObligatorio(dp,b!=null)); }
    }
    public static Dialog<ButtonType> dialog(String titulo){return dialog(titulo,new VBox());}
    public static Dialog<ButtonType> dialog(String titulo,Node contenido){
        Dialog<ButtonType>d=new Dialog<>(); d.setTitle("Tatú Carreta"); d.setHeaderText(titulo); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL); d.getDialogPane().setContent(contenido); d.setResizable(false); d.setOnShown(e->Estilos.estilizarDialog(d.getDialogPane())); return d;
    }
}
