package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaHabitaculos {

    private final HabitaculoDAO habitaculoDAO =
            new HabitaculoDAO();

    private final TableView<Habitaculo> tabla =
            new TableView<>();

    private final TextField txtNombre =
            new TextField();

    private final TextField txtSector =
            new TextField();

    private final TextField txtCapacidad =
            new TextField();

    private final TextField txtEstado =
            new TextField();

    private final TextArea txtObservaciones =
            new TextArea();

    public ScrollPane crearContenido() {

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
                        "Administre los espacios destinados al alojamiento de animales"
                );

        subtitulo.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6B756F;"
        );

        VBox encabezado =
                new VBox(
                        6,
                        breadcrumb,
                        titulo,
                        subtitulo
                );

        // =========================================
        // CAMPOS
        // =========================================

        Label tituloDatos =
                new Label("Datos del Habitáculo");

        tituloDatos.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        Label lblNombre =
                crearEtiqueta("Nombre");

        Label lblSector =
                crearEtiqueta("Sector");

        Label lblCapacidad =
                crearEtiqueta("Capacidad");

        Label lblEstado =
                crearEtiqueta("Estado");

        Label lblObservaciones =
                crearEtiqueta("Observaciones");

        txtNombre.setPromptText(
                "Ej: Recinto de felinos"
        );

        txtSector.setPromptText(
                "Ej: Sector Norte"
        );

        txtCapacidad.setPromptText(
                "Ej: 10"
        );

        txtEstado.setPromptText(
                "Ej: Disponible"
        );

        txtObservaciones.setPromptText(
                "Observaciones del habitáculo"
        );

        txtObservaciones.setPrefRowCount(3);

        VBox campoNombre =
                crearCampo(
                        lblNombre,
                        txtNombre
                );

        VBox campoSector =
                crearCampo(
                        lblSector,
                        txtSector
                );

        VBox campoCapacidad =
                crearCampo(
                        lblCapacidad,
                        txtCapacidad
                );

        VBox campoEstado =
                crearCampo(
                        lblEstado,
                        txtEstado
                );

        VBox campoObservaciones =
                crearCampo(
                        lblObservaciones,
                        txtObservaciones
                );

        HBox fila1 =
                new HBox(
                        20,
                        campoNombre,
                        campoSector
                );

        HBox fila2 =
                new HBox(
                        20,
                        campoCapacidad,
                        campoEstado
                );

        campoNombre.setPrefWidth(350);
        campoSector.setPrefWidth(350);
        campoCapacidad.setPrefWidth(350);
        campoEstado.setPrefWidth(350);
        campoObservaciones.setPrefWidth(720);

        // =========================================
        // BOTONES
        // =========================================

        Button btnLimpiar =
                new Button("LIMPIAR");

        Button btnEliminar =
                new Button("ELIMINAR");

        Button btnModificar =
                new Button("MODIFICAR");

        Button btnAgregar =
                new Button("AGREGAR");

        btnLimpiar.setPrefHeight(36);
        btnEliminar.setPrefHeight(36);
        btnModificar.setPrefHeight(36);
        btnAgregar.setPrefHeight(36);

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

        btnAgregar.setStyle(
                "-fx-background-color: #254D3D;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );

        HBox botones =
                new HBox(
                        10,
                        btnLimpiar,
                        btnEliminar,
                        btnModificar,
                        btnAgregar
                );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox tarjetaDatos =
                new VBox(
                        18,
                        tituloDatos,
                        fila1,
                        fila2,
                        campoObservaciones,
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

        TableColumn<Habitaculo, String> columnaNombre =
                new TableColumn<>("Nombre");

        columnaNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        TableColumn<Habitaculo, String> columnaSector =
                new TableColumn<>("Sector");

        columnaSector.setCellValueFactory(
                new PropertyValueFactory<>("sector")
        );

        TableColumn<Habitaculo, Integer> columnaCapacidad =
                new TableColumn<>("Capacidad");

        columnaCapacidad.setCellValueFactory(
                new PropertyValueFactory<>("capacidad")
        );

        TableColumn<Habitaculo, String> columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        tabla.getColumns().clear();

        tabla.getColumns().addAll(
                columnaNombre,
                columnaSector,
                columnaCapacidad,
                columnaEstado
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(280);

        Label tituloTabla =
                new Label("Habitáculos Registrados");

        tituloTabla.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        VBox tarjetaTabla =
                new VBox(
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
        // CARGAR DATOS
        // =========================================

        cargarHabitaculos();

        // =========================================
        // SELECCIONAR
        // =========================================

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         anterior,
                         seleccionado) -> {

                            if (seleccionado != null) {

                                txtNombre.setText(
                                        seleccionado.getNombre()
                                );

                                txtSector.setText(
                                        seleccionado.getSector()
                                );

                                txtCapacidad.setText(
                                        String.valueOf(
                                                seleccionado.getCapacidad()
                                        )
                                );

                                txtEstado.setText(
                                        seleccionado.getEstado()
                                );

                                txtObservaciones.setText(
                                        seleccionado.getObservaciones()
                                );
                            }
                        }
                );

        // =========================================
        // AGREGAR
        // =========================================

        btnAgregar.setOnAction(e -> {

            if (txtNombre.getText().isBlank()
                    || txtSector.getText().isBlank()
                    || txtCapacidad.getText().isBlank()
                    || txtEstado.getText().isBlank()) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Datos incompletos",
                        "Complete todos los campos obligatorios."
                );

                return;
            }

            int capacidad;

            try {

                capacidad =
                        Integer.parseInt(
                                txtCapacidad.getText().trim()
                        );

            } catch (NumberFormatException ex) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Capacidad inválida",
                        "La capacidad debe ser un número entero."
                );

                return;
            }

            Habitaculo habitaculo =
                    new Habitaculo();

            habitaculo.setNombre(
                    txtNombre.getText().trim()
            );

            habitaculo.setSector(
                    txtSector.getText().trim()
            );

            habitaculo.setCapacidad(
                    capacidad
            );

            habitaculo.setEstado(
                    txtEstado.getText().trim()
            );

            habitaculo.setObservaciones(
                    txtObservaciones.getText().trim()
            );

            habitaculoDAO.agregar(
                    habitaculo
            );

            limpiarCampos();
            cargarHabitaculos();
        });

        // =========================================
        // MODIFICAR
        // =========================================

        btnModificar.setOnAction(e -> {

            Habitaculo seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Sin selección",
                        "Seleccione un habitáculo para modificar."
                );

                return;
            }

            if (txtNombre.getText().isBlank()
                    || txtSector.getText().isBlank()
                    || txtCapacidad.getText().isBlank()
                    || txtEstado.getText().isBlank()) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Datos incompletos",
                        "Complete todos los campos obligatorios."
                );

                return;
            }

            int capacidad;

            try {

                capacidad =
                        Integer.parseInt(
                                txtCapacidad.getText().trim()
                        );

            } catch (NumberFormatException ex) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Capacidad inválida",
                        "La capacidad debe ser un número entero."
                );

                return;
            }

            seleccionado.setNombre(
                    txtNombre.getText().trim()
            );

            seleccionado.setSector(
                    txtSector.getText().trim()
            );

            seleccionado.setCapacidad(
                    capacidad
            );

            seleccionado.setEstado(
                    txtEstado.getText().trim()
            );

            seleccionado.setObservaciones(
                    txtObservaciones.getText().trim()
            );

            habitaculoDAO.modificar(
                    seleccionado
            );

            limpiarCampos();
            cargarHabitaculos();
        });

        // =========================================
        // ELIMINAR
        // =========================================

        btnEliminar.setOnAction(e -> {

            Habitaculo seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Sin selección",
                        "Seleccione un habitáculo para eliminar."
                );

                return;
            }

            habitaculoDAO.eliminar(
                    seleccionado.getIdHabitaculo()
            );

            limpiarCampos();
            cargarHabitaculos();
        });

        // =========================================
        // LIMPIAR
        // =========================================

        btnLimpiar.setOnAction(e ->
                limpiarCampos()
        );

        // =========================================
        // CONTENIDO
        // =========================================

        VBox contenido =
                new VBox(
                        25,
                        encabezado,
                        tarjetaDatos,
                        tarjetaTabla
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

        return scroll;
    }

    // =========================================
    // ETIQUETA
    // =========================================

    private Label crearEtiqueta(
            String texto) {

        Label etiqueta =
                new Label(texto);

        etiqueta.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        return etiqueta;
    }

    // =========================================
    // CAMPO
    // =========================================

    private VBox crearCampo(
            Label etiqueta,
            javafx.scene.control.Control control) {

        if (control instanceof TextField campo) {

            campo.setPrefHeight(38);

            campo.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-color: #D1D8D2;" +
                    "-fx-padding: 8;"
            );

        } else if (control instanceof TextArea area) {

            area.setWrapText(true);

            area.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-color: #D1D8D2;" +
                    "-fx-padding: 8;"
            );
        }

        VBox campo =
                new VBox(
                        8,
                        etiqueta,
                        control
                );

        return campo;
    }

    // =========================================
    // CARGAR
    // =========================================

    private void cargarHabitaculos() {

        ObservableList<Habitaculo> lista =
                FXCollections.observableArrayList(
                        habitaculoDAO.listar()
                );

        tabla.setItems(lista);
    }

    // =========================================
    // LIMPIAR
    // =========================================

    private void limpiarCampos() {

        txtNombre.clear();

        txtSector.clear();

        txtCapacidad.clear();

        txtEstado.clear();

        txtObservaciones.clear();

        tabla.getSelectionModel()
                .clearSelection();
    }

    // =========================================
    // ALERTA
    // =========================================

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta =
                new Alert(tipo);

        alerta.setTitle(
                "Tatú Carreta"
        );

        alerta.setHeaderText(
                titulo
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    // =========================================
    // MOSTRAR
    // =========================================

    public void mostrar() {

        Stage escenario =
                new Stage();

        Scene escena =
                new Scene(
                        crearContenido(),
                        900,
                        700
                );

        escenario.setTitle(
                "Tatú Carreta - Habitáculos"
        );

        escenario.setScene(escena);

        escenario.show();
    }
}