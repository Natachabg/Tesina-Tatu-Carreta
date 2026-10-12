package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class VistaEspecies extends VistaCRUD {

    private final StackPane modalLayer = new StackPane();

    public VistaEspecies() {
        super("Especies",
                "Catálogo de especies y su estado dentro de la Reserva.",
                new String[] { "Especie", "Estado" });

        agregar.setText("＋ AGREGAR ESPECIE");

        prepararOverlay();
        prepararDobleClick();

        cargar("");
    }

    // =========================================================
    // OVERLAY GENERAL
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

    private void mostrarOverlay(Region contenido,
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

        StackPane.setAlignment(contenido, Pos.CENTER);

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
    // TABLA
    // =========================================================

    private void prepararDobleClick() {

        tabla.setRowFactory(tv -> {

            TableRow<SistemaDAO.Fila> row =
                    new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !row.isEmpty()) {

                    mostrarInformacion(row.getItem());
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
        return SistemaDAO.especies(q);
    }

    @Override
    protected List<SistemaDAO.Fila> consultarInactivos(String q) {
        return SistemaDAO.especiesInactivas(q);
    }

    @Override
    protected void activarRegistro(int id) {
        SistemaDAO.activar(
                "especies",
                "id_especie",
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
    // MODIFICAR DESDE EL BOTÓN DEL CRUD
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

        if (!SistemaDAO.especiePuedeEliminar(id)) {

            mostrarMensaje(
                    "No se puede enviar a inactivos",
                    "Esta especie está asociada a uno o más animales. "
                            + "Primero revisá esas asociaciones.");

            return;
        }

        mostrarConfirmacion(
                "Enviar especie a inactivos",
                "¿Estás segura de que querés quitar esta "
                        + "especie del catálogo activo?",
                () -> {

                    SistemaDAO.baja(
                            "especies",
                            "id_especie",
                            id);

                    cargar(buscador.getText());
                });
    }

    // =========================================================
    // MODAL DE INFORMACIÓN
    // =========================================================

    private void mostrarInformacion(SistemaDAO.Fila f) {

        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.setFillWidth(true);
        contenido.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:18;"
                        + "-fx-border-radius:18;"
                        + "-fx-border-color:" + Estilos.BORDE + ";"
                        + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.22),24,0,0,8);");

        // -----------------------------------------------------
        // ENCABEZADO
        // -----------------------------------------------------

        BorderPane encabezado = new BorderPane();

        VBox titulos = new VBox(4);

        Label titulo = new Label("Información de la especie");
        titulo.setStyle(
                "-fx-font-size:20px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

        Label subtitulo = new Label(
                "Consulta de los datos registrados. "
                        + "Para modificar la especie utilizá MODIFICAR.");

        subtitulo.setWrapText(true);
        subtitulo.setStyle(
                "-fx-font-size:12px;"
                        + "-fx-text-fill:#666666;");

        titulos.getChildren().addAll(
                titulo,
                subtitulo);

        Button modificar =
                Estilos.boton("✎ MODIFICAR");

        Button cerrar =
                Estilos.botonSec("CERRAR");

        encabezado.setLeft(titulos);

        // -----------------------------------------------------
        // DATOS
        // -----------------------------------------------------

        GridPane datos = Formulario.grid();
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

        agregarDato(datos, 0, "Especie", valor(f, 1));
        agregarDato(datos, 1, "Estado", valor(f, 2));

        ScrollPane scroll =
                new ScrollPane(datos);

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background-color:transparent;"
                        + "-fx-background:"
                        + "transparent;");

        VBox.setVgrow(scroll, Priority.ALWAYS);

        // -----------------------------------------------------
        // ACCIONES
        // -----------------------------------------------------

        modificar.setOnAction(e -> {

            cerrarOverlay();

            form(f.id(), f);
        });

        cerrar.setOnAction(e ->
                cerrarOverlay());

        HBox acciones = new HBox(
                10,
                cerrar,
                new Region(),
                modificar);
        HBox.setHgrow(
                acciones.getChildren().get(1),
                Priority.ALWAYS);
        acciones.setAlignment(Pos.CENTER_LEFT);

        contenido.getChildren().addAll(
                encabezado,
                scroll,
                acciones);

        mostrarOverlay(
                contenido,
                900,
                470);
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
    // FORMULARIO AGREGAR / MODIFICAR
    // =========================================================

    private void form(int id, SistemaDAO.Fila f) {
        TextField nombre = Estilos.campo("Nombre de la especie");
        ComboBox<String> estado = Estilos.combo();
        estado.getItems().setAll("Activo", "Inactivo");
        if (f != null) {
            nombre.setText(valor(f, 1));
            estado.setValue(valor(f, 2));
        } else {
            estado.setValue("Activo");
        }

        Label titulo = new Label(id == 0 ? "Nueva especie" : "Modificar especie");
        titulo.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");
        Label subtitulo = new Label(id == 0 ? "Completá los datos para registrar una nueva especie." : "Modificá el nombre o el estado de la especie seleccionada.");
        subtitulo.setWrapText(true);
        subtitulo.setStyle("-fx-font-size:12px;-fx-text-fill:#666666;");

        GridPane campos = Formulario.grid();
        Formulario.add(campos, 0, "Especie", nombre, true);
        Formulario.add(campos, 1, "Estado", estado, true);

        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-radius:14;-fx-border-color:" + Estilos.BORDE + ";");
        BorderPane encabezado = new BorderPane();
        encabezado.setLeft(new VBox(4, titulo, subtitulo));
        Button cerrar = Estilos.botonSec("✕");
        cerrar.setTooltip(new Tooltip("Cerrar"));
        encabezado.setRight(cerrar);

        Button cancelar = Estilos.botonSec("CANCELAR");
        Button guardar = Estilos.boton(id == 0 ? "✓ AGREGAR ESPECIE" : "✓ GUARDAR CAMBIOS");
        HBox acciones = new HBox(10, cancelar, new Region(), guardar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(acciones.getChildren().get(1), Priority.ALWAYS);

        cerrar.setOnAction(e -> cerrarOverlay());
        cancelar.setOnAction(e -> cerrarOverlay());
        guardar.setOnAction(e -> {
            String nombreFinal = nombre.getText() == null ? "" : nombre.getText().trim();
            String estadoFinal = estado.getValue();
            if (!validar(nombreFinal) || estadoFinal == null) {
                mostrarMensaje("Datos incompletos", "Completá el nombre y el estado de la especie.");
                return;
            }
            mostrarConfirmacion(
                    id == 0 ? "Agregar especie" : "Guardar cambios",
                    id == 0 ? "¿Querés agregar esta especie?" : "¿Querés guardar los cambios?",
                    () -> {
                        try {
                            SistemaDAO.guardarEspecie(id, nombreFinal, estadoFinal);
                            cerrarOverlay();
                            cargar(buscador.getText());
                        } catch (Exception ex) {
                            mostrarMensaje("No se pudo guardar", ex.getMessage() == null ? ex.toString() : ex.getMessage());
                        }
                    });
        });

        contenido.getChildren().addAll(encabezado, campos, acciones);
        mostrarOverlay(contenido, 760, 420);
        nombre.requestFocus();
    }
    // =========================================================
    // MENSAJE OVERLAY
    // =========================================================

    private void mostrarMensaje(
            String tituloTexto,
            String mensajeTexto) {

        VBox card =
                new VBox(16);

        card.setPadding(
                new Insets(24));

        card.setMaxWidth(500);
        card.setMaxHeight(300);

        card.setStyle(
                "-fx-background-color:white;"
                        + "-fx-background-radius:14;"
                        + "-fx-border-radius:14;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";");

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
                Estilos.boton("ACEPTAR");

        HBox acciones =
                new HBox(aceptar);

        acciones.setAlignment(
                Pos.CENTER_RIGHT);

        aceptar.setOnAction(e ->
                cerrarOverlay());

        card.getChildren().addAll(
                titulo,
                mensaje,
                acciones);

        mostrarOverlay(
                card,
                500,
                250);
    }

    // =========================================================
    // CONFIRMACIÓN OVERLAY
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
                        + "-fx-background-radius:14;"
                        + "-fx-border-radius:14;"
                        + "-fx-border-color:"
                        + Estilos.BORDE
                        + ";");

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

        Button cancelar =
                Estilos.botonSec("CANCELAR");

        Button aceptar =
                Estilos.boton("ACEPTAR");

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

