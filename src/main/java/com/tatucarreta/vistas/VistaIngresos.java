package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;

public class VistaIngresos extends VistaCRUD {
    public VistaIngresos(){super("Ingresos / Actas","Cada acta puede contener varios animales. Doble click para abrir el acta completa y sus detalles.",new String[]{"Acta","Fecha","Organismo","Responsable","Recibe en Reserva","Procedencia","Motivo","Estado"});agregar.setText("＋ AGREGAR INGRESO");cargar("");}
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.ingresos(q);}
    protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.ingresosInactivos(q);}
    protected void activarRegistro(int id){SistemaDAO.activar("ingresos","id_ingreso",id);}
    protected void agregarNuevo(){form(0,null);}
    protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)mostrarActa(f.id(),f);}
    protected void desactivar(int id){if(!SistemaDAO.ingresoPuedeBaja(id)){info("No se puede enviar a inactivos","El acta contiene animales asociados. La trazabilidad del acta debe conservarse.");return;}if(confirmarOK("Enviar acta a inactivos","¿Estás segura de que querés quitar esta acta de los registros activos?")){SistemaDAO.baja("ingresos","id_ingreso",id);cargar(buscador.getText());}}

    private void form(int id,SistemaDAO.Fila f){
        TextField acta=Estilos.campo("Número de acta"),org=Estilos.campo("Organismo"),resp=Estilos.campo("Responsable de entrega"),recibe=Estilos.campo("Quién recibe en la Reserva"),proc=Estilos.campo("Procedencia"),mot=Estilos.campo("Motivo");
        DatePicker fecha=Estilos.fecha(); TextArea obs=Estilos.area("Observaciones");
        if(f!=null){
            acta.setText(f.c().get(1));fecha.setValue(LocalDate.parse(f.c().get(2)));org.setText(f.c().get(3));resp.setText(f.c().get(4));recibe.setText(f.c().get(5));proc.setText(f.c().get(6));mot.setText(f.c().get(7));
            obs.setText(SistemaDAO.scalar("SELECT COALESCE(observaciones,'') FROM ingresos WHERE id_ingreso="+f.id()));
        } else fecha.setValue(LocalDate.now());
        GridPane g=Formulario.grid();
        Formulario.add(g,0,"N° Acta",acta,true);Formulario.add(g,1,"Fecha",fecha,true);Formulario.add(g,2,"Organismo",org,true);Formulario.add(g,3,"Responsable de entrega",resp,true);Formulario.add(g,4,"Recibe en la Reserva",recibe,true);Formulario.add(g,5,"Procedencia",proc,true);Formulario.add(g,6,"Motivo",mot,true);Formulario.add(g,7,"Observaciones",obs,false);
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Nuevo ingreso / acta":"Modificar ingreso / acta",g);
        d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(fecha.getValue()==null||!validar(acta.getText(),org.getText(),resp.getText(),recibe.getText(),proc.getText(),mot.getText()))throw new IllegalArgumentException("Completá todos los campos obligatorios.");if(!confirmarOK(id==0?"Agregar ingreso":"Guardar cambios",id==0?"¿Estás segura de que querés agregar este ingreso?":"¿Querés guardar los cambios del acta?"))return;int nuevo=SistemaDAO.guardarIngreso(id,acta.getText().trim(),fecha.getValue().toString(),org.getText().trim(),resp.getText().trim(),recibe.getText().trim(),proc.getText().trim(),mot.getText().trim(),obs.getText().trim());cargar(buscador.getText());mostrarActa(nuevo,null);}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }

    private void mostrarActa(int ingreso,SistemaDAO.Fila f){
        String numero=f!=null?f.c().get(1):SistemaDAO.scalar("SELECT numero_acta FROM ingresos WHERE id_ingreso="+ingreso);
        String fecha=f!=null?f.c().get(2):SistemaDAO.scalar("SELECT fecha_ingreso FROM ingresos WHERE id_ingreso="+ingreso);
        String org=f!=null?f.c().get(3):SistemaDAO.scalar("SELECT COALESCE(organismo_procedencia,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String resp=f!=null?f.c().get(4):SistemaDAO.scalar("SELECT COALESCE(responsable_entrega,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String recibe=f!=null?f.c().get(5):SistemaDAO.scalar("SELECT COALESCE(recibe_reserva,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String proc=f!=null?f.c().get(6):SistemaDAO.scalar("SELECT COALESCE(procedencia,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String mot=f!=null?f.c().get(7):SistemaDAO.scalar("SELECT COALESCE(motivo_ingreso,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String obs=SistemaDAO.scalar("SELECT COALESCE(observaciones,'') FROM ingresos WHERE id_ingreso="+ingreso);
        String fechaVista=fecha;try{fechaVista=LocalDate.parse(fecha).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));}catch(Exception ignored){}
        Label cab=new Label("ACTA N° "+numero+"   ·   "+fechaVista);cab.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");
        Label datos=new Label("Organismo: "+org+"\nResponsable de entrega: "+resp+"\nRecibe en la Reserva: "+recibe+"\nProcedencia: "+proc+"\nMotivo: "+mot+"\nObservaciones: "+obs);datos.setStyle("-fx-font-size:12px;-fx-text-fill:"+Estilos.TEXTO+";-fx-line-spacing:3;");
        TableView<SistemaDAO.Fila>t=new TableView<>();Estilos.tablaCompacta(t);t.setPrefHeight(210);String[]cs={"Animal","Cantidad","Sexo","Edad","Peso","Estado"};for(int i=0;i<cs.length;i++){int ix=i;TableColumn<SistemaDAO.Fila,String>c=new TableColumn<>(cs[i]);c.setCellValueFactory(x->new javafx.beans.property.SimpleStringProperty(x.getValue().c().size()>ix+1?x.getValue().c().get(ix+1):""));t.getColumns().add(c);}t.setItems(FXCollections.observableArrayList(SistemaDAO.detallesIngreso(ingreso)));
        Button add=Estilos.boton("＋ AGREGAR ANIMAL AL ACTA");add.setOnAction(e->detalleForm(ingreso,0,null,t));
        t.setRowFactory(tv->{TableRow<SistemaDAO.Fila>r=new TableRow<>();r.setOnMouseClicked(e->{if(e.getClickCount()==2&&!r.isEmpty())detalleForm(ingreso,r.getItem().id(),r.getItem(),t);});return r;});
        TableView<SistemaDAO.Fila>res=new TableView<>();Estilos.tablaCompacta(res);res.setPrefHeight(170);String[]rc={"Animal","Ingresado","En Plantel","Fallecidos","Trasladados","Destinos de traslado"};for(int i=0;i<rc.length;i++){int ix=i;TableColumn<SistemaDAO.Fila,String>c=new TableColumn<>(rc[i]);c.setCellValueFactory(x->new javafx.beans.property.SimpleStringProperty(x.getValue().c().size()>ix+1?x.getValue().c().get(ix+1):""));res.getColumns().add(c);}res.setItems(FXCollections.observableArrayList(SistemaDAO.resumenActa(ingreso)));
        Label resumen=new Label("Resumen de trazabilidad del acta · el estado de cada animal se actualiza con Plantel y Movimientos.");resumen.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+Estilos.VERDE_OSCURO+";");
        VBox box=new VBox(10,cab,datos,new Label("Animales asociados al acta"),t,add,resumen,res);box.setPadding(new Insets(4));box.setPrefSize(980,650);Dialog<ButtonType>d=new Dialog<>();d.setTitle("Tatú Carreta · Detalle del acta");d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);d.getDialogPane().setContent(box);d.setResizable(false);d.setOnShown(e->Estilos.estilizarDialog(d.getDialogPane()));d.showAndWait();
    }

    private void detalleForm(int ingreso,int id,SistemaDAO.Fila f,TableView<SistemaDAO.Fila>t){
        ComboBox<SistemaDAO.Opcion>a=Estilos.combo();a.getItems().setAll(SistemaDAO.animalesOpciones());Button nuevoAnimal=Estilos.botonSec("＋ Animal");HBox ab=new HBox(8,a,nuevoAnimal);HBox.setHgrow(a,Priority.ALWAYS);nuevoAnimal.setOnAction(e->crearAnimal(a));
        TextField c=Estilos.entero("Cantidad ingresada",6),edad=Estilos.campo("Edad"),peso=Estilos.campo("Peso");
        ComboBox<String>sexo=Estilos.combo();sexo.getItems().setAll("Macho","Hembra","Indeterminado");
        ComboBox<String>estado=Estilos.combo();estado.getItems().setAll("Bueno","Regular","Delicado","Crítico","Sin especificar");
        TextArea obs=Estilos.area("Observaciones");
        if(f!=null){a.getItems().stream().filter(o->o.texto().equals(f.c().get(1))).findFirst().ifPresent(a::setValue);c.setText(f.c().get(2));sexo.setValue(f.c().get(3).isBlank()?null:f.c().get(3));edad.setText(f.c().get(4));peso.setText(f.c().get(5));estado.setValue(estado.getItems().contains(f.c().get(6))?f.c().get(6):"Sin especificar");}
        GridPane g=Formulario.grid();Formulario.add(g,0,"Animal",ab,true);Formulario.add(g,1,"Cantidad",c,true);Formulario.add(g,2,"Sexo",sexo,false);Formulario.add(g,3,"Edad",edad,false);Formulario.add(g,4,"Peso",peso,false);Formulario.add(g,5,"Estado sanitario al ingreso",estado,false);Formulario.add(g,6,"Observaciones",obs,false);
        Button ids=Estilos.botonSec("◎ IDENTIFICACIONES DEL ANIMAL");g.add(ids,1,7);ids.setOnAction(e->{if(a.getValue()==null)error("Primero elegí un animal","Las identificaciones son opcionales.");else identificaciones(a.getValue().id());});
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Agregar animal al acta":"Modificar animal del acta",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(a.getValue()==null||!validar(c.getText()))throw new IllegalArgumentException("Elegí un animal e indicá la cantidad.");int n=Integer.parseInt(c.getText());if(n<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");if(id==0&&!confirmarOK("Agregar animal al acta","¿Estás segura de que querés agregar este animal a esta acta?"))return;SistemaDAO.guardarDetalle(id,ingreso,a.getValue().id(),n,sexo.getValue(),edad.getText(),peso.getText(),estado.getValue(),obs.getText());t.setItems(FXCollections.observableArrayList(SistemaDAO.detallesIngreso(ingreso)));}catch(Exception e){error("No se pudo guardar el detalle",e.getMessage());}});
    }

    private void identificaciones(int animal){
        TableView<SistemaDAO.Fila>t=new TableView<>();Estilos.tablaCompacta(t);t.setPrefHeight(220);String[]cs={"Tipo","Número","Fecha","Observaciones","Estado"};for(int i=0;i<cs.length;i++){int ix=i;TableColumn<SistemaDAO.Fila,String>c=new TableColumn<>(cs[i]);c.setCellValueFactory(x->new javafx.beans.property.SimpleStringProperty(x.getValue().c().size()>ix+1?x.getValue().c().get(ix+1):""));t.getColumns().add(c);}t.setItems(FXCollections.observableArrayList(SistemaDAO.identificaciones(animal)));Button add=Estilos.boton("＋ AGREGAR IDENTIFICACIÓN");add.setOnAction(e->identForm(animal,0,null,t));t.setRowFactory(tv->{TableRow<SistemaDAO.Fila>r=new TableRow<>();r.setOnMouseClicked(e->{if(e.getClickCount()==2&&!r.isEmpty())identForm(animal,r.getItem().id(),r.getItem(),t);});return r;});Dialog<ButtonType>d=new Dialog<>();d.setTitle("Tatú Carreta · Identificaciones");d.setHeaderText("Identificaciones del animal");d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);VBox b=new VBox(10,new Label("Chip, caravana, anillo u otro. Son opcionales, pero si existen quedan visibles en Plantel y Movimientos."),t,add);b.setPadding(new Insets(4));d.getDialogPane().setContent(b);d.setResizable(false);d.setOnShown(e->Estilos.estilizarDialog(d.getDialogPane()));d.showAndWait();
    }

    private void identForm(int animal,int id,SistemaDAO.Fila f,TableView<SistemaDAO.Fila>t){ComboBox<String>tipo=Estilos.combo();tipo.getItems().setAll("Chip","Caravana","Anillo","Otro");TextField num=Estilos.campo("Número / código"),obs=Estilos.campo("Observaciones");DatePicker fecha=Estilos.fecha();if(f!=null){tipo.setValue(f.c().get(1));num.setText(f.c().get(2));if(!f.c().get(3).isBlank())fecha.setValue(LocalDate.parse(f.c().get(3)));obs.setText(f.c().get(4));}GridPane g=Formulario.grid();Formulario.add(g,0,"Tipo",tipo,true);Formulario.add(g,1,"Número",num,true);Formulario.add(g,2,"Fecha asignación",fecha,false);Formulario.add(g,3,"Observaciones",obs,false);Dialog<ButtonType>d=Formulario.dialog(id==0?"Nueva identificación":"Modificar identificación",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(tipo.getValue()==null||!validar(num.getText()))throw new IllegalArgumentException("Elegí el tipo e indicá el número.");SistemaDAO.guardarIdentificacion(id,animal,tipo.getValue(),num.getText().trim(),fecha.getValue()==null?"":fecha.getValue().toString(),obs.getText().trim());t.setItems(FXCollections.observableArrayList(SistemaDAO.identificaciones(animal)));}catch(Exception ex){error("No se pudo guardar",ex.getMessage());}});}


    private void crearAnimal(ComboBox<SistemaDAO.Opcion>a){ComboBox<SistemaDAO.Opcion>esp=Estilos.combo();esp.getItems().setAll(SistemaDAO.especiesOpciones());Button ne=Estilos.botonSec("＋ Especie");HBox eb=new HBox(8,esp,ne);HBox.setHgrow(esp,Priority.ALWAYS);ne.setOnAction(e->crearEspecie(esp));TextField v=Estilos.campo("Nombre común"),c=Estilos.campo("Nombre científico"),o=Estilos.campo("Origen");GridPane g=Formulario.grid();Formulario.add(g,0,"Especie",eb,true);Formulario.add(g,1,"Nombre vulgar",v,true);Formulario.add(g,2,"Nombre científico",c,false);Formulario.add(g,3,"Origen",o,false);Dialog<ButtonType>d=Formulario.dialog("Crear animal desde el acta",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(esp.getValue()==null||!validar(v.getText()))throw new IllegalArgumentException("Elegí una especie y completá el nombre.");if(confirmarOK("Agregar animal","¿Estás segura de que querés agregar este animal?")){int id=SistemaDAO.guardarAnimal(0,esp.getValue().id(),v.getText().trim(),c.getText().trim(),o.getText().trim());a.getItems().setAll(SistemaDAO.animalesOpciones());a.getItems().stream().filter(q->q.id()==id).findFirst().ifPresent(a::setValue);}}catch(Exception ex){error("No se pudo crear el animal",ex.getMessage());}});}
    private void crearEspecie(ComboBox<SistemaDAO.Opcion>esp){TextField n=Estilos.campo("Nombre de la especie");GridPane g=Formulario.grid();Formulario.add(g,0,"Nombre",n,true);Dialog<ButtonType>d=Formulario.dialog("Crear especie desde el acta",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(n.getText()))throw new IllegalArgumentException("Completá el nombre.");if(confirmarOK("Agregar especie","¿Querés agregar esta especie?")){int id=SistemaDAO.guardarEspecie(0,n.getText().trim());esp.getItems().setAll(SistemaDAO.especiesOpciones());esp.getItems().stream().filter(q->q.id()==id).findFirst().ifPresent(esp::setValue);}}catch(Exception ex){error("No se pudo guardar",ex.getMessage());}});}
}
