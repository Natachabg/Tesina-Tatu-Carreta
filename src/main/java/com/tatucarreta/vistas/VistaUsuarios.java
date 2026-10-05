package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class VistaUsuarios extends VistaCRUD {
    public VistaUsuarios(){super("Usuarios","Solo el Administrador gestiona accesos, contraseñas y estado de las cuentas.",new String[]{"Nombre","Usuario","Rol","Estado"});agregar.setText("＋ AGREGAR USUARIO");cargar("");}
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.usuarios(q);}
    protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.usuariosInactivos(q);}
    protected void activarRegistro(int id){SistemaDAO.activar("usuarios","id_usuario",id);}
    protected void agregarNuevo(){form(0,null);}
    protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)form(f.id(),f);}
    protected void desactivar(int id){if(!SistemaDAO.puedeDesactivarUsuario(id)){info("Cuenta protegida","La cuenta principal del Administrador no puede desactivarse desde este módulo.");return;}if(confirmarOK("Enviar usuario a inactivos","¿Estás segura de que querés quitarle el acceso a este usuario?")){SistemaDAO.baja("usuarios","id_usuario",id);cargar(buscador.getText());}}
    private void form(int id,SistemaDAO.Fila f){
        TextField n=Estilos.campo("Nombre completo"),u=Estilos.campo("Usuario");
        ComboBox<String> rol=Estilos.combo(); rol.getItems().setAll("Administrador","Operador","Consulta");
        OjoPassword.Campo pass=OjoPassword.crear(id==0?"Contraseña":"Nueva contraseña (opcional)");
        if(f!=null){n.setText(f.c().get(1));u.setText(f.c().get(2));rol.setValue(f.c().get(3));}
        GridPane g=Formulario.grid();Formulario.add(g,0,"Nombre",n,true);Formulario.add(g,1,"Usuario",u,true);Formulario.add(g,2,"Contraseña",pass.control(),id==0);Formulario.add(g,3,"Rol",rol,true);
        Label ayuda=new Label("La cuenta principal admin queda protegida. Los usuarios enviados a inactivos se pueden restaurar desde el botón REGISTROS INACTIVOS.");ayuda.setWrapText(true);ayuda.setStyle("-fx-background-color:"+Estilos.VERDE_SUAVE+";-fx-text-fill:"+Estilos.VERDE_OSCURO+";-fx-padding:10;-fx-background-radius:8;");g.add(ayuda,1,4);
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Nuevo usuario":"Modificar usuario",g);
        d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(n.getText(),u.getText())||rol.getValue()==null||(id==0&&!validar(pass.getText())))throw new IllegalArgumentException("Completá los campos obligatorios.");if(confirmarOK(id==0?"Agregar usuario":"Guardar cambios",id==0?"¿Estás segura de que querés agregar este usuario?":"¿Querés guardar los cambios?")){SistemaDAO.guardarUsuario(id,n.getText().trim(),u.getText().trim(),pass.getText(),rol.getValue());cargar(buscador.getText());}}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }
}
