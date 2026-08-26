package com.tatucarreta;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaLogin {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void mostrar(Stage escenario) {

        // =========================
        // LOGO
        // =========================

        Image logo = new Image(
                getClass().getResourceAsStream(
                        "/images/logo.png"
                )
        );

        ImageView imagenLogo = new ImageView(logo);

        imagenLogo.setFitWidth(150);
        imagenLogo.setPreserveRatio(true);


        // =========================
        // NOMBRE DEL SISTEMA
        // =========================

        Label tituloSistema =
                new Label("TATÚ CARRETA");

        tituloSistema.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        Label subtituloSistema =
                new Label("Gestión de Fauna Nativa");

        subtituloSistema.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #68746B;"
        );


        // =========================
        // TÍTULO LOGIN
        // =========================

        Label tituloLogin =
                new Label("Iniciar sesión");

        tituloLogin.setStyle(
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        Label descripcion =
                new Label(
                        "Ingrese sus datos para acceder al sistema"
                );

        descripcion.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #6B7280;"
        );


        // =========================
        // USUARIO
        // =========================

        Label lblUsuario =
                new Label("Usuario");

        lblUsuario.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #34463A;"
        );

        TextField txtUsuario =
                new TextField();

        txtUsuario.setPromptText(
                "Ingrese su usuario"
        );

        txtUsuario.setPrefHeight(38);

        txtUsuario.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-font-size: 13px;"
        );


        // =========================
        // CONTRASEÑA
        // =========================

        Label lblPassword =
                new Label("Contraseña");

        lblPassword.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #34463A;"
        );

        PasswordField txtPassword =
                new PasswordField();

        txtPassword.setPromptText(
                "Ingrese su contraseña"
        );

        txtPassword.setPrefHeight(38);

        txtPassword.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-font-size: 13px;"
        );


        // =========================
        // BOTÓN
        // =========================

        Button btnIngresar =
                new Button("INGRESAR");

        btnIngresar.setMaxWidth(
                Double.MAX_VALUE
        );

        btnIngresar.setPrefHeight(42);

        btnIngresar.setStyle(
                "-fx-background-color: #23452C;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );


        // =========================
        // ACCIÓN LOGIN
        // =========================

        btnIngresar.setOnAction(e -> {

            String nombreUsuario =
                    txtUsuario.getText().trim();

            String password =
                    txtPassword.getText();

            if (nombreUsuario.isBlank()
                    || password.isBlank()) {

                mostrarMensaje(
                        "Complete usuario y contraseña."
                );

                return;
            }

            Usuario usuario =
                    usuarioDAO.iniciarSesion(
                            nombreUsuario,
                            password
                    );

            if (usuario != null) {

                VentanaPrincipal ventanaPrincipal =
                        new VentanaPrincipal(usuario);

                Stage nuevaVentana =
                        new Stage();

                ventanaPrincipal.start(
                        nuevaVentana
                );

                escenario.close();

            } else {

                mostrarMensaje(
                        "Usuario o contraseña incorrectos, o usuario inactivo."
                );
            }
        });


        txtPassword.setOnAction(e ->
                btnIngresar.fire()
        );


        // =========================
        // TARJETA LOGIN
        // =========================

        VBox tarjeta = new VBox(9);

        tarjeta.setAlignment(
                Pos.CENTER_LEFT
        );

        tarjeta.setPadding(
                new Insets(25)
        );

        tarjeta.setPrefWidth(390);

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #D2D8D1;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        tarjeta.getChildren().addAll(
                tituloLogin,
                descripcion,
                lblUsuario,
                txtUsuario,
                lblPassword,
                txtPassword,
                btnIngresar
        );


        // =========================
        // PIE
        // =========================

        Label pie =
                new Label(
                        "Reserva Natural Tatú Carreta"
                );

        pie.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A857C;"
        );


        // =========================
        // CONTENEDOR
        // =========================

        VBox contenedor =
                new VBox(10);

        // IMPORTANTE:
        // TODO EL CONTENIDO CENTRADO
        contenedor.setAlignment(
                Pos.CENTER
        );

        contenedor.setPadding(
                new Insets(15)
        );

        contenedor.getChildren().addAll(
                imagenLogo,
                tituloSistema,
                subtituloSistema,
                tarjeta,
                pie
        );

        contenedor.setStyle(
                "-fx-background-color: #F2F0E6;"
        );


        // =========================
        // VENTANA
        // =========================

        Scene escena =
                new Scene(
                        contenedor,
                        600,
                        700
                );

        escenario.setTitle(
                "Tatú Carreta - Inicio de Sesión"
        );

        escenario.setScene(
                escena
        );

        escenario.setMinWidth(600);
        escenario.setMinHeight(700);

        escenario.show();
    }


    private void mostrarMensaje(
            String mensaje
    ) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
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
}