package com.tatucarreta.vistas;

import com.tatucarreta.config.CrearTablas;
import com.tatucarreta.ui.Estilos;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Supplier;

public class VentanaPrincipal extends Application {

    private BorderPane root;
    private VBox menu;
    private Button seleccionado;
    private String rolActual = "Administrador";

    @Override
    public void start(Stage stage) {
        CrearTablas.crearTablas();
        mostrar(stage, "Administrador");
    }

    public void mostrar(Stage stage) {
        mostrar(stage, "Administrador");
    }

    public void mostrar(Stage stage, String rol) {

        rolActual = rol == null ? "Usuario" : rol;

        // =========================================================
        // CONTENEDOR PRINCIPAL
        // =========================================================

        root = new BorderPane();
        Estilos.aplicarVentana(root);

        // =========================================================
        // MENÚ LATERAL
        // =========================================================

        menu = new VBox(6);
        menu.setPadding(new Insets(14, 12, 12, 12));
        menu.setPrefWidth(235);
        menu.setStyle(
                "-fx-background-color:" + Estilos.VERDE_OSCURO + ";"
        );

        ImageView logo = new ImageView();

        try {
            logo.setImage(
                    new Image(
                            getClass().getResourceAsStream("/images/logo.png")
                    )
            );

            logo.setFitWidth(155);
            logo.setFitHeight(125);
            logo.setPreserveRatio(true);

        } catch (Exception ignored) {
        }

        Label marca = new Label("Gestión de Reserva Natural");

        marca.setStyle(
                "-fx-text-fill:#DCEBDD;" +
                "-fx-font-size:11px;" +
                "-fx-font-weight:bold;"
        );

        VBox marcaBox = new VBox(3, logo, marca);
        marcaBox.setAlignment(Pos.CENTER);
        marcaBox.setPadding(new Insets(0, 0, 10, 0));

        Label estado = new Label(
                "●  Sesión activa · " + rolActual
        );

        estado.setStyle(
                "-fx-text-fill:#DDE9DF;" +
                "-fx-font-size:11px;" +
                "-fx-font-weight:bold;"
        );

        menu.getChildren().addAll(
                marcaBox,
                estado,
                new Separator()
        );

        add("⌂  Panel general", VistaDashboard::new);
        add("🐾  Animales Totales", VistaAnimales::new);
        add("📋  Ingresos / Actas", VistaIngresos::new);
        add("↔  Movimientos", VistaMovimientos::new);
        add("🌿  Plantel Permanente", VistaPlantel::new);
        add("🏠  Habitáculos", VistaHabitaculos::new);
        add("🧬  Especies", VistaEspecies::new);

        if (esAdministrador()) {
            add("👤  Usuarios", VistaUsuarios::new);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        menu.getChildren().add(spacer);

        // =========================================================
        // CERRAR SESIÓN
        // =========================================================

        Button salir = menuBtn("⇦  Cerrar sesión");

        salir.setOnAction(e -> {

            stage.close();

            Stage loginStage = new Stage();

            new VentanaLogin().mostrar(loginStage);
        });

        menu.getChildren().add(salir);

        // =========================================================
        // BARRA SUPERIOR DE LA APLICACIÓN
        // =========================================================

        HBox barraSuperior = new HBox();

        barraSuperior.setAlignment(Pos.CENTER_RIGHT);

        barraSuperior.setPadding(
                new Insets(6, 8, 6, 14)
        );

        barraSuperior.setStyle(
                "-fx-background-color:" +
                Estilos.VERDE_OSCURO +
                ";"
        );

        // Nombre de la aplicación

        Label titulo = new Label(
                "🐾  Tatú Carreta"
        );

        titulo.setStyle(
                "-fx-text-fill:#E8F2EA;" +
                "-fx-font-size:13px;" +
                "-fx-font-weight:bold;"
        );

        // Separador flexible

        Region espacioSuperior = new Region();

        HBox.setHgrow(
                espacioSuperior,
                Priority.ALWAYS
        );

        // Información de sesión

        Label sesion = new Label(
                "Sesión · " + rolActual
        );

        sesion.setStyle(
                "-fx-text-fill:#C9DCCF;" +
                "-fx-font-size:11px;" +
                "-fx-font-weight:bold;"
        );

        // Botón cerrar

        Button cerrar = new Button("✕");

        cerrar.setPrefWidth(38);
        cerrar.setPrefHeight(30);

        cerrar.setFocusTraversable(false);

        cerrar.setStyle(estiloCerrarNormal());

        cerrar.setOnMouseEntered(e ->
                cerrar.setStyle(estiloCerrarHover())
        );

        cerrar.setOnMouseExited(e ->
                cerrar.setStyle(estiloCerrarNormal())
        );

        cerrar.setOnAction(e -> {
    javafx.application.Platform.exit();
});

        barraSuperior.getChildren().addAll(
                titulo,
                espacioSuperior,
                sesion,
                cerrar
        );

        // =========================================================
        // CONTENIDO
        // =========================================================

        root.setTop(barraSuperior);
        root.setLeft(menu);
        root.setCenter(new VistaDashboard());

        // =========================================================
        // ESCENA
        // =========================================================

        Scene scene = new Scene(root);

        // La ventana no tiene decoración de Windows.
        // Esto elimina bordes redondeados y barra de título.

        stage.initStyle(StageStyle.UNDECORATED);

        stage.setScene(scene);

        stage.setTitle(
                "Tatú Carreta · Gestión de Reserva Natural"
        );

        // =========================================================
        // TAMAÑO DEL ESCRITORIO
        // =========================================================

        Screen screen = Screen.getPrimary();

        Rectangle2D bounds =
                screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());

        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // No permitir redimensionar.

        stage.setResizable(false);

        // Mostrar.

        stage.show();
    }

    // =============================================================
    // PERMISOS
    // =============================================================

    private boolean esAdministrador() {

        return "administrador".equalsIgnoreCase(rolActual)
                || "admin".equalsIgnoreCase(rolActual);
    }

    // =============================================================
    // MENÚ
    // =============================================================

    private void add(
            String texto,
            Supplier<javafx.scene.Node> view
    ) {

        Button b = menuBtn(texto);

        b.setOnAction(e -> {

            if (seleccionado != null) {
                seleccionado.setStyle(
                        menuNormal()
                );
            }

            seleccionado = b;

            b.setStyle(
                    menuActivo()
            );

            root.setCenter(
                    view.get()
            );
        });

        menu.getChildren().add(b);
    }

    private Button menuBtn(String texto) {

        Button b = new Button(texto);

        b.setMaxWidth(
                Double.MAX_VALUE
        );

        b.setAlignment(
                Pos.CENTER_LEFT
        );

        b.setPrefHeight(43);
        b.setMinHeight(43);

        b.setStyle(
                menuNormal()
        );

        b.setOnMouseEntered(e -> {

            if (b != seleccionado) {
                b.setStyle(
                        menuHover()
                );
            }
        });

        b.setOnMouseExited(e -> {

            if (b != seleccionado) {
                b.setStyle(
                        menuNormal()
                );
            }
        });

        return b;
    }

    // =============================================================
    // ESTILOS DEL MENÚ
    // =============================================================

    private String menuNormal() {

        return
                "-fx-background-color:transparent;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:9;" +
                "-fx-padding:9 12;" +
                "-fx-cursor:hand;";
    }

    private String menuHover() {

        return
                "-fx-background-color:#315E3A;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:9;" +
                "-fx-padding:9 12;" +
                "-fx-cursor:hand;";
    }

    private String menuActivo() {

        return
                "-fx-background-color:#4D9660;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:9;" +
                "-fx-padding:9 12;" +
                "-fx-cursor:hand;";
    }

    // =============================================================
    // ESTILOS DEL BOTÓN CERRAR
    // =============================================================

    private String estiloCerrarNormal() {

        return
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#E8F2EA;" +
                "-fx-font-size:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:7;" +
                "-fx-cursor:hand;";
    }

    private String estiloCerrarHover() {

        return
                "-fx-background-color:#B83A3A;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:7;" +
                "-fx-cursor:hand;";
    }
}