package com.tatucarreta.vistas;

import com.tatucarreta.data.ReportePlantelPDF;
import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.Estilos;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

public class VistaPlantel extends VistaCRUD {

        // ============================================================
        // FILTRO PRINCIPAL DEL PLANTEL
        // ============================================================

        private enum FiltroPlantel {
                INGRESOS,
                FALLECIMIENTOS,
                TRASLADOS
        }

        private FiltroPlantel filtroActual = FiltroPlantel.INGRESOS;

        private final ComboBox<String> filtroRegistros = Estilos.combo();

        // ============================================================
        // CAPAS DE MODALES
        // ============================================================

        private final StackPane modalLayer = new StackPane();

        private final StackPane secondaryModalLayer = new StackPane();

        private final StackPane tertiaryModalLayer = new StackPane();

        // ============================================================
        // CONSTRUCTOR
        // ============================================================

        public VistaPlantel() {

                super(
                                "Plantel Permanente",
                                "Ejemplares que permanecen en la Reserva. Desde acá se registran altas, nacimientos, fallecimientos e identificaciones.",
                                new String[] {
                                                "Animal",
                                                "Identificación",
                                                "Especie",
                                                "Cantidad",
                                                "Ubicación",
                                                "Habitáculo",
                                                "Ingreso",
                                                "Estado"
                                });
                                

                // ============================================================
                // BOTONES PRINCIPALES
                // ============================================================

                agregar.setText("＋ AGREGAR EJEMPLAR");
                mostrarBotonEliminar(false);
                mostrarBotonInactivos(false);
                mostrarBotonModificar(true);

                // ============================================================
                // ACCIONES DEL PLANTEL
                // ============================================================

                Button nac = Estilos.botonSec("🐣 NACIMIENTO");
                Button fall = Estilos.botonSec("⚠ FALLECIMIENTO");
                Button tras = Estilos.botonSec("↔ TRASLADO");
                Button ids = Estilos.botonSec("◎ IDENTIFICACIONES");
                Button rep = Estilos.boton("▣ REPORTE MENSUAL");

                nac.setOnAction(e -> movimientoVital(true));
                fall.setOnAction(e -> movimientoVital(false));
                tras.setOnAction(e -> trasladarSeleccionado());
                rep.setOnAction(e -> abrirSelectorReportes());

                ids.setOnAction(e -> {
                        try {
                                identificaciones();
                        } catch (Exception ex) {
                                mostrarError(
                                                "No se pudo abrir Identificaciones",
                                                ex.getMessage() == null ? ex.toString() : ex.getMessage());
                        }
                });

                // Las actas de ingreso general, fallecimientos y traslados
                // ya se consultan mediante el filtro dinámico de la tabla.
                // No se duplican como botones ni dentro de un menú.

                Label tituloAcciones = Estilos.etiqueta("ACCIONES");
                tituloAcciones.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" + Estilos.GRIS + ";" +
                                                "-fx-padding:4 8 4 2;");

                FlowPane acciones = new FlowPane(
                                8,
                                8,
                                nac,
                                fall,
                                tras,
                                ids,
                                rep);

                acciones.setPrefWrapLength(900);
                acciones.setAlignment(Pos.CENTER_LEFT);

                VBox bloqueAcciones = new VBox(
                                5,
                                tituloAcciones,
                                acciones);

                bloqueAcciones.setFillWidth(true);

                VBox top = (VBox) getTop();
                top.getChildren().add(2, bloqueAcciones);

                // ============================================================
                // FILTRO DINÁMICO
                // ============================================================

                filtroRegistros.getItems().setAll(
                                "Ingresos generales",
                                "Fallecimientos",
                                "Traslados");

                filtroRegistros.setValue("Ingresos generales");
                filtroRegistros.setPrefWidth(220);
                filtroRegistros.setMinWidth(200);

                Label etiquetaFiltro = Estilos.etiqueta("MOSTRAR");

                VBox selectorFiltro = new VBox(
                                4,
                                etiquetaFiltro,
                                filtroRegistros);

                top.getChildren().add(2, selectorFiltro);

                filtroRegistros.setOnAction(e -> {

                        String seleccionado = filtroRegistros.getValue();

                        if ("Fallecimientos".equals(seleccionado)) {
                                filtroActual = FiltroPlantel.FALLECIMIENTOS;
                        } else if ("Traslados".equals(seleccionado)) {
                                filtroActual = FiltroPlantel.TRASLADOS;
                        } else {
                                filtroActual = FiltroPlantel.INGRESOS;
                        }

                        configurarTablaSegunFiltro();
                        actualizarAccionesSegunFiltro();
                        cargar(buscador.getText());
                });

                // ============================================================
                // DOBLE CLICK
                // ============================================================

                tabla.setRowFactory(tv -> {

                        TableRow<SistemaDAO.Fila> r = new TableRow<>();

                        r.setOnMouseClicked(e -> {

                                if (e.getClickCount() == 2 && !r.isEmpty()) {

                                        mostrarDetalleRegistroSeleccionado();
                                }
                        });

                        return r;
                });

                tabla.getSelectionModel()
                                .selectedItemProperty()
                                .addListener((obs, anterior, nuevo) -> actualizarAccionesSegunFiltro());

                prepararOverlayPrincipal();
                prepararOverlaySecundario();
                prepararOverlayTerciario();

                configurarTablaSegunFiltro();
                actualizarAccionesSegunFiltro();

                mostrarMensajeVista(
                                "Ingresos generales · muestra los ejemplares vivos actuales del Plantel Permanente.");

                cargar("");
        }

        // ============================================================
        // TABLA DINÁMICA
        // ============================================================

        private void configurarTablaSegunFiltro() {

                tabla.getColumns().clear();

                // La tabla principal usa anchos mínimos reales para evitar que
                // la política CONSTRAINED colapse las columnas secundarias
                // y deje todo el espacio en "Animal".
                tabla.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                if (filtroActual == FiltroPlantel.INGRESOS) {

                        agregarColumnaTabla("Animal", 1, false, 150);
                        agregarColumnaTabla("Identificación", 2, false, 170);
                        agregarColumnaTabla("Especie", 3, false, 150);
                        agregarColumnaTabla("Cantidad", 4, false, 80);
                        agregarColumnaTabla("Ubicación", 5, false, 120);
                        agregarColumnaTabla("Habitáculo", 6, false, 140);
                        agregarColumnaTabla("Ingreso", 7, true, 110);
                        agregarColumnaTabla("Estado", 8, false, 90);

                } else if (filtroActual == FiltroPlantel.FALLECIMIENTOS) {

                        agregarColumnaTabla("Fecha", 1, true, 110);
                        agregarColumnaTabla("Animal", 2, false, 170);
                        agregarColumnaTabla("Cantidad", 3, false, 90);
                        agregarColumnaTabla("Sexo", 4, false, 110);
                        agregarColumnaTabla("Observaciones", 5, false, 320);

                } else {

                        agregarColumnaTabla("Fecha", 1, true, 110);
                        agregarColumnaTabla("Animal", 2, false, 170);
                        agregarColumnaTabla("Cantidad", 3, false, 90);
                        agregarColumnaTabla("Destino", 4, false, 240);
                        agregarColumnaTabla("Observaciones", 5, false, 320);
                }
        }

        private void agregarColumnaTabla(
                        String titulo,
                        int indice,
                        boolean fecha,
                        double ancho) {

                TableColumn<SistemaDAO.Fila, String> columna = new TableColumn<>(titulo);

                columna.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                x.getValue().c().size() > indice
                                                                ? x.getValue().c().get(indice)
                                                                : ""));

                columna.setMinWidth(ancho);
                columna.setPrefWidth(ancho);

                if (fecha) {

                        columna.setCellFactory(
                                        col -> new TableCell<>() {

                                                @Override
                                                protected void updateItem(
                                                                String item,
                                                                boolean empty) {

                                                        super.updateItem(item, empty);

                                                        if (empty || item == null || item.isBlank()) {
                                                                setText("");
                                                                return;
                                                        }

                                                        try {
                                                                setText(
                                                                                LocalDate.parse(item)
                                                                                                .format(java.time.format.DateTimeFormatter
                                                                                                                .ofPattern("dd/MM/yyyy")));
                                                        } catch (Exception ex) {
                                                                setText(item);
                                                        }
                                                }
                                        });
                }

                if ("Observaciones".equalsIgnoreCase(titulo)) {
                        columna.setPrefWidth(320);
                }

                tabla.getColumns().add(columna);
        }

        // ============================================================
        // ACCIONES SEGÚN FILTRO
        // ============================================================

        private void actualizarAccionesSegunFiltro() {

                boolean ingresos = filtroActual == FiltroPlantel.INGRESOS;

                SistemaDAO.Fila seleccionado = tabla.getSelectionModel().getSelectedItem();

                modificar.setDisable(!ingresos || seleccionado == null);

                if (ingresos) {

                        agregar.setDisable(false);

                        mostrarMensajeVista(
                                        "Ingresos generales · ejemplares vivos actuales del Plantel Permanente.");

                } else if (filtroActual == FiltroPlantel.FALLECIMIENTOS) {

                        agregar.setDisable(true);

                        mostrarMensajeVista(
                                        "Fallecimientos · únicamente ocurridos sobre ejemplares del Plantel Permanente.");

                } else {

                        agregar.setDisable(true);

                        mostrarMensajeVista(
                                        "Traslados · únicamente salidas fuera de la Reserva desde el Plantel Permanente.");
                }
        }

        // ============================================================
        // DETALLE DE REGISTRO HISTÓRICO
        // ============================================================

        private void mostrarDetalleRegistroSeleccionado() {

                SistemaDAO.Fila f = tabla.getSelectionModel().getSelectedItem();

                if (f == null) {

                        mostrarInfo(
                                        "Seleccioná un registro",
                                        "Elegí un registro de la tabla.");
                        return;
                }

                boolean ingresos = filtroActual == FiltroPlantel.INGRESOS;

                boolean fallecimientos = filtroActual == FiltroPlantel.FALLECIMIENTOS;

                String tituloTexto;

                if (ingresos) {
                        tituloTexto = "DETALLE DEL EJEMPLAR";
                } else if (fallecimientos) {
                        tituloTexto = "DETALLE DEL FALLECIMIENTO";
                } else {
                        tituloTexto = "DETALLE DEL TRASLADO";
                }

                Label titulo = tituloModal(
                                tituloTexto);

                Label subtitulo = subtituloModal(
                                ingresos
                                                ? "Información del ejemplar registrado en el Plantel Permanente."
                                                : "Información registrada en el historial del Plantel Permanente.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                3,
                                                titulo,
                                                subtitulo),
                                () -> cerrarModal());

                GridPane datos = new GridPane();

                datos.setHgap(18);
                datos.setVgap(14);

                datos.setPadding(
                                new Insets(4, 0, 4, 0));

                datos.setMaxWidth(
                                Double.MAX_VALUE);

                ColumnConstraints columna1 = new ColumnConstraints();

                columna1.setPercentWidth(33);

                ColumnConstraints columna2 = new ColumnConstraints();

                columna2.setPercentWidth(33);

                ColumnConstraints columna3 = new ColumnConstraints();

                columna3.setPercentWidth(34);

                datos.getColumnConstraints().addAll(
                                columna1,
                                columna2,
                                columna3);

                // ============================================================
                // INGRESOS GENERALES / PLANTEL PERMANENTE
                // ============================================================

                if (ingresos) {

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        0,
                                        "ANIMAL",
                                        f.c().size() > 1
                                                        ? f.c().get(1)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        1,
                                        0,
                                        "IDENTIFICACIÓN",
                                        f.c().size() > 2
                                                        ? f.c().get(2)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        2,
                                        0,
                                        "ESPECIE",
                                        f.c().size() > 3
                                                        ? f.c().get(3)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        1,
                                        "CANTIDAD",
                                        f.c().size() > 4
                                                        ? f.c().get(4)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        1,
                                        1,
                                        "UBICACIÓN",
                                        f.c().size() > 5
                                                        ? f.c().get(5)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        2,
                                        1,
                                        "HABITÁCULO",
                                        f.c().size() > 6
                                                        ? f.c().get(6)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        2,
                                        "INGRESO",
                                        f.c().size() > 7
                                                        ? f.c().get(7)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        1,
                                        2,
                                        "ESTADO",
                                        f.c().size() > 8
                                                        ? f.c().get(8)
                                                        : "");
                }

                // ============================================================
                // FALLECIMIENTOS
                // ============================================================

                else if (fallecimientos) {

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        0,
                                        "FECHA",
                                        f.c().size() > 1
                                                        ? f.c().get(1)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        1,
                                        0,
                                        "ANIMAL",
                                        f.c().size() > 2
                                                        ? f.c().get(2)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        2,
                                        0,
                                        "CANTIDAD",
                                        f.c().size() > 3
                                                        ? f.c().get(3)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        1,
                                        "SEXO",
                                        f.c().size() > 4
                                                        ? f.c().get(4)
                                                        : "");

                        agregarObservacionesDetalle(
                                        datos,
                                        1,
                                        1,
                                        f.c().size() > 5
                                                        ? f.c().get(5)
                                                        : "");
                }

                // ============================================================
                // TRASLADOS
                // ============================================================

                else {

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        0,
                                        "FECHA",
                                        f.c().size() > 1
                                                        ? f.c().get(1)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        1,
                                        0,
                                        "ANIMAL",
                                        f.c().size() > 2
                                                        ? f.c().get(2)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        2,
                                        0,
                                        "CANTIDAD",
                                        f.c().size() > 3
                                                        ? f.c().get(3)
                                                        : "");

                        agregarDatoDetalle(
                                        datos,
                                        0,
                                        1,
                                        "DESTINO",
                                        f.c().size() > 4
                                                        ? f.c().get(4)
                                                        : "");

                        agregarObservacionesDetalle(
                                        datos,
                                        1,
                                        1,
                                        f.c().size() > 5
                                                        ? f.c().get(5)
                                                        : "");
                }

                // ============================================================
                // BOTONES
                // ============================================================

                Button cerrar = Estilos.botonSec(
                                "CERRAR");

                Button modificar = Estilos.boton(
                                "✎ MODIFICAR");

                HBox acciones = new HBox(
                                10,
                                cerrar,
                                new Region(),
                                modificar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                // ============================================================
                // MODAL
                // ============================================================

                VBox content = new VBox(
                                18);

                content.setPadding(
                                new Insets(24));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                VBox.setVgrow(
                                datos,
                                Priority.ALWAYS);

                content.getChildren().addAll(
                                header,
                                datos,
                                acciones);

                cerrar.setOnAction(
                                e -> cerrarModal());

                modificar.setOnAction(
                                e -> {

                                        cerrarModal();

                                        editarSeleccionado();
                                });

                mostrarModal(
                                content,
                                850,
                                520);
        }

        private void agregarDatoDetalle(
                        GridPane grid,
                        int columna,
                        int fila,
                        String etiqueta,
                        String valor) {

                Label titulo = new Label(
                                etiqueta);

                titulo.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" + Estilos.GRIS + ";");

                Label contenido = new Label(
                                valor == null || valor.isBlank()
                                                ? "Sin información"
                                                : valor);

                contenido.setWrapText(true);

                contenido.setMaxWidth(
                                Double.MAX_VALUE);

                contenido.setStyle(
                                "-fx-background-color:#EAF4ED;" +
                                                "-fx-background-radius:10;" +
                                                "-fx-padding:10 12 10 12;" +
                                                "-fx-text-fill:#183B2A;" +
                                                "-fx-font-size:14px;");

                VBox bloque = new VBox(
                                5,
                                titulo,
                                contenido);

                bloque.setFillWidth(true);

                bloque.setMaxWidth(
                                Double.MAX_VALUE);

                grid.add(
                                bloque,
                                columna,
                                fila);

                GridPane.setHgrow(
                                bloque,
                                Priority.ALWAYS);
        }

        private void agregarObservacionesDetalle(
                        GridPane grid,
                        int columna,
                        int fila,
                        String valor) {

                Label titulo = new Label(
                                "OBSERVACIONES");

                titulo.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" + Estilos.GRIS + ";");

                Label contenido = new Label(
                                valor == null || valor.isBlank()
                                                ? "Sin observaciones"
                                                : valor);

                contenido.setWrapText(true);

                contenido.setMaxWidth(
                                Double.MAX_VALUE);

                contenido.setStyle(
                                "-fx-background-color:#EAF4ED;" +
                                                "-fx-background-radius:10;" +
                                                "-fx-padding:12px;" +
                                                "-fx-text-fill:#183B2A;" +
                                                "-fx-font-size:14px;");

                ScrollPane scroll = new ScrollPane(
                                contenido);

                scroll.setFitToWidth(true);

                scroll.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);

                scroll.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);

                scroll.setPrefHeight(
                                110);

                scroll.setMaxHeight(
                                150);

                scroll.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;");

                VBox bloque = new VBox(
                                5,
                                titulo,
                                scroll);

                bloque.setFillWidth(true);

                bloque.setMaxWidth(
                                Double.MAX_VALUE);

                grid.add(
                                bloque,
                                columna,
                                fila);

                GridPane.setColumnSpan(
                                bloque,
                                2);

                GridPane.setHgrow(
                                bloque,
                                Priority.ALWAYS);
        }

        // ============================================================
        // LAYOUT DE LOS OVERLAYS
        // ============================================================

        @Override
        protected void layoutChildren() {

                super.layoutChildren();

                double width = getWidth();
                double height = getHeight();

                if (width <= 0) {
                        width = getLayoutBounds().getWidth();
                }

                if (height <= 0) {
                        height = getLayoutBounds().getHeight();
                }

                modalLayer.resizeRelocate(
                                0,
                                0,
                                width,
                                height);

                secondaryModalLayer.resizeRelocate(
                                0,
                                0,
                                width,
                                height);

                tertiaryModalLayer.resizeRelocate(
                                0,
                                0,
                                width,
                                height);

                if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                if (secondaryModalLayer.isVisible()) {
                        secondaryModalLayer.toFront();
                }

                if (tertiaryModalLayer.isVisible()) {
                        tertiaryModalLayer.toFront();
                }
        }

        // ============================================================
        // PREPARAR OVERLAY PRINCIPAL
        // ============================================================

        private void prepararOverlayPrincipal() {

                modalLayer.setManaged(false);
                modalLayer.setVisible(false);
                modalLayer.setPickOnBounds(true);

                modalLayer.setStyle(
                                "-fx-background-color:rgba(0,0,0,0.55);");

                getChildren().add(
                                modalLayer);
        }

        // ============================================================
        // PREPARAR OVERLAY SECUNDARIO
        // ============================================================

        private void prepararOverlaySecundario() {

                secondaryModalLayer.setManaged(false);
                secondaryModalLayer.setVisible(false);
                secondaryModalLayer.setPickOnBounds(true);

                secondaryModalLayer.setStyle(
                                "-fx-background-color:rgba(0,0,0,0.65);");

                getChildren().add(
                                secondaryModalLayer);
        }

        // ============================================================
        // PREPARAR OVERLAY TERCIARIO
        // ============================================================

        private void prepararOverlayTerciario() {

                tertiaryModalLayer.setManaged(false);
                tertiaryModalLayer.setVisible(false);
                tertiaryModalLayer.setPickOnBounds(true);

                tertiaryModalLayer.setStyle(
                                "-fx-background-color:rgba(0,0,0,0.70);");

                getChildren().add(
                                tertiaryModalLayer);
        }

        // ============================================================
        // MOSTRAR MODAL PRINCIPAL
        // ============================================================

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
                                Math.max(
                                                320,
                                                availableWidth - 60));

                double height = Math.min(
                                preferredHeight,
                                Math.max(
                                                260,
                                                availableHeight - 60));

                content.setMinSize(0, 0);
                content.setPrefSize(width, height);
                content.setMaxSize(width, height);

                modalLayer
                                .getChildren()
                                .setAll(content);

                StackPane.setAlignment(
                                content,
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

                aplicarLayoutModal(
                                modalLayer);
        }

        // ============================================================
        // MOSTRAR MODAL SECUNDARIO
        // ============================================================

        private void mostrarModalSecundario(
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
                                Math.max(
                                                320,
                                                availableWidth - 80));

                double height = Math.min(
                                preferredHeight,
                                Math.max(
                                                240,
                                                availableHeight - 80));

                content.setMinSize(0, 0);
                content.setPrefSize(width, height);
                content.setMaxSize(width, height);

                secondaryModalLayer
                                .getChildren()
                                .setAll(content);

                StackPane.setAlignment(
                                content,
                                Pos.CENTER);

                secondaryModalLayer.setManaged(false);
                secondaryModalLayer.setVisible(true);

                requestLayout();

                secondaryModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                secondaryModalLayer.toFront();

                aplicarLayoutModal(
                                secondaryModalLayer);
        }

        // ============================================================
        // MOSTRAR MODAL TERCIARIO
        // ============================================================

        private void mostrarModalTerciario(
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
                                Math.max(
                                                300,
                                                availableWidth - 100));

                double height = Math.min(
                                preferredHeight,
                                Math.max(
                                                220,
                                                availableHeight - 100));

                content.setMinSize(0, 0);
                content.setPrefSize(width, height);
                content.setMaxSize(width, height);

                tertiaryModalLayer
                                .getChildren()
                                .setAll(content);

                StackPane.setAlignment(
                                content,
                                Pos.CENTER);

                tertiaryModalLayer.setManaged(false);
                tertiaryModalLayer.setVisible(true);

                requestLayout();

                tertiaryModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                tertiaryModalLayer.toFront();

                aplicarLayoutModal(
                                tertiaryModalLayer);
        }

        // ============================================================
        // FORZAR LAYOUT
        // ============================================================

        private void aplicarLayoutModal(
                        StackPane layer) {

                applyCss();
                layout();

                layer.applyCss();
                layer.layout();
        }

        // ============================================================
        // CERRAR MODAL PRINCIPAL
        // ============================================================

        private void cerrarModal() {

                cerrarModalTerciario();
                cerrarModalSecundario();

                modalLayer
                                .getChildren()
                                .clear();

                modalLayer.setVisible(false);
                modalLayer.setManaged(false);

                requestLayout();
        }

        // ============================================================
        // CERRAR MODAL SECUNDARIO
        // ============================================================

        private void cerrarModalSecundario() {

                secondaryModalLayer
                                .getChildren()
                                .clear();

                secondaryModalLayer.setVisible(false);
                secondaryModalLayer.setManaged(false);

                if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                requestLayout();
        }

        // ============================================================
        // CERRAR MODAL TERCIARIO
        // ============================================================

        private void cerrarModalTerciario() {

                tertiaryModalLayer
                                .getChildren()
                                .clear();

                tertiaryModalLayer.setVisible(false);
                tertiaryModalLayer.setManaged(false);

                if (secondaryModalLayer.isVisible()) {
                        secondaryModalLayer.toFront();

                } else if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                requestLayout();
        }

        // ============================================================
        // BASE VISUAL DEL MODAL
        // ============================================================

        private VBox modalBase(
                        Node header,
                        Node contenido,
                        Node acciones) {

                VBox box = new VBox(12);

                box.setPadding(
                                new Insets(20));

                box.setFillWidth(true);

                box.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                box.getChildren().addAll(
                                header,
                                contenido,
                                acciones);

                return box;
        }

        // ============================================================
        // CABECERA MODAL
        // ============================================================

        private StackPane cabeceraModal(
                        Node titulo,
                        Runnable cerrar) {

                Label close = new Label("✕");

                close.setPrefSize(
                                34,
                                34);

                close.setMinSize(
                                34,
                                34);

                close.setMaxSize(
                                34,
                                34);

                close.setAlignment(
                                Pos.CENTER);

                close.setStyle(
                                "-fx-text-fill:" + Estilos.GRIS + ";" +
                                                "-fx-font-size:16px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-cursor:hand;");

                close.setOnMouseClicked(
                                e -> cerrar.run());

                StackPane header = new StackPane();

                header.setMaxWidth(
                                Double.MAX_VALUE);

                header.getChildren().addAll(
                                titulo,
                                close);

                StackPane.setAlignment(
                                titulo,
                                Pos.CENTER_LEFT);

                StackPane.setAlignment(
                                close,
                                Pos.TOP_RIGHT);

                return header;
        }

        // ============================================================
        // CRUD
        // ============================================================

        @Override
        protected List<SistemaDAO.Fila> consultar(
                        String q) {

                if (filtroActual == FiltroPlantel.FALLECIMIENTOS) {
                        return SistemaDAO.fallecimientosPlantel(q);
                }

                if (filtroActual == FiltroPlantel.TRASLADOS) {
                        return SistemaDAO.trasladosPlantel(q);
                }

                return SistemaDAO.plantel(q);
        }

        @Override
        protected List<SistemaDAO.Fila> consultarInactivos(
                        String q) {

                return SistemaDAO.plantelInactivos(q);
        }

        @Override
        protected void activarRegistro(
                        int id) {

                SistemaDAO.activar(
                                "plantel_permanente",
                                "id_plantel",
                                id);
        }

        @Override
        protected void agregarNuevo() {

                form(
                                0,
                                null);
        }

        @Override
        protected void editarSeleccionado() {

                if (filtroActual != FiltroPlantel.INGRESOS) {

                        mostrarInfo(
                                        "Registro histórico",
                                        "Los fallecimientos y traslados son registros históricos. " +
                                                        "Para modificar un ejemplar activo seleccioná \"Ingresos generales\".");
                        return;
                }

                SistemaDAO.Fila f = tabla
                                .getSelectionModel()
                                .getSelectedItem();

                if (f != null) {

                        form(
                                        f.id(),
                                        f);
                }
        }

        @Override
        protected void desactivar(
                        int id) {

                mostrarInfo(
                                "Salida del Plantel",
                                "Un ejemplar no se elimina manualmente del Plantel Permanente. " +
                                                "Para mantener el Acta General correcta, registrá un fallecimiento " +
                                                "o un traslado fuera de la Reserva. Los movimientos internos " +
                                                "(habitáculo ↔ campo abierto) conservan al ejemplar en el Plantel.");
        }

        // ============================================================
        // FORMULARIO PRINCIPAL DE PLANTEL
        // ============================================================

        private void form(
                        int id,
                        SistemaDAO.Fila f) {

                ComboBox<SistemaDAO.Opcion> animal = Estilos.combo();

                animal.getItems().setAll(
                                SistemaDAO.animalesOpciones());

                Button nuevoAnimal = Estilos.botonSec(
                                "＋ Animal");

                HBox animalBox = new HBox(
                                8,
                                animal,
                                nuevoAnimal);

                HBox.setHgrow(
                                animal,
                                Priority.ALWAYS);

                nuevoAnimal.setOnAction(
                                e -> crearAnimal(animal));

                ComboBox<String> ubic = Estilos.combo();

                ubic.getItems().setAll(
                                "Habitáculo",
                                "Campo abierto");

                ComboBox<SistemaDAO.Opcion> hab = Estilos.combo();
                Label disponibilidadHabitaculo = new Label();
                disponibilidadHabitaculo.setWrapText(true);
                disponibilidadHabitaculo.setStyle("-fx-font-size:10px;-fx-text-fill:" + Estilos.GRIS + ";");
                VBox habitaculoBox = new VBox(4, hab, disponibilidadHabitaculo);
                int plantelAExcluirDisponibilidad = id > 0 ? id : 0;
                List<SistemaDAO.Opcion> habitaculosDisponibles = SistemaDAO.habitaculoOpciones().stream()
                                .filter(op -> habitaculoDisponible(op.id(), plantelAExcluirDisponibilidad))
                                .toList();
                hab.getItems().setAll(habitaculosDisponibles);
                hab.setPromptText(habitaculosDisponibles.isEmpty()
                                ? "No hay habitáculos disponibles"
                                : "Seleccioná un habitáculo");
                Runnable actualizarDisponibilidadHab = () -> {
                        SistemaDAO.Opcion elegido = hab.getValue();
                        if (elegido == null) {
                                disponibilidadHabitaculo.setText(habitaculosDisponibles.isEmpty()
                                                ? "No hay habitáculos disponibles con capacidad libre."
                                                : "Elegí un habitáculo para ver su capacidad disponible.");
                                return;
                        }
                        int capacidad = SistemaDAO.capacidadHabitaculo(elegido.id());
                        int ocupados = SistemaDAO.ocupacionHabitaculo(elegido.id(), plantelAExcluirDisponibilidad);
                        int libres = Math.max(0, capacidad - ocupados);
                        disponibilidadHabitaculo.setText("Capacidad: " + capacidad + " · Ocupados: " + ocupados
                                        + " · Disponibles: " + libres + " lugar(es).");
                };
                hab.valueProperty().addListener((o, anterior, nuevo) -> actualizarDisponibilidadHab.run());

                TextField cant = Estilos.entero(
                                "Cantidad en plantel",
                                6);

                TextArea obs = Estilos.area(
                                "Observaciones");

                obs.setPrefRowCount(
                                4);

                DatePicker fecha = Estilos.fecha();

                // ========================================================
                // DATOS INDIVIDUALES
                //
                // SOLO SE UTILIZAN CUANDO CANTIDAD == 1
                // ========================================================

                TextField peso = new TextField();
                peso.setPromptText("Peso");

                TextField edad = new TextField();
                edad.setPromptText("Edad");

                TextField estado = new TextField();
                estado.setPromptText("Estado");

                // ========================================================
                // NUEVA IDENTIFICACIÓN
                //
                // NO SE SELECCIONA UNA IDENTIFICACIÓN EXISTENTE.
                // SE CREA UNA NUEVA.
                // ========================================================

                ComboBox<String> tipoIdentificacion = Estilos.combo();

                tipoIdentificacion.getItems().setAll(
                                "Chip",
                                "Anillo",
                                "Caravana",
                                "Otro");

                tipoIdentificacion.setPromptText(
                                "Tipo de identificación");

                TextField numeroIdentificacion = new TextField();

                numeroIdentificacion.setPromptText(
                                "Número de identificación");

                HBox identificacionBox = new HBox(
                                8,
                                tipoIdentificacion,
                                numeroIdentificacion);

                HBox.setHgrow(
                                tipoIdentificacion,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                numeroIdentificacion,
                                Priority.ALWAYS);

                // ========================================================
                // CARGAR EDICIÓN
                // ========================================================

                if (f != null) {

                        animal.getItems()
                                        .stream()
                                        .filter(
                                                        o -> o.texto().equals(
                                                                        f.c().get(1)))
                                        .findFirst()
                                        .ifPresent(
                                                        animal::setValue);

                        cant.setText(
                                        f.c().get(4));

                        ubic.setValue(
                                        f.c().get(5).isBlank()
                                                        ? "Campo abierto"
                                                        : f.c().get(5));

                        hab.getItems()
                                        .stream()
                                        .filter(
                                                        o -> o.texto().equals(
                                                                        f.c().get(6)))
                                        .findFirst()
                                        .ifPresent(
                                                        hab::setValue);

                        if (!f.c().get(7).isBlank()) {

                                try {

                                        fecha.setValue(
                                                        LocalDate.parse(
                                                                        f.c().get(7)));

                                } catch (Exception ignored) {
                                }
                        }

                        String observacionesExistentes = SistemaDAO.scalar(
                                        "SELECT COALESCE(observaciones,'') " +
                                                        "FROM plantel_permanente " +
                                                        "WHERE id_plantel=" +
                                                        id);

                        if (observacionesExistentes != null &&
                                        !"0".equals(observacionesExistentes)) {

                                obs.setText(
                                                observacionesExistentes);

                                obs.positionCaret(
                                                obs.getText().length());
                        }

                } else {

                        fecha.setValue(
                                        LocalDate.now());
                }

                // ========================================================
                // FORMULARIO
                // ========================================================
                GridPane g = grid();

                addForm(
                                g,
                                0,
                                "Animal",
                                animalBox,
                                true);

                addForm(
                                g,
                                1,
                                "Cantidad",
                                cant,
                                true);

                addForm(
                                g,
                                2,
                                "Ubicación",
                                ubic,
                                true);

                int filaHabitaculo = 3;

Label etiquetaHabitaculo = addForm(g, filaHabitaculo, "Habitáculo", habitaculoBox, false);
                Runnable validarHabitaculoPlantel = aplicarValidacionObligatoria(hab, () -> "Habitáculo".equals(ubic.getValue()));

                /*
                 * HABITÁCULO:
                 * La fila se muestra cuando la ubicación elegida es "Habitáculo".
                 */
                Runnable actualizarHabitaculo = () -> {
                        boolean mostrar = "Habitáculo".equals(ubic.getValue());
                        etiquetaHabitaculo.setText(mostrar ? "* Habitáculo" : "Habitáculo");
                        validarHabitaculoPlantel.run();
                        hab.setVisible(mostrar);
                        hab.setManaged(mostrar);
                        disponibilidadHabitaculo.setVisible(mostrar);
                        disponibilidadHabitaculo.setManaged(mostrar);
                        habitaculoBox.setVisible(mostrar);
                        habitaculoBox.setManaged(mostrar);
                        if (!mostrar) hab.setValue(null);
                        actualizarDisponibilidadHab.run();
                        ocultarFila(g, filaHabitaculo, mostrar);
                };

                ubic.valueProperty().addListener(
                                (o, anterior, nuevo) -> actualizarHabitaculo.run());

                cant.textProperty().addListener(
                                (o, anterior, nuevo) -> actualizarHabitaculo.run());

                actualizarHabitaculo.run();

                int filaIndividual = 4;

                // --------------------------------------------------------
                // PESO
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Peso",
                                peso,
                                false);

                int filaPeso = filaIndividual;
                filaIndividual++;

                // --------------------------------------------------------
                // EDAD
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Edad",
                                edad,
                                false);

                int filaEdad = filaIndividual;
                filaIndividual++;

                // --------------------------------------------------------
                // ESTADO
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Estado",
                                estado,
                                false);

                int filaEstado = filaIndividual;
                filaIndividual++;

                // --------------------------------------------------------
                // IDENTIFICACIÓN
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Identificación",
                                identificacionBox,
                                false);

                int filaIdentificacion = filaIndividual;
                filaIndividual++;

                // --------------------------------------------------------
                // FECHA
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Fecha de ingreso",
                                fecha,
                                true);

                int filaFecha = filaIndividual;
                filaIndividual++;

                // --------------------------------------------------------
                // OBSERVACIONES
                // --------------------------------------------------------

                addForm(
                                g,
                                filaIndividual,
                                "Observaciones",
                                obs,
                                false);

                int filaObservaciones = filaIndividual;

                // ========================================================
                // VISIBILIDAD DE DATOS INDIVIDUALES
                // ========================================================

                Runnable actualizarCamposIndividuales = () -> {

                        boolean mostrar = id == 0
                                        && validar(cant.getText())
                                        && Integer.parseInt(
                                                        cant.getText()) == 1;

                        peso.setVisible(mostrar);
                        peso.setManaged(mostrar);

                        edad.setVisible(mostrar);
                        edad.setManaged(mostrar);

                        estado.setVisible(mostrar);
                        estado.setManaged(mostrar);

                        identificacionBox.setVisible(mostrar);
                        identificacionBox.setManaged(mostrar);

                        fecha.setVisible(true);
                        fecha.setManaged(true);

                        obs.setVisible(true);
                        obs.setManaged(true);

                        ocultarFila(
                                        g,
                                        filaPeso,
                                        mostrar);

                        ocultarFila(
                                        g,
                                        filaEdad,
                                        mostrar);

                        ocultarFila(
                                        g,
                                        filaEstado,
                                        mostrar);

                        ocultarFila(
                                        g,
                                        filaIdentificacion,
                                        mostrar);
                };

                // ========================================================
                // CAMBIO DE CANTIDAD
                // ========================================================

                cant.textProperty().addListener(
                                (o, anterior, nuevo) -> {

                                        actualizarCamposIndividuales.run();
                                });

                actualizarCamposIndividuales.run();

                // ========================================================
                // TÍTULO
                // ========================================================

                Label titulo = tituloModal(
                                id == 0
                                                ? "AGREGAR EJEMPLAR AL PLANTEL"
                                                : "MODIFICAR REGISTRO DEL PLANTEL");

                Label ayuda = subtituloModal(
                                id == 0
                                                ? "Completá los datos del ejemplar que permanecerá en la Reserva."
                                                : "Modificá los datos del registro sin perder su historial.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModal());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                id == 0
                                                ? "＋ AGREGAR AL PLANTEL"
                                                : "GUARDAR CAMBIOS");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                VBox.setVgrow(g, Priority.ALWAYS);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                content.getChildren().addAll(
                                header,
                                g,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModal());

                // ========================================================
                // GUARDAR
                // ========================================================

                guardar.setOnAction(e -> {

                        try {

                                if (animal.getValue() == null ||
                                                fecha.getValue() == null ||
                                                ubic.getValue() == null ||
                                                !validar(cant.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Completá los campos obligatorios.");
                                }

                                int n = Integer.parseInt(
                                                cant.getText());

                                if (n <= 0) {

                                        throw new IllegalArgumentException(
                                                        "La cantidad debe ser mayor que cero.");
                                }

                                if ("Habitáculo".equals(
                                                ubic.getValue()) &&
                                                hab.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Elegí el habitáculo.");
                                }

                                if ("Habitáculo".equals(ubic.getValue()) && hab.getValue() != null) {
                                        int capacidad = SistemaDAO.capacidadHabitaculo(hab.getValue().id());
                                        int ocupados = SistemaDAO.ocupacionHabitaculo(hab.getValue().id(), id);
                                        if (capacidad <= 0 || ocupados + n > capacidad) {
                                                throw new IllegalArgumentException("El habitáculo tiene "
                                                                + Math.max(0, capacidad - ocupados)
                                                                + " lugar(es) disponible(s); la cantidad supera su capacidad.");
                                        }
                                }

                                // ====================================================
                                // VALIDACIONES DEL EJEMPLAR INDIVIDUAL
                                // ====================================================

                                if (id == 0 && n == 1) {

                                        // ------------------------------------------------
                                        // PESO
                                        // ------------------------------------------------

                                        if (peso.getText() != null &&
                                                        !peso.getText().trim().isBlank()) {

                                                try {

                                                        double pesoValor = Double.parseDouble(
                                                                        peso.getText()
                                                                                        .trim()
                                                                                        .replace(",", "."));

                                                        if (pesoValor < 0) {

                                                                throw new IllegalArgumentException(
                                                                                "El peso no puede ser negativo.");
                                                        }

                                                } catch (NumberFormatException ex) {

                                                        throw new IllegalArgumentException(
                                                                        "El peso ingresado no es válido.");
                                                }
                                        }

                                        // ------------------------------------------------
                                        // IDENTIFICACIÓN
                                        // ------------------------------------------------

                                        if (tipoIdentificacion.getValue() != null &&
                                                        !tipoIdentificacion.getValue()
                                                                        .isBlank()) {

                                                if (numeroIdentificacion.getText() == null ||
                                                                numeroIdentificacion.getText()
                                                                                .trim()
                                                                                .isBlank()) {

                                                        throw new IllegalArgumentException(
                                                                        "Ingresá el número de identificación.");
                                                }
                                        }

                                        if (numeroIdentificacion.getText() != null &&
                                                        !numeroIdentificacion.getText()
                                                                        .trim()
                                                                        .isBlank()) {

                                                if (tipoIdentificacion.getValue() == null ||
                                                                tipoIdentificacion.getValue()
                                                                                .isBlank()) {

                                                        throw new IllegalArgumentException(
                                                                        "Seleccioná el tipo de identificación.");
                                                }
                                        }
                                }

                                String mensaje = id == 0
                                                ? "¿Estás segura de que querés agregar este ejemplar al Plantel Permanente?"
                                                : "¿Querés guardar los cambios?";

                                mostrarConfirmacion(
                                                id == 0
                                                                ? "Agregar al Plantel"
                                                                : "Guardar cambios",
                                                mensaje,
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                // ==================================================
                                                                // NUEVO FLUJO INDIVIDUAL
                                                                //
                                                                // SOLAMENTE:
                                                                // ALTA + CANTIDAD = 1
                                                                // ==================================================

                                                                if (id == 0 && n == 1) {

                                                                        Integer idIdentificacion = null;

                                                                        // ------------------------------------------
                                                                        // CREAR IDENTIFICACIÓN NUEVA
                                                                        // ------------------------------------------

                                                                        if (tipoIdentificacion.getValue() != null &&
                                                                                        !tipoIdentificacion.getValue()
                                                                                                        .isBlank()) {

                                                                                idIdentificacion = SistemaDAO
                                                                                                .guardarIdentificacion(
                                                                                                                0,
                                                                                                                animal.getValue()
                                                                                                                                .id(),
                                                                                                                tipoIdentificacion
                                                                                                                                .getValue(),
                                                                                                                numeroIdentificacion
                                                                                                                                .getText()
                                                                                                                                .trim(),
                                                                                                                fecha.getValue()
                                                                                                                                .toString(),
                                                                                                                "");

                                                                        }

                                                                        // ------------------------------------------
                                                                        // GUARDAR EJEMPLAR INDIVIDUAL
                                                                        // ------------------------------------------

                                                                        SistemaDAO.guardarPlantelIndividual(
                                                                                        animal.getValue().id(),
                                                                                        ubic.getValue(),
                                                                                        hab.getValue() == null
                                                                                                        ? null
                                                                                                        : hab.getValue().id(),
                                                                                        fecha.getValue().toString(),
                                                                                        obs.getText().trim(),
                                                                                        peso.getText().trim(),
                                                                                        edad.getText().trim(),
                                                                                        estado.getText().trim(),
                                                                                        idIdentificacion);

                                                                } else {

                                                                        // ==================================================
                                                                        // LÓGICA ORIGINAL
                                                                        //
                                                                        // NO SE MODIFICA.
                                                                        // ==================================================

                                                                        SistemaDAO.guardarPlantel(
                                                                                        id,
                                                                                        animal.getValue().id(),
                                                                                        n,
                                                                                        ubic.getValue(),
                                                                                        hab.getValue() == null
                                                                                                        ? null
                                                                                                        : hab.getValue().id(),
                                                                                        fecha.getValue().toString(),
                                                                                        obs.getText().trim());
                                                                }

                                                                cargar(
                                                                                buscador.getText());

                                                                cerrarModal();

                                                        } catch (Exception ex) {

                                                                mostrarError(
                                                                                "No se pudo guardar",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarError(
                                                "No se puede guardar",
                                                ex.getMessage());
                        }
                });

                mostrarModal(
                                content,
                                850,
                                650);
        }

        // ============================================================
        // TRASLADO
        // ============================================================

        private void trasladarSeleccionado() {

                SistemaDAO.Fila f = tabla.getSelectionModel()
                                .getSelectedItem();

                if (f == null) {

                        mostrarInfo(
                                        "Seleccioná un ejemplar",
                                        "Elegí un registro del Plantel Permanente para registrar su traslado.");

                        return;
                }

                if (f.c().get(1).startsWith("⚠")) {

                        mostrarInfo(
                                        "Registro pendiente de asociación",
                                        "Este registro histórico no tiene un animal asociado.");

                        return;
                }

                int plantel = f.id();

                int actual;

                try {

                        actual = SistemaDAO.cantidadPlantel(
                                        plantel);

                } catch (Exception ex) {

                        mostrarError(
                                        "No se pudo consultar la existencia",
                                        ex.getMessage());

                        return;
                }

                ComboBox<String> sexo = Estilos.combo();

                sexo.getItems().setAll(
                                "Macho",
                                "Hembra",
                                "Indeterminado");

                TextField cant = Estilos.entero(
                                "Cantidad a trasladar",
                                6);

                TextField centro = Estilos.campo(
                                "Centro / institución de destino");

                TextField localidad = Estilos.campo(
                                "Localidad / provincia");

                ComboBox<String> tipoDestino = Estilos.combo();

                ComboBox<String> ubicacionDestino = Estilos.combo();

                ubicacionDestino.getItems().setAll(
                                "Campo abierto",
                                "Habitáculo");

                ubicacionDestino.setValue(
                                "Campo abierto");

                ComboBox<String> habitaculoDestino = Estilos.combo();

                List<SistemaDAO.Fila> habitaculos = SistemaDAO.habitaculos("");

                for (SistemaDAO.Fila h : habitaculos) {

                        if (h.c().size() > 1) {
                                int idHab = h.id();
                                if (habitaculoDisponible(idHab, plantel)) {
                                        habitaculoDestino.getItems().add(h.c().get(1));
                                }
                        }
                }
                habitaculoDestino.setPromptText(habitaculoDestino.getItems().isEmpty()
                                ? "No hay habitáculos disponibles"
                                : "Seleccioná un habitáculo");
                Label disponibilidadDestinoHab = new Label();
                disponibilidadDestinoHab.setWrapText(true);
                disponibilidadDestinoHab.setStyle("-fx-font-size:10px;-fx-text-fill:" + Estilos.GRIS + ";");
                Runnable actualizarDisponibilidadDestinoHab = () -> {
                        String seleccionado = habitaculoDestino.getValue();
                        if (seleccionado == null || seleccionado.isBlank()) {
                                disponibilidadDestinoHab.setText(habitaculoDestino.getItems().isEmpty()
                                                ? "No hay habitáculos disponibles con capacidad libre."
                                                : "Elegí un habitáculo para ver sus lugares disponibles.");
                                return;
                        }
                        int idHab = habitaculos.stream()
                                        .filter(h -> h.c().size() > 1 && seleccionado.equals(h.c().get(1)))
                                        .map(SistemaDAO.Fila::id).findFirst().orElse(0);
                        int capacidad = SistemaDAO.capacidadHabitaculo(idHab);
                        int ocupados = SistemaDAO.ocupacionHabitaculo(idHab, plantel);
                        disponibilidadDestinoHab.setText("Capacidad: " + capacidad + " · Ocupados: " + ocupados
                                        + " · Disponibles: " + Math.max(0, capacidad - ocupados) + " lugar(es).");
                };
                habitaculoDestino.valueProperty().addListener((o, anterior, nuevo) -> actualizarDisponibilidadDestinoHab.run());
                actualizarDisponibilidadDestinoHab.run();
                VBox selectorHabitaculoDestino = new VBox(4, habitaculoDestino, disponibilidadDestinoHab);

                centro.setVisible(false);
                centro.setManaged(false);

                localidad.setVisible(false);
                localidad.setManaged(false);

                ubicacionDestino.setVisible(true);
                ubicacionDestino.setManaged(true);

                habitaculoDestino.setVisible(false);
                habitaculoDestino.setManaged(false);

                // ========================================================
                // CAMBIO DE TIPO DE DESTINO
                // ========================================================

                tipoDestino.valueProperty().addListener(
                                (o, anterior, nuevo) -> {

                                        boolean externo = "Fuera de la reserva".equalsIgnoreCase(nuevo);

                                        centro.setVisible(externo);
                                        centro.setManaged(externo);

                                        localidad.setVisible(externo);
                                        localidad.setManaged(externo);

                                        ubicacionDestino.setVisible(!externo);
                                        ubicacionDestino.setManaged(!externo);

                                        boolean esHabitaculo = !externo &&
                                                        "Habitáculo".equalsIgnoreCase(
                                                                        ubicacionDestino.getValue());

                                        habitaculoDestino.setVisible(
                                                        esHabitaculo);

                                        habitaculoDestino.setManaged(
                                                        esHabitaculo);
                                        selectorHabitaculoDestino.setVisible(esHabitaculo);
                                        selectorHabitaculoDestino.setManaged(esHabitaculo);
                                        disponibilidadDestinoHab.setVisible(esHabitaculo);
                                        disponibilidadDestinoHab.setManaged(esHabitaculo);
                                });

                tipoDestino.getItems().setAll(
                                "Dentro de la reserva",
                                "Fuera de la reserva");

                tipoDestino.setValue(
                                "Dentro de la reserva");

                // ========================================================
                // CAMBIO DE UBICACIÓN DENTRO DE LA RESERVA
                // ========================================================

                ubicacionDestino.valueProperty().addListener(
                                (o, anterior, nuevo) -> {

                                        boolean esHabitaculo = "Habitáculo".equalsIgnoreCase(nuevo);

                                        boolean dentroReserva = "Dentro de la reserva".equalsIgnoreCase(
                                                        tipoDestino.getValue());

                                        boolean mostrar = esHabitaculo &&
                                                        dentroReserva;

                                        habitaculoDestino.setVisible(
                                                        mostrar);

                                        habitaculoDestino.setManaged(
                                                        mostrar);
                                        selectorHabitaculoDestino.setVisible(mostrar);
                                        selectorHabitaculoDestino.setManaged(mostrar);
                                        disponibilidadDestinoHab.setVisible(mostrar);
                                        disponibilidadDestinoHab.setManaged(mostrar);

                                        if (!mostrar) {
                                                habitaculoDestino.setValue(null);
                                        }
                                });

                DatePicker fecha = Estilos.fecha();

                fecha.setValue(
                                LocalDate.now());

                TextArea obs = Estilos.area(
                                "Motivo / observaciones");

                obs.setPrefRowCount(
                                4);

                Label disponibilidad = new Label(
                                "Existencia actual en Plantel: "
                                                + actual);

                disponibilidad.setStyle(
                                "-fx-text-fill:" +
                                                "#18492A" +
                                                ";" +
                                                "-fx-font-weight:bold;");

                GridPane g = grid();

                Label animalNombre = new Label(f.c().get(1));
                animalNombre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");
                Label animalEspecie = new Label("Especie: " + (f.c().size() > 3 ? f.c().get(3) : "Sin especie"));
                animalEspecie.setStyle("-fx-font-size:11px;-fx-text-fill:" + Estilos.GRIS + ";");
                addForm(g, 0, "Animal", new VBox(3, animalNombre, animalEspecie), false);
                addForm(g, 1, "Cantidad", cant, true);
                addForm(g, 2, "Sexo", sexo, false);
                addForm(g, 3, "Fecha", fecha, true);
                addForm(g, 4, "Tipo de traslado", tipoDestino, true);
                Label centroLabel = addForm(g, 5, "Centro / institución", centro, false);
                Label localidadLabel = addForm(g, 6, "Localidad / provincia", localidad, false);
                addForm(g, 7, "Motivo / observaciones", obs, false);
                addForm(g, 8, "Ubicación destino", ubicacionDestino, true);
                Label habitaculoDestinoLabel = addForm(g, 9, "Habitáculo destino", selectorHabitaculoDestino, false);
                g.add(disponibilidad, 1, 10);

                Runnable validarCentro = aplicarValidacionObligatoria(centro, () ->
                                "Fuera de la reserva".equalsIgnoreCase(tipoDestino.getValue()));
                Runnable validarLocalidad = aplicarValidacionObligatoria(localidad, () ->
                                "Fuera de la reserva".equalsIgnoreCase(tipoDestino.getValue()));
                Runnable validarHabitaculo = aplicarValidacionObligatoria(habitaculoDestino, () ->
                                "Dentro de la reserva".equalsIgnoreCase(tipoDestino.getValue())
                                                && "Habitáculo".equalsIgnoreCase(ubicacionDestino.getValue()));

                Runnable actualizarFilasDestino = () -> {
                        boolean externo = "Fuera de la reserva".equalsIgnoreCase(tipoDestino.getValue());
                        ocultarFila(g, 5, externo);
                        ocultarFila(g, 6, externo);
                        ocultarFila(g, 8, !externo);
                        boolean mostrarHabitaculo = !externo
                                        && "Habitáculo".equalsIgnoreCase(ubicacionDestino.getValue());
                        ocultarFila(g, 9, mostrarHabitaculo);
                        habitaculoDestinoLabel.setText(mostrarHabitaculo ? "* Habitáculo destino" : "Habitáculo destino");
                        centroLabel.setText(externo ? "* Centro / institución" : "Centro / institución");
                        localidadLabel.setText(externo ? "* Localidad / provincia" : "Localidad / provincia");
                        validarCentro.run();
                        validarLocalidad.run();
                        validarHabitaculo.run();
                };
                tipoDestino.valueProperty().addListener((o, anterior, nuevo) -> actualizarFilasDestino.run());
                ubicacionDestino.valueProperty().addListener((o, anterior, nuevo) -> actualizarFilasDestino.run());
                actualizarFilasDestino.run();

                Label titulo = tituloModal(
                                "TRASLADAR ANIMAL");

                Label ayuda = subtituloModal(
                                "Registrá el traslado y el destino del ejemplar. El movimiento quedará conservado en el historial.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModal());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                "↔ REGISTRAR TRASLADO");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                ScrollPane formularioScroll = new ScrollPane(g);
                formularioScroll.setFitToWidth(true);
                formularioScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                formularioScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                formularioScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
                VBox.setVgrow(formularioScroll, Priority.ALWAYS);

                BorderPane content = new BorderPane();
                content.setTop(header);
                content.setCenter(formularioScroll);
                content.setBottom(acciones);
                BorderPane.setMargin(header, new Insets(20, 22, 8, 22));
                BorderPane.setMargin(formularioScroll, new Insets(8, 22, 8, 22));
                BorderPane.setMargin(acciones, new Insets(8, 22, 20, 22));
                content.setStyle("-fx-background-color:white;"
                                + "-fx-background-radius:18;"
                                + "-fx-border-color:" + Estilos.BORDE + ";"
                                + "-fx-border-radius:18;"
                                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.22),24,0,0,8);");

                cancelar.setOnAction(e -> cerrarModal());

                guardar.setOnAction(e -> {

                        try {

                                boolean fueraReserva = "Fuera de la reserva".equalsIgnoreCase(
                                                tipoDestino.getValue());

                                if (fecha.getValue() == null ||
                                                !validar(cant.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Completá cantidad y fecha.");
                                }

                                if (fueraReserva) {

                                        if (!validar(
                                                        centro.getText(),
                                                        localidad.getText())) {

                                                throw new IllegalArgumentException(
                                                                "Completá el centro/institución y la localidad/provincia de destino.");
                                        }

                                } else {

                                        if (ubicacionDestino.getValue() == null) {

                                                throw new IllegalArgumentException(
                                                                "Elegí la ubicación de destino.");
                                        }

                                        if ("Habitáculo".equalsIgnoreCase(
                                                        ubicacionDestino.getValue()) &&
                                                        habitaculoDestino.getValue() == null) {

                                                throw new IllegalArgumentException(
                                                                "Elegí el habitáculo de destino.");
                                        }
                                }

                                int n = Integer.parseInt(
                                                cant.getText());

                                if (n <= 0 ||
                                                n > actual) {

                                        throw new IllegalArgumentException(
                                                        "La cantidad debe ser mayor que cero y no superar la existencia actual ("
                                                                        + actual
                                                                        + ").");
                                }

                                if (!fueraReserva && "Habitáculo".equalsIgnoreCase(ubicacionDestino.getValue())) {
                                        int idHab = habitaculos.stream()
                                                        .filter(h -> h.c().size() > 1
                                                                        && habitaculoDestino.getValue().equals(h.c().get(1)))
                                                        .map(SistemaDAO.Fila::id).findFirst().orElse(0);
                                        int capacidad = SistemaDAO.capacidadHabitaculo(idHab);
                                        int ocupados = SistemaDAO.ocupacionHabitaculo(idHab, plantel);
                                        int disponibles = Math.max(0, capacidad - ocupados);
                                        if (n > disponibles) {
                                                throw new IllegalArgumentException("El habitáculo solo tiene "
                                                                + disponibles + " lugar(es) disponible(s).");
                                        }
                                }

                                String destino;

                                if (fueraReserva) {

                                        destino = centro.getText().trim()
                                                        + " · "
                                                        + localidad.getText().trim();

                                } else {

                                        destino = ubicacionDestino.getValue();

                                        if ("Habitáculo".equalsIgnoreCase(
                                                        ubicacionDestino.getValue()) &&
                                                        habitaculoDestino.getValue() != null) {

                                                destino += " · "
                                                                + habitaculoDestino.getValue();
                                        }
                                }

                                final String destinoFinal = destino;

                                mostrarConfirmacion(
                                                "Confirmar traslado",
                                                "¿Querés registrar el traslado de "
                                                                + n
                                                                + " ejemplar(es) a "
                                                                + destinoFinal
                                                                + "?",
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                /*
                                                                 * IMPORTANTE:
                                                                 *
                                                                 * Se conserva la llamada existente.
                                                                 *
                                                                 * La actualización de cantidad y el
                                                                 * movimiento histórico se resuelven
                                                                 * actualmente en SistemaDAO.
                                                                 *
                                                                 * El siguiente paso es modificar
                                                                 * trasladarPlantel() en el DAO para
                                                                 * que también interprete correctamente
                                                                 * Campo abierto / Habitáculo y conserve
                                                                 * el movimiento.
                                                                 */
                                                                Integer idHabitaculoDestinoSeleccionado = null;
                                                                if (!fueraReserva && "Habitáculo".equalsIgnoreCase(ubicacionDestino.getValue())) {
                                                                        String nombreSeleccionado = habitaculoDestino.getValue();
                                                                        for (SistemaDAO.Fila habitaculo : habitaculos) {
                                                                                if (habitaculo.c().size() > 1
                                                                                                && habitaculo.c().get(1).equals(nombreSeleccionado)) {
                                                                                        idHabitaculoDestinoSeleccionado = habitaculo.id();
                                                                                        break;
                                                                                }
                                                                        }
                                                                        if (idHabitaculoDestinoSeleccionado == null) {
                                                                                throw new IllegalArgumentException("No se pudo identificar el habitáculo seleccionado.");
                                                                        }
                                                                }

                                                                SistemaDAO.trasladarPlantel(
                                                                                plantel,
                                                                                n,
                                                                                fecha.getValue().toString(),
                                                                                sexo.getValue(),
                                                                                destinoFinal,
                                                                                obs.getText().trim(),
                                                                                fueraReserva ? null : ubicacionDestino.getValue(),
                                                                                idHabitaculoDestinoSeleccionado);

                                                                cargar(
                                                                                buscador.getText());

                                                                cerrarModal();

                                                                mostrarInfo(
                                                                                "Traslado registrado",
                                                                                "El traslado quedó guardado en Movimientos y la existencia del Plantel fue actualizada.");

                                                        } catch (Exception ex) {

                                                                mostrarError(
                                                                                "No se pudo registrar el traslado",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarError(
                                                "No se puede registrar el traslado",
                                                ex.getMessage());
                        }
                });

                mostrarModal(content, 980, 690);
        }

        // ============================================================
        // NACIMIENTO / FALLECIMIENTO
        // ============================================================

        private void movimientoVital(
                        boolean nacimiento) {

                ComboBox<SistemaDAO.Opcion> p = Estilos.combo();

                p.getItems().setAll(
                                SistemaDAO.plantelOpciones());

                ComboBox<String> sexo = Estilos.combo();

                sexo.getItems().setAll(
                                "Macho",
                                "Hembra",
                                "Indeterminado");

                DatePicker fecha = Estilos.fecha();

                fecha.setValue(
                                LocalDate.now());

                TextField cant = Estilos.entero(
                                nacimiento
                                                ? "Cantidad nacida"
                                                : "Cantidad fallecida",
                                6);

                TextArea obs = Estilos.area(
                                "Observaciones");

                obs.setPrefRowCount(
                                4);

                GridPane g = grid();

                addForm(
                                g,
                                0,
                                "Ejemplar del plantel",
                                p,
                                true);

                addForm(
                                g,
                                1,
                                "Sexo",
                                sexo,
                                true);

                addForm(
                                g,
                                2,
                                "Cantidad",
                                cant,
                                true);

                addForm(
                                g,
                                3,
                                "Fecha",
                                fecha,
                                true);

                addForm(
                                g,
                                4,
                                "Observaciones",
                                obs,
                                false);

                Label titulo = tituloModal(
                                nacimiento
                                                ? "REGISTRAR NACIMIENTO"
                                                : "REGISTRAR FALLECIMIENTO");

                Label ayuda = subtituloModal(
                                nacimiento
                                                ? "Registrá el nacimiento asociado a un ejemplar activo del Plantel."
                                                : "Registrá el fallecimiento conservando el movimiento y las observaciones.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModal());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                nacimiento
                                                ? "🐣 REGISTRAR NACIMIENTO"
                                                : "⚠ REGISTRAR FALLECIMIENTO");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                VBox.setVgrow(g, Priority.ALWAYS);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                content.getChildren().addAll(
                                header,
                                g,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModal());

                guardar.setOnAction(e -> {

                        try {

                                if (p.getValue() == null ||
                                                sexo.getValue() == null ||
                                                fecha.getValue() == null ||
                                                !validar(cant.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Elegí el ejemplar, sexo, cantidad y fecha.");
                                }

                                int n = Integer.parseInt(
                                                cant.getText());

                                if (n <= 0) {

                                        throw new IllegalArgumentException(
                                                        "La cantidad debe ser mayor que cero.");
                                }

                                SistemaDAO.Fila pf = SistemaDAO.plantel("")
                                                .stream()
                                                .filter(
                                                                r -> r.id() == p.getValue().id())
                                                .findFirst()
                                                .orElse(null);

                                if (pf == null) {

                                        throw new IllegalArgumentException(
                                                        "No se encontró el ejemplar seleccionado.");
                                }

                                int actual = Integer.parseInt(
                                                pf.c().get(4));

                                if (!nacimiento &&
                                                n > actual) {

                                        throw new IllegalArgumentException(
                                                        "La cantidad no puede superar la existencia actual ("
                                                                        + actual
                                                                        + ").");
                                }

                                mostrarConfirmacion(
                                                nacimiento
                                                                ? "Confirmar nacimiento"
                                                                : "Confirmar fallecimiento",
                                                nacimiento
                                                                ? "¿Estás segura de que querés registrar este nacimiento?"
                                                                : "¿Estás segura de que querés registrar este fallecimiento?",
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                int animal = SistemaDAO.animalPorPlantel(
                                                                                p.getValue().id());

                                                                if (nacimiento) {

                                                                        SistemaDAO.registrarNacimiento(
                                                                                        p.getValue().id(),
                                                                                        animal,
                                                                                        n,
                                                                                        sexo.getValue(),
                                                                                        fecha.getValue().toString(),
                                                                                        obs.getText().trim());

                                                                } else {

                                                                        SistemaDAO.registrarFallecimiento(
                                                                                        p.getValue().id(),
                                                                                        animal,
                                                                                        n,
                                                                                        sexo.getValue(),
                                                                                        fecha.getValue().toString(),
                                                                                        obs.getText().trim());
                                                                }

                                                                cargar(
                                                                                buscador.getText());

                                                                cerrarModal();

                                                                mostrarInfo(
                                                                                "Registro guardado",
                                                                                "El movimiento quedó conservado en el historial.");

                                                        } catch (Exception ex) {

                                                                mostrarError(
                                                                                "No se pudo registrar el movimiento",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarError(
                                                "No se puede registrar el movimiento",
                                                ex.getMessage());
                        }
                });

                mostrarModal(
                                content,
                                760,
                                500);
        }

        // ============================================================
        // IDENTIFICACIONES
        // ============================================================

        private void identificaciones() {

                TableView<SistemaDAO.Fila> t = new TableView<>();

                Estilos.tablaCompacta(t);

                t.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                t.setPrefHeight(330);

                String[] cs = {
                                "Animal",
                                "Tipo",
                                "Número",
                                "Fecha",
                                "Observaciones",
                                "Estado"
                };

                int[] indices = { 6, 1, 2, 3, 4, 5 };

                for (int i = 0; i < cs.length; i++) {

                        int ix = indices[i];

                        TableColumn<SistemaDAO.Fila, String> c = new TableColumn<>(
                                        cs[i]);

                        c.setCellValueFactory(
                                        x -> new SimpleStringProperty(
                                                        x.getValue().c().size() > ix
                                                                        ? x.getValue().c().get(ix)
                                                                        : ""));

                        t.getColumns().add(c);
                }

                t.setItems(
                                FXCollections.observableArrayList(
                                                SistemaDAO.identificacionesTodas()));

                Button modificar = Estilos.botonSec(
                                "✎ MODIFICAR");

                Button cerrar = Estilos.boton(
                                "CERRAR");

                // Anchos fijos para que el texto de los botones del encabezado
                // nunca quede recortado por el ancho por defecto del estilo.
                modificar.setMinWidth(150);
                modificar.setPrefWidth(150);
                cerrar.setMinWidth(100);
                cerrar.setPrefWidth(100);

                modificar.setDisable(true);

                t.getSelectionModel().selectedItemProperty().addListener(
                                (obs, anterior, nuevo) -> modificar.setDisable(nuevo == null));

                Runnable modificarSeleccionada = () -> {

                        SistemaDAO.Fila f = t.getSelectionModel().getSelectedItem();

                        if (f == null) {
                                return;
                        }

                        int animal = SistemaDAO.animalPorIdentificacion(f.id());

                        if (animal <= 0) {
                                mostrarError(
                                                "No se puede modificar",
                                                "No se encontró el animal asociado a la identificación.");
                                return;
                        }

                        // identForm conserva su estructura actual de índices.
                        SistemaDAO.Fila paraEditar = new SistemaDAO.Fila(
                                        f.id(),
                                        List.of(
                                                        String.valueOf(f.id()),
                                                        f.c().size() > 1 ? f.c().get(1) : "",
                                                        f.c().size() > 2 ? f.c().get(2) : "",
                                                        f.c().size() > 3 ? f.c().get(3) : "",
                                                        f.c().size() > 4 ? f.c().get(4) : "",
                                                        f.c().size() > 5 ? f.c().get(5) : "",
                                                        f.c().size() > 6 ? f.c().get(6) : ""));

                        identForm(animal, f.id(), paraEditar, t);
                };

                modificar.setOnAction(e -> modificarSeleccionada.run());

                t.setRowFactory(tv -> {

                        TableRow<SistemaDAO.Fila> r = new TableRow<>();

                        r.setOnMouseClicked(e -> {

                                if (e.getClickCount() == 2 && !r.isEmpty()) {
                                        modificarSeleccionada.run();
                                }
                        });

                        return r;
                });

                Label titulo = tituloModal(
                                "IDENTIFICACIONES");

                Label ayuda = subtituloModal(
                                "Identificaciones de ejemplares que permanecen actualmente en el Plantel Permanente. Seleccioná una y presioná MODIFICAR, o hacé doble click para editarla.");

                VBox tituloHeader = new VBox(
                                2,
                                titulo,
                                ayuda);

                StackPane header = cabeceraModal(
                                tituloHeader,
                                this::cerrarModal);

                VBox content = new VBox(12);

                content.setPadding(new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                Region espacioAcciones = new Region();
                HBox.setHgrow(espacioAcciones, Priority.ALWAYS);
                HBox accionesFooter = new HBox(
                                10,
                                cerrar,
                                espacioAcciones,
                                modificar);

                content.getChildren().addAll(
                                header,
                                t,
                                accionesFooter);

                VBox.setVgrow(t, Priority.ALWAYS);

                cerrar.setOnAction(e -> cerrarModal());

                mostrarModal(content, 1050, 620);
        }

        // ============================================================
        // FORMULARIO DE IDENTIFICACIÓN
        // ============================================================

        private void identForm(
                        int animal,
                        int id,
                        SistemaDAO.Fila f,
                        TableView<SistemaDAO.Fila> t) {

                ComboBox<String> tipo = Estilos.combo();

                tipo.getItems().setAll(
                                "Chip",
                                "Caravana",
                                "Anillo",
                                "Otro");

                TextField num = Estilos.campo(
                                "Número / código");

                TextField obs = Estilos.campo(
                                "Observaciones");

                DatePicker fecha = Estilos.fecha();

                if (f != null) {

                        tipo.setValue(
                                        f.c().get(1));

                        num.setText(
                                        f.c().get(2));

                        if (!f.c().get(3).isBlank()) {

                                try {

                                        fecha.setValue(
                                                        LocalDate.parse(
                                                                        f.c().get(3)));

                                } catch (Exception ignored) {
                                }
                        }

                        obs.setText(
                                        f.c().get(4));
                }

                GridPane g = grid();

                String nombreAnimal = f != null && f.c().size() > 6
                                ? f.c().get(6)
                                : "Animal asociado";

                Label animalValor = new Label(nombreAnimal);
                animalValor.setStyle(
                                "-fx-font-size:14px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" + "#18492A" + ";" +
                                                "-fx-padding:8 10 8 10;" +
                                                "-fx-background-color:" + "#E8F3EB" + ";" +
                                                "-fx-background-radius:8;");

                addForm(
                                g,
                                0,
                                "Animal",
                                animalValor,
                                false);

                addForm(
                                g,
                                1,
                                "Tipo",
                                tipo,
                                true);

                addForm(
                                g,
                                2,
                                "Número",
                                num,
                                true);

                addForm(
                                g,
                                3,
                                "Fecha asignación",
                                fecha,
                                false);

                addForm(
                                g,
                                4,
                                "Observaciones",
                                obs,
                                false);

                Label titulo = tituloModal(
                                id == 0
                                                ? "NUEVA IDENTIFICACIÓN"
                                                : "MODIFICAR IDENTIFICACIÓN");

                Label ayuda = subtituloModal(
                                "La identificación queda asociada al animal y será visible en Plantel y Movimientos.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModalSecundario());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                id == 0
                                                ? "＋ GUARDAR IDENTIFICACIÓN"
                                                : "GUARDAR CAMBIOS");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                content.getChildren().addAll(
                                header,
                                g,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModalSecundario());

                guardar.setOnAction(e -> {

                        try {

                                if (tipo.getValue() == null ||
                                                !validar(num.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Elegí el tipo e indicá el número.");
                                }

                                mostrarConfirmacionSecundaria(
                                                id == 0
                                                                ? "Agregar identificación"
                                                                : "Guardar cambios",
                                                id == 0
                                                                ? "¿Querés agregar esta identificación al animal?"
                                                                : "¿Querés guardar los cambios de esta identificación?",
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                SistemaDAO.guardarIdentificacion(
                                                                                id,
                                                                                animal,
                                                                                tipo.getValue(),
                                                                                num.getText().trim(),
                                                                                fecha.getValue() == null
                                                                                                ? ""
                                                                                                : fecha.getValue()
                                                                                                                .toString(),
                                                                                obs.getText().trim());

                                                                t.setItems(
                                                                                FXCollections.observableArrayList(
                                                                                                SistemaDAO.identificacionesTodas()));

                                                                // Actualiza también la tabla principal del Plantel para
                                                                // que
                                                                // el cambio de identificación se vea inmediatamente.
                                                                cargar(buscador.getText());

                                                                cerrarModalTerciario();

                                                                cerrarModalSecundario();

                                                        } catch (Exception ex) {

                                                                mostrarErrorSecundario(
                                                                                "No se pudo guardar",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarErrorSecundario(
                                                "No se puede guardar",
                                                ex.getMessage());
                        }
                });

                mostrarModalSecundario(
                                content,
                                680,
                                430);
        }

        // ============================================================
        // CREAR ANIMAL
        // ============================================================

        private void crearAnimal(
                        ComboBox<SistemaDAO.Opcion> animal) {

                ComboBox<SistemaDAO.Opcion> esp = Estilos.combo();

                esp.getItems().setAll(
                                SistemaDAO.especiesOpciones());

                Button ne = Estilos.botonSec(
                                "＋ Especie");

                HBox eb = new HBox(
                                8,
                                esp,
                                ne);

                HBox.setHgrow(
                                esp,
                                Priority.ALWAYS);

                ne.setOnAction(
                                e -> crearEspecie(esp));

                TextField v = Estilos.campo(
                                "Nombre común");

                TextField c = Estilos.campo(
                                "Nombre científico");

                TextField o = Estilos.campo(
                                "Origen");

                GridPane g = grid();

                addForm(
                                g,
                                0,
                                "Especie",
                                eb,
                                true);

                addForm(
                                g,
                                1,
                                "Nombre vulgar",
                                v,
                                true);

                addForm(
                                g,
                                2,
                                "Nombre científico",
                                c,
                                true);

                addForm(
                                g,
                                3,
                                "Origen",
                                o,
                                false);

                Label titulo = tituloModal(
                                "CREAR ANIMAL");

                Label ayuda = subtituloModal(
                                "Agregá un nuevo animal al catálogo para poder seleccionarlo inmediatamente.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModalSecundario());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                "＋ GUARDAR ANIMAL");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                content.getChildren().addAll(
                                header,
                                g,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModalSecundario());

                guardar.setOnAction(e -> {

                        try {

                                if (esp.getValue() == null ||
                                                !validar(v.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Elegí una especie y completá el nombre.");
                                }

                                mostrarConfirmacionSecundaria(
                                                "Agregar animal",
                                                "¿Querés agregar este animal?",
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                int nuevoId = SistemaDAO.guardarAnimal(
                                                                                0,
                                                                                esp.getValue().id(),
                                                                                v.getText().trim(),
                                                                                c.getText().trim(),
                                                                                o.getText().trim());

                                                                animal.getItems().setAll(
                                                                                SistemaDAO.animalesOpciones());

                                                                animal.getItems()
                                                                                .stream()
                                                                                .filter(
                                                                                                q -> q.id() == nuevoId)
                                                                                .findFirst()
                                                                                .ifPresent(
                                                                                                animal::setValue);

                                                                cerrarModalTerciario();

                                                                cerrarModalSecundario();

                                                        } catch (Exception ex) {

                                                                mostrarErrorSecundario(
                                                                                "No se pudo crear el animal",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarErrorSecundario(
                                                "No se puede crear el animal",
                                                ex.getMessage());
                        }
                });

                mostrarModalSecundario(
                                content,
                                760,
                                470);
        }

        // ============================================================
        // CREAR ESPECIE
        // ============================================================

        private void crearEspecie(
                        ComboBox<SistemaDAO.Opcion> esp) {

                TextField n = Estilos.campo(
                                "Nombre de la especie");

                GridPane g = grid();

                addForm(
                                g,
                                0,
                                "Nombre",
                                n,
                                true);

                Label titulo = tituloModal(
                                "CREAR ESPECIE");

                Label ayuda = subtituloModal(
                                "Agregá una nueva especie al catálogo.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModalTerciario());

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button guardar = Estilos.boton(
                                "＋ GUARDAR ESPECIE");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                guardar);

                HBox.setHgrow(
                                acciones.getChildren().get(1),
                                Priority.ALWAYS);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                content.getChildren().addAll(
                                header,
                                g,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModalTerciario());

                guardar.setOnAction(e -> {

                        try {

                                if (!validar(n.getText())) {

                                        throw new IllegalArgumentException(
                                                        "Completá el nombre.");
                                }

                                mostrarConfirmacionTerciaria(
                                                "Agregar especie",
                                                "¿Querés agregar esta especie?",
                                                confirmado -> {

                                                        if (!confirmado) {
                                                                return;
                                                        }

                                                        try {

                                                                int id = SistemaDAO.guardarEspecie(
                                                                                0,
                                                                                n.getText().trim());

                                                                esp.getItems().setAll(
                                                                                SistemaDAO.especiesOpciones());

                                                                esp.getItems()
                                                                                .stream()
                                                                                .filter(
                                                                                                q -> q.id() == id)
                                                                                .findFirst()
                                                                                .ifPresent(
                                                                                                esp::setValue);

                                                                cerrarModalTerciario();

                                                        } catch (Exception ex) {

                                                                mostrarErrorTerciario(
                                                                                "No se pudo guardar",
                                                                                ex.getMessage());
                                                        }
                                                });

                        } catch (Exception ex) {

                                mostrarErrorTerciario(
                                                "No se puede guardar",
                                                ex.getMessage());
                        }
                });

                mostrarModalTerciario(
                                content,
                                520,
                                300);
        }

        // ============================================================
        // CONFIRMACIÓN PRINCIPAL
        // ============================================================

        private void mostrarConfirmacion(
                        String titulo,
                        String mensaje,
                        Consumer<Boolean> respuesta) {

                mostrarMensajeModal(
                                modalLayer,
                                titulo,
                                mensaje,
                                "CONFIRMAR",
                                "CANCELAR",
                                respuesta,
                                false);
        }

        // ============================================================
        // CONFIRMACIÓN SECUNDARIA
        // ============================================================

        private void mostrarConfirmacionSecundaria(
                        String titulo,
                        String mensaje,
                        Consumer<Boolean> respuesta) {

                mostrarMensajeModal(
                                secondaryModalLayer,
                                titulo,
                                mensaje,
                                "CONFIRMAR",
                                "CANCELAR",
                                respuesta,
                                true);
        }

        // ============================================================
        // CONFIRMACIÓN TERCIARIA
        // ============================================================

        private void mostrarConfirmacionTerciaria(
                        String titulo,
                        String mensaje,
                        Consumer<Boolean> respuesta) {

                mostrarMensajeModal(
                                tertiaryModalLayer,
                                titulo,
                                mensaje,
                                "CONFIRMAR",
                                "CANCELAR",
                                respuesta,
                                true);
        }

        // ============================================================
        // INFO
        // ============================================================

        private void mostrarInfo(
                        String titulo,
                        String mensaje) {

                StackPane layer = secondaryModalLayer.isVisible()
                                ? secondaryModalLayer
                                : modalLayer;

                mostrarMensajeModal(
                                layer,
                                titulo,
                                mensaje,
                                "ACEPTAR",
                                null,
                                ok -> {
                                },
                                true);
        }

        // ============================================================
        // ERROR
        // ============================================================

        private void mostrarError(
                        String titulo,
                        String mensaje) {

                StackPane layer = secondaryModalLayer.isVisible()
                                ? secondaryModalLayer
                                : modalLayer;

                mostrarMensajeModal(
                                layer,
                                titulo,
                                mensaje == null
                                                ? "Ocurrió un error inesperado."
                                                : mensaje,
                                "CERRAR",
                                null,
                                ok -> {
                                },
                                true);
        }

        // ============================================================
        // ERROR SECUNDARIO
        // ============================================================

        private void mostrarErrorSecundario(
                        String titulo,
                        String mensaje) {

                mostrarMensajeModal(
                                secondaryModalLayer,
                                titulo,
                                mensaje == null
                                                ? "Ocurrió un error inesperado."
                                                : mensaje,
                                "CERRAR",
                                null,
                                ok -> {
                                },
                                true);
        }

        // ============================================================
        // ERROR TERCIARIO
        // ============================================================

        private void mostrarErrorTerciario(
                        String titulo,
                        String mensaje) {

                mostrarMensajeModal(
                                tertiaryModalLayer,
                                titulo,
                                mensaje == null
                                                ? "Ocurrió un error inesperado."
                                                : mensaje,
                                "CERRAR",
                                null,
                                ok -> {
                                },
                                true);
        }

        // ============================================================
        // MENSAJE MODAL
        // ============================================================

        private void mostrarMensajeModal(
                        StackPane layer,
                        String titulo,
                        String mensaje,
                        String textoAceptar,
                        String textoCancelar,
                        Consumer<Boolean> respuesta,
                        boolean secundario) {

                Label title = tituloModal(
                                titulo);

                Label message = subtituloModal(
                                mensaje);

                message.setWrapText(true);

                Button aceptar = Estilos.boton(
                                textoAceptar);

                Button cancelar = textoCancelar == null
                                ? null
                                : Estilos.botonSec(
                                                textoCancelar);

                HBox acciones;

                if (cancelar != null) {

                        acciones = new HBox(
                                        10,
                                        cancelar,
                                        new Region(),
                                        aceptar);

                        HBox.setHgrow(
                                        acciones.getChildren().get(1),
                                        Priority.ALWAYS);

                } else {

                        acciones = new HBox(
                                        aceptar);
                }

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = new VBox(
                                14);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.25),24,0,0,8" +
                                                ");");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                title,
                                                message),
                                () -> {

                                        cerrarMensajeLayer(
                                                        layer);

                                        respuesta.accept(
                                                        false);
                                });

                content.getChildren().addAll(
                                header,
                                acciones);

                aceptar.setOnAction(e -> {

                        cerrarMensajeLayer(
                                        layer);

                        respuesta.accept(
                                        true);
                });

                if (cancelar != null) {

                        cancelar.setOnAction(e -> {

                                cerrarMensajeLayer(
                                                layer);

                                respuesta.accept(
                                                false);
                        });
                }

                if (layer == modalLayer) {

                        double width = Math.min(
                                        540,
                                        Math.max(
                                                        360,
                                                        getWidth() - 100));

                        double height = Math.min(
                                        300,
                                        Math.max(
                                                        220,
                                                        getHeight() - 100));

                        layer
                                        .getChildren()
                                        .setAll(content);

                        StackPane.setAlignment(
                                        content,
                                        Pos.CENTER);

                        content.setMinSize(
                                        0,
                                        0);

                        content.setPrefSize(
                                        width,
                                        height);

                        content.setMaxSize(
                                        width,
                                        height);

                        layer.setManaged(false);
                        layer.setVisible(true);

                        layer.toFront();

                        aplicarLayoutModal(
                                        layer);

                } else if (layer == secondaryModalLayer) {

                        mostrarModalSecundario(
                                        content,
                                        540,
                                        300);

                } else {

                        mostrarModalTerciario(
                                        content,
                                        540,
                                        300);
                }
        }

        // ============================================================
        // CERRAR CAPA DE MENSAJE
        // ============================================================

        private void cerrarMensajeLayer(
                        StackPane layer) {

                if (layer == tertiaryModalLayer) {

                        cerrarModalTerciario();

                } else if (layer == secondaryModalLayer) {

                        cerrarModalSecundario();

                } else {

                        /*
                         * Si el mensaje era el modal principal,
                         * simplemente dejamos la capa limpia.
                         */
                        modalLayer
                                        .getChildren()
                                        .clear();

                        modalLayer.setVisible(false);
                        modalLayer.setManaged(false);

                        requestLayout();
                }
        }

        // ============================================================
        // GRID
        // ============================================================

        private GridPane grid() {

                GridPane g = new GridPane();

                g.setHgap(14);
                g.setVgap(9);

                g.setPadding(
                                new Insets(
                                                8,
                                                0,
                                                0,
                                                0));

                return g;
        }

        // ============================================================
        // AGREGAR CAMPO
        // ============================================================

        private Label addForm(GridPane g, int row, String label, Region node, boolean required) {
                Label l = new Label((required ? "* " : "") + label);
                l.setStyle("-fx-font-weight:bold;-fx-text-fill:" + Estilos.TEXTO + ";-fx-font-size:12px;");
                g.add(l, 0, row);
                g.add(node, 1, row);
                GridPane.setHgrow(node, Priority.ALWAYS);
                node.setMaxWidth(Double.MAX_VALUE);
                if (required) {
                        aplicarValidacionObligatoria(node, () -> true);
                }
                return l;
        }

        private Runnable aplicarValidacionObligatoria(Region campo, BooleanSupplier requerido) {
                Runnable actualizar = () -> {
                        boolean marcar = requerido.getAsBoolean() && !tieneValor(campo);
                        actualizarBordeObligatorio(campo, marcar);
                };
                escucharCambios(campo, actualizar);
                actualizar.run();
                return actualizar;
        }

        private boolean habitaculoDisponible(int idHabitaculo, int plantelAExcluir) {
                try {
                        String observacion = SistemaDAO.scalar("SELECT COALESCE(observaciones,'') FROM habitaculos "
                                        + "WHERE id_habitaculo=" + idHabitaculo + " AND estado='Activo'");
                        if (observacion == null || observacion.toLowerCase(java.util.Locale.ROOT).contains("mantenimiento")) {
                                return false;
                        }
                        int capacidad = SistemaDAO.capacidadHabitaculo(idHabitaculo);
                        int ocupacion = SistemaDAO.ocupacionHabitaculo(idHabitaculo, plantelAExcluir);
                        return capacidad > 0 && ocupacion < capacidad;
                } catch (Exception e) {
                        return false;
                }
        }

        private void actualizarBordeObligatorio(Node nodo, boolean marcar) {
                if (nodo instanceof TextInputControl || nodo instanceof ComboBoxBase<?>) {
                        Region entrada = (Region) nodo;
                        String base = entrada.getProperties().computeIfAbsent(
                                        "estiloBaseObligatorio", k -> entrada.getStyle() == null ? "" : entrada.getStyle()).toString();
                        entrada.setStyle(marcar
                                        ? base + ";-fx-border-color:#C62828;-fx-border-width:2;-fx-border-radius:8;"
                                        : base);
                } else if (nodo instanceof Parent padre) {
                        padre.getChildrenUnmodifiable().forEach(hijo -> actualizarBordeObligatorio(hijo, marcar));
                }
        }

        private boolean tieneValor(Node nodo) {
                if (nodo instanceof TextInputControl texto) {
                        return texto.getText() != null && !texto.getText().trim().isEmpty();
                }
                if (nodo instanceof ComboBoxBase<?> combo) {
                        Object valor = combo.getValue();
                        return valor != null && !valor.toString().trim().isEmpty();
                }
                if (nodo instanceof Parent padre) {
                        boolean encontroEntrada = false;
                        for (Node hijo : padre.getChildrenUnmodifiable()) {
                                if (esEntrada(hijo)) {
                                        encontroEntrada = true;
                                        if (!tieneValor(hijo)) return false;
                                }
                        }
                        return encontroEntrada;
                }
                return true;
        }

        private boolean esEntrada(Node nodo) {
                if (nodo instanceof TextInputControl || nodo instanceof ComboBoxBase<?>) return true;
                if (nodo instanceof Parent padre) {
                        return padre.getChildrenUnmodifiable().stream().anyMatch(this::esEntrada);
                }
                return false;
        }

        private void escucharCambios(Node nodo, Runnable actualizar) {
                if (nodo instanceof TextInputControl texto) {
                        texto.textProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof ComboBoxBase<?> combo) {
                        combo.valueProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof Parent padre) {
                        padre.getChildrenUnmodifiable().forEach(hijo -> escucharCambios(hijo, actualizar));
                }
        }

        private void ocultarFila(
                        GridPane g,
                        int row,
                        boolean mostrar) {

                g.getChildren().forEach(
                                node -> {

                                        Integer fila = GridPane.getRowIndex(node);

                                        if (fila != null &&
                                                        fila == row) {

                                                node.setVisible(mostrar);
                                                node.setManaged(mostrar);
                                        }
                                });
        }

        // ============================================================
        // TÍTULO
        // ============================================================

        private Label tituloModal(
                        String text) {

                Label l = new Label(
                                text);

                l.setStyle(
                                "-fx-font-size:21px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                "#18492A" +
                                                ";");

                return l;
        }

        // ============================================================
        // SUBTÍTULO
        // ============================================================

        private Label subtituloModal(
                        String text) {

                Label l = new Label(
                                text);

                l.setWrapText(true);

                l.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                return l;
        }

        // ============================================================
        // ACTAS DEL PLANTEL PERMANENTE
        // ============================================================

        private void mostrarActaGeneralPlantel() {

                List<SistemaDAO.Fila> filas = SistemaDAO.actaGeneralPlantel("");

                int total = 0;

                for (SistemaDAO.Fila f : filas) {
                        try {
                                total += Integer.parseInt(f.c().get(4));
                        } catch (Exception ignored) {
                        }
                }

                mostrarActaPlantel(
                                "ACTA DE INGRESO GENERAL · PLANTEL PERMANENTE",
                                "Esta acta representa la existencia viva actual del Plantel. " +
                                                "Incluye ejemplares ingresados manualmente, provenientes de actas " +
                                                "individuales y trasladados dentro de la Reserva. Los fallecimientos " +
                                                "y traslados fuera de la Reserva dejan de aparecer automáticamente.",
                                new String[] {
                                                "Animal",
                                                "Identificación",
                                                "Especie",
                                                "Cantidad",
                                                "Ubicación",
                                                "Habitáculo",
                                                "Ingreso"
                                },
                                new int[] {
                                                1, 2, 3, 4, 5, 6, 7
                                },
                                filas,
                                "Ejemplares vivos actualmente en Plantel: " + total,
                                1180,
                                680);
        }

        private void mostrarActaFallecimientosPlantel() {

                List<SistemaDAO.Fila> filas = SistemaDAO.fallecimientosPlantel("");

                mostrarActaPlantel(
                                "ACTA DE FALLECIMIENTOS · PLANTEL PERMANENTE",
                                "Solo aparecen fallecimientos registrados sobre ejemplares que " +
                                                "pertenecían al Plantel Permanente. Los fallecimientos realizados " +
                                                "directamente desde un Acta individual quedan exclusivamente en esa acta.",
                                new String[] {
                                                "Fecha",
                                                "Animal",
                                                "Cantidad",
                                                "Sexo",
                                                "Observaciones"
                                },
                                new int[] {
                                                1, 2, 3, 4, 5
                                },
                                filas,
                                "Fallecimientos registrados: " + filas.size(),
                                1080,
                                640);
        }

        private void mostrarActaTrasladosPlantel() {

                List<SistemaDAO.Fila> filas = SistemaDAO.trasladosPlantel("");

                mostrarActaPlantel(
                                "ACTA DE TRASLADOS · PLANTEL PERMANENTE",
                                "Solo aparecen traslados que salen de la Reserva desde el Plantel " +
                                                "Permanente. Los cambios internos entre habitáculos y campo abierto " +
                                                "no generan una salida del Acta General.",
                                new String[] {
                                                "Fecha",
                                                "Animal",
                                                "Cantidad",
                                                "Destino",
                                                "Observaciones"
                                },
                                new int[] {
                                                1, 2, 3, 4, 5
                                },
                                filas,
                                "Traslados fuera de la Reserva: " + filas.size(),
                                1080,
                                640);
        }

        private void mostrarActaPlantel(
                        String titulo,
                        String ayuda,
                        String[] columnas,
                        int[] indices,
                        List<SistemaDAO.Fila> filas,
                        String resumen,
                        double ancho,
                        double alto) {

                TableView<SistemaDAO.Fila> tablaActa = new TableView<>();

                Estilos.tablaCompacta(tablaActa);

                tablaActa.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                for (int i = 0; i < columnas.length; i++) {

                        final int indice = indices[i];

                        TableColumn<SistemaDAO.Fila, String> columna = new TableColumn<>(columnas[i]);

                        columna.setCellValueFactory(
                                        x -> new javafx.beans.property.SimpleStringProperty(
                                                        x.getValue().c().size() > indice
                                                                        ? x.getValue().c().get(indice)
                                                                        : ""));

                        if ("Fecha".equalsIgnoreCase(columnas[i])
                                        || "Ingreso".equalsIgnoreCase(columnas[i])) {

                                columna.setCellFactory(
                                                col -> new TableCell<>() {

                                                        @Override
                                                        protected void updateItem(
                                                                        String item,
                                                                        boolean empty) {

                                                                super.updateItem(
                                                                                item,
                                                                                empty);

                                                                if (empty ||
                                                                                item == null ||
                                                                                item.isBlank()) {

                                                                        setText("");
                                                                        return;
                                                                }

                                                                try {

                                                                        setText(
                                                                                        LocalDate.parse(item)
                                                                                                        .format(
                                                                                                                        java.time.format.DateTimeFormatter
                                                                                                                                        .ofPattern("dd/MM/yyyy")));

                                                                } catch (Exception ex) {

                                                                        setText(item);
                                                                }
                                                        }
                                                });
                        }

                        tablaActa.getColumns().add(columna);
                }

                tablaActa.getItems().setAll(filas);

                Label contadorActa = subtituloModal(resumen);

                VBox contenidoActa = new VBox(12);

                contenidoActa.setPadding(
                                new Insets(20));

                contenidoActa.setFillWidth(true);

                contenidoActa.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.22),24,0,0,8" +
                                                ");");

                Label tituloLabel = tituloModal(titulo);

                Label ayudaLabel = subtituloModal(ayuda);

                VBox encabezado = new VBox(
                                3,
                                tituloLabel,
                                ayudaLabel,
                                contadorActa);

                Button cerrar = Estilos.botonSec("CERRAR");

                HBox pie = new HBox(cerrar);
                pie.setAlignment(Pos.CENTER_LEFT);

                contenidoActa.getChildren().addAll(
                                encabezado,
                                tablaActa,
                                pie);

                VBox.setVgrow(
                                tablaActa,
                                Priority.ALWAYS);

                cerrar.setOnAction(
                                e -> cerrarModal());

                mostrarModal(
                                contenidoActa,
                                ancho,
                                alto);
        }

        // ============================================================
        // REPORTES
        // ============================================================

        private void abrirSelectorReportes() {

                Label titulo = tituloModal("REPORTES DEL PLANTEL PERMANENTE");

                Label ayuda = subtituloModal(
                                "Seleccioná el informe que querés generar. Cada opción crea un PDF independiente.");

                VBox encabezado = new VBox(4, titulo, ayuda);

                VBox botones = new VBox(12);
                botones.setFillWidth(true);

                Label etiquetaPeriodo = Estilos.etiqueta("Mes del reporte");
                DatePicker fechaPeriodo = Estilos.fecha();
                fechaPeriodo.setValue(LocalDate.now());
                HBox selectorPeriodo = new HBox(12, etiquetaPeriodo, fechaPeriodo);
                selectorPeriodo.setAlignment(Pos.CENTER_LEFT);

                Button resumen = Estilos.boton(
                                "▣  RESUMEN TOTAL");
                Button ingresos = Estilos.botonSec(
                                "🐾  INGRESOS GENERALES / VIVOS");
                Button fallecimientos = Estilos.botonSec(
                                "⚠  FALLECIMIENTOS");
                Button nacimientos = Estilos.botonSec(
                                "🐣  NACIMIENTOS");
                Button traslados = Estilos.botonSec(
                                "↔  TRASLADOS");

                for (Button b : new Button[] {
                                resumen, ingresos, fallecimientos, nacimientos, traslados }) {
                        b.setMaxWidth(Double.MAX_VALUE);
                        b.setPrefHeight(48);
                }

                resumen.setTooltip(new javafx.scene.control.Tooltip(
                                "Resumen ejecutivo del Plantel Permanente: vivos, ingresos, nacimientos, fallecimientos y traslados."));
                ingresos.setTooltip(new javafx.scene.control.Tooltip(
                                "Listado actual de ejemplares vivos y activos que permanecen en el Plantel."));
                fallecimientos.setTooltip(new javafx.scene.control.Tooltip(
                                "Detalle de los fallecimientos registrados en el Plantel Permanente."));
                nacimientos.setTooltip(new javafx.scene.control.Tooltip(
                                "Detalle de los nacimientos registrados en el Plantel Permanente."));
                traslados.setTooltip(new javafx.scene.control.Tooltip(
                                "Detalle de los traslados registrados en el Plantel Permanente."));

                resumen.setOnAction(e -> generarReporteTipo("RESUMEN", fechaPeriodo.getValue()));
                ingresos.setOnAction(e -> generarReporteTipo("INGRESOS", fechaPeriodo.getValue()));
                fallecimientos.setOnAction(e -> generarReporteTipo("FALLECIMIENTO", fechaPeriodo.getValue()));
                nacimientos.setOnAction(e -> generarReporteTipo("NACIMIENTO", fechaPeriodo.getValue()));
                traslados.setOnAction(e -> generarReporteTipo("TRASLADO", fechaPeriodo.getValue()));

                botones.getChildren().addAll(
                                resumen,
                                ingresos,
                                fallecimientos,
                                nacimientos,
                                traslados);

                Button cerrar = Estilos.botonSec("CERRAR");
                HBox pie = new HBox(cerrar);
                pie.setAlignment(Pos.CENTER_LEFT);

                VBox contenido = new VBox(18, encabezado, selectorPeriodo, botones, pie);
                contenido.setPadding(new Insets(24));
                contenido.setFillWidth(true);
                contenido.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.22),24,0,0,8);");

                VBox.setVgrow(botones, Priority.ALWAYS);

                cerrar.setOnAction(e -> cerrarModal());

                mostrarModal(contenido, 680, 500);
        }

        private void generarReporteTipo(String tipo, LocalDate fechaSeleccionada) {

                try {

                        java.io.File f;

                        if (fechaSeleccionada == null) {
                                throw new IllegalArgumentException("Seleccioná una fecha para definir el mes del reporte.");
                        }
                        java.time.YearMonth periodo = java.time.YearMonth.from(fechaSeleccionada);
                        switch (tipo) {
                                case "RESUMEN" -> f = ReportePlantelPDF.generarResumenTotal(periodo);
                                case "INGRESOS" -> f = ReportePlantelPDF.generarIngresosGenerales(periodo);
                                case "FALLECIMIENTO" -> f = ReportePlantelPDF.generarMovimientos("Fallecimiento", periodo);
                                case "NACIMIENTO" -> f = ReportePlantelPDF.generarMovimientos("Nacimiento", periodo);
                                case "TRASLADO" -> f = ReportePlantelPDF.generarMovimientos("Traslado", periodo);
                                default -> throw new IllegalArgumentException("Tipo de reporte no válido.");
                        }

                        if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(f);
                        }

                        cerrarModal();

                } catch (Exception e) {
                        mostrarError(
                                        "No se pudo generar el reporte",
                                        e.getMessage() == null ? "Ocurrió un error al generar el PDF."
                                                        : e.getMessage());
                }
        }
}
