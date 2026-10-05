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
import javafx.stage.Stage;
import java.sql.*;

public class VentanaLogin {
    private static final double ANCHO = 500;
    private static final double ALTO = 590;

    public void mostrar(Stage stage) {
        CrearTablas.crearTablas();

        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setResizable(false);
        stage.setWidth(ANCHO);
        stage.setHeight(ALTO);
        stage.centerOnScreen();

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:#EEF4EF;");

        VBox card = new VBox(13);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setMaxWidth(390);
        card.setMaxHeight(535);
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;-fx-border-color:" + Estilos.BORDE + ";-fx-border-radius:18;" +
                "-fx-effect:dropshadow(gaussian,rgba(35,79,46,0.16),22,0,0,5);");

        ImageView logo = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logo.setImage(img);
            logo.setFitWidth(120);
            logo.setFitHeight(82);
            logo.setPreserveRatio(true);
        } catch (Exception ignored) {}

        Label sub = Estilos.subtitulo("Gestión de la Reserva Natural");
        Label welcome = new Label("Iniciá sesión para continuar");
        welcome.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + Estilos.TEXTO + ";");

        TextField user = Estilos.campo("Usuario");
        OjoPassword.Campo pass = OjoPassword.crear("Contraseña");
        pass.control().setMaxWidth(Double.MAX_VALUE);

        Button entrar = Estilos.boton("INGRESAR");
        entrar.setMaxWidth(Double.MAX_VALUE);

        Hyperlink olvidaste = new Hyperlink("¿Olvidaste tu contraseña?");
        olvidaste.setStyle("-fx-text-fill:" + Estilos.VERDE + ";-fx-font-weight:bold;-fx-font-size:12px;");

        Label msg = new Label();
        msg.setWrapText(true);
        msg.setStyle("-fx-text-fill:" + Estilos.ROJO + ";-fx-font-size:12px;-fx-font-weight:bold;");

        logo.setFitWidth(190);
        logo.setFitHeight(150);
        card.getChildren().addAll(logo, sub, welcome, user, pass.control(), entrar, olvidaste, msg);
        root.getChildren().add(card);

        entrar.setOnAction(e -> {
            String rol = validar(user.getText().trim(), pass.getText());
            if (rol != null) {
                new VentanaPrincipal().mostrar(stage, rol);
            } else {
                msg.setText("Usuario o contraseña incorrectos.");
                pass.clear();
                pass.control().requestFocus();
            }
        });

        olvidaste.setOnAction(e -> mostrarAyudaContrasena());
        user.setOnAction(e -> pass.control().requestFocus());
        stage.setScene(new Scene(root, ANCHO, ALTO));
        stage.setTitle("Tatú Carreta · Acceso");
        stage.show();
        user.requestFocus();
    }

    private String validar(String u, String p) {
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement s = c.prepareStatement("SELECT rol FROM usuarios WHERE usuario=? AND password=? AND estado='Activo'")) {
            s.setString(1, u);
            s.setString(2, p);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void mostrarAyudaContrasena() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Recuperación de contraseña");
        a.setHeaderText("¿Olvidaste tu contraseña?");
        a.setContentText("Por seguridad, la contraseña no se puede recuperar desde esta pantalla.\n\nComunicate con el administrador de la Reserva Natural Tatú Carreta para solicitar el cambio de contraseña.");
        a.showAndWait();
    }
}
