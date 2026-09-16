package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaEspecies {

    private final EspecieDAO especieDAO =
            new EspecieDAO();

    private final TableView<Especie> tabla =
            new TableView<>();

    private final TextField txtNombre =
            new TextField();

    public ScrollPane crearContenido() {

        // =========================================
        // ENCABEZADO
        // =========================================

        Label breadcrumb =
                new Label("Inicio / Gestión de Especies");

        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A8580;"
        );

        Label titulo =
                new Label("Gestión de Especies");

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        Label subtitulo =
                new Label(
                        "Administre las especies registradas en la reserva"
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
        // DATOS DE LA ESPECIE
        // =========================================

        Label tituloDatos =
                new Label("Datos de la Especie");

        tituloDatos.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        Label lblNombre =
                new Label("Nombre de la especie *");

        lblNombre.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #A33A3A;"
        );

        txtNombre.setPromptText(
                "Ej: Mamíferos, Aves, Reptiles"
        );

        txtNombre.setPrefHeight(38);

        txtNombre.setMaxWidth(
                Double.MAX_VALUE
        );

        txtNombre.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #D1D8D2;" +
                "-fx-padding: 8;"
        );

        VBox campoNombre =
                new VBox(
                        8,
                        lblNombre,
                        txtNombre
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
                        20,
                        tituloDatos,
                        campoNombre,
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

        TableColumn<Especie, String> columnaNombre =
                new TableColumn<>(
                        "Nombre de la especie"
                );

        columnaNombre.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombre"
                )
        );

        tabla.getColumns().clear();

        // IMPORTANTE:
        // No mostramos el ID.
        // El ID sigue existiendo internamente
        // para modificar y eliminar registros.

        tabla.getColumns().add(
                columnaNombre
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(260);

        tabla.setMinHeight(260);

        tabla.setMaxHeight(260);


        Label tituloTabla =
                new Label("Especies Registradas");

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

        cargarEspecies();


        // =========================================
        // SELECCIONAR
        // =========================================

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         anterior,
                         seleccionada) -> {

                            if (seleccionada != null) {

                                txtNombre.setText(
                                        seleccionada.getNombre()
                                );
                            }
                        }
                );


        // =========================================
        // AGREGAR
        // =========================================

        btnAgregar.setOnAction(e -> {

            if (txtNombre.getText().isBlank()) {

                mostrarMensaje(
                        "Campo obligatorio",
                        "Debe ingresar el nombre de la especie."
                );

                return;
            }

            Especie especie =
                    new Especie();

            especie.setNombre(
                    txtNombre.getText().trim()
            );

            especieDAO.agregar(especie);

            limpiarCampos();

            cargarEspecies();
        });


        // =========================================
        // MODIFICAR
        // =========================================

        btnModificar.setOnAction(e -> {

            Especie seleccionada =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionada == null) {

                mostrarMensaje(
                        "Modificar especie",
                        "Debe seleccionar una especie."
                );

                return;
            }

            if (txtNombre.getText().isBlank()) {

                mostrarMensaje(
                        "Campo obligatorio",
                        "Debe ingresar el nombre de la especie."
                );

                return;
            }

            seleccionada.setNombre(
                    txtNombre.getText().trim()
            );

            especieDAO.modificar(
                    seleccionada
            );

            limpiarCampos();

            cargarEspecies();
        });


        // =========================================
        // ELIMINAR
        // =========================================

        btnEliminar.setOnAction(e -> {

            Especie seleccionada =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionada == null) {

                mostrarMensaje(
                        "Eliminar especie",
                        "Debe seleccionar una especie."
                );

                return;
            }

            boolean confirmar =
                    mostrarConfirmacion(
                            "Eliminar especie",
                            "¿Está seguro de eliminar la especie \""
                                    + seleccionada.getNombre()
                                    + "\"?"
                    );

            if (!confirmar) {
                return;
            }

            especieDAO.eliminar(
                    seleccionada.getIdEspecie()
            );

            limpiarCampos();

            cargarEspecies();
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
                new Insets(
                        25,
                        35,
                        35,
                        35
                )
        );

        contenido.setAlignment(
                Pos.TOP_CENTER
        );

        contenido.setMaxWidth(1200);

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
    // CARGAR ESPECIES
    // =========================================

    private void cargarEspecies() {

        ObservableList<Especie> lista =
                FXCollections.observableArrayList(
                        especieDAO.listar()
                );

        tabla.setItems(lista);
    }


    // =========================================
    // LIMPIAR
    // =========================================

    private void limpiarCampos() {

        txtNombre.clear();

        tabla.getSelectionModel()
                .clearSelection();
    }


    // =========================================
    // MENSAJE
    // =========================================

    private void mostrarMensaje(
            String titulo,
            String mensaje) {

        javafx.scene.control.Alert alerta =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );

        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }


    // =========================================
    // CONFIRMACIÓN
    // =========================================

    private boolean mostrarConfirmacion(
            String titulo,
            String mensaje) {

        javafx.scene.control.Alert alerta =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION
                );

        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(mensaje);

        return alerta.showAndWait()
                .filter(
                        respuesta ->
                                respuesta ==
                                javafx.scene.control.ButtonType.OK
                )
                .isPresent();
    }


    // =========================================
    // MOSTRAR VENTANA
    // =========================================

    public void mostrar() {

        Stage ventana =
                new Stage();

        Scene escena =
                new Scene(
                        crearContenido(),
                        1100,
                        750
                );

        ventana.setTitle(
                "Tatú Carreta - Gestión de Especies"
        );

        ventana.setScene(escena);

        ventana.show();
    }
}