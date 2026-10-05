package com.tatucarreta.vistas;

import com.tatucarreta.data.*;
import com.tatucarreta.ui.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;

public class VistaPlantel extends VistaCRUD {
    public VistaPlantel(){
        super("Plantel Permanente","Ejemplares que permanecen en la Reserva. Desde acá se registran altas, nacimientos, fallecimientos e identificaciones.",new String[]{"Animal","Identificación","Especie","Cantidad","Ubicación","Habitáculo","Ingreso","Estado"});
        agregar.setText("＋ AGREGAR EJEMPLAR");
        Button nac=Estilos.botonSec("🐣 NACIMIENTO"),fall=Estilos.botonSec("⚠ FALLECIMIENTO"),tras=Estilos.botonSec("↔ TRASLADO"),ids=Estilos.botonSec("◎ IDENTIFICACIONES"),rep=Estilos.boton("▣ REPORTE MENSUAL");
        FlowPane acciones=new FlowPane(8,8,nac,fall,tras,ids,rep);acciones.setPrefWrapLength(900);
        VBox top=(VBox)getTop();top.getChildren().add(2,acciones);
        nac.setOnAction(e->movimientoVital(true));fall.setOnAction(e->movimientoVital(false));tras.setOnAction(e->trasladarSeleccionado());rep.setOnAction(e->generarReporte());
        ids.setOnAction(e->{var f=tabla.getSelectionModel().getSelectedItem();if(f==null)info("Seleccioná un ejemplar","Elegí un animal del Plantel para administrar sus identificaciones.");else if(f.c().get(1).startsWith("⚠"))info("Registro pendiente de asociación","Este registro histórico no tiene un animal asociado.");else identificaciones(SistemaDAO.animalPorPlantel(f.id()));});
        tabla.setRowFactory(tv->{TableRow<SistemaDAO.Fila>r=new TableRow<>();r.setOnMouseClicked(e->{if(e.getClickCount()==2&&!r.isEmpty())editarSeleccionado();});return r;});
        cargar("");
    }
    protected List<SistemaDAO.Fila> consultar(String q){return SistemaDAO.plantel(q);}
    protected List<SistemaDAO.Fila> consultarInactivos(String q){return SistemaDAO.plantelInactivos(q);}
    protected void activarRegistro(int id){SistemaDAO.activar("plantel_permanente","id_plantel",id);}
    protected void agregarNuevo(){form(0,null);}
    protected void editarSeleccionado(){var f=tabla.getSelectionModel().getSelectedItem();if(f!=null)form(f.id(),f);}
    protected void desactivar(int id){if(confirmarOK("Enviar registro a inactivos","¿Estás segura de que querés quitar este registro del Plantel activo? El historial se conserva.")){SistemaDAO.bajaPlantel(id);cargar(buscador.getText());}}

    private void form(int id,SistemaDAO.Fila f){
        ComboBox<SistemaDAO.Opcion>animal=Estilos.combo();animal.getItems().setAll(SistemaDAO.animalesOpciones());Button nuevoAnimal=Estilos.botonSec("＋ Animal");HBox animalBox=new HBox(8,animal,nuevoAnimal);HBox.setHgrow(animal,Priority.ALWAYS);nuevoAnimal.setOnAction(e->crearAnimal(animal));
        ComboBox<String>ubic=Estilos.combo();ubic.getItems().setAll("Habitáculo","Campo abierto");ComboBox<SistemaDAO.Opcion>hab=Estilos.combo();hab.getItems().setAll(SistemaDAO.habitaculoOpciones());
        TextField cant=Estilos.entero("Cantidad en plantel",6);TextArea obs=Estilos.area("Observaciones");DatePicker fecha=Estilos.fecha();
        if(f!=null){animal.getItems().stream().filter(o->o.texto().equals(f.c().get(1))).findFirst().ifPresent(animal::setValue);cant.setText(f.c().get(4));ubic.setValue(f.c().get(5).isBlank()?"Campo abierto":f.c().get(5));hab.getItems().stream().filter(o->o.texto().equals(f.c().get(6))).findFirst().ifPresent(hab::setValue);if(!f.c().get(7).isBlank())fecha.setValue(LocalDate.parse(f.c().get(7)));}
        else fecha.setValue(LocalDate.now());
        hab.setVisible(false);hab.setManaged(false);ubic.valueProperty().addListener((o,a,v)->{boolean show="Habitáculo".equals(v);hab.setVisible(show);hab.setManaged(show);if(!show)hab.setValue(null);});
        GridPane g=Formulario.grid();Formulario.add(g,0,"Animal",animalBox,true);Formulario.add(g,1,"Cantidad",cant,true);Formulario.add(g,2,"Ubicación",ubic,true);Formulario.add(g,3,"Habitáculo",hab,false);Formulario.add(g,4,"Fecha de ingreso",fecha,true);Formulario.add(g,5,"Observaciones",obs,false);
        Button ids=Estilos.botonSec("◎ IDENTIFICACIONES DEL ANIMAL");g.add(ids,1,6);ids.setOnAction(e->{if(animal.getValue()==null)info("Primero elegí un animal","Las identificaciones son opcionales.");else identificaciones(animal.getValue().id());});
        Dialog<ButtonType>d=Formulario.dialog(id==0?"Agregar ejemplar al Plantel Permanente":"Modificar registro del Plantel",g);
        d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(animal.getValue()==null||fecha.getValue()==null||ubic.getValue()==null||!validar(cant.getText()))throw new IllegalArgumentException("Completá los campos obligatorios.");int n=Integer.parseInt(cant.getText());if(n<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");if("Habitáculo".equals(ubic.getValue())&&hab.getValue()==null)throw new IllegalArgumentException("Elegí el habitáculo.");if(confirmarOK(id==0?"Agregar al Plantel":"Guardar cambios",id==0?"¿Estás segura de que querés agregar este ejemplar al Plantel Permanente?":"¿Querés guardar los cambios?")){SistemaDAO.guardarPlantel(id,animal.getValue().id(),n,ubic.getValue(),hab.getValue()==null?null:hab.getValue().id(),fecha.getValue().toString(),obs.getText().trim());cargar(buscador.getText());}}catch(Exception e){error("No se pudo guardar",e.getMessage());}});
    }

    private void trasladarSeleccionado(){
        var f=tabla.getSelectionModel().getSelectedItem();
        if(f==null){info("Seleccioná un ejemplar","Elegí un registro del Plantel Permanente para registrar su traslado.");return;}
        if(f.c().get(1).startsWith("⚠")){info("Registro pendiente de asociación","Este registro histórico no tiene un animal asociado.");return;}
        int plantel=f.id();
        int actual;
        try{actual=SistemaDAO.cantidadPlantel(plantel);}catch(Exception ex){error("No se pudo consultar la existencia",ex.getMessage());return;}
        ComboBox<String>sexo=Estilos.combo();sexo.getItems().setAll("Macho","Hembra","Indeterminado");
        TextField cant=Estilos.entero("Cantidad a trasladar",6),centro=Estilos.campo("Centro / institución de destino"),localidad=Estilos.campo("Localidad / provincia");
        DatePicker fecha=Estilos.fecha();fecha.setValue(LocalDate.now());TextArea obs=Estilos.area("Motivo / observaciones");
        Label disponibilidad=new Label("Existencia actual en Plantel: "+actual);disponibilidad.setStyle("-fx-text-fill:"+Estilos.VERDE_OSCURO+";-fx-font-weight:bold;");
        GridPane g=Formulario.grid();Formulario.add(g,0,"Animal",new Label(f.c().get(1)),false);Formulario.add(g,1,"Cantidad",cant,true);Formulario.add(g,2,"Sexo",sexo,false);Formulario.add(g,3,"Fecha",fecha,true);Formulario.add(g,4,"Centro / institución",centro,true);Formulario.add(g,5,"Localidad / provincia",localidad,true);Formulario.add(g,6,"Motivo / observaciones",obs,false);g.add(disponibilidad,1,7);
        Dialog<ButtonType>d=Formulario.dialog("Trasladar animal desde Plantel Permanente",g);
        d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(cant.getText(),centro.getText(),localidad.getText())||fecha.getValue()==null)throw new IllegalArgumentException("Completá cantidad, fecha y datos del destino.");int n=Integer.parseInt(cant.getText());if(n<=0||n>actual)throw new IllegalArgumentException("La cantidad debe ser mayor que cero y no superar la existencia actual ("+actual+").");String destino=centro.getText().trim()+" · "+localidad.getText().trim();if(confirmarOK("Confirmar traslado","¿Querés registrar el traslado de "+n+" ejemplar(es) a "+destino+"?")){SistemaDAO.trasladarPlantel(plantel,n,fecha.getValue().toString(),sexo.getValue(),destino,obs.getText().trim());cargar(buscador.getText());info("Traslado registrado","El traslado quedó guardado en Movimientos y la existencia del Plantel fue actualizada.");}}catch(Exception ex){error("No se pudo registrar el traslado",ex.getMessage());}});
    }

    private void movimientoVital(boolean nacimiento){
        ComboBox<SistemaDAO.Opcion>p=Estilos.combo();p.getItems().setAll(SistemaDAO.plantelOpciones());ComboBox<String>sexo=Estilos.combo();sexo.getItems().setAll("Macho","Hembra","Indeterminado");DatePicker fecha=Estilos.fecha();fecha.setValue(LocalDate.now());TextField cant=Estilos.entero(nacimiento?"Cantidad nacida":"Cantidad fallecida",6);TextArea obs=Estilos.area("Observaciones");
        GridPane g=Formulario.grid();Formulario.add(g,0,"Ejemplar del plantel",p,true);Formulario.add(g,1,"Sexo",sexo,true);Formulario.add(g,2,"Cantidad",cant,true);Formulario.add(g,3,"Fecha",fecha,true);Formulario.add(g,4,"Observaciones",obs,false);Dialog<ButtonType>d=Formulario.dialog(nacimiento?"Registrar nacimiento":"Registrar fallecimiento",g);
        d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(p.getValue()==null||sexo.getValue()==null||fecha.getValue()==null||!validar(cant.getText()))throw new IllegalArgumentException("Elegí el ejemplar, sexo, cantidad y fecha.");int n=Integer.parseInt(cant.getText());if(n<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");var pf=SistemaDAO.plantel("").stream().filter(r->r.id()==p.getValue().id()).findFirst().orElse(null);if(pf==null)throw new IllegalArgumentException("No se encontró el ejemplar seleccionado.");int actual=Integer.parseInt(pf.c().get(4));if(!nacimiento&&n>actual)throw new IllegalArgumentException("La cantidad no puede superar la existencia actual ("+actual+").");if(confirmarOK(nacimiento?"Confirmar nacimiento":"Confirmar fallecimiento",nacimiento?"¿Estás segura de que querés registrar este nacimiento?":"¿Estás segura de que querés registrar este fallecimiento?")){int animal=SistemaDAO.animalPorPlantel(p.getValue().id());if(nacimiento)SistemaDAO.registrarNacimiento(p.getValue().id(),animal,n,sexo.getValue(),fecha.getValue().toString(),obs.getText().trim());else SistemaDAO.registrarFallecimiento(p.getValue().id(),animal,n,sexo.getValue(),fecha.getValue().toString(),obs.getText().trim());cargar(buscador.getText());info("Registro guardado","El movimiento quedó conservado en el historial.");}}catch(Exception e){error("No se pudo registrar el movimiento",e.getMessage());}});
    }

    private void identificaciones(int animal){
        TableView<SistemaDAO.Fila>t=new TableView<>();Estilos.tablaCompacta(t);t.setPrefHeight(230);String[]cs={"Tipo","Número","Fecha","Observaciones","Estado"};for(int i=0;i<cs.length;i++){int ix=i;TableColumn<SistemaDAO.Fila,String>c=new TableColumn<>(cs[i]);c.setCellValueFactory(x->new javafx.beans.property.SimpleStringProperty(x.getValue().c().size()>ix+1?x.getValue().c().get(ix+1):""));t.getColumns().add(c);}t.setItems(FXCollections.observableArrayList(SistemaDAO.identificaciones(animal)));
        Button add=Estilos.boton("＋ AGREGAR IDENTIFICACIÓN");add.setOnAction(e->identForm(animal,0,null,t));t.setRowFactory(tv->{TableRow<SistemaDAO.Fila>r=new TableRow<>();r.setOnMouseClicked(e->{if(e.getClickCount()==2&&!r.isEmpty())identForm(animal,r.getItem().id(),r.getItem(),t);});return r;});Dialog<ButtonType>d=new Dialog<>();d.setTitle("Tatú Carreta · Identificaciones");d.setHeaderText("Identificaciones del animal");d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);VBox b=new VBox(10,new Label("Chip, caravana, anillo u otro. Quedan visibles también en Plantel y Movimientos."),t,add);b.setPadding(new Insets(4));d.getDialogPane().setContent(b);d.setResizable(false);d.setOnShown(e->Estilos.estilizarDialog(d.getDialogPane()));d.showAndWait();
    }
    private void identForm(int animal,int id,SistemaDAO.Fila f,TableView<SistemaDAO.Fila>t){ComboBox<String>tipo=Estilos.combo();tipo.getItems().setAll("Chip","Caravana","Anillo","Otro");TextField num=Estilos.campo("Número / código"),obs=Estilos.campo("Observaciones");DatePicker fecha=Estilos.fecha();if(f!=null){tipo.setValue(f.c().get(1));num.setText(f.c().get(2));if(!f.c().get(3).isBlank())fecha.setValue(LocalDate.parse(f.c().get(3)));obs.setText(f.c().get(4));}GridPane g=Formulario.grid();Formulario.add(g,0,"Tipo",tipo,true);Formulario.add(g,1,"Número",num,true);Formulario.add(g,2,"Fecha asignación",fecha,false);Formulario.add(g,3,"Observaciones",obs,false);Dialog<ButtonType>d=Formulario.dialog(id==0?"Nueva identificación":"Modificar identificación",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(tipo.getValue()==null||!validar(num.getText()))throw new IllegalArgumentException("Elegí el tipo e indicá el número.");SistemaDAO.guardarIdentificacion(id,animal,tipo.getValue(),num.getText().trim(),fecha.getValue()==null?"":fecha.getValue().toString(),obs.getText().trim());t.setItems(FXCollections.observableArrayList(SistemaDAO.identificaciones(animal)));}catch(Exception ex){error("No se pudo guardar",ex.getMessage());}});
    }

    private void crearAnimal(ComboBox<SistemaDAO.Opcion>animal){ComboBox<SistemaDAO.Opcion>esp=Estilos.combo();esp.getItems().setAll(SistemaDAO.especiesOpciones());Button ne=Estilos.botonSec("＋ Especie");HBox eb=new HBox(8,esp,ne);HBox.setHgrow(esp,Priority.ALWAYS);ne.setOnAction(e->crearEspecie(esp));TextField v=Estilos.campo("Nombre común"),c=Estilos.campo("Nombre científico"),o=Estilos.campo("Origen");GridPane g=Formulario.grid();Formulario.add(g,0,"Especie",eb,true);Formulario.add(g,1,"Nombre vulgar",v,true);Formulario.add(g,2,"Nombre científico",c,false);Formulario.add(g,3,"Origen",o,false);Dialog<ButtonType>d=Formulario.dialog("Crear animal desde Plantel Permanente",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(esp.getValue()==null||!validar(v.getText()))throw new IllegalArgumentException("Elegí una especie y completá el nombre.");if(confirmarOK("Agregar animal","¿Querés agregar este animal?")){int id=SistemaDAO.guardarAnimal(0,esp.getValue().id(),v.getText().trim(),c.getText().trim(),o.getText().trim());animal.getItems().setAll(SistemaDAO.animalesOpciones());animal.getItems().stream().filter(q->q.id()==id).findFirst().ifPresent(animal::setValue);}}catch(Exception ex){error("No se pudo crear el animal",ex.getMessage());}});}
    private void crearEspecie(ComboBox<SistemaDAO.Opcion>esp){TextField n=Estilos.campo("Nombre de la especie");GridPane g=Formulario.grid();Formulario.add(g,0,"Nombre",n,true);Dialog<ButtonType>d=Formulario.dialog("Crear especie desde Plantel Permanente",g);d.showAndWait().ifPresent(x->{if(x==ButtonType.OK)try{if(!validar(n.getText()))throw new IllegalArgumentException("Completá el nombre.");if(confirmarOK("Agregar especie","¿Querés agregar esta especie?")){int id=SistemaDAO.guardarEspecie(0,n.getText().trim());esp.getItems().setAll(SistemaDAO.especiesOpciones());esp.getItems().stream().filter(q->q.id()==id).findFirst().ifPresent(esp::setValue);}}catch(Exception ex){error("No se pudo guardar",ex.getMessage());}});}
    private void generarReporte(){try{java.io.File f=ReportePlantelPDF.generar();if(java.awt.Desktop.isDesktopSupported())java.awt.Desktop.getDesktop().open(f);info("Reporte generado","Se generó el reporte mensual en: "+f.getAbsolutePath());}catch(Exception e){error("No se pudo generar el reporte",e.getMessage());}}
}
