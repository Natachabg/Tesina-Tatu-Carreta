package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class VistaEspecies extends VistaCRUD {
    public VistaEspecies(){super("Especies","Catálogo biológico de la Reserva. La clasificación y conservación alimentan el reporte mensual.",new String[]{"Especie","Clasificación","Clase","Conservación","Estado"});agregar.setText("＋ AGREGAR ESPECIE");cargar("");}
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.especies(q);}
    protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.especiesInactivas(q);}
    protected void activarRegistro(int id){SistemaDAO.activar("especies","id_especie",id);}
    protected void agregarNuevo(){form(0,null);}
    protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)form(f.id(),f);}
    protected void desactivar(int id){if(!SistemaDAO.especiePuedeEliminar(id)){info("No se puede enviar a inactivos","Esta especie está asociada a uno o más animales. Primero revisá esas asociaciones.");return;}if(confirmarOK("Enviar especie a inactivos","¿Estás segura de que querés quitar esta especie del catálogo activo?")){SistemaDAO.baja("especies","id_especie",id);cargar(buscador.getText());}}
    private void form(int id,SistemaDAO.Fila f){
        TextField n=Estilos.campo("Nombre de la especie");ComboBox<String>clas=Estilos.combo(),clase=Estilos.combo(),conserv=Estilos.combo();
        clas.getItems().setAll("Autóctonos","Exóticos");clase.getItems().setAll("Invertebrados","Peces","Anfibios","Reptiles","Aves","Mamíferos","Sin clasificar");conserv.getItems().setAll("Sin evaluar","Preocupación menor","Casi amenazada","Vulnerable","En peligro","En peligro crítico");
        if(f!=null){n.setText(f.c().get(1));clas.setValue(f.c().get(2));clase.setValue(f.c().get(3));conserv.setValue(f.c().get(4).isBlank()?"Sin evaluar":f.c().get(4));}else{clas.setValue("Autóctonos");clase.setValue("Sin clasificar");conserv.setValue("Sin evaluar");}
        GridPane g=Formulario.grid();Formulario.add(g,0,"Nombre",n,true);Formulario.add(g,1,"Clasificación",clas,true);Formulario.add(g,2,"Clase",clase,true);Formulario.add(g,3,"Estado de conservación",conserv,true);
        Label ayuda=new Label("Permite identificar especies según su clasificación zoológica y registrar su estado de conservación para consultas y reportes.");ayuda.setWrapText(true);ayuda.setStyle("-fx-background-color:"+Estilos.VERDE_SUAVE+";-fx-text-fill:"+Estilos.VERDE_OSCURO+";-fx-padding:10;-fx-background-radius:8;");g.add(ayuda,1,4);
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Nueva especie":"Modificar especie",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(n.getText())||clas.getValue()==null||clase.getValue()==null||conserv.getValue()==null)throw new IllegalArgumentException("Completá los campos obligatorios.");if(confirmarOK(id==0?"Agregar especie":"Guardar cambios",id==0?"¿Estás segura de que querés agregar esta especie?":"¿Querés guardar los cambios?")){SistemaDAO.guardarEspecie(id,n.getText().trim(),clas.getValue(),clase.getValue(),conserv.getValue());cargar(buscador.getText());}}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }
}
