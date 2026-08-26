package com.tatucarreta;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaHabitaculos {

    private final HabitaculoDAO habitaculoDAO =
            new HabitaculoDAO();

    private final ObservableList<Habitaculo> listaHabitaculos =
            FXCollections.observableArrayList();

    private Habitaculo habitaculoSeleccionado;

    private TableView<Habitaculo> tabla;

    private TextField txtNombre;
    private TextField txtSector;
    private TextField txtCapacidad;

    private ComboBox<String> cmbEstado;

    private TextArea txtObservaciones;

    public void mostrar() {

        Stage escenario = new Stage();

        // =========================================
        // ENCABEZADO
        // =========================================

        Label breadcrumb =
                new Label("Inicio / Gestión de Habitáculos");

        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A8580;"
        );

        Label titulo =
                new Label("Gestión de Habitáculos");

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        Label subtitulo =
                new Label(
                        "Administre los habitáculos de la reserva"
                );

        subtitulo.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6B756F;"
        );

        VBox encabezado = new VBox(
                6,
                breadcrumb,
                titulo,
                subtitulo
        );


        // =========================================
        // CAMPOS
        // =========================================

        txtNombre = new TextField();

        txtNombre.setPromptText(
                "Ej: Aviario principal"
        );

        txtSector = new TextField();

        txtSector.setPromptText(
                "Ej: Sector A"
        );

        txtCapacidad = new TextField();

        txtCapacidad.setPromptText(
                "Ej: 50"
        );

        cmbEstado = new ComboBox<>();

        cmbEstado.getItems().addAll(
                "Activo",
                "Inactivo"
        );

        cmbEstado.setValue("Activo");

        txtObservaciones = new TextArea();

        txtObservaciones.setPromptText(
                "Observaciones adicionales..."
        );

        txtObservaciones.setPrefRowCount(3);

        txtObservaciones.setWrapText(true);


        // =========================================
        // ESTILO DE CAMPOS
        // =========================================

        String estiloCampo =
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #D1D8D2;" +
                "-fx-padding: 8;";

        txtNombre.setStyle(estiloCampo);
        txtSector.setStyle(estiloCampo);
        txtCapacidad.setStyle(estiloCampo);
        cmbEstado.setStyle(estiloCampo);
        txtObservaciones.setStyle(estiloCampo);

        txtNombre.setPrefHeight(38);
        txtSector.setPrefHeight(38);
        txtCapacidad.setPrefHeight(38);
        cmbEstado.setPrefHeight(38);


        // =========================================
        // FORMULARIO
        // =========================================

        Label lblNombre =
                crearLabelCampo("Nombre");

        Label lblSector =
                crearLabelCampo("Sector");

        Label lblCapacidad =
                crearLabelCampo("Capacidad");

        Label lblEstado =
                crearLabelCampo("Estado");

        Label lblObservaciones =
                crearLabelCampo("Observaciones");


        GridPane formulario = new GridPane();

        formulario.setHgap(20);
        formulario.setVgap(15);

        formulario.add(
                lblNombre,
                0,
                0
        );

        formulario.add(
                lblSector,
                1,
                0
        );

        formulario.add(
                txtNombre,
                0,
                1
        );

        formulario.add(
                txtSector,
                1,
                1
        );


        formulario.add(
                lblCapacidad,
                0,
                2
        );

        formulario.add(
                lblEstado,
                1,
                2
        );

        formulario.add(
                txtCapacidad,
                0,
                3
        );

        formulario.add(
                cmbEstado,
                1,
                3
        );


        formulario.add(
                lblObservaciones,
                0,
                4,
                2,
                1
        );

        formulario.add(
                txtObservaciones,
                0,
                5,
                2,
                1
        );


        GridPane.setHgrow(
                txtNombre,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                txtSector,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                txtCapacidad,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                cmbEstado,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                txtObservaciones,
                Priority.ALWAYS
        );


        // =========================================
        // BOTONES
        // =========================================

        Button btnLimpiar =
                new Button("LIMPIAR");

        Button btnEliminar =
                new Button("ELIMINAR");

        Button btnModificar =
                new Button("MODIFICAR");

        Button btnGuardar =
                new Button("AGREGAR");

        Button btnVolver =
                new Button("VOLVER");


        btnLimpiar.setPrefHeight(38);
        btnEliminar.setPrefHeight(38);
        btnModificar.setPrefHeight(38);
        btnGuardar.setPrefHeight(38);
        btnVolver.setPrefHeight(38);


        btnLimpiar.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #405047;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C9D2CB;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        btnEliminar.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #A34A4A;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #E2C5C5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        btnModificar.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #405047;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C9D2CB;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        btnGuardar.setStyle(
                "-fx-background-color: #254D3D;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );

        btnVolver.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #405047;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C9D2CB;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );


        HBox botones = new HBox(
                10,
                btnLimpiar,
                btnEliminar,
                btnModificar,
                btnGuardar
        );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );


        // =========================================
        // TARJETA DEL FORMULARIO
        // =========================================

        Label tituloDatos =
                new Label("Datos del Habitáculo");

        tituloDatos.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );


        VBox tarjetaDatos = new VBox(
                20,
                tituloDatos,
                formulario,
                botones
        );

        tarjetaDatos.setPadding(
                new Insets(25)
        );

        tarjetaDatos.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D8DED9;" +
                "-fx-border-radius: 16;"
        );


        // =========================================
        // TABLA
        // =========================================

        tabla = new TableView<>();

        TableColumn<Habitaculo, Number> colId =
                new TableColumn<>("ID");

        colId.setCellValueFactory(
                celda ->
                        new SimpleIntegerProperty(
                                celda.getValue()
                                        .getIdHabitaculo()
                        )
        );


        TableColumn<Habitaculo, String> colNombre =
                new TableColumn<>("Nombre");

        colNombre.setCellValueFactory(
                celda ->
                        new SimpleStringProperty(
                                celda.getValue()
                                        .getNombre()
                        )
        );


        TableColumn<Habitaculo, String> colSector =
                new TableColumn<>("Sector");

        colSector.setCellValueFactory(
                celda ->
                        new SimpleStringProperty(
                                celda.getValue()
                                        .getSector()
                        )
        );


        TableColumn<Habitaculo, Number> colCapacidad =
                new TableColumn<>("Capacidad");

        colCapacidad.setCellValueFactory(
                celda -> {

                    Integer capacidad =
                            celda.getValue()
                                    .getCapacidad();

                    if (capacidad == null) {

                        return new SimpleIntegerProperty(0);
                    }

                    return new SimpleIntegerProperty(
                            capacidad
                    );
                }
        );


        TableColumn<Habitaculo, String> colEstado =
                new TableColumn<>("Estado");

        colEstado.setCellValueFactory(
                celda ->
                        new SimpleStringProperty(
                                celda.getValue()
                                        .getEstado()
                        )
        );


        tabla.getColumns().addAll(
                colId,
                colNombre,
                colSector,
                colCapacidad,
                colEstado
        );

        tabla.setItems(
                listaHabitaculos
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(280);

        tabla.setMinHeight(220);


        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         anterior,
                         seleccionado) -> {

                            if (seleccionado != null) {

                                cargarHabitaculoSeleccionado(
                                        seleccionado
                                );
                            }
                        }
                );


        // =========================================
        // TARJETA DE TABLA
        // =========================================

        Label tituloTabla =
                new Label("Habitáculos Registrados");

        tituloTabla.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );


        VBox tarjetaTabla = new VBox(
                15,
                tituloTabla,
                tabla
        );

        tarjetaTabla.setPadding(
                new Insets(25)
        );

        tarjetaTabla.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D8DED9;" +
                "-fx-border-radius: 16;"
        );


        // =========================================
        // BOTÓN VOLVER
        // =========================================

        HBox contenedorVolver =
                new HBox(btnVolver);

        contenedorVolver.setAlignment(
                Pos.CENTER
        );


        // =========================================
        // ACCIONES
        // =========================================

        btnGuardar.setOnAction(
                e -> guardar()
        );

        btnModificar.setOnAction(
                e -> modificar()
        );

        btnEliminar.setOnAction(
                e -> eliminar()
        );

        btnLimpiar.setOnAction(
                e -> limpiar()
        );

        btnVolver.setOnAction(
                e -> escenario.close()
        );


        // =========================================
        // CONTENEDOR PRINCIPAL
        // =========================================

        VBox contenido = new VBox(
                25,
                encabezado,
                tarjetaDatos,
                tarjetaTabla,
                contenedorVolver
        );

        contenido.setPadding(
                new Insets(25, 35, 35, 35)
        );

        contenido.setAlignment(
                Pos.TOP_CENTER
        );

        contenido.setStyle(
                "-fx-background-color: #F4F1E8;"
        );


        // =========================================
        // SCROLL
        // =========================================

        ScrollPane scroll =
                new ScrollPane(contenido);

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background: #F4F1E8;" +
                "-fx-background-color: #F4F1E8;"
        );


        // =========================================
        // ESCENA
        // =========================================

        Scene escena = new Scene(
                scroll,
                1100,
                750
        );

        escenario.setTitle(
                "Tatú Carreta - Gestión de Habitáculos"
        );

        escenario.setMinWidth(900);

        escenario.setMinHeight(650);

        escenario.setScene(escena);

        escenario.setMaximized(true);

        cargarHabitaculos();

        escenario.show();
    }


    // =========================================
    // CREAR LABEL
    // =========================================

    private Label crearLabelCampo(
            String texto) {

        Label label =
                new Label(texto);

        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        return label;
    }


    // =========================================
    // GUARDAR
    // =========================================

    private void guardar() {

        String nombre =
                txtNombre.getText().trim();

        String sector =
                txtSector.getText().trim();

        String capacidadTexto =
                txtCapacidad.getText().trim();

        String estado =
                cmbEstado.getValue();

        String observaciones =
                txtObservaciones.getText().trim();

        if (nombre.isBlank()
                || sector.isBlank()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Complete los campos obligatorios."
            );

            return;
        }

        Integer capacidad = null;

        if (!capacidadTexto.isBlank()) {

            try {

                capacidad =
                        Integer.parseInt(
                                capacidadTexto
                        );

            } catch (NumberFormatException e) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "La capacidad debe ser un número."
                );

                return;
            }
        }

        Habitaculo habitaculo =
                new Habitaculo();

        habitaculo.setNombre(nombre);

        habitaculo.setSector(sector);

        habitaculo.setCapacidad(capacidad);

        habitaculo.setEstado(estado);

        habitaculo.setObservaciones(
                observaciones
        );

        habitaculoDAO.agregar(habitaculo);

        cargarHabitaculos();

        limpiar();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Habitáculo agregado correctamente."
        );
    }


    // =========================================
    // MODIFICAR
    // =========================================

    private void modificar() {

        if (habitaculoSeleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione un habitáculo para modificar."
            );

            return;
        }

        String nombre =
                txtNombre.getText().trim();

        String sector =
                txtSector.getText().trim();

        String capacidadTexto =
                txtCapacidad.getText().trim();

        if (nombre.isBlank()
                || sector.isBlank()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Complete los campos obligatorios."
            );

            return;
        }

        Integer capacidad = null;

        if (!capacidadTexto.isBlank()) {

            try {

                capacidad =
                        Integer.parseInt(
                                capacidadTexto
                        );

            } catch (NumberFormatException e) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "La capacidad debe ser un número."
                );

                return;
            }
        }

        habitaculoSeleccionado.setNombre(
                nombre
        );

        habitaculoSeleccionado.setSector(
                sector
        );

        habitaculoSeleccionado.setCapacidad(
                capacidad
        );

        habitaculoSeleccionado.setEstado(
                cmbEstado.getValue()
        );

        habitaculoSeleccionado.setObservaciones(
                txtObservaciones
                        .getText()
                        .trim()
        );

        habitaculoDAO.modificar(
                habitaculoSeleccionado
        );

        cargarHabitaculos();

        limpiar();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Habitáculo modificado correctamente."
        );
    }


    // =========================================
    // ELIMINAR
    // =========================================

    private void eliminar() {

        if (habitaculoSeleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione un habitáculo para eliminar."
            );

            return;
        }

        habitaculoDAO.eliminar(
                habitaculoSeleccionado
                        .getIdHabitaculo()
        );

        cargarHabitaculos();

        limpiar();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Habitáculo eliminado correctamente."
        );
    }


    // =========================================
    // CARGAR SELECCIONADO
    // =========================================

    private void cargarHabitaculoSeleccionado(
            Habitaculo habitaculo) {

        habitaculoSeleccionado =
                habitaculo;

        txtNombre.setText(
                habitaculo.getNombre()
        );

        txtSector.setText(
                habitaculo.getSector()
        );

        if (habitaculo.getCapacidad() != null) {

            txtCapacidad.setText(
                    String.valueOf(
                            habitaculo.getCapacidad()
                    )
            );

        } else {

            txtCapacidad.clear();
        }

        cmbEstado.setValue(
                habitaculo.getEstado()
        );

        txtObservaciones.setText(
                habitaculo.getObservaciones()
        );
    }


    // =========================================
    // CARGAR TABLA
    // =========================================

    private void cargarHabitaculos() {

        listaHabitaculos.setAll(
                habitaculoDAO.listar()
        );
    }


    // =========================================
    // LIMPIAR
    // =========================================

    private void limpiar() {

        habitaculoSeleccionado = null;

        txtNombre.clear();

        txtSector.clear();

        txtCapacidad.clear();

        cmbEstado.setValue("Activo");

        txtObservaciones.clear();

        tabla.getSelectionModel()
                .clearSelection();
    }


    // =========================================
    // MENSAJES
    // =========================================

    private void mostrarMensaje(
            Alert.AlertType tipo,
            String mensaje) {

        Alert alerta =
                new Alert(tipo);

        alerta.setTitle("Tatú Carreta");

        alerta.setHeaderText(null);

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}