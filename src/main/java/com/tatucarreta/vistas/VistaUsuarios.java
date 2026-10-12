package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.util.List;

public class VistaUsuarios extends VistaCRUD {

    private final StackPane modalLayer = new StackPane();

    public VistaUsuarios() {

        super(
                "Usuarios",
                "Solo el Administrador gestiona accesos, contraseñas y estado de las cuentas.",
                new String[] { "Nombre", "Usuario", "Rol", "Estado" });

        agregar.setText("＋ AGREGAR USUARIO");

        prepararOverlay();
        prepararDobleClick();

        cargar("");
    }

    // =========================================================
    // OVERLAY
    // =========================================================

    private void prepararOverlay() {

        modalLayer.setManaged(false);
        modalLayer.setVisible(false);
        modalLayer.setPickOnBounds(true);

        modalLayer.setStyle(
                "-fx-background-color: rgba(0,0,0,0.60);");

        getChildren().add(modalLayer);
    }

    @Override
    protected void layoutChildren() {

        super.layoutChildren();

        modalLayer.resizeRelocate(
                0,
                0,
                getWidth(),
                getHeight());

        if (modalLayer.isVisible()) {
            modalLayer.toFront();
        }
    }

    private void mostrarOverlay(
            Region contenido,
            double anchoPreferido,
            double altoPreferido) {

        double anchoDisponible = getWidth();
        double altoDisponible = getHeight();

        if (anchoDisponible <= 0) {
            anchoDisponible = getLayoutBounds().getWidth();
        }

        if (altoDisponible <= 0) {
            altoDisponible = getLayoutBounds().getHeight();
        }

        double ancho = Math.min(
                anchoPreferido,
                Math.max(380, anchoDisponible - 80));

        double alto = Math.min(
                altoPreferido,
                Math.max(300, altoDisponible - 80));

        contenido.setMinSize(0, 0);
        contenido.setPrefSize(ancho, alto);
        contenido.setMaxSize(ancho, alto);

        modalLayer.getChildren().setAll(contenido);

        StackPane.setAlignment(
                contenido,
                Pos.CENTER);

        modalLayer.setManaged(false);
        modalLayer.setVisible(true);

        requestLayout();

        modalLayer.resizeRelocate(
                0,
                0,
                getWidth(),
                getHeight());

        modalLayer.toFront();

        applyCss();
        layout();

        modalLayer.applyCss();
        modalLayer.layout();
    }

    private void cerrarOverlay() {

        modalLayer.getChildren().clear();

        modalLayer.setVisible(false);
        modalLayer.setManaged(false);

        requestLayout();
    }

    // =========================================================
    // DOBLE CLICK
    // =========================================================

    private void prepararDobleClick() {

        tabla.setRowFactory(tv -> {

            TableRow<SistemaDAO.Fila> row =
                    new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !row.isEmpty()) {

                    mostrarInformacion(
                            row.getItem());
                }
            });

            return row;
        });
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Override
    protected List<SistemaDAO.Fila> consultar(String q) {
        return SistemaDAO.usuarios(q);
    }

    @Override
    protected List<SistemaDAO.Fila> consultarInactivos(String q) {
        return SistemaDAO.usuariosInactivos(q);
    }

    @Override
    protected void activarRegistro(int id) {
        SistemaDAO.activar(
                "usuarios",
                "id_usuario",
                id);
    }

    // =========================================================
    // AGREGAR
    // =========================================================

    @Override
    protected void agregarNuevo() {
        form(0, null);
    }

    // =========================================================
    // MODIFICAR
    // =========================================================

    @Override
    protected void editarSeleccionado() {

        var f = tabla.getSelectionModel()
                .getSelectedItem();

        if (f != null) {
            form(f.id(), f);
        }
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================

    @Override
    protected void desactivar(int id) {

        if (!SistemaDAO.puedeDesactivarUsuario(id)) {

            mostrarMensaje(
                    "Cuenta protegida",
                    "La cuenta principal del Administrador "
                            + "no puede desactivarse desde este módulo.");

            return;
        }

        mostrarConfirmacion(
                "Enviar usuario a inactivos",
                "¿Estás segura de que querés quitarle "
                        + "el acceso a este usuario?",
                () -> {

                    SistemaDAO.baja(
                            "usuarios",
                            "id_usuario",
                            id);

                    cargar(
                            buscador.getText());
                });
    }

    // =========================================================
    // INFORMACIÓN
    // =========================================================

    private void mostrarInformacion(
            SistemaDAO.Fila f) {

        VBox contenido =
                new VBox(16);

        contenido.setPadding(
                new Insets(24));

        contenido.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:18;"
                        + "-fx-border-radius:18;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-effect:dropshadow("
                        + "gaussian,rgba(0,0,0,.22),24,0,0,8"
                        + ");");

        // -----------------------------------------------------
        // ENCABEZADO
        // -----------------------------------------------------

        StackPane encabezado =
                new StackPane();

        VBox titulos =
                new VBox(4);

        Label titulo =
                new Label(
                        "Información del usuario");

        titulo.setStyle(
                "-fx-font-size:22px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";");

        Label subtitulo =
                new Label(
                        "Consulta de los datos registrados. "
                                + "Para modificar el usuario utilizá MODIFICAR.");

        subtitulo.setWrapText(true);

        subtitulo.setStyle(
                "-fx-font-size:13px;"
                        + "-fx-text-fill:#666666;");

        titulos.getChildren().addAll(
                titulo,
                subtitulo);

        Label cerrarX =
                new Label("✕");

        cerrarX.setPrefSize(
                34,
                34);

        cerrarX.setAlignment(
                Pos.CENTER);

        cerrarX.setStyle(
                "-fx-text-fill:" + Estilos.GRIS + ";"
                        + "-fx-font-size:16px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-cursor:hand;");

        cerrarX.setOnMouseClicked(
                e -> cerrarOverlay());

        encabezado.getChildren().addAll(
                titulos,
                cerrarX);

        StackPane.setAlignment(
                titulos,
                Pos.CENTER_LEFT);

        StackPane.setAlignment(
                cerrarX,
                Pos.TOP_RIGHT);

        // -----------------------------------------------------
        // DATOS
        // -----------------------------------------------------

        GridPane datos =
                Formulario.grid();

        datos.setHgap(14);
        datos.setVgap(12);
        datos.setPadding(new Insets(6, 0, 6, 0));

        ColumnConstraints columnaIzquierda = new ColumnConstraints();
        columnaIzquierda.setPercentWidth(50);
        columnaIzquierda.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnaDerecha = new ColumnConstraints();
        columnaDerecha.setPercentWidth(50);
        columnaDerecha.setHgrow(Priority.ALWAYS);

        datos.getColumnConstraints().setAll(
                columnaIzquierda,
                columnaDerecha);

        agregarDato(
                datos,
                0,
                "Nombre",
                valor(f, 1));

        agregarDato(
                datos,
                1,
                "Usuario",
                valor(f, 2));

        agregarDato(
                datos,
                2,
                "Rol",
                valor(f, 3));

        agregarDato(
                datos,
                3,
                "Estado",
                valor(f, 4));

        ScrollPane scroll =
                new ScrollPane(datos);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background-color:transparent;"
                        + "-fx-background:transparent;");

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS);

        // -----------------------------------------------------
        // BOTONES
        // -----------------------------------------------------

        Button cerrar =
                Estilos.botonSec(
                        "CERRAR");

        Button modificar =
                Estilos.boton(
                        "✎ MODIFICAR");

        HBox acciones =
                new HBox(
                        10,
                        cerrar,
                        new Region(),
                        modificar);

        HBox.setHgrow(
                acciones.getChildren().get(1),
                Priority.ALWAYS);

        acciones.setAlignment(
                Pos.CENTER_RIGHT);

        modificar.setOnAction(e -> {

            cerrarOverlay();

            form(
                    f.id(),
                    f);
        });

        cerrar.setOnAction(e ->
                cerrarOverlay());

        contenido.getChildren().addAll(
                encabezado,
                scroll,
                acciones);

        mostrarOverlay(
                contenido,
                820,
                430);
    }

    private void agregarDato(
            GridPane grid,
            int fila,
            String nombre,
            String valor) {

        Label etiqueta = new Label(nombre);
        etiqueta.setStyle(
                "-fx-font-size:10px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:" + Estilos.GRIS + ";");

        Label textoValor = new Label(
                valor == null || valor.isBlank()
                        ? "—"
                        : valor);
        textoValor.setMaxWidth(Double.MAX_VALUE);
        textoValor.setWrapText(true);
        textoValor.setStyle(
                "-fx-font-size:15px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:" + Estilos.TEXTO + ";");

        VBox tarjeta = new VBox(7, etiqueta, textoValor);
        tarjeta.setPadding(new Insets(13, 15, 13, 15));
        tarjeta.setFillWidth(true);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setMinHeight(68);
        tarjeta.setStyle(
                "-fx-background-color:#F7F9F7;"
                        + "-fx-background-radius:11;"
                        + "-fx-border-color:" + Estilos.BORDE + ";"
                        + "-fx-border-radius:11;");

        int columna = fila % 2;
        int filaVisual = fila / 2;
        grid.add(tarjeta, columna, filaVisual);
        GridPane.setHgrow(tarjeta, Priority.ALWAYS);
    }

    private String valor(
            SistemaDAO.Fila f,
            int indice) {

        if (f == null
                || f.c() == null
                || f.c().size() <= indice
                || f.c().get(indice) == null) {

            return "";
        }

        return f.c().get(indice);
    }

    // =========================================================
    // FORMULARIO
    // =========================================================

    private void form(
            int id,
            SistemaDAO.Fila f) {

        TextField n =
                Estilos.campo(
                        "Nombre completo");

        TextField u =
                Estilos.campo(
                        "Usuario");

        ComboBox<String> rol =
                Estilos.combo();

        rol.getItems().setAll(
                "Administrador",
                "Operador",
                "Consulta");

        OjoPassword.Campo pass =
                OjoPassword.crear(
                        id == 0
                                ? "Contraseña"
                                : "Nueva contraseña (opcional)");

        if (f != null) {

            n.setText(
                    f.c().get(1));

            u.setText(
                    f.c().get(2));

            rol.setValue(
                    f.c().get(3));
        }

        // -----------------------------------------------------
        // GRID
        // -----------------------------------------------------

        GridPane g =
                Formulario.grid();

        ColumnConstraints columnaEtiquetas =
                new ColumnConstraints();

        columnaEtiquetas.setMinWidth(120);
        columnaEtiquetas.setPrefWidth(135);
        columnaEtiquetas.setMaxWidth(160);

        ColumnConstraints columnaControles =
                new ColumnConstraints();

        columnaControles.setHgrow(
                Priority.ALWAYS);

        g.getColumnConstraints().setAll(
                columnaEtiquetas,
                columnaControles);

        Formulario.add(
                g,
                0,
                "Nombre",
                n,
                true);

        Formulario.add(
                g,
                1,
                "Usuario",
                u,
                true);

        Formulario.add(
                g,
                2,
                "Contraseña",
                pass.control(),
                id == 0);

        Formulario.add(
                g,
                3,
                "Rol",
                rol,
                true);

        Label ayuda =
                new Label(
                        "La cuenta principal admin queda protegida. "
                                + "Los usuarios enviados a inactivos se "
                                + "pueden restaurar desde el botón "
                                + "REGISTROS INACTIVOS.");

        ayuda.setWrapText(true);

        ayuda.setStyle(
                "-fx-background-color:"
                        + Estilos.VERDE_SUAVE
                        + ";"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";"
                        + "-fx-padding:10;"
                        + "-fx-background-radius:8;");

        g.add(
                ayuda,
                1,
                4);

        // -----------------------------------------------------
        // CONTENEDOR
        // -----------------------------------------------------

        VBox contenido =
                new VBox(18);

        contenido.setPadding(
                new Insets(24));

        contenido.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:18;"
                        + "-fx-border-radius:18;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-effect:dropshadow("
                        + "gaussian,rgba(0,0,0,.22),24,0,0,8"
                        + ");");

        // -----------------------------------------------------
        // ENCABEZADO
        // -----------------------------------------------------

        StackPane encabezado =
                new StackPane();

        VBox titulos =
                new VBox(4);

        Label titulo =
                new Label(
                        id == 0
                                ? "Nuevo usuario"
                                : "Modificar usuario");

        titulo.setStyle(
                "-fx-font-size:22px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";");

        Label subtitulo =
                new Label(
                        id == 0
                                ? "Completá los datos para crear "
                                  + "una nueva cuenta."
                                : "Modificá los datos del usuario "
                                  + "seleccionado.");

        subtitulo.setWrapText(true);

        subtitulo.setStyle(
                "-fx-font-size:13px;"
                        + "-fx-text-fill:#666666;");

        titulos.getChildren().addAll(
                titulo,
                subtitulo);

        Label cerrarX =
                new Label("✕");

        cerrarX.setPrefSize(
                34,
                34);

        cerrarX.setAlignment(
                Pos.CENTER);

        cerrarX.setStyle(
                "-fx-text-fill:" + Estilos.GRIS + ";"
                        + "-fx-font-size:16px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-cursor:hand;");

        cerrarX.setOnMouseClicked(
                e -> cerrarOverlay());

        encabezado.getChildren().addAll(
                titulos,
                cerrarX);

        StackPane.setAlignment(
                titulos,
                Pos.CENTER_LEFT);

        StackPane.setAlignment(
                cerrarX,
                Pos.TOP_RIGHT);

        // -----------------------------------------------------
        // SCROLL
        // -----------------------------------------------------

        ScrollPane scroll =
                new ScrollPane(g);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background-color:transparent;"
                        + "-fx-background:transparent;");

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS);

        // -----------------------------------------------------
        // BOTONES
        // -----------------------------------------------------

        HBox acciones =
                new HBox(
                        10,
                        new Region());

        Button cancelar =
                Estilos.botonSec(
                        "CANCELAR");

        Button guardar =
                Estilos.boton(
                        id == 0
                                ? "✓ AGREGAR USUARIO"
                                : "✓ GUARDAR CAMBIOS");

        acciones.getChildren().add(
                0,
                cancelar);

        acciones.getChildren().add(
                guardar);

        HBox.setHgrow(
                acciones.getChildren().get(1),
                Priority.ALWAYS);

        acciones.setAlignment(
                Pos.CENTER_RIGHT);

        cancelar.setOnAction(e ->
                cerrarOverlay());

        // -----------------------------------------------------
        // GUARDAR
        // -----------------------------------------------------

        guardar.setOnAction(e -> {

            try {

                if (!validar(
                        n.getText(),
                        u.getText())
                        || rol.getValue() == null
                        || (id == 0
                                && !validar(
                                        pass.getText()))) {

                    mostrarMensaje(
                            "Datos incompletos",
                            "Completá los campos obligatorios.");

                    return;
                }

                mostrarConfirmacion(
                        id == 0
                                ? "Agregar usuario"
                                : "Guardar cambios",

                        id == 0
                                ? "¿Estás segura de que querés "
                                  + "agregar este usuario?"
                                : "¿Querés guardar los cambios?",

                        () -> {

                            try {

                                SistemaDAO.guardarUsuario(
                                        id,
                                        n.getText().trim(),
                                        u.getText().trim(),
                                        pass.getText(),
                                        rol.getValue());

                                cerrarOverlay();

                                cargar(
                                        buscador.getText());

                            } catch (Exception ex) {

                                mostrarMensaje(
                                        "No se pudo guardar",
                                        ex.getMessage() == null
                                                ? ex.toString()
                                                : ex.getMessage());
                            }
                        });

            } catch (Exception ex) {

                mostrarMensaje(
                        "No se pudo guardar",
                        ex.getMessage() == null
                                ? ex.toString()
                                : ex.getMessage());
            }
        });

        contenido.getChildren().addAll(
                encabezado,
                scroll,
                acciones);

        mostrarOverlay(
                contenido,
                760,
                600);
    }

    // =========================================================
    // MENSAJE
    // =========================================================

    private void mostrarMensaje(
            String tituloTexto,
            String mensajeTexto) {

        VBox card =
                new VBox(16);

        card.setPadding(
                new Insets(24));

        card.setMaxWidth(520);
        card.setMaxHeight(300);

        card.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:18;"
                        + "-fx-border-radius:18;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-effect:dropshadow("
                        + "gaussian,rgba(0,0,0,.22),24,0,0,8"
                        + ");");

        Label titulo =
                new Label(tituloTexto);

        titulo.setStyle(
                "-fx-font-size:19px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";");

        Label mensaje =
                new Label(mensajeTexto);

        mensaje.setWrapText(true);

        mensaje.setStyle(
                "-fx-font-size:13px;"
                        + "-fx-text-fill:#444444;");

        Button aceptar =
                Estilos.botonSec(
                        "ACEPTAR");

        HBox acciones =
                new HBox(aceptar);

        acciones.setAlignment(
                Pos.CENTER_RIGHT);

        VBox.setVgrow(
                mensaje,
                Priority.ALWAYS);

        aceptar.setOnAction(e ->
                cerrarOverlay());

        card.getChildren().addAll(
                titulo,
                mensaje,
                acciones);

        mostrarOverlay(
                card,
                520,
                250);
    }

    // =========================================================
    // CONFIRMACIÓN
    // =========================================================

    private void mostrarConfirmacion(
            String tituloTexto,
            String mensajeTexto,
            Runnable accionConfirmar) {

        VBox card =
                new VBox(16);

        card.setPadding(
                new Insets(24));

        card.setMaxWidth(520);
        card.setMaxHeight(320);

        card.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:18;"
                        + "-fx-border-radius:18;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";"
                        + "-fx-effect:dropshadow("
                        + "gaussian,rgba(0,0,0,.22),24,0,0,8"
                        + ");");

        Label titulo =
                new Label(tituloTexto);

        titulo.setStyle(
                "-fx-font-size:19px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:"
                        + Estilos.VERDE_OSCURO
                        + ";");

        Label mensaje =
                new Label(mensajeTexto);

        mensaje.setWrapText(true);

        mensaje.setStyle(
                "-fx-font-size:13px;"
                        + "-fx-text-fill:#444444;");

        HBox acciones =
                new HBox(10);

        acciones.setAlignment(
                Pos.CENTER_RIGHT);

        VBox.setVgrow(
                mensaje,
                Priority.ALWAYS);

        Button cancelar =
                Estilos.botonSec(
                        "CANCELAR");

        Button aceptar =
                Estilos.boton(
                        "ACEPTAR");

        acciones.getChildren().addAll(
                cancelar,
                new Region(),
                aceptar);

        HBox.setHgrow(
                acciones.getChildren().get(1),
                Priority.ALWAYS);

        cancelar.setOnAction(e ->
                cerrarOverlay());

        aceptar.setOnAction(e -> {

            cerrarOverlay();

            if (accionConfirmar != null) {
                accionConfirmar.run();
            }
        });

        card.getChildren().addAll(
                titulo,
                mensaje,
                acciones);

        mostrarOverlay(
                card,
                520,
                250);
    }
}
