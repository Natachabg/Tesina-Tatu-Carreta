package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class VistaHabitaculos extends VistaCRUD {
    public VistaHabitaculos(){super("Habitáculos","Espacios físicos y capacidad máxima de la Reserva. No se permite enviar a inactivos un habitáculo que conserve animales asociados.",new String[]{"Nombre","Sector","Capacidad","Estado","Observaciones"});agregar.setText("＋ AGREGAR HABITÁCULO");cargar("");}
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.habitaculos(q);}protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.habitaculosInactivos(q);}protected void activarRegistro(int id){SistemaDAO.activar("habitaculos","id_habitaculo",id);}protected void agregarNuevo(){form(0,null);}protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)form(f.id(),f);}
    protected void desactivar(int id){if(!SistemaDAO.habitaculoPuedeEliminar(id)){info("No se puede enviar a inactivos","Este habitáculo tiene o tuvo animales asignados. El historial debe conservarse.");return;}if(confirmarOK("Enviar habitáculo a inactivos","¿Estás segura de que querés quitar este habitáculo del catálogo activo?")){SistemaDAO.baja("habitaculos","id_habitaculo",id);cargar(buscador.getText());}}
    private void form(int id,SistemaDAO.Fila f){TextField n=Estilos.campo("Nombre del habitáculo"),s=Estilos.campo("Sector"),cap=Estilos.entero("Capacidad máxima",6);TextArea obs=Estilos.area("Observaciones");if(f!=null){n.setText(f.c().get(1));s.setText(f.c().get(2));cap.setText(f.c().get(3));obs.setText(f.c().get(5));}
        GridPane g=Formulario.grid();Formulario.add(g,0,"Nombre",n,true);Formulario.add(g,1,"Sector",s,true);Formulario.add(g,2,"Capacidad máxima",cap,true);Formulario.add(g,3,"Observaciones",obs,false);
        Label ayuda=new Label("La capacidad limita automáticamente cuántos ejemplares se pueden asignar a este espacio.");ayuda.setWrapText(true);ayuda.setStyle("-fx-background-color:"+Estilos.VERDE_SUAVE+";-fx-text-fill:"+Estilos.VERDE_OSCURO+";-fx-padding:10;-fx-background-radius:8;");g.add(ayuda,1,4);
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Nuevo habitáculo":"Modificar habitáculo",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(n.getText(),s.getText(),cap.getText()))throw new IllegalArgumentException("Completá los campos obligatorios.");int c=Integer.parseInt(cap.getText());if(c<=0)throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");int ocup=id==0?0:SistemaDAO.ocupacionHabitaculo(id,0);if(c<ocup)throw new IllegalArgumentException("No podés reducir la capacidad por debajo de la ocupación actual ("+ocup+").");if(confirmarOK(id==0?"Agregar habitáculo":"Guardar cambios",id==0?"¿Estás segura de que querés agregar este habitáculo?":"¿Querés guardar los cambios?")){SistemaDAO.guardarHabitaculo(id,n.getText().trim(),s.getText().trim(),c,obs.getText().trim());cargar(buscador.getText());}}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }
}
