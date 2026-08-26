package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaMovimientos {

    private final MovimientoDAO movimientoDAO =
            new MovimientoDAO();

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final IngresoDAO ingresoDAO =
            new IngresoDAO();

    private TableView<Movimiento> tabla;

    private ComboBox<Animal> comboAnimal;
    private ComboBox<Ingreso> comboIngreso;

    private TextField txtFecha;
    private ComboBox<String> comboTipo;
    private TextField txtCantidad;
    private TextField txtDestino;
    private TextArea txtObservaciones;


    public void mostrar(Stage escenario) {

        // =========================
        // TITULO
        // =========================

        Label ruta = new Label(
                "Inicio / Gestión de Movimientos"
        );

        ruta.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A827B;"
        );


        Label titulo = new Label(
                "Movimientos de Animales"
        );

        titulo.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #30463A;"
        );


        Label subtitulo = new Label(
                "Registre y consulte los movimientos de los animales de la reserva"
        );

        subtitulo.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #68746B;"
        );


        VBox encabezado = new VBox(
                5,
                ruta,
                titulo,
                subtitulo
        );


        // =========================
        // CAMPOS
        // =========================

        comboAnimal = new ComboBox<>();

        comboAnimal.setMaxWidth(
                Double.MAX_VALUE
        );

        comboAnimal.setPromptText(
                "Seleccione un animal"
        );

        cargarAnimales();


        comboAnimal.setOnAction(e ->
                cargarIngresosPorAnimal()
        );


        comboIngreso = new ComboBox<>();

        comboIngreso.setMaxWidth(
                Double.MAX_VALUE
        );

        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );


        txtFecha = new TextField();

        txtFecha.setPromptText(
                "Ej: 25/08/2026"
        );


        comboTipo = new ComboBox<>();

        comboTipo.setItems(
                FXCollections.observableArrayList(
                        "LIBERACIÓN",
                        "TRASLADO",
                        "FALLECIMIENTO"
                )
        );

        comboTipo.setMaxWidth(
                Double.MAX_VALUE
        );

        comboTipo.setPromptText(
                "Seleccione el tipo"
        );


        txtCantidad = new TextField();

        txtCantidad.setPromptText(
                "Ingrese la cantidad"
        );


        txtDestino = new TextField();

        txtDestino.setPromptText(
                "Solo si corresponde"
        );


        txtObservaciones = new TextArea();

        txtObservaciones.setPromptText(
                "Ingrese observaciones adicionales"
        );

        txtObservaciones.setPrefRowCount(2);

        txtObservaciones.setWrapText(true);


        // =========================
        // ESTILO CAMPOS
        // =========================

        String estiloCampo =
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-font-size: 13px;";

        comboAnimal.setStyle(estiloCampo);
        comboIngreso.setStyle(estiloCampo);
        txtFecha.setStyle(estiloCampo);
        comboTipo.setStyle(estiloCampo);
        txtCantidad.setStyle(estiloCampo);
        txtDestino.setStyle(estiloCampo);
        txtObservaciones.setStyle(estiloCampo);


        // =========================
        // FORMULARIO
        // =========================

        GridPane formulario = new GridPane();

        formulario.setHgap(15);
        formulario.setVgap(10);

        formulario.setAlignment(
                Pos.CENTER
        );


        ColumnConstraints columnaLabel =
                new ColumnConstraints();

        columnaLabel.setPercentWidth(28);


        ColumnConstraints columnaCampo =
                new ColumnConstraints();

        columnaCampo.setPercentWidth(72);


        formulario.getColumnConstraints().addAll(
                columnaLabel,
                columnaCampo
        );


        formulario.add(
                crearLabel("Animal *"),
                0,
                0
        );

        formulario.add(
                comboAnimal,
                1,
                0
        );


        formulario.add(
                crearLabel("N° de Acta *"),
                0,
                1
        );

        formulario.add(
                comboIngreso,
                1,
                1
        );


        formulario.add(
                crearLabel("Fecha *"),
                0,
                2
        );

        formulario.add(
                txtFecha,
                1,
                2
        );


        formulario.add(
                crearLabel("Tipo de movimiento *"),
                0,
                3
        );

        formulario.add(
                comboTipo,
                1,
                3
        );


        formulario.add(
                crearLabel("Cantidad *"),
                0,
                4
        );

        formulario.add(
                txtCantidad,
                1,
                4
        );


        formulario.add(
                crearLabel("Destino"),
                0,
                5
        );

        formulario.add(
                txtDestino,
                1,
                5
        );


        formulario.add(
                crearLabel("Observaciones"),
                0,
                6
        );

        formulario.add(
                txtObservaciones,
                1,
                6
        );


        // =========================
        // TARJETA FORMULARIO
        // =========================

        Label tituloFormulario = new Label(
                "Datos del movimiento"
        );

        tituloFormulario.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #30463A;"
        );


        VBox tarjetaFormulario =
                new VBox(
                        18,
                        tituloFormulario,
                        formulario
                );

        tarjetaFormulario.setPadding(
                new Insets(22)
        );

        tarjetaFormulario.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D5DBD5;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );


        // =========================
        // BOTONES
        // =========================

        Button btnRegistrar =
                new Button(
                        "REGISTRAR MOVIMIENTO"
                );

        btnRegistrar.setPrefHeight(38);

        btnRegistrar.setStyle(
                "-fx-background-color: #23452C;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 20 0 20;"
        );


        btnRegistrar.setOnAction(e ->
                guardarMovimiento()
        );


        Button btnLimpiar =
                new Button("LIMPIAR");

        btnLimpiar.setPrefHeight(38);

        btnLimpiar.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: #30463A;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 20 0 20;"
        );


        btnLimpiar.setOnAction(e ->
                limpiarCampos()
        );


        HBox botones = new HBox(
                10,
                btnRegistrar,
                btnLimpiar
        );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );


        // =========================
        // TABLA
        // =========================

        tabla = new TableView<>();

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        TableColumn<Movimiento, Integer>
                columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idMovimiento"
                )
        );


        TableColumn<Movimiento, Integer>
                columnaAnimal =
                new TableColumn<>("ID Animal");

        columnaAnimal.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idAnimal"
                )
        );


        TableColumn<Movimiento, String>
                columnaFecha =
                new TableColumn<>("Fecha");

        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fechaMovimiento"
                )
        );


        TableColumn<Movimiento, String>
                columnaTipo =
                new TableColumn<>("Tipo");

        columnaTipo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "tipoMovimiento"
                )
        );


        TableColumn<Movimiento, Integer>
                columnaCantidad =
                new TableColumn<>("Cantidad");

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "cantidad"
                )
        );


        TableColumn<Movimiento, String>
                columnaDestino =
                new TableColumn<>("Destino");

        columnaDestino.setCellValueFactory(
                new PropertyValueFactory<>(
                        "destino"
                )
        );


        tabla.getColumns().addAll(
                columnaId,
                columnaAnimal,
                columnaFecha,
                columnaTipo,
                columnaCantidad,
                columnaDestino
        );

        // TABLA MAS BAJA PARA QUE ENTRE COMPLETA
        tabla.setPrefHeight(170);

        cargarMovimientos();


        // =========================
        // TARJETA TABLA
        // =========================

        Label tituloTabla = new Label(
                "Historial de movimientos"
        );

        tituloTabla.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #30463A;"
        );


        VBox tarjetaTabla =
                new VBox(
                        12,
                        tituloTabla,
                        tabla
                );

        tarjetaTabla.setPadding(
                new Insets(18)
        );

        tarjetaTabla.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D5DBD5;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );


        // =========================
        // CONTENEDOR PRINCIPAL
        // =========================

        VBox contenido =
                new VBox(16);

        contenido.setPadding(
                new Insets(25, 35, 25, 35)
        );

        contenido.getChildren().addAll(
                encabezado,
                tarjetaFormulario,
                botones,
                tarjetaTabla
        );

        contenido.setStyle(
                "-fx-background-color: #F2F0E6;"
        );


        BorderPane raiz =
                new BorderPane();

        raiz.setCenter(contenido);


        // =========================
        // ESCENA
        // =========================

        Scene escena = new Scene(
                raiz,
                900,
                700
        );

        escenario.setTitle(
                "Tatú Carreta - Movimientos"
        );

        escenario.setScene(escena);

        escenario.setMinWidth(850);
        escenario.setMinHeight(650);

        escenario.show();
    }


    // =========================
    // LABEL
    // =========================

    private Label crearLabel(String texto) {

        Label label = new Label(texto);

        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #34463A;"
        );

        return label;
    }


    // =========================
    // CARGAR ANIMALES
    // =========================

    private void cargarAnimales() {

        ObservableList<Animal> animales =
                FXCollections.observableArrayList(
                        animalDAO.listar()
                );

        comboAnimal.setItems(animales);
    }


    // =========================
    // CARGAR INGRESOS
    // =========================

    private void cargarIngresosPorAnimal() {

        Animal animalSeleccionado =
                comboAnimal.getValue();

        comboIngreso.getItems().clear();

        if (animalSeleccionado == null) {
            return;
        }

        ObservableList<Ingreso> ingresos =
                FXCollections.observableArrayList(
                        ingresoDAO.listarPorAnimal(
                                animalSeleccionado.getIdAnimal()
                        )
                );

        comboIngreso.setItems(ingresos);

        comboIngreso.setPromptText(
                "Seleccione el N° de acta"
        );
    }


    // =========================
    // GUARDAR
    // =========================

    private void guardarMovimiento() {

        try {

            if (comboAnimal.getValue() == null
                    || comboIngreso.getValue() == null
                    || txtFecha.getText().isBlank()
                    || comboTipo.getValue() == null
                    || txtCantidad.getText().isBlank()) {

                mostrarMensaje(
                        "Complete los campos obligatorios."
                );

                return;
            }

            Movimiento movimiento =
                    new Movimiento();

            movimiento.setIdAnimal(
                    comboAnimal.getValue()
                            .getIdAnimal()
            );

            movimiento.setIdIngreso(
                    comboIngreso.getValue()
                            .getIdIngreso()
            );

            movimiento.setFechaMovimiento(
                    txtFecha.getText()
            );

            movimiento.setTipoMovimiento(
                    comboTipo.getValue()
            );

            movimiento.setCantidad(
                    Integer.parseInt(
                            txtCantidad.getText()
                    )
            );

            movimiento.setDestino(
                    txtDestino.getText()
            );

            movimiento.setObservaciones(
                    txtObservaciones.getText()
            );

            movimientoDAO.agregar(movimiento);

            mostrarInformacion(
                    "Movimiento registrado correctamente."
            );

            limpiarCampos();

            cargarMovimientos();

        } catch (NumberFormatException e) {

            mostrarMensaje(
                    "La cantidad debe ser un número."
            );
        }
    }


    // =========================
    // CARGAR TABLA
    // =========================

    private void cargarMovimientos() {

        ObservableList<Movimiento> movimientos =
                FXCollections.observableArrayList(
                        movimientoDAO.listar()
                );

        tabla.setItems(movimientos);
    }


    // =========================
    // LIMPIAR
    // =========================

    private void limpiarCampos() {

        comboAnimal.setValue(null);

        comboIngreso.getItems().clear();

        comboIngreso.setValue(null);

        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );

        txtFecha.clear();

        comboTipo.setValue(null);

        txtCantidad.clear();

        txtDestino.clear();

        txtObservaciones.clear();
    }


    // =========================
    // MENSAJES
    // =========================

    private void mostrarMensaje(String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.WARNING
        );

        alerta.setTitle("Tatú Carreta");

        alerta.setHeaderText(null);

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }


    private void mostrarInformacion(
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION
        );

        alerta.setTitle("Tatú Carreta");

        alerta.setHeaderText(null);

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}