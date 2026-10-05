package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;

public class VistaMovimientos extends VistaCRUD {
    public VistaMovimientos(){super("Movimientos","Historial de ingresos, traslados, nacimientos y fallecimientos. Cada movimiento conserva su relación con el acta o con el Plantel.",new String[]{"Animal","Acta","Fecha","Tipo","Cantidad","Sexo","Identificación","Destino","Estado"});agregar.setText("＋ AGREGAR MOVIMIENTO");cargar("");}
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.movimientos(q);}
    protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.movimientosInactivos(q);}
    protected void activarRegistro(int id){SistemaDAO.activar("movimientos","id_movimiento",id);}
    protected void agregarNuevo(){form(0,null);}
    protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)detalle(f);}
    protected void desactivar(int id){if(confirmarOK("Enviar movimiento a inactivos","¿Estás segura de que querés quitar este movimiento del listado activo? El historial se conserva.")){SistemaDAO.baja("movimientos","id_movimiento",id);cargar(buscador.getText());}}
    private void form(int id,SistemaDAO.Fila f){
        ComboBox<SistemaDAO.Opcion>d=Estilos.combo();d.getItems().setAll(SistemaDAO.detallesOpciones());ComboBox<String>tipo=Estilos.combo();tipo.getItems().setAll("Ingreso","Traslado","Fallecimiento","Nacimiento","Otro");ComboBox<String>sexo=Estilos.combo();sexo.getItems().setAll("Macho","Hembra","Indeterminado");DatePicker fecha=Estilos.fecha();fecha.setValue(LocalDate.now());TextField cant=Estilos.entero("Cantidad",6),dest=Estilos.campo("Destino");TextArea obs=Estilos.area("Observaciones");
        if(f!=null){fecha.setValue(LocalDate.parse(f.c().get(3)));tipo.setValue(f.c().get(4));cant.setText(f.c().get(5));sexo.setValue(f.c().get(6).isBlank()?null:f.c().get(6));dest.setText(f.c().get(8));}else{tipo.setValue("Traslado");cant.setText("1");}
        GridPane g=Formulario.grid();Formulario.add(g,0,"Detalle de ingreso",d,false);Formulario.add(g,1,"Fecha",fecha,true);Formulario.add(g,2,"Tipo",tipo,true);Formulario.add(g,3,"Sexo",sexo,false);Formulario.add(g,4,"Cantidad",cant,true);Formulario.add(g,5,"Destino / centro de traslado",dest,false);Formulario.add(g,6,"Observaciones",obs,false);
        Dialog<ButtonType>x=Formulario.dialog(id==0?"Nuevo movimiento":"Modificar movimiento",g);x.showAndWait().ifPresent(r->{if(r==ButtonType.OK)try{if(fecha.getValue()==null||tipo.getValue()==null||!validar(cant.getText()))throw new IllegalArgumentException("Completá los campos obligatorios.");if("Traslado".equals(tipo.getValue())&&!validar(dest.getText()))throw new IllegalArgumentException("Para un traslado tenés que indicar el centro o institución de destino.");int n=Integer.parseInt(cant.getText());if(n<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");if(id==0&&!confirmarOK("Agregar movimiento","¿Estás segura de que querés agregar este movimiento?"))return;Integer det=d.getValue()==null?null:d.getValue().id();SistemaDAO.guardarMovimiento(id,det,null,fecha.getValue().toString(),tipo.getValue(),n,sexo.getValue(),dest.getText().trim(),obs.getText().trim());cargar(buscador.getText());}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }
    private void detalle(SistemaDAO.Fila f){
        String animal=f.c().get(1),acta=f.c().get(2),ident=f.c().get(7);Label cab=new Label("Movimiento · "+f.c().get(4));cab.setStyle("-fx-font-size:19px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");
        Label data=new Label("Animal: "+animal+"\nIdentificación: "+ident+"\nActa relacionada: "+(acta.isBlank()?"No corresponde / Plantel Permanente":acta)+"\nFecha: "+formatearFecha(f.c().get(3))+"\nCantidad: "+f.c().get(5)+"\nSexo: "+(f.c().get(6).isBlank()?"Sin especificar":f.c().get(6))+"\nDestino: "+f.c().get(8));data.setStyle("-fx-font-size:13px;-fx-text-fill:"+Estilos.TEXTO+";-fx-line-spacing:4;");
        Label nota=new Label("Este detalle muestra una sola vez la información del movimiento y conserva la identificación asociada al animal.");nota.setWrapText(true);nota.setStyle("-fx-background-color:"+Estilos.VERDE_SUAVE+";-fx-padding:10;-fx-background-radius:8;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");
        Dialog<ButtonType>d=new Dialog<>();d.setTitle("Tatú Carreta · Detalle del movimiento");d.setHeaderText("Detalle completo");d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);VBox b=new VBox(12,cab,data,nota);b.setPadding(new Insets(10));b.setPrefSize(620,340);d.getDialogPane().setContent(b);d.setResizable(false);d.setOnShown(e->Estilos.estilizarDialog(d.getDialogPane()));d.showAndWait();
    }
    private String formatearFecha(String s){try{return LocalDate.parse(s).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));}catch(Exception e){return s;}}
}
