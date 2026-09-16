package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaUsuarios {

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    private final TableView<Usuario> tabla =
            new TableView<>();

    private final TextField txtNombre =
            new TextField();

    private final TextField txtUsuario =
            new TextField();

    private final PasswordField txtPassword =
            new PasswordField();

    private final ComboBox<String> comboRol =
            new ComboBox<>();

    private final ComboBox<String> comboEstado =
            new ComboBox<>();


    // =========================================================
    // CONTENIDO
    // =========================================================

    public ScrollPane crearContenido() {

        Label breadcrumb =
                new Label(
                        "Inicio / Administración de Usuarios"
                );

        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A8580;"
        );


        Label titulo =
                new Label(
                        "Administración de Usuarios"
                );

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );


        Label subtitulo =
                new Label(
                        "Gestione los usuarios, roles y estados " +
                        "de acceso al sistema."
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


        // =====================================================
        // FORMULARIO
        // =====================================================

        GridPane formulario =
                new GridPane();

        formulario.setHgap(18);
        formulario.setVgap(10);
        formulario.setPadding(
                new Insets(5, 20, 15, 20)
        );


        formulario.add(
                crearEtiquetaObligatoria("Nombre"),
                0,
                0
        );

        formulario.add(
                crearEtiquetaObligatoria("Usuario"),
                2,
                0
        );

        formulario.add(
                crearEtiquetaObligatoria("Contraseña"),
                0,
                2
        );

        formulario.add(
                crearEtiquetaObligatoria("Rol"),
                2,
                2
        );

        formulario.add(
                crearEtiquetaObligatoria("Estado"),
                0,
                4
        );


        txtNombre.setPromptText(
                "Ej: Juan Pérez"
        );

        txtUsuario.setPromptText(
                "Ej: jperez"
        );

        txtPassword.setPromptText(
                "Ingrese una contraseña"
        );


        comboRol.getItems().setAll(
                "Administrador",
                "Encargado",
                "Cuidador",
                "Veterinario"
        );


        comboEstado.getItems().setAll(
                "Activo",
                "Inactivo"
        );

        comboEstado.setValue("Activo");


        formulario.add(
                txtNombre,
                0,
                1
        );

        formulario.add(
                txtUsuario,
                2,
                1
        );

        formulario.add(
                txtPassword,
                0,
                3
        );

        formulario.add(
                comboRol,
                2,
                3
        );

        formulario.add(
                comboEstado,
                0,
                5
        );


        for (int i = 0; i < 4; i++) {

            ColumnConstraints columna =
                    new ColumnConstraints();

            columna.setHgrow(
                    Priority.ALWAYS
            );

            columna.setFillWidth(true);

            formulario
                    .getColumnConstraints()
                    .add(columna);
        }


        txtNombre.setMaxWidth(
                Double.MAX_VALUE
        );

        txtUsuario.setMaxWidth(
                Double.MAX_VALUE
        );

        txtPassword.setMaxWidth(
                Double.MAX_VALUE
        );

        comboRol.setMaxWidth(
                Double.MAX_VALUE
        );

        comboEstado.setMaxWidth(
                Double.MAX_VALUE
        );


        // =====================================================
        // BOTONES
        // =====================================================

        Button btnAgregar =
                crearBotonPrincipal(
                        "AGREGAR"
                );

        Button btnModificar =
                crearBotonPrincipal(
                        "MODIFICAR"
                );

        Button btnEliminar =
                crearBotonEliminar(
                        "ELIMINAR"
                );

        Button btnLimpiar =
                crearBotonSecundario(
                        "LIMPIAR"
                );


        HBox botones =
                new HBox(
                        10,
                        btnAgregar,
                        btnModificar,
                        btnEliminar,
                        btnLimpiar
                );

        botones.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // TARJETA FORMULARIO
        // =====================================================

        Label tituloFormulario =
                new Label(
                        "Datos del usuario"
                );

        tituloFormulario.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );


        VBox tarjetaFormulario =
                new VBox(
                        12,
                        tituloFormulario,
                        formulario,
                        botones
                );

        tarjetaFormulario.setPadding(
                new Insets(20)
        );

        tarjetaFormulario.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D8DED9;" +
                "-fx-border-radius: 16;"
        );


        // =====================================================
        // TABLA
        // =====================================================

        configurarTabla();

        cargarUsuarios();


        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         anterior,
                         seleccionado) -> {

                            if (seleccionado != null) {

                                txtNombre.setText(
                                        seleccionado
                                                .getNombreUsuario()
                                );

                                txtUsuario.setText(
                                        seleccionado
                                                .getUsuario()
                                );

                                txtPassword.setText(
                                        seleccionado
                                                .getPassword()
                                );

                                comboRol.setValue(
                                        seleccionado
                                                .getRol()
                                );

                                comboEstado.setValue(
                                        seleccionado
                                                .getEstado()
                                );
                            }
                        }
                );


        // =====================================================
        // ACCIONES
        // =====================================================

        btnAgregar.setOnAction(
                e -> agregarUsuario()
        );

        btnModificar.setOnAction(
                e -> modificarUsuario()
        );

        btnEliminar.setOnAction(
                e -> eliminarUsuario()
        );

        btnLimpiar.setOnAction(
                e -> limpiarCampos()
        );


        // =====================================================
        // TARJETA TABLA
        // =====================================================

        Label tituloTabla =
                new Label(
                        "Usuarios registrados"
                );

        tituloTabla.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );


        VBox tarjetaTabla =
                new VBox(
                        12,
                        tituloTabla,
                        tabla
                );

        tarjetaTabla.setPadding(
                new Insets(20)
        );

        tarjetaTabla.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D8DED9;" +
                "-fx-border-radius: 16;"
        );


        // =====================================================
        // CONTENEDOR
        // =====================================================

        VBox contenedor =
                new VBox(
                        18,
                        encabezado,
                        tarjetaFormulario,
                        tarjetaTabla
                );

        contenedor.setPadding(
                new Insets(30)
        );

        contenedor.setAlignment(
                Pos.TOP_LEFT
        );

        contenedor.setStyle(
                "-fx-background-color: #F2F0E6;"
        );


        ScrollPane scroll =
                new ScrollPane(
                        contenedor
                );

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setStyle(
                "-fx-background-color: #F2F0E6;" +
                "-fx-background: #F2F0E6;"
        );


        return scroll;
    }


    // =========================================================
    // TABLA
    // =========================================================

    private void configurarTabla() {

        tabla.getColumns().clear();


        TableColumn<Usuario, String>
                columnaNombre =
                new TableColumn<>("Nombre");

        columnaNombre.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombreUsuario"
                )
        );


        TableColumn<Usuario, String>
                columnaUsuario =
                new TableColumn<>("Usuario");

        columnaUsuario.setCellValueFactory(
                new PropertyValueFactory<>(
                        "usuario"
                )
        );


        TableColumn<Usuario, String>
                columnaRol =
                new TableColumn<>("Rol");

        columnaRol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "rol"
                )
        );


        TableColumn<Usuario, String>
                columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>(
                        "estado"
                )
        );


        tabla.getColumns().addAll(
                columnaNombre,
                columnaUsuario,
                columnaRol,
                columnaEstado
        );


        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(300);

        tabla.setMinHeight(260);
    }


    // =========================================================
    // AGREGAR
    // =========================================================

    private void agregarUsuario() {

        if (!validarCampos()) {
            return;
        }


        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Confirmar registro"
        );

        confirmacion.setHeaderText(
                "¿Registrar nuevo usuario?"
        );

        confirmacion.setContentText(
                "Se creará el usuario '" +
                txtUsuario.getText().trim() +
                "'."
        );


        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }


        Usuario usuario =
                new Usuario();

        usuario.setNombreUsuario(
                txtNombre.getText().trim()
        );

        usuario.setUsuario(
                txtUsuario.getText().trim()
        );

        usuario.setPassword(
                txtPassword.getText()
        );

        usuario.setRol(
                comboRol.getValue()
        );

        usuario.setEstado(
                comboEstado.getValue()
        );


        usuarioDAO.agregar(
                usuario
        );


        mostrarInformacion(
                "El usuario se registró correctamente."
        );


        limpiarCampos();

        cargarUsuarios();
    }


    // =========================================================
    // MODIFICAR
    // =========================================================

    private void modificarUsuario() {

        Usuario seleccionado =
                tabla.getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            mostrarAviso(
                    "Modificar usuario",
                    "Seleccioná un usuario de la tabla."
            );

            return;
        }


        if (!validarCampos()) {
            return;
        }


        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Confirmar modificación"
        );

        confirmacion.setHeaderText(
                "¿Modificar usuario?"
        );

        confirmacion.setContentText(
                "Se modificarán los datos del usuario '" +
                seleccionado.getUsuario() +
                "'."
        );


        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }


        seleccionado.setNombreUsuario(
                txtNombre.getText().trim()
        );

        seleccionado.setUsuario(
                txtUsuario.getText().trim()
        );

        seleccionado.setPassword(
                txtPassword.getText()
        );

        seleccionado.setRol(
                comboRol.getValue()
        );

        seleccionado.setEstado(
                comboEstado.getValue()
        );


        usuarioDAO.modificar(
                seleccionado
        );


        mostrarInformacion(
                "El usuario se modificó correctamente."
        );


        limpiarCampos();

        cargarUsuarios();
    }


    // =========================================================
    // ELIMINAR
    // =========================================================

    private void eliminarUsuario() {

        Usuario seleccionado =
                tabla.getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            mostrarAviso(
                    "Eliminar usuario",
                    "Seleccioná un usuario de la tabla."
            );

            return;
        }


        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Confirmar eliminación"
        );

        confirmacion.setHeaderText(
                "¿Eliminar usuario?"
        );

        confirmacion.setContentText(
                "Se eliminará el usuario '" +
                seleccionado.getUsuario() +
                "'. Esta acción no se puede deshacer."
        );


        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }


        usuarioDAO.eliminar(
                seleccionado.getIdUsuario()
        );


        mostrarInformacion(
                "El usuario se eliminó correctamente."
        );


        limpiarCampos();

        cargarUsuarios();
    }


    // =========================================================
    // VALIDACIÓN
    // =========================================================

    private boolean validarCampos() {

        if (txtNombre.getText().isBlank()) {

            mostrarAviso(
                    "Datos incompletos",
                    "Completá el campo obligatorio: Nombre."
            );

            txtNombre.requestFocus();

            return false;
        }


        if (txtUsuario.getText().isBlank()) {

            mostrarAviso(
                    "Datos incompletos",
                    "Completá el campo obligatorio: Usuario."
            );

            txtUsuario.requestFocus();

            return false;
        }


        if (txtPassword.getText().isBlank()) {

            mostrarAviso(
                    "Datos incompletos",
                    "Completá el campo obligatorio: Contraseña."
            );

            txtPassword.requestFocus();

            return false;
        }


        if (comboRol.getValue() == null) {

            mostrarAviso(
                    "Datos incompletos",
                    "Seleccioná el campo obligatorio: Rol."
            );

            comboRol.requestFocus();

            return false;
        }


        if (comboEstado.getValue() == null) {

            mostrarAviso(
                    "Datos incompletos",
                    "Seleccioná el campo obligatorio: Estado."
            );

            comboEstado.requestFocus();

            return false;
        }


        return true;
    }


    // =========================================================
    // CARGAR
    // =========================================================

    private void cargarUsuarios() {

        ObservableList<Usuario> lista =
                FXCollections.observableArrayList(
                        usuarioDAO.listar()
                );

        tabla.setItems(lista);
    }


    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiarCampos() {

        txtNombre.clear();

        txtUsuario.clear();

        txtPassword.clear();

        comboRol.setValue(null);

        comboEstado.setValue(
                "Activo"
        );

        tabla.getSelectionModel()
                .clearSelection();
    }


    // =========================================================
    // ETIQUETA OBLIGATORIA
    // =========================================================

    private HBox crearEtiquetaObligatoria(
            String texto) {

        Label etiqueta =
                new Label(texto);

        etiqueta.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #30463A;"
        );


        Label asterisco =
                new Label(" *");

        asterisco.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #C62828;"
        );


        HBox contenedor =
                new HBox(
                        0,
                        etiqueta,
                        asterisco
                );

        contenedor.setAlignment(
                Pos.CENTER_LEFT
        );


        return contenedor;
    }


    // =========================================================
    // BOTONES
    // =========================================================

    private Button crearBotonPrincipal(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setStyle(
                "-fx-background-color: #23452C;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 18;"
        );

        return boton;
    }


    private Button crearBotonEliminar(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setStyle(
                "-fx-background-color: #8B3A3A;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 18;"
        );

        return boton;
    }


    private Button crearBotonSecundario(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setStyle(
                "-fx-background-color: #E2E7E3;" +
                "-fx-text-fill: #23452C;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 18;"
        );

        return boton;
    }


    // =========================================================
    // MENSAJES
    // =========================================================

    private void mostrarAviso(
            String titulo,
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }


    private void mostrarInformacion(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alerta.setTitle(
                "Tatú Carreta"
        );

        alerta.setHeaderText(null);

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }


    // =========================================================
    // COMPATIBILIDAD
    // =========================================================

    public void mostrar() {

        Stage ventana =
                new Stage();

        ScrollPane contenido =
                crearContenido();

        Scene escena =
                new Scene(
                        contenido,
                        1000,
                        700
                );

        ventana.setTitle(
                "Tatú Carreta - Usuarios"
        );

        ventana.setScene(
                escena
        );

        ventana.show();
    }
}