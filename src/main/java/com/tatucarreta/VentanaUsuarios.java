package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaUsuarios {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final TableView<Usuario> tabla = new TableView<>();

    private final TextField txtNombre = new TextField();
    private final TextField txtUsuario = new TextField();
    private final PasswordField txtPassword = new PasswordField();

    private final ComboBox<String> comboRol = new ComboBox<>();
    private final ComboBox<String> comboEstado = new ComboBox<>();

    public void mostrar() {

        Stage ventana = new Stage();

        Label titulo = new Label("ADMINISTRACIÓN DE USUARIOS");

        titulo.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        Label lblNombre = new Label("Nombre:");
        Label lblUsuario = new Label("Usuario:");
        Label lblPassword = new Label("Contraseña:");
        Label lblRol = new Label("Rol:");
        Label lblEstado = new Label("Estado:");

        txtNombre.setPromptText("Ej: Juan Pérez");
        txtUsuario.setPromptText("Ej: jperez");
        txtPassword.setPromptText("Contraseña");

        comboRol.getItems().addAll(
                "Administrador",
                "Encargado",
                "Cuidador",
                "Veterinario"
        );

        comboEstado.getItems().addAll(
                "Activo",
                "Inactivo"
        );

        comboEstado.setValue("Activo");

        HBox filaNombre = new HBox(
                10,
                lblNombre,
                txtNombre
        );

        HBox filaUsuario = new HBox(
                10,
                lblUsuario,
                txtUsuario
        );

        HBox filaPassword = new HBox(
                10,
                lblPassword,
                txtPassword
        );

        HBox filaRol = new HBox(
                10,
                lblRol,
                comboRol
        );

        HBox filaEstado = new HBox(
                10,
                lblEstado,
                comboEstado
        );

        Button btnAgregar = new Button("AGREGAR");
        Button btnModificar = new Button("MODIFICAR");
        Button btnEliminar = new Button("ELIMINAR");
        Button btnVolver = new Button("VOLVER");

        HBox botones = new HBox(
                10,
                btnAgregar,
                btnModificar,
                btnEliminar
        );

        botones.setAlignment(Pos.CENTER);

        // COLUMNAS DE LA TABLA

        TableColumn<Usuario, Integer> columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(
                new PropertyValueFactory<>("idUsuario")
        );

        TableColumn<Usuario, String> columnaNombre =
                new TableColumn<>("Nombre");

        columnaNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombreUsuario")
        );

        TableColumn<Usuario, String> columnaUsuario =
                new TableColumn<>("Usuario");

        columnaUsuario.setCellValueFactory(
                new PropertyValueFactory<>("usuario")
        );

        TableColumn<Usuario, String> columnaRol =
                new TableColumn<>("Rol");

        columnaRol.setCellValueFactory(
                new PropertyValueFactory<>("rol")
        );

        TableColumn<Usuario, String> columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        tabla.getColumns().addAll(
                columnaId,
                columnaNombre,
                columnaUsuario,
                columnaRol,
                columnaEstado
        );

        tabla.setPrefHeight(300);

        cargarUsuarios();

        // SELECCIONAR USUARIO

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {

                    if (seleccionado != null) {

                        txtNombre.setText(
                                seleccionado.getNombreUsuario()
                        );

                        txtUsuario.setText(
                                seleccionado.getUsuario()
                        );

                        txtPassword.setText(
                                seleccionado.getPassword()
                        );

                        comboRol.setValue(
                                seleccionado.getRol()
                        );

                        comboEstado.setValue(
                                seleccionado.getEstado()
                        );
                    }
                });

        // AGREGAR

        btnAgregar.setOnAction(e -> {

            if (txtNombre.getText().isBlank()
                    || txtUsuario.getText().isBlank()
                    || txtPassword.getText().isBlank()
                    || comboRol.getValue() == null
                    || comboEstado.getValue() == null) {

                return;
            }

            Usuario usuario = new Usuario();

            usuario.setNombreUsuario(
                    txtNombre.getText()
            );

            usuario.setUsuario(
                    txtUsuario.getText()
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

            usuarioDAO.agregar(usuario);

            limpiarCampos();
            cargarUsuarios();
        });

        // MODIFICAR

        btnModificar.setOnAction(e -> {

            Usuario seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {
                return;
            }

            seleccionado.setNombreUsuario(
                    txtNombre.getText()
            );

            seleccionado.setUsuario(
                    txtUsuario.getText()
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

            usuarioDAO.modificar(seleccionado);

            limpiarCampos();
            cargarUsuarios();
        });

        // ELIMINAR

        btnEliminar.setOnAction(e -> {

            Usuario seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {
                return;
            }

            usuarioDAO.eliminar(
                    seleccionado.getIdUsuario()
            );

            limpiarCampos();
            cargarUsuarios();
        });

        // VOLVER

        btnVolver.setOnAction(e -> ventana.close());

        VBox contenedor = new VBox(15);

        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(30));

        contenedor.getChildren().addAll(
                titulo,
                filaNombre,
                filaUsuario,
                filaPassword,
                filaRol,
                filaEstado,
                botones,
                tabla,
                btnVolver
        );

        contenedor.setStyle(
                "-fx-background-color: #F2F0E6;"
        );

        Scene escena = new Scene(
                contenedor,
                850,
                700
        );

        ventana.setTitle(
                "Tatú Carreta - Usuarios"
        );

        ventana.setScene(escena);
        ventana.show();
    }

    private void cargarUsuarios() {

        ObservableList<Usuario> lista =
                FXCollections.observableArrayList(
                        usuarioDAO.listar()
                );

        tabla.setItems(lista);
    }

    private void limpiarCampos() {

        txtNombre.clear();
        txtUsuario.clear();
        txtPassword.clear();

        comboRol.setValue(null);
        comboEstado.setValue("Activo");

        tabla.getSelectionModel().clearSelection();
    }
}