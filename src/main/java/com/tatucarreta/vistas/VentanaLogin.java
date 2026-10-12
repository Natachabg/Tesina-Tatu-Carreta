package com.tatucarreta.vistas;

import com.tatucarreta.config.CrearTablas;
import com.tatucarreta.config.ConexionSQLite;
import com.tatucarreta.ui.Estilos;
import com.tatucarreta.ui.OjoPassword;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;

public class VentanaLogin {

    private static final double ANCHO = 560;
    private static final double ALTO = 660;

    private double mouseX;
    private double mouseY;

    public void mostrar(Stage stage) {

        CrearTablas.crearTablas();

        // =====================================================
        // VENTANA COMPLETAMENTE TRANSPARENTE
        // =====================================================

        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setResizable(false);

        stage.setWidth(ANCHO);
        stage.setHeight(ALTO);

        stage.centerOnScreen();

        // =====================================================
        // ROOT TRANSPARENTE
        // =====================================================

        StackPane root = new StackPane();

        root.setStyle("-fx-background-color:transparent;");

        // =====================================================
        // MODAL PRINCIPAL
        // =====================================================

        VBox card = new VBox(13);

        card.setAlignment(Pos.CENTER);

        card.setPadding(
                new Insets(14, 42, 30, 42));

        card.setPrefWidth(500);
        card.setPrefHeight(610);

        card.setMaxWidth(500);
        card.setMaxHeight(610);

        card.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:26;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-border-width:1;"
                        + "-fx-border-radius:26;"
                        + "-fx-effect:"
                        + "dropshadow(gaussian,"
                        + "rgba(35,79,46,0.22),"
                        + "30,0,0,8);");

        // =====================================================
        // BARRA SUPERIOR DEL MODAL
        // =====================================================

        BorderPane barraSuperior =
                new BorderPane();

        barraSuperior.setPrefHeight(32);
        barraSuperior.setMinHeight(32);

        // -----------------------------------------------------
        // NOMBRE
        // -----------------------------------------------------

        Label nombreApp =
                new Label("Tatú Carreta");

        nombreApp.setStyle(
                "-fx-font-size:11px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:#7A8A80;");

        // -----------------------------------------------------
        // BOTONES DE VENTANA
        // -----------------------------------------------------

        HBox controles =
                new HBox(5);

        controles.setAlignment(
                Pos.CENTER_RIGHT);

        Button minimizar =
                new Button("—");

        Button cerrar =
                new Button("×");

        // -----------------------------------------------------
        // MINIMIZAR
        // -----------------------------------------------------

        minimizar.setMinSize(32, 28);
        minimizar.setPrefSize(32, 28);
        minimizar.setMaxSize(32, 28);

        minimizar.setStyle(
                "-fx-background-color:transparent;"
                        + "-fx-background-radius:8;"
                        + "-fx-border-color:transparent;"
                        + "-fx-text-fill:#65736B;"
                        + "-fx-font-size:16px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-padding:0;"
                        + "-fx-cursor:hand;");

        minimizar.setOnMouseEntered(e ->
                minimizar.setStyle(
                        "-fx-background-color:#EEF4EF;"
                                + "-fx-background-radius:8;"
                                + "-fx-border-color:transparent;"
                                + "-fx-text-fill:"
                                + Estilos.VERDE_OSCURO
                                + ";"
                                + "-fx-font-size:16px;"
                                + "-fx-font-weight:bold;"
                                + "-fx-padding:0;"
                                + "-fx-cursor:hand;"));

        minimizar.setOnMouseExited(e ->
                minimizar.setStyle(
                        "-fx-background-color:transparent;"
                                + "-fx-background-radius:8;"
                                + "-fx-border-color:transparent;"
                                + "-fx-text-fill:#65736B;"
                                + "-fx-font-size:16px;"
                                + "-fx-font-weight:bold;"
                                + "-fx-padding:0;"
                                + "-fx-cursor:hand;"));

        // -----------------------------------------------------
        // CERRAR
        // -----------------------------------------------------

        cerrar.setMinSize(32, 28);
        cerrar.setPrefSize(32, 28);
        cerrar.setMaxSize(32, 28);

        cerrar.setStyle(
                "-fx-background-color:transparent;"
                        + "-fx-background-radius:8;"
                        + "-fx-border-color:transparent;"
                        + "-fx-text-fill:#65736B;"
                        + "-fx-font-size:20px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-padding:0;"
                        + "-fx-cursor:hand;");

        cerrar.setOnMouseEntered(e ->
                cerrar.setStyle(
                        "-fx-background-color:#FDECEC;"
                                + "-fx-background-radius:8;"
                                + "-fx-border-color:transparent;"
                                + "-fx-text-fill:#B42318;"
                                + "-fx-font-size:20px;"
                                + "-fx-font-weight:bold;"
                                + "-fx-padding:0;"
                                + "-fx-cursor:hand;"));

        cerrar.setOnMouseExited(e ->
                cerrar.setStyle(
                        "-fx-background-color:transparent;"
                                + "-fx-background-radius:8;"
                                + "-fx-border-color:transparent;"
                                + "-fx-text-fill:#65736B;"
                                + "-fx-font-size:20px;"
                                + "-fx-font-weight:bold;"
                                + "-fx-padding:0;"
                                + "-fx-cursor:hand;"));

        controles.getChildren().addAll(
                minimizar,
                cerrar);

        barraSuperior.setLeft(
                nombreApp);

        barraSuperior.setRight(
                controles);

        // =====================================================
        // ACCIONES DE LOS BOTONES
        // =====================================================

        minimizar.setOnAction(e ->
                stage.setIconified(true));

        cerrar.setOnAction(e ->
                stage.close());

        // =====================================================
        // ARRASTRAR LA VENTANA
        // =====================================================

        barraSuperior.setOnMousePressed(e -> {

            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        barraSuperior.setOnMouseDragged(e -> {

            stage.setX(
                    e.getScreenX() - mouseX);

            stage.setY(
                    e.getScreenY() - mouseY);
        });

        // =====================================================
        // LOGO
        // =====================================================

        ImageView logo =
                new ImageView();

        try {

            Image img =
                    new Image(
                            getClass()
                                    .getResourceAsStream(
                                            "/images/logo.png"));

            logo.setImage(img);

            logo.setFitWidth(205);
            logo.setFitHeight(165);

            logo.setPreserveRatio(true);

        } catch (Exception ignored) {
        }

        // =====================================================
        // TEXTOS
        // =====================================================

        Label sub =
                Estilos.subtitulo(
                        "Gestión de la Reserva Natural");

        Label welcome =
                new Label(
                        "Iniciá sesión para continuar");

        welcome.setStyle(
                "-fx-font-size:16px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.TEXTO
                        + ";");

        // =====================================================
        // USUARIO
        // =====================================================

        TextField user =
                Estilos.campo("Usuario");

        user.setMaxWidth(
                Double.MAX_VALUE);

        // =====================================================
        // CONTRASEÑA
        // =====================================================

        OjoPassword.Campo pass =
                OjoPassword.crear(
                        "Contraseña");

        pass.control().setMaxWidth(
                Double.MAX_VALUE);

        // =====================================================
        // INGRESAR
        // =====================================================

        Button entrar =
                Estilos.boton(
                        "INGRESAR");

        entrar.setMaxWidth(
                Double.MAX_VALUE);

        // =====================================================
        // RECUPERACIÓN
        // =====================================================

        Hyperlink olvidaste =
                new Hyperlink(
                        "¿Olvidaste tu contraseña?");

        olvidaste.setStyle(
                "-fx-text-fill:"
                        + Estilos.VERDE
                        + ";"
                        + "-fx-font-weight:bold;"
                        + "-fx-font-size:12px;"
                        + "-fx-border-color:transparent;"
                        + "-fx-padding:5 0 5 0;"
                        + "-fx-cursor:hand;");

        // =====================================================
        // MENSAJE DE ERROR
        // =====================================================

        Label msg =
                new Label();

        msg.setWrapText(true);

        msg.setStyle(
                "-fx-text-fill:"
                        + Estilos.ROJO
                        + ";"
                        + "-fx-font-size:12px;"
                        + "-fx-font-weight:bold;");

        // =====================================================
        // ARMAR MODAL
        // =====================================================

        card.getChildren().addAll(
                barraSuperior,
                logo,
                sub,
                welcome,
                user,
                pass.control(),
                entrar,
                olvidaste,
                msg);

        root.getChildren().add(
                card);

        StackPane.setAlignment(
                card,
                Pos.CENTER);

        // =====================================================
        // LOGIN
        // =====================================================

        entrar.setOnAction(e -> {

            String rol =
                    validar(
                            user.getText().trim(),
                            pass.getText());

            if (rol != null) {

                Stage ventanaPrincipal =
                        new Stage();

                new VentanaPrincipal()
                        .mostrar(
                                ventanaPrincipal,
                                rol);

                stage.close();

            } else {

                msg.setText(
                        "Usuario o contraseña incorrectos.");

                pass.clear();

                pass.control()
                        .requestFocus();
            }
        });

        // =====================================================
        // OLVIDASTE CONTRASEÑA
        // =====================================================

        olvidaste.setOnAction(
                e -> mostrarAyudaContrasena(root));

        // =====================================================
        // ENTER
        // =====================================================

        user.setOnAction(
                e -> pass.control()
                        .requestFocus());

        // =====================================================
        // ESCENA TRANSPARENTE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        ANCHO,
                        ALTO);

        scene.setFill(
                Color.TRANSPARENT);

        stage.setScene(scene);

        stage.setTitle(
                "Tatú Carreta · Acceso");

        stage.show();

        user.requestFocus();
    }

    // =========================================================
    // VALIDAR
    // =========================================================

    private String validar(
            String u,
            String p) {

        try (
                Connection c =
                        ConexionSQLite.conectar();

                PreparedStatement s =
                        c.prepareStatement(
                                "SELECT rol FROM usuarios "
                                        + "WHERE usuario=? "
                                        + "AND password=? "
                                        + "AND estado='Activo'")
        ) {

            s.setString(1, u);
            s.setString(2, p);

            try (
                    ResultSet r =
                            s.executeQuery()
            ) {

                return r.next()
                        ? r.getString(1)
                        : null;
            }

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // MODAL OLVIDASTE CONTRASEÑA
    // =========================================================

    private void mostrarAyudaContrasena(
            StackPane root) {

        StackPane overlay =
                new StackPane();

        overlay.setPickOnBounds(true);

        overlay.setStyle(
                "-fx-background-color:"
                        + "rgba(20,35,25,0.52);");

        // =====================================================
        // MODAL
        // =====================================================

        VBox modal =
                new VBox(15);

        modal.setAlignment(
                Pos.CENTER);

        modal.setPadding(
                new Insets(28, 32, 25, 32));

        modal.setPrefWidth(400);
        modal.setPrefHeight(280);

        modal.setMaxWidth(400);
        modal.setMaxHeight(280);

        modal.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:20;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-border-radius:20;"
                        + "-fx-effect:"
                        + "dropshadow(gaussian,"
                        + "rgba(0,0,0,0.25),"
                        + "24,0,0,6);");

        // =====================================================
        // ICONO
        // =====================================================

        StackPane icono =
                new StackPane();

        icono.setPrefSize(
                48,
                48);

        icono.setMinSize(
                48,
                48);

        icono.setMaxSize(
                48,
                48);

        icono.setStyle(
                "-fx-background-color:"
                        + Estilos.VERDE_SUAVE
                        + ";"
                        + "-fx-background-radius:24;");

        Label signo =
                new Label("?");

        signo.setStyle(
                "-fx-font-size:23px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";");

        icono.getChildren()
                .add(signo);

        // =====================================================
        // TÍTULO
        // =====================================================

        Label titulo =
                new Label(
                        "¿Olvidaste tu contraseña?");

        titulo.setStyle(
                "-fx-font-size:18px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.TEXTO
                        + ";");

        // =====================================================
        // MENSAJE
        // =====================================================

        Label mensaje =
                new Label(
                        "Por seguridad, la contraseña no "
                                + "se puede recuperar desde esta pantalla.\n\n"
                                + "Comunicate con el administrador "
                                + "de la Reserva Natural Tatú Carreta "
                                + "para solicitar el cambio.");

        mensaje.setWrapText(true);

        mensaje.setMaxWidth(330);

        mensaje.setAlignment(
                Pos.CENTER);

        mensaje.setStyle(
                "-fx-font-size:12px;"
                        + "-fx-text-fill:#666666;"
                        + "-fx-line-spacing:2px;");

        // =====================================================
        // BOTÓN
        // =====================================================

        Button entendido =
                Estilos.botonSec(
                        "ENTENDIDO");

        entendido.setPrefWidth(
                125);

        entendido.setOnAction(
                e -> root.getChildren()
                        .remove(overlay));

        // =====================================================
        // ARMAR
        // =====================================================

        modal.getChildren().addAll(
                icono,
                titulo,
                mensaje,
                entendido);

        overlay.getChildren()
                .add(modal);

        StackPane.setAlignment(
                modal,
                Pos.CENTER);

        root.getChildren()
                .add(overlay);

        overlay.toFront();
    }
}