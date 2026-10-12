package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.util.List;

public class VistaHabitaculos extends VistaCRUD {

    private final StackPane modalLayer = new StackPane();

    public VistaHabitaculos() {
        super(
                "Habitáculos",
                "Espacios físicos y capacidad máxima de la Reserva. No se permite enviar a inactivos un habitáculo que conserve animales asociados.",
                new String[] {
                        "Nombre", "Sector", "Capacidad", "Estado", "Observaciones"
                });

        agregar.setText("＋ AGREGAR HABITÁCULO");

        prepararOverlay();

        // Doble click = información. NO modifica directamente.
        tabla.setRowFactory(tv -> {
            TableRow<SistemaDAO.Fila> fila = new TableRow<>();

            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY
                        && e.getClickCount() == 2
                        && !fila.isEmpty()) {
                    mostrarInformacion(fila.getItem());
                }
            });

            return fila;
        });

        cargar("");
    }

    // ============================================================
    // OVERLAY
    // ============================================================

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

    private void prepararOverlay() {
        modalLayer.setManaged(false);
        modalLayer.setVisible(false);
        modalLayer.setPickOnBounds(true);

        modalLayer.setStyle(
                "-fx-background-color: rgba(0,0,0,0.60);");

        getChildren().add(modalLayer);
    }

    private void mostrarModal(
            Region content,
            double preferredWidth,
            double preferredHeight) {

        double availableWidth = getWidth();
        double availableHeight = getHeight();

        if (availableWidth <= 0) {
            availableWidth = getLayoutBounds().getWidth();
        }

        if (availableHeight <= 0) {
            availableHeight = getLayoutBounds().getHeight();
        }

        double width = Math.min(
                preferredWidth,
                Math.max(360, availableWidth - 80));

        double height = Math.min(
                preferredHeight,
                Math.max(300, availableHeight - 80));

        content.setMinSize(0, 0);
        content.setPrefSize(width, height);
        content.setMaxSize(width, height);

        modalLayer.getChildren().setAll(content);

        StackPane.setAlignment(content, Pos.CENTER);

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

    private void cerrarModal() {
        modalLayer.getChildren().clear();
        modalLayer.setVisible(false);
        modalLayer.setManaged(false);
        requestLayout();
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @Override
    protected List<SistemaDAO.Fila> consultar(String q) {
        return SistemaDAO.habitaculos(q);
    }

    @Override
    protected List<SistemaDAO.Fila> consultarInactivos(String q) {
        return SistemaDAO.habitaculosInactivos(q);
    }

    @Override
    protected void activarRegistro(int id) {
        SistemaDAO.activar("habitaculos", "id_habitaculo", id);
    }

    // ============================================================
    // CRUD
    // ============================================================

    @Override
    protected void agregarNuevo() {
        form(0, null);
    }

    // El botón MODIFICAR del CRUD sigue editando directamente.
    @Override
    protected void editarSeleccionado() {
        SistemaDAO.Fila f =
                tabla.getSelectionModel().getSelectedItem();

        if (f != null) {
            form(f.id(), f);
        }
    }

    @Override
    protected void desactivar(int id) {
        if (!SistemaDAO.habitaculoPuedeEliminar(id)) {
            info(
                    "No se puede enviar a inactivos",
                    "Este habitáculo tiene o tuvo animales asignados. El historial debe conservarse.");
            return;
        }

        if (!confirmarOK(
                "Enviar habitáculo a inactivos",
                "¿Estás segura de que querés quitar este habitáculo del catálogo activo?")) {
            return;
        }

        TextInputDialog dialogoMotivo = new TextInputDialog();
        dialogoMotivo.setTitle("Motivo de inactivación");
        dialogoMotivo.setHeaderText("Indicá por qué se envía este habitáculo a inactivos.");
        dialogoMotivo.setContentText("Motivo:");
        if (getScene() != null && getScene().getWindow() != null) {
            dialogoMotivo.initOwner(getScene().getWindow());
        }

        java.util.Optional<String> resultado = dialogoMotivo.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        String motivo = resultado.get().trim();
        if (motivo.isBlank()) {
            info("Falta el motivo", "Escribí el motivo para poder enviar el habitáculo a inactivos.");
            return;
        }

        try {
            SistemaDAO.inactivarHabitaculoConMotivo(id, motivo);
            cargar(buscador.getText());
        } catch (Exception ex) {
            error("No se pudo enviar a inactivos", ex.getMessage());
        }
    }

    // ============================================================
    // MODAL INFORMATIVO CON OVERLAY
    // ============================================================

    private void mostrarInformacion(SistemaDAO.Fila f) {

        if (f == null) {
            return;
        }

        List<String> c = f.c();

        String nombre = valor(c, 1);
        String sector = valor(c, 2);
        String capacidad = valor(c, 3);
        String estado = valor(c, 4);
        String observaciones = valor(c, 5);

        // --------------------------------------------------------
        // TÍTULO
        // --------------------------------------------------------

        Label titulo =
                new Label("INFORMACIÓN DEL HABITÁCULO");

        titulo.setStyle(
                "-fx-font-size:21px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

        Label subtitulo =
                new Label(
                        "Consulta de solo lectura · Registro #" + f.id());

        subtitulo.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:" + Estilos.GRIS + ";");

        VBox encabezado =
                new VBox(
                        3,
                        titulo,
                        subtitulo);

        // --------------------------------------------------------
        // ACCIONES INFERIORES
        // --------------------------------------------------------

        Button modificar =
                Estilos.boton("✎ MODIFICAR");

        Button cerrar =
                Estilos.botonSec("CERRAR");

        HBox botones =
                new HBox(
                        8,
                        cerrar,
                        new Region(),
                        modificar);

        HBox.setHgrow(
                botones.getChildren().get(1),
                Priority.ALWAYS);

        botones.setAlignment(Pos.CENTER_RIGHT);

        // --------------------------------------------------------
        // DATOS
        // --------------------------------------------------------

        GridPane datos = new GridPane();

        datos.setHgap(28);
        datos.setVgap(16);

        datos.setPadding(
                new Insets(
                        10,
                        0,
                        8,
                        0));

        ColumnConstraints col1 =
                new ColumnConstraints();

        col1.setPercentWidth(50);

        ColumnConstraints col2 =
                new ColumnConstraints();

        col2.setPercentWidth(50);

        datos.getColumnConstraints().addAll(
                col1,
                col2);

        agregarDato(
                datos,
                0,
                0,
                "Nombre",
                nombre);

        agregarDato(
                datos,
                1,
                0,
                "Sector",
                sector);

        agregarDato(
                datos,
                0,
                1,
                "Capacidad máxima",
                capacidad);

        agregarDato(
                datos,
                1,
                1,
                "Estado",
                estado);

        // --------------------------------------------------------
        // OBSERVACIONES
        // --------------------------------------------------------

        Label tituloObservaciones =
                new Label("OBSERVACIONES");

        tituloObservaciones.setStyle(
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" + Estilos.GRIS + ";");

        TextArea areaObservaciones =
                new TextArea(
                        observaciones == null
                                ? ""
                                : observaciones);

        areaObservaciones.setEditable(false);
        areaObservaciones.setWrapText(true);
        areaObservaciones.setPrefRowCount(8);
        areaObservaciones.setMinHeight(150);
        areaObservaciones.setMaxWidth(Double.MAX_VALUE);

        areaObservaciones.setStyle(
                "-fx-control-inner-background:white;" +
                "-fx-background-color:white;" +
                "-fx-border-color:" + Estilos.BORDE + ";" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-font-size:13px;");

        VBox bloqueObservaciones =
                new VBox(
                        6,
                        tituloObservaciones,
                        areaObservaciones);

        VBox.setVgrow(
                areaObservaciones,
                Priority.ALWAYS);

        // --------------------------------------------------------
        // NOTA
        // --------------------------------------------------------

        Label nota =
                new Label(
                        "La información mostrada corresponde al registro actual del habitáculo. "
                        + "La capacidad limita automáticamente la cantidad de ejemplares que pueden "
                        + "asignarse a este espacio.");

        nota.setWrapText(true);

        nota.setStyle(
                "-fx-background-color:" +
                Estilos.VERDE_SUAVE + ";" +
                "-fx-text-fill:" +
                Estilos.VERDE_OSCURO + ";" +
                "-fx-padding:10;" +
                "-fx-background-radius:8;" +
                "-fx-font-size:12px;");

        // --------------------------------------------------------
        // CENTRO SCROLLABLE
        // --------------------------------------------------------

        VBox contenido =
                new VBox(
                        14,
                        encabezado,
                        datos,
                        bloqueObservaciones,
                        nota);

        contenido.setPadding(
                new Insets(20));

        contenido.setFillWidth(true);

        contenido.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:16;" +
                "-fx-border-color:" + Estilos.BORDE + ";" +
                "-fx-border-radius:16;");

        ScrollPane scroll =
                new ScrollPane(contenido);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;");

        VBox centro =
                new VBox(scroll);

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS);

        // --------------------------------------------------------
        // CONTENEDOR DEL MODAL
        // --------------------------------------------------------

        BorderPane modal =
                new BorderPane();

        modal.setBottom(botones);
        modal.setCenter(centro);

        BorderPane.setMargin(
                botones,
                new Insets(
                        8,
                        18,
                        18,
                        18));

        modal.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:18;" +
                "-fx-border-color:" + Estilos.BORDE + ";" +
                "-fx-border-radius:18;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.22),24,0,0,8);");

        // --------------------------------------------------------
        // CERRAR
        // --------------------------------------------------------

        cerrar.setOnAction(e ->
                cerrarModal());

        // --------------------------------------------------------
        // MODIFICAR
        //
        // Cierra el overlay informativo y abre el formulario
        // de edición existente.
        // --------------------------------------------------------

        modificar.setOnAction(e -> {

            cerrarModal();

            form(
                    f.id(),
                    f);
        });

        // --------------------------------------------------------
        // ABRIR OVERLAY
        // --------------------------------------------------------

        mostrarModal(
                modal,
                820,
                620);
    }

    // ============================================================
    // FORMULARIO DE EDICIÓN / ALTA CON OVERLAY
    // ============================================================

    private void form(
            int id,
            SistemaDAO.Fila f) {

        TextField n =
                Estilos.campo("Nombre del habitáculo");

        TextField s =
                Estilos.campo("Sector");

        TextField cap =
                Estilos.entero("Capacidad máxima", 6);

        TextArea obs =
                Estilos.area("Observaciones");

        if (f != null) {
            n.setText(f.c().get(1));
            s.setText(f.c().get(2));
            cap.setText(f.c().get(3));
            obs.setText(f.c().get(5));
        }

        Label titulo =
                new Label(
                        id == 0
                                ? "NUEVO HABITÁCULO"
                                : "MODIFICAR HABITÁCULO");

        titulo.setStyle(
                "-fx-font-size:21px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

        Label subtitulo =
                new Label(
                        id == 0
                                ? "Completá los datos del nuevo habitáculo."
                                : "Modificá los datos del habitáculo seleccionado.");

        subtitulo.setWrapText(true);
        subtitulo.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:" + Estilos.GRIS + ";");

        VBox encabezado =
                new VBox(
                        3,
                        titulo,
                        subtitulo);

        // --------------------------------------------------------
        // DATOS DEL FORMULARIO
        // --------------------------------------------------------

        GridPane g =
                Formulario.grid();

        Formulario.add(
                g,
                0,
                "Nombre",
                n,
                true);

        Formulario.add(
                g,
                1,
                "Sector",
                s,
                true);

        Formulario.add(
                g,
                2,
                "Capacidad máxima",
                cap,
                true);

        Formulario.add(
                g,
                3,
                "Observaciones",
                obs,
                false);

        Label ayuda =
                new Label(
                        "La capacidad limita automáticamente cuántos ejemplares se pueden asignar a este espacio.");

        ayuda.setWrapText(true);
        ayuda.setStyle(
                "-fx-background-color:" +
                Estilos.VERDE_SUAVE +
                ";-fx-text-fill:" +
                Estilos.VERDE_OSCURO +
                ";-fx-padding:10;" +
                "-fx-background-radius:8;");

        g.add(
                ayuda,
                1,
                4);

        // --------------------------------------------------------
        // BOTONES
        // --------------------------------------------------------

        Button guardar =
                Estilos.boton(
                        id == 0
                                ? "✓ AGREGAR"
                                : "✓ GUARDAR CAMBIOS");

        Button cerrar =
                Estilos.botonSec("CANCELAR");

        HBox botones =
                new HBox(
                        8,
                        cerrar,
                        new Region(),
                        guardar);

        HBox.setHgrow(
                botones.getChildren().get(1),
                Priority.ALWAYS);

        botones.setAlignment(
                Pos.CENTER_RIGHT);

        // --------------------------------------------------------
        // CONTENIDO SCROLLABLE
        // --------------------------------------------------------

        VBox contenido =
                new VBox(
                        14,
                        encabezado,
                        g);

        contenido.setPadding(
                new Insets(20));

        contenido.setFillWidth(true);

        contenido.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:16;" +
                "-fx-border-color:" + Estilos.BORDE + ";" +
                "-fx-border-radius:16;");

        ScrollPane scroll =
                new ScrollPane(contenido);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;");

        VBox centro =
                new VBox(scroll);

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS);

        // --------------------------------------------------------
        // MODAL COMPLETO
        // --------------------------------------------------------

        BorderPane modal =
                new BorderPane();

        modal.setBottom(botones);
        modal.setCenter(centro);

        BorderPane.setMargin(
                botones,
                new Insets(
                        8,
                        18,
                        18,
                        18));

        modal.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:18;" +
                "-fx-border-color:" + Estilos.BORDE + ";" +
                "-fx-border-radius:18;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.22),24,0,0,8);");

        // --------------------------------------------------------
        // CANCELAR: SOLO CIERRA EL OVERLAY
        // --------------------------------------------------------

        cerrar.setOnAction(e ->
                cerrarModal());

        // --------------------------------------------------------
        // GUARDAR: MISMA LÓGICA DEL FORMULARIO ORIGINAL
        // PERO SIN Dialog NI VENTANA EXTRA
        // --------------------------------------------------------

        guardar.setOnAction(e -> {

            try {

                if (!validar(
                        n.getText(),
                        s.getText(),
                        cap.getText())) {

                    throw new IllegalArgumentException(
                            "Completá los campos obligatorios.");
                }

                int c =
                        Integer.parseInt(
                                cap.getText());

                if (c <= 0) {
                    throw new IllegalArgumentException(
                            "La capacidad debe ser mayor que cero.");
                }

                int ocup =
                        id == 0
                                ? 0
                                : SistemaDAO.ocupacionHabitaculo(
                                        id,
                                        0);

                if (c < ocup) {
                    throw new IllegalArgumentException(
                            "No podés reducir la capacidad por debajo de la ocupación actual ("
                                    + ocup + ").");
                }

                SistemaDAO.guardarHabitaculo(
                        id,
                        n.getText().trim(),
                        s.getText().trim(),
                        c,
                        obs.getText().trim());

                cerrarModal();

                cargar(
                        buscador.getText());

            } catch (Exception ex) {

                error(
                        "No se pudo guardar",
                        ex.getMessage());
            }
        });

        // --------------------------------------------------------
        // ABRIR EL MISMO OVERLAY
        // --------------------------------------------------------

        mostrarModal(
                modal,
                820,
                620);

        n.requestFocus();
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private String valor(
            List<String> valores,
            int indice) {

        if (valores == null
                || valores.size() <= indice
                || valores.get(indice) == null) {

            return "";
        }

        return valores.get(indice);
    }

    private void agregarDato(
            GridPane g,
            int columna,
            int fila,
            String titulo,
            String valor) {

        VBox box =
                new VBox(4);

        Label t =
                new Label(titulo);

        t.setStyle(
                "-fx-font-size:11px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" +
                Estilos.GRIS + ";");

        Label v =
                new Label(
                        valor == null || valor.isBlank()
                                ? "—"
                                : valor);

        v.setWrapText(true);
        v.setMaxWidth(Double.MAX_VALUE);

        v.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#263238;");

        box.getChildren().addAll(
                t,
                v);

        g.add(
                box,
                columna,
                fila);

        GridPane.setHgrow(
                box,
                Priority.ALWAYS);
    }
}

