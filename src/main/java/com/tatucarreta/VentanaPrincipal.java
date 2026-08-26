package com.tatucarreta;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaPrincipal extends Application {

    private Usuario usuarioActual;

    private VBox menu;

    private Button botonSeleccionado;

    private BorderPane raiz;

    private Stage escenario;

    private final String COLOR_FONDO =
            "#F4F2EA";

    private final String COLOR_MENU =
            "#173D31";

    private final String COLOR_MENU_SECUNDARIO =
            "#1D493A";

    private final String COLOR_SELECCION =
            "#62B397";

    private final String COLOR_TEXTO =
            "#2F3E3A";


    public VentanaPrincipal() {
    }


    public VentanaPrincipal(
            Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;
    }


    @Override
    public void start(Stage escenario) {

        this.escenario = escenario;

        raiz = new BorderPane();

        raiz.setStyle(
                "-fx-background-color: "
                        + COLOR_FONDO + ";"
        );


        // =========================
        // MENÚ LATERAL
        // =========================

        VBox barraLateral =
                crearBarraLateral();

        raiz.setLeft(barraLateral);


        // =========================
        // PANEL INICIAL
        // =========================

        mostrarPanelInicio();


        // =========================
        // ESCENA
        // =========================

        Scene escena = new Scene(
                raiz,
                1200,
                750
        );

        escenario.setTitle(
                "Tatú Carreta - Sistema de Gestión"
        );

        escenario.setScene(escena);

        escenario.setMinWidth(1000);
        escenario.setMinHeight(700);

        // Se abre maximizada
        escenario.setMaximized(true);

        escenario.show();
    }


    // =====================================================
    // BARRA LATERAL
    // =====================================================

    private VBox crearBarraLateral() {

        VBox barra =
                new VBox();

        barra.setPrefWidth(260);

        barra.setMinWidth(230);

        barra.setStyle(
                "-fx-background-color: "
                        + COLOR_MENU + ";"
        );


        // =========================
        // LOGO Y NOMBRE
        // =========================

        Label icono =
                new Label("◉");

        icono.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-text-fill: #74C4A7;"
        );


        Label nombre =
                new Label("Tatú Carreta");

        nombre.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );


        Label sistema =
                new Label("SISTEMA DE GESTIÓN");

        sistema.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-text-fill: #A7BEB5;"
        );


        VBox textosLogo =
                new VBox(2);

        textosLogo.getChildren().addAll(
                nombre,
                sistema
        );


        HBox encabezado =
                new HBox(10);

        encabezado.setAlignment(
                Pos.CENTER_LEFT
        );

        encabezado.getChildren().addAll(
                icono,
                textosLogo
        );

        encabezado.setPadding(
                new Insets(25, 20, 25, 20)
        );


        // =========================
        // SEPARADOR
        // =========================

        Region separador =
                new Region();

        separador.setPrefHeight(1);

        separador.setStyle(
                "-fx-background-color: #2A5747;"
        );


        // =========================
        // BOTONES DEL MENÚ
        // =========================

        menu = new VBox(8);

        menu.setPadding(
                new Insets(20, 12, 20, 12)
        );


        Button btnInicio =
                crearBotonMenu(
                        "▦",
                        "Resumen del Sistema"
                );

        btnInicio.setOnAction(e -> {

            seleccionarBoton(btnInicio);

            mostrarPanelInicio();
        });


        Button btnAnimales =
                crearBotonMenu(
                        "🐾",
                        "ABM Animales"
                );

        btnAnimales.setOnAction(e -> {

            seleccionarBoton(btnAnimales);

            VentanaAnimales ventana =
                    new VentanaAnimales();

            ventana.mostrar();
        });


        Button btnIngresos =
                crearBotonMenu(
                        "▣",
                        "Registrar Ingreso"
                );

        btnIngresos.setOnAction(e -> {

            seleccionarBoton(btnIngresos);

            VentanaIngresos ventana =
                    new VentanaIngresos();

            ventana.mostrar();
        });


        Button btnMovimientos =
                crearBotonMenu(
                        "↔",
                        "Historial de Ingresos"
                );

        btnMovimientos.setOnAction(e -> {

            seleccionarBoton(btnMovimientos);

            VentanaMovimientos ventana =
                    new VentanaMovimientos();

            Stage nuevaVentana =
                    new Stage();

            ventana.mostrar(nuevaVentana);
        });


        Button btnHabitaculos =
                crearBotonMenu(
                        "⌂",
                        "Habitáculos"
                );

        btnHabitaculos.setOnAction(e -> {

            seleccionarBoton(btnHabitaculos);

            VentanaHabitaculos ventana =
                    new VentanaHabitaculos();

            ventana.mostrar();
        });


        Button btnPlantel =
                crearBotonMenu(
                        "▤",
                        "Plantel Permanente"
                );

        btnPlantel.setOnAction(e -> {

            seleccionarBoton(btnPlantel);

            VentanaPlantelPermanente ventana =
                    new VentanaPlantelPermanente();

            ventana.mostrar();
        });


        Button btnEspecies =
                crearBotonMenu(
                        "♧",
                        "Especies"
                );

        btnEspecies.setOnAction(e -> {

            seleccionarBoton(btnEspecies);

            VentanaEspecies ventana =
                    new VentanaEspecies();

            ventana.mostrar();
        });


        menu.getChildren().addAll(
                btnInicio,
                btnAnimales,
                btnIngresos,
                btnMovimientos,
                btnHabitaculos,
                btnPlantel,
                btnEspecies
        );


        // =========================
        // USUARIOS
        // SOLO ADMINISTRADOR
        // =========================

        if (usuarioActual != null
                && usuarioActual.getRol()
                .equalsIgnoreCase(
                        "Administrador"
                )) {

            Button btnUsuarios =
                    crearBotonMenu(
                            "⚙",
                            "Configuración Usuarios"
                    );

            btnUsuarios.setOnAction(e -> {

                seleccionarBoton(btnUsuarios);

                VentanaUsuarios ventana =
                        new VentanaUsuarios();

                ventana.mostrar();
            });

            menu.getChildren().add(
                    btnUsuarios
            );
        }


        // =========================
        // ESPACIO FLEXIBLE
        // =========================

        Region espacio =
                new Region();

        VBox.setVgrow(
                espacio,
                Priority.ALWAYS
        );


        // =========================
        // INFORMACIÓN USUARIO
        // =========================

        Region separadorInferior =
                new Region();

        separadorInferior.setPrefHeight(1);

        separadorInferior.setStyle(
                "-fx-background-color: #2A5747;"
        );


        String nombreUsuario =
                "Usuario";

        String rolUsuario =
                "";

        if (usuarioActual != null) {

            nombreUsuario =
                    usuarioActual
                            .getNombreUsuario();

            rolUsuario =
                    usuarioActual
                            .getRol();
        }


        Label nombreUsuarioLabel =
                new Label(nombreUsuario);

        nombreUsuarioLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );


        Label rolUsuarioLabel =
                new Label(rolUsuario);

        rolUsuarioLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #A7BEB5;"
        );


        VBox datosUsuario =
                new VBox(3);

        datosUsuario.getChildren().addAll(
                nombreUsuarioLabel,
                rolUsuarioLabel
        );


        Label avatar =
                new Label("●");

        avatar.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-text-fill: #74C4A7;"
        );


        HBox usuario =
                new HBox(10);

        usuario.setAlignment(
                Pos.CENTER_LEFT
        );

        usuario.setPadding(
                new Insets(15, 20, 10, 20)
        );

        usuario.getChildren().addAll(
                avatar,
                datosUsuario
        );


        // =========================
        // CERRAR SESIÓN
        // =========================

        Button btnCerrarSesion =
                new Button(
                        "↪   Cerrar Sesión"
                );

        btnCerrarSesion.setMaxWidth(
                Double.MAX_VALUE
        );

        btnCerrarSesion.setStyle(
                "-fx-background-color: "
                        + COLOR_MENU_SECUNDARIO
                        + ";" +
                "-fx-text-fill: #D9E5DF;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 12px;" +
                "-fx-alignment: CENTER_LEFT;"
        );

        btnCerrarSesion.setOnAction(e -> {

            escenario.close();

            VentanaLogin login =
                    new VentanaLogin();

            Stage ventanaLogin =
                    new Stage();

            login.mostrar(
                    ventanaLogin
            );
        });


        VBox pie =
                new VBox(8);

        pie.setPadding(
                new Insets(12, 12, 20, 12)
        );

        pie.getChildren().addAll(
                separadorInferior,
                usuario,
                btnCerrarSesion
        );


        // =========================
        // ARMAR BARRA
        // =========================

        barra.getChildren().addAll(
                encabezado,
                separador,
                menu,
                espacio,
                pie
        );


        // Panel inicial seleccionado
        seleccionarBoton(btnInicio);

        return barra;
    }


    // =====================================================
    // CREAR BOTÓN DEL MENÚ
    // =====================================================

    private Button crearBotonMenu(
            String icono,
            String texto) {

        Button boton =
                new Button(
                        icono
                                + "    "
                                + texto
                );

        boton.setMaxWidth(
                Double.MAX_VALUE
        );

        boton.setPrefHeight(42);

        boton.setAlignment(
                Pos.CENTER_LEFT
        );

        boton.setStyle(
                estiloBotonNormal()
        );

        boton.setOnMouseEntered(e -> {

            if (boton != botonSeleccionado) {

                boton.setStyle(
                        estiloBotonHover()
                );
            }
        });


        boton.setOnMouseExited(e -> {

            if (boton != botonSeleccionado) {

                boton.setStyle(
                        estiloBotonNormal()
                );
            }
        });

        return boton;
    }


    // =====================================================
    // SELECCIONAR BOTÓN
    // =====================================================

    private void seleccionarBoton(
            Button boton) {

        if (botonSeleccionado != null) {

            botonSeleccionado.setStyle(
                    estiloBotonNormal()
            );
        }

        botonSeleccionado = boton;

        botonSeleccionado.setStyle(
                "-fx-background-color: "
                        + COLOR_SELECCION
                        + ";" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-font-weight: bold;"
        );
    }


    private String estiloBotonNormal() {

        return
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #D7E4DE;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;" +
                "-fx-alignment: CENTER_LEFT;";
    }


    private String estiloBotonHover() {

        return
                "-fx-background-color: #285745;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;" +
                "-fx-alignment: CENTER_LEFT;";
    }


    // =====================================================
    // PANEL PRINCIPAL / DASHBOARD
    // =====================================================

    private void mostrarPanelInicio() {

        VBox contenido =
                new VBox(25);

        contenido.setPadding(
                new Insets(35, 40, 35, 40)
        );

        contenido.setStyle(
                "-fx-background-color: "
                        + COLOR_FONDO + ";"
        );


        // =========================
        // TÍTULO
        // =========================

        Label ruta =
                new Label(
                        "Inicio / Resumen del Sistema"
                );

        ruta.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #8A918E;"
        );


        String nombre =
                "Administrador";

        if (usuarioActual != null) {

            nombre =
                    usuarioActual
                            .getNombreUsuario();
        }


        Label titulo =
                new Label(
                        "Hola, " + nombre
                );

        titulo.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: "
                        + COLOR_TEXTO + ";"
        );


        Label descripcion =
                new Label(
                        "Bienvenido al panel de control de la Reserva Natural Tatú Carreta."
                );

        descripcion.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #727A76;"
        );


        VBox encabezado =
                new VBox(7);

        encabezado.getChildren().addAll(
                ruta,
                titulo,
                descripcion
        );


        // =========================
        // SEPARADOR
        // =========================

        Region linea =
                new Region();

        linea.setPrefHeight(1);

        linea.setStyle(
                "-fx-background-color: #D8D6CE;"
        );


        // =========================
        // TARJETAS
        // =========================

        HBox tarjetas =
                new HBox(18);

        tarjetas.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox tarjetaAnimales =
                crearTarjeta(
                        "TOTAL ANIMALES ACTIVOS",
                        "—",
                        "Registrados actualmente"
                );


        VBox tarjetaHabitaculos =
                crearTarjeta(
                        "HABITÁCULOS",
                        "—",
                        "Ocupación actual"
                );


        VBox tarjetaIngresos =
                crearTarjeta(
                        "INGRESOS DEL MES",
                        "—",
                        "Ingresos registrados"
                );


        VBox tarjetaEgresos =
                crearTarjeta(
                        "EGRESOS DEL MES",
                        "—",
                        "Liberaciones y traslados"
                );


        HBox.setHgrow(
                tarjetaAnimales,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                tarjetaHabitaculos,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                tarjetaIngresos,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                tarjetaEgresos,
                Priority.ALWAYS
        );


        tarjetas.getChildren().addAll(
                tarjetaAnimales,
                tarjetaHabitaculos,
                tarjetaIngresos,
                tarjetaEgresos
        );


        // =========================
        // ACTIVIDAD RECIENTE
        // =========================

        Label tituloActividad =
                new Label(
                        "Actividades Recientes"
                );

        tituloActividad.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: "
                        + COLOR_TEXTO + ";"
        );


        Label sinActividad =
                new Label(
                        "Aquí aparecerán los últimos ingresos, movimientos y cambios registrados."
                );

        sinActividad.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #727A76;"
        );


        VBox actividad =
                new VBox(10);

        actividad.setPadding(
                new Insets(25)
        );

        actividad.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #DDDAD1;" +
                "-fx-border-radius: 12;"
        );

        actividad.getChildren().addAll(
                tituloActividad,
                sinActividad
        );


        contenido.getChildren().addAll(
                encabezado,
                linea,
                tarjetas,
                actividad
        );


        raiz.setCenter(contenido);
    }


    // =====================================================
    // CREAR TARJETA
    // =====================================================

    private VBox crearTarjeta(
            String titulo,
            String numero,
            String descripcion) {

        Label lblTitulo =
                new Label(titulo);

        lblTitulo.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #6D7671;" +
                "-fx-font-weight: bold;"
        );


        Label lblNumero =
                new Label(numero);

        lblNumero.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: "
                        + COLOR_TEXTO + ";"
        );


        Label lblDescripcion =
                new Label(descripcion);

        lblDescripcion.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #7D8581;"
        );


        VBox tarjeta =
                new VBox(10);

        tarjeta.setPadding(
                new Insets(20)
        );

        tarjeta.setPrefHeight(130);

        tarjeta.setMaxWidth(
                Double.MAX_VALUE
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #DDDAD1;" +
                "-fx-border-radius: 12;"
        );

        tarjeta.getChildren().addAll(
                lblTitulo,
                lblNumero,
                lblDescripcion
        );

        return tarjeta;
    }
}