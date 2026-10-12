package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.Estilos;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Base visual para todos los ABM:
 * búsqueda, alta, restauración y doble click.
 */
public abstract class VistaCRUD extends BorderPane {

    protected final VBox contenido = new VBox(12);

    protected final TextField buscador = Estilos.campo("Buscar por nombre, acta, especie...");

    protected final TableView<SistemaDAO.Fila> tabla = new TableView<>();

    protected final Button agregar = Estilos.boton("＋ AGREGAR");

    protected final Button modificar = Estilos.botonSec("✎ MODIFICAR");

    protected final Button inactivos = Estilos.botonSec("▣ REGISTROS INACTIVOS");

    protected final Button eliminar = Estilos.botonPeligro("⌫ ENVIAR A INACTIVOS");

    protected final Label contador = new Label();

    /**
     * Barra de herramientas reutilizable.
     *
     * Las vistas hijas pueden agregar componentes
     * personalizados si lo necesitan.
     */
    protected final HBox tools = new HBox(10);

    /**
     * Mensaje informativo opcional.
     *
     * Por defecto no se muestra.
     */
    protected final Label mensajeVista = new Label();

    private final String titulo;

    protected VistaCRUD(
            String titulo,
            String ayuda,
            String[] cols) {

        this.titulo = titulo;

        Estilos.aplicarVentana(this);

        setPadding(new Insets(22));

        // ============================================================
        // ENCABEZADO
        // ============================================================

        ImageView encabezado = crearEncabezado(titulo);

        VBox cab = new VBox(4, encabezado);

        // ============================================================
        // BUSCADOR
        // ============================================================

        Label bl = Estilos.etiqueta("BUSCAR");

        VBox search = new VBox(
                4,
                bl,
                buscador);

        /*
         * El buscador ya no ocupa todo el espacio disponible.
         * Esto permite que los botones o mensajes tengan espacio.
         */
        search.setPrefWidth(280);
        search.setMinWidth(250);
        search.setMaxWidth(320);

        // ============================================================
        // BOTONES
        // ============================================================

        agregar.setPrefWidth(190);
        agregar.setMinWidth(190);

        modificar.setPrefWidth(160);
        modificar.setMinWidth(160);

        inactivos.setPrefWidth(200);
        inactivos.setMinWidth(200);

        eliminar.setPrefWidth(190);
        eliminar.setMinWidth(190);

        modificar.setDisable(true);

        // ============================================================
        // BARRA DE HERRAMIENTAS
        // ============================================================

        tools.getChildren().addAll(
                search,
                agregar,
                modificar,
                inactivos,
                eliminar);

        tools.setAlignment(
                Pos.BOTTOM_LEFT);

        tools.setFillHeight(true);

        // ============================================================
        // MENSAJE INFORMATIVO
        // ============================================================

        mensajeVista.setWrapText(true);

        mensajeVista.setVisible(false);
        mensajeVista.setManaged(false);

        mensajeVista.setStyle(
                "-fx-font-size:12px;" +
                        "-fx-text-fill:" + Estilos.GRIS + ";" +
                        "-fx-background-color:rgba(115,77,44,0.08);" +
                        "-fx-background-radius:10px;" +
                        "-fx-padding:9px 14px;");

        /*
         * Cuando una vista utilice este mensaje,
         * ocupará el espacio libre de la barra.
         */
        HBox.setHgrow(
                mensajeVista,
                Priority.ALWAYS);

        // ============================================================
        // CONTADOR
        // ============================================================

        contador.setStyle(
                "-fx-font-size:11px;" +
                        "-fx-text-fill:" + Estilos.GRIS + ";" +
                        "-fx-font-weight:bold;");

        // ============================================================
        // TABLA
        // ============================================================

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Estilos.tablaCompacta(tabla);

        tabla.setPrefHeight(330);

        for (int i = 0; i < cols.length; i++) {

            final int ix = i;

            TableColumn<SistemaDAO.Fila, String> c = new TableColumn<>(cols[i]);

            c.setCellValueFactory(
                    x -> new javafx.beans.property.SimpleStringProperty(
                            x.getValue().c().size() > ix + 1
                                    ? x.getValue().c().get(ix + 1)
                                    : ""));

            /*
             * Formatear fechas automáticamente.
             */
            if (cols[i].toLowerCase().contains("fecha")
                    || cols[i].equalsIgnoreCase("ingreso")) {

                c.setCellFactory(
                        col -> new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty);

                                if (empty
                                        || item == null
                                        || item.isBlank()) {

                                    setText("");

                                } else {

                                    try {

                                        setText(
                                                LocalDate
                                                        .parse(item)
                                                        .format(
                                                                DateTimeFormatter
                                                                        .ofPattern(
                                                                                "dd/MM/yyyy")));

                                    } catch (Exception e) {

                                        setText(item);
                                    }
                                }
                            }
                        });
            }

            tabla.getColumns().add(c);
        }

        // ============================================================
        // DOBLE CLICK
        // ============================================================

        tabla.setRowFactory(tv -> {

            TableRow<SistemaDAO.Fila> r = new TableRow<>();

            r.setOnMouseClicked(e -> {

                if (e.getClickCount() == 2
                        && !r.isEmpty()) {

                    editarSeleccionado();
                }
            });

            return r;
        });

        // ============================================================
        // LAYOUT SUPERIOR
        // ============================================================

        VBox top = new VBox(
                10,
                cab,
                tools,
                contador);

        setTop(top);

        setCenter(tabla);

        // ============================================================
        // EVENTOS
        // ============================================================

        buscador.textProperty()
                .addListener(
                        (o, a, b) -> cargar(b));

        agregar.setOnAction(
                e -> agregarNuevo());

        modificar.setOnAction(
                e -> editarSeleccionado());

        inactivos.setOnAction(
                e -> mostrarInactivos());

        eliminar.setOnAction(
                e -> eliminarSeleccionado());

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (o, a, b) -> modificar.setDisable(b == null));
    }

    // ================================================================
    // CONTROL DEL BOTÓN "ENVIAR A INACTIVOS"
    // ================================================================

    /**
     * Permite que una vista hija oculte el botón
     * "ENVIAR A INACTIVOS".
     *
     * Ejemplo:
     *
     * mostrarBotonEliminar(false);
     */
    protected void mostrarBotonEliminar(boolean mostrar) {

        eliminar.setVisible(mostrar);
        eliminar.setManaged(mostrar);
    }

    // ================================================================
    // CONTROL DEL BOTÓN "REGISTROS INACTIVOS"
    // ================================================================

    /**
     * Permite ocultar el botón de registros inactivos
     * si una vista no lo necesita.
     */
    protected void mostrarBotonInactivos(boolean mostrar) {

        inactivos.setVisible(mostrar);
        inactivos.setManaged(mostrar);
    }

    // ================================================================
    // CONTROL DEL BOTÓN "AGREGAR"
    // ================================================================

    /**
     * Permite ocultar el botón "AGREGAR".
     */
    protected void mostrarBotonAgregar(boolean mostrar) {

        agregar.setVisible(mostrar);
        agregar.setManaged(mostrar);
    }

    // ================================================================
    // CONTROL DEL BOTÓN "MODIFICAR"
    // ================================================================

    /**
     * Permite ocultar el botón "MODIFICAR".
     */
    protected void mostrarBotonModificar(boolean mostrar) {

        modificar.setVisible(mostrar);
        modificar.setManaged(mostrar);
    }

    // ================================================================
    // CONTROL DEL DOBLE CLICK
    // ================================================================

    protected void mostrarDobleClick(boolean mostrar) {

        if (mostrar) {

            tabla.setRowFactory(tv -> {

                TableRow<SistemaDAO.Fila> r = new TableRow<>();

                r.setOnMouseClicked(e -> {

                    if (e.getClickCount() == 2
                            && !r.isEmpty()) {

                        editarSeleccionado();
                    }
                });

                return r;
            });

        } else {

            tabla.setRowFactory(null);
        }
    }

    // ================================================================
    // MENSAJE PERSONALIZADO DE LA VISTA
    // ================================================================

    /**
     * Muestra un mensaje informativo dentro de la barra
     * de herramientas, aprovechando el espacio disponible.
     */
    protected void mostrarMensajeVista(String mensaje) {

        /*
         * Por seguridad, si el mensaje ya estuviera agregado
         * a la barra, primero lo quitamos.
         */
        tools.getChildren().remove(mensajeVista);

        mensajeVista.setText(mensaje);

        mensajeVista.setVisible(true);
        mensajeVista.setManaged(true);

        /*
         * Lo agregamos al final de la barra.
         */
        tools.getChildren().add(mensajeVista);
    }

    // ================================================================
    // ENCABEZADO
    // ================================================================

    private ImageView crearEncabezado(
            String titulo) {

        String recurso = null;

        if (titulo.equalsIgnoreCase("Animales"))

            recurso = "/images/secciones/animales.png";

        else if (titulo.equalsIgnoreCase("Especies"))

            recurso = "/images/secciones/especies.png";

        else if (titulo.equalsIgnoreCase("Plantel Permanente"))

            recurso = "/images/secciones/plantel.png";

        else if (titulo.equalsIgnoreCase("Movimientos"))

            recurso = "/images/secciones/movimientos.png";

        else if (titulo.equalsIgnoreCase("Ingresos / Actas"))

            recurso = "/images/secciones/ingresos.png";

        else if (titulo.equalsIgnoreCase("Habitáculos"))

            recurso = "/images/secciones/habitaculos.png";

        else if (titulo.equalsIgnoreCase("Usuarios"))

            recurso = "/images/secciones/usuarios.png";

        ImageView iv = new ImageView();

        if (recurso != null) {

            try {

                iv.setImage(
                        new javafx.scene.image.Image(
                                getClass()
                                        .getResourceAsStream(
                                                recurso)));

            } catch (Exception ignored) {
            }
        }

        iv.setPreserveRatio(true);

        iv.setFitWidth(1080);
        iv.setFitHeight(155);

        iv.setSmooth(true);

        return iv;
    }

    // ================================================================
    // MÉTODOS ABSTRACTOS
    // ================================================================

    protected abstract List<SistemaDAO.Fila> consultar(
            String q);

    protected abstract void agregarNuevo();

    protected abstract void editarSeleccionado();

    protected abstract void desactivar(
            int id);

    protected List<SistemaDAO.Fila> consultarInactivos(
            String q) {

        return List.of();
    }

    // ================================================================
    // CARGAR
    // ================================================================

    protected void cargar(String q) {

        try {

            List<SistemaDAO.Fila> data = consultar(q);

            tabla.setItems(
                    FXCollections.observableArrayList(
                            data));

            tabla.setPrefHeight(
                    Math.max(
                            105,
                            Math.min(
                                    430,
                                    48 +
                                            Math.max(
                                                    1,
                                                    data.size())
                                                    * 31)));

            contador.setText(
                    data.size()
                            + " registro"
                            + (data.size() == 1
                                    ? ""
                                    : "s")
                            + " · doble click para abrir y modificar");

        } catch (Exception e) {

            error(
                    "No se pudieron cargar los datos",
                    e.getMessage());
        }
    }

    // ================================================================
    // ELIMINAR / DESACTIVAR
    // ================================================================

    protected void eliminarSeleccionado() {

        var f = tabla.getSelectionModel()
                .getSelectedItem();

        if (f == null) {

            info(
                    "Seleccioná un registro",
                    "Elegí una fila para continuar.");

            return;
        }

        desactivar(f.id());

        cargar(
                buscador.getText());
    }

    // ================================================================
    // INACTIVOS
    // ================================================================

    protected void mostrarInactivos() {

        TableView<SistemaDAO.Fila> t = new TableView<>();

        Estilos.tablaCompacta(t);

        t.setPrefHeight(300);

        t.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        for (int ix = 0; ix < tabla.getColumns().size(); ix++) {

            TableColumn<SistemaDAO.Fila, String> c = new TableColumn<>(
                    tabla.getColumns()
                            .get(ix)
                            .getText());

            final int k = ix;

            c.setCellValueFactory(
                    x -> new javafx.beans.property.SimpleStringProperty(
                            x.getValue().c().size() > k + 1
                                    ? x.getValue().c().get(k + 1)
                                    : ""));

            t.getColumns().add(c);
        }

        TextField b = Estilos.campo(
                "Buscar registros inactivos...");

        Button restaurar = Estilos.boton(
                "↻ RESTAURAR REGISTRO");

        t.setItems(
                FXCollections.observableArrayList(
                        consultarInactivos("")));

        b.textProperty()
                .addListener(
                        (o, a, v) -> t.setItems(
                                FXCollections.observableArrayList(
                                        consultarInactivos(v))));

        restaurar.setOnAction(e -> {

            var f = t.getSelectionModel()
                    .getSelectedItem();

            if (f == null) {

                info(
                        "Seleccioná un registro",
                        "Elegí el registro que querés restaurar.");

                return;
            }

            confirmar(
                    "Restaurar registro",
                    "¿Querés volver a habilitar este registro?",
                    () -> {

                        activarRegistro(f.id());

                        t.setItems(
                                FXCollections.observableArrayList(
                                        consultarInactivos(
                                                b.getText())));

                        cargar(
                                buscador.getText());
                    });
        });

        Dialog<ButtonType> d = new Dialog<>();

        d.setTitle("Tatú Carreta");

        d.setHeaderText(
                "Registros inactivos · "
                        + titulo);

        d.getDialogPane()
                .getButtonTypes()
                .add(ButtonType.CLOSE);

        VBox box = new VBox(
                10,
                b,
                t,
                restaurar);

        box.setPadding(
                new Insets(4));

        box.setPrefSize(
                820,
                420);

        d.getDialogPane()
                .setContent(box);

        d.setResizable(false);

        d.setOnShown(
                e -> Estilos.estilizarDialog(
                        d.getDialogPane()));

        d.showAndWait();
    }

    // ================================================================
    // RESTAURAR
    // ================================================================

    protected void activarRegistro(int id) {
    }

    // ================================================================
    // CONFIRMACIONES
    // ================================================================

    protected void confirmar(
            String h,
            String txt,
            Runnable ok) {

        Alert a = new Alert(
                Alert.AlertType.CONFIRMATION);

        a.setTitle("Tatú Carreta");

        a.setHeaderText(h);

        a.setContentText(txt);

        a.showAndWait()
                .ifPresent(r -> {

                    if (r == ButtonType.OK)

                        ok.run();
                });
    }

    // ================================================================
    // VALIDACIÓN
    // ================================================================

    protected boolean validar(
            String... v) {

        for (String x : v) {

            if (x == null || x.isBlank())

                return false;

            String t = x.trim();

            if (t.matches(
                    "\\d+(?:[.,]\\d+)?"))

                continue;

            if (t.length() < 2)

                return false;
        }

        return true;
    }

    // ================================================================
    // MENSAJES
    // ================================================================

    protected void info(
            String h,
            String c) {

        Alert a = new Alert(
                Alert.AlertType.INFORMATION);

        a.setTitle("Tatú Carreta");

        a.setHeaderText(h);

        a.setContentText(c);

        a.showAndWait();
    }

    protected void error(
            String h,
            String c) {

        Alert a = new Alert(
                Alert.AlertType.ERROR);

        a.setTitle("Tatú Carreta");

        a.setHeaderText(h);

        a.setContentText(c);

        a.showAndWait();
    }

    protected boolean confirmarOK(
            String h,
            String c) {

        Alert a = new Alert(
                Alert.AlertType.CONFIRMATION);

        a.setTitle("Tatú Carreta");

        a.setHeaderText(h);

        a.setContentText(c);

        return a.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}