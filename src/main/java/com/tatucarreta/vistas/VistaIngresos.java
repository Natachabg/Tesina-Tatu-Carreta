package com.tatucarreta.vistas;

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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VistaIngresos extends VistaCRUD {

        private final StackPane modalLayer = new StackPane();

        private final StackPane especieModalLayer = new StackPane();

        private final StackPane animalModalLayer = new StackPane();

        private final DateTimeFormatter fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ============================================================
        // ACTA NUEVA - DATOS TEMPORALES
        // ============================================================

        private record DetallePendiente(
                        int idAnimal,
                        int cantidad,
                        String sexo,
                        String edad,
                        String peso,
                        String estadoSanitario,
                        String tipoIdentificacion,
                        String numeroIdentificacion,
                        String observaciones) {
        }

        private String actaPendiente;
        private LocalDate fechaPendiente;
        private String organismoPendiente;
        private String responsablePendiente;
        private String recibePendiente;
        private String procedenciaPendiente;
        private String motivoPendiente;
        private String observacionesPendientes;

        private final List<DetallePendiente> detallesPendientes = new java.util.ArrayList<>();

        /*
         * Índice del animal que se está editando dentro
         * de detallesPendientes.
         *
         * -1 significa que no estamos editando ninguno.
         */
        private int indiceDetallePendienteEditando = -1;

        // ============================================================
        // CONSTRUCTOR
        // ============================================================

        public VistaIngresos() {

                super(
                                "Ingresos / Actas",
                                "Cada acta puede contener varios animales. Doble click para abrir el acta completa y sus detalles.",
                                new String[] {
                                                "Acta",
                                                "Fecha",
                                                "Organismo",
                                                "Responsable",
                                                "Recibe en Reserva",
                                                "Procedencia",
                                                "Motivo",
                                                "Estado"
                                });

                mostrarBotonEliminar(false);
                mostrarBotonInactivos(false);

                agregar.setText("＋ AGREGAR INGRESO");

                prepararOverlay();
                prepararOverlayEspecie();
                prepararAnimalOverlay();

                modificar.setOnAction(e -> {

                        SistemaDAO.Fila f = tabla.getSelectionModel().getSelectedItem();

                        if (f == null) {
                                return;
                        }

                        /*
                         * El ID -1 identifica exclusivamente el Acta General
                         * del Plantel Permanente.
                         */
                        if (f.id() == -1) {

                                mostrarActaGeneralPlantel();

                                return;
                        }

                        /*
                         * Todo ingreso normal continúa funcionando exactamente
                         * como antes.
                         */
                        mostrarActa(
                                        f.id(),
                                        f);
                });
                cargar("");
        }

        // ============================================================
        // LAYOUT DE LOS OVERLAYS
        // ============================================================

        @Override
        protected void layoutChildren() {

                super.layoutChildren();

                modalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                animalModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                especieModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                if (animalModalLayer.isVisible()) {
                        animalModalLayer.toFront();
                }

                if (especieModalLayer.isVisible()) {
                        especieModalLayer.toFront();
                }
        }

        // ============================================================
        // OVERLAY PRINCIPAL
        // ============================================================

        private void prepararOverlay() {

                modalLayer.setManaged(false);
                modalLayer.setVisible(false);
                modalLayer.setPickOnBounds(true);

                modalLayer.setStyle(
                                "-fx-background-color: rgba(0, 0, 0, 0.55);");

                getChildren().add(modalLayer);
        }

        private void prepararOverlayEspecie() {

                especieModalLayer.setManaged(false);
                especieModalLayer.setVisible(false);
                especieModalLayer.setPickOnBounds(true);

                especieModalLayer.setStyle(
                                "-fx-background-color: rgba(0, 0, 0, 0.65);");

                getChildren().add(especieModalLayer);
        }

        // ============================================================
        // OVERLAY NUEVO ANIMAL
        // ============================================================

        private void prepararAnimalOverlay() {

                animalModalLayer.setManaged(false);
                animalModalLayer.setVisible(false);
                animalModalLayer.setPickOnBounds(true);

                animalModalLayer.setStyle(
                                "-fx-background-color: rgba(0, 0, 0, 0.68);");

                getChildren().add(animalModalLayer);
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
                                Math.max(320, availableWidth - 60));

                double height = Math.min(
                                preferredHeight,
                                Math.max(260, availableHeight - 60));

                content.setMinSize(0, 0);
                content.setPrefSize(width, height);
                content.setMaxSize(width, height);

                modalLayer.getChildren().setAll(content);

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

                applyCss();
                layout();

                modalLayer.applyCss();
                modalLayer.layout();
        }

        // ============================================================
        // CERRAR MODAL PRINCIPAL
        // ============================================================

        private void cerrarModal() {

                cerrarModalEspecie();

                cerrarAnimalModal();

                modalLayer.getChildren().clear();

                modalLayer.setVisible(false);
                modalLayer.setManaged(false);

                requestLayout();
        }

        // ============================================================
        // LIMPIAR ACTA PENDIENTE
        // ============================================================

        private void limpiarActaPendiente() {

                actaPendiente = null;
                fechaPendiente = null;
                organismoPendiente = null;
                responsablePendiente = null;
                recibePendiente = null;
                procedenciaPendiente = null;
                motivoPendiente = null;
                observacionesPendientes = null;

                detallesPendientes.clear();

                indiceDetallePendienteEditando = -1;
        }

        // ============================================================
        // CANCELAR ACTA NUEVA
        // ============================================================

        private void cancelarActaNueva() {

                limpiarActaPendiente();

                cerrarModal();
        }

        // ============================================================
        // MOSTRAR MODAL NUEVO ANIMAL
        // ============================================================

        private void mostrarModalNuevoAnimal(
                        ComboBox<SistemaDAO.Opcion> animalCombo) {

                TextField nombreVulgar = Estilos.campo("Nombre vulgar");

                TextField nombreCientifico = Estilos.campo("Nombre científico");

                TextField origen = Estilos.campo("Origen");

                ComboBox<SistemaDAO.Opcion> especie = Estilos.combo();

                especie.getItems().setAll(
                                SistemaDAO.especiesOpciones());

                GridPane form = new GridPane();

                Button nuevaEspecie = botonAgregar();

                HBox especieRow = new HBox(
                                8,
                                especie,
                                nuevaEspecie);

                HBox.setHgrow(
                                especie,
                                Priority.ALWAYS);

                nuevaEspecie.setOnAction(
                                e -> mostrarModalNuevaEspecie(especie));

                form.setHgap(12);
                form.setVgap(10);

                form.add(
                                etiqueta("Especie *"),
                                0,
                                0);

                form.add(
                                especieRow,
                                1,
                                0);

                form.add(
                                etiqueta("Nombre vulgar *"),
                                0,
                                1);

                form.add(
                                nombreVulgar,
                                1,
                                1);

                form.add(
                                etiqueta("Nombre científico *"),
                                0,
                                2);

                form.add(
                                nombreCientifico,
                                1,
                                2);

                form.add(
                                etiqueta("Origen"),
                                0,
                                3);

                form.add(
                                origen,
                                1,
                                3);

                GridPane.setHgrow(
                                especieRow,
                                Priority.ALWAYS);

                aplicarValidacionObligatoria(especieRow, () -> true);
                aplicarValidacionObligatoria(nombreVulgar, () -> true);
                aplicarValidacionObligatoria(nombreCientifico, () -> true);

                GridPane.setHgrow(
                                nombreVulgar,
                                Priority.ALWAYS);

                GridPane.setHgrow(
                                nombreCientifico,
                                Priority.ALWAYS);

                GridPane.setHgrow(
                                origen,
                                Priority.ALWAYS);

                especie.setMaxWidth(Double.MAX_VALUE);
                nombreVulgar.setMaxWidth(Double.MAX_VALUE);
                nombreCientifico.setMaxWidth(Double.MAX_VALUE);
                origen.setMaxWidth(Double.MAX_VALUE);

                Button cancelar = Estilos.botonSec("CANCELAR");

                Button guardar = Estilos.boton("＋ GUARDAR ANIMAL");

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

                Label titulo = tituloModal("NUEVO ANIMAL");

                Label ayuda = subtituloModal(
                                "Agregá un animal al catálogo para poder seleccionarlo inmediatamente en el acta.");

                VBox content = new VBox(12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(true);

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.28),24,0,0,8" +
                                                ");");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarAnimalModal());

                content.getChildren().addAll(
                                header,
                                form,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarAnimalModal());

                guardar.setOnAction(e -> {

                        try {

                                if (especie.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Seleccioná una especie.");
                                }

                                if (nombreVulgar.getText() == null ||
                                                nombreVulgar.getText().isBlank()) {

                                        throw new IllegalArgumentException(
                                                        "Ingresá el nombre vulgar del animal.");
                                }

                                if (nombreCientifico.getText() == null || nombreCientifico.getText().isBlank()) {
                                        throw new IllegalArgumentException("Ingresá el nombre científico del animal.");
                                }

                                int nuevoId = SistemaDAO.guardarAnimal(
                                                0,
                                                especie.getValue().id(),
                                                nombreVulgar.getText().trim(),
                                                nombreCientifico.getText().trim(),
                                                origen.getText().trim());

                                List<SistemaDAO.Opcion> opciones = SistemaDAO.animalesOpciones();

                                animalCombo.getItems().setAll(
                                                opciones);

                                opciones.stream()
                                                .filter(
                                                                o -> o.id() == nuevoId)
                                                .findFirst()
                                                .ifPresent(
                                                                animalCombo::setValue);

                                cerrarAnimalModal();

                        } catch (Exception ex) {

                                ex.printStackTrace();

                                Alert alert = new Alert(
                                                Alert.AlertType.ERROR);

                                alert.setTitle(
                                                "Tatú Carreta");

                                alert.setHeaderText(
                                                "No se pudo guardar el animal");

                                TextArea detalle = new TextArea();

                                detalle.setText(
                                                ex.toString()
                                                                + "\n\n"
                                                                + ex.getMessage());

                                detalle.setEditable(false);
                                detalle.setWrapText(true);
                                detalle.setPrefRowCount(8);
                                detalle.setPrefColumnCount(60);

                                alert.getDialogPane()
                                                .setContent(detalle);

                                alert.setResizable(true);

                                alert.showAndWait();
                        }
                });

                animalModalLayer
                                .getChildren()
                                .setAll(content);

                StackPane.setAlignment(
                                content,
                                Pos.CENTER);

                double width = Math.min(
                                620,
                                Math.max(
                                                420,
                                                getWidth() - 100));

                double height = Math.min(
                                430,
                                Math.max(
                                                320,
                                                getHeight() - 100));

                content.setMinSize(0, 0);
                content.setPrefSize(
                                width,
                                height);
                content.setMaxSize(
                                width,
                                height);

                animalModalLayer.setVisible(true);
                animalModalLayer.setManaged(false);

                requestLayout();

                animalModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                animalModalLayer.toFront();

                applyCss();
                layout();

                animalModalLayer.applyCss();
                animalModalLayer.layout();
        }

        // ============================================================
        // NUEVA ESPECIE
        // ============================================================

        private void mostrarModalNuevaEspecie(
                        ComboBox<SistemaDAO.Opcion> especieCombo) {

                VBox content = new VBox(14);

                content.setPadding(
                                new Insets(20));

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.28),24,0,0,8" +
                                                ");");

                Label titulo = tituloModal("NUEVA ESPECIE");

                Label ayuda = subtituloModal(
                                "Agregá una nueva especie al catálogo.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                ayuda),
                                () -> cerrarModalEspecie());

                TextField nombre = Estilos.campo("Especie");

                VBox campoEspecie = new VBox(
                                5,
                                etiqueta("Especie *"),
                                nombre);

                aplicarValidacionObligatoria(nombre, () -> true);

                Button cancelar = Estilos.botonSec("CANCELAR");

                Button guardar = Estilos.boton("＋ GUARDAR ESPECIE");

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

                content.getChildren().addAll(
                                header,
                                campoEspecie,
                                acciones);

                cancelar.setOnAction(
                                e -> cerrarModalEspecie());

                guardar.setOnAction(e -> {

                        try {

                                String nombreValue = nombre.getText().trim();

                                if (nombreValue.isBlank()) {

                                        throw new IllegalArgumentException(
                                                        "Ingresá el nombre de la especie.");
                                }

                                int nuevoId = SistemaDAO.guardarEspecie(
                                                0,
                                                nombreValue);

                                List<SistemaDAO.Opcion> opciones = SistemaDAO.especiesOpciones();

                                especieCombo.getItems().setAll(
                                                opciones);

                                opciones.stream()
                                                .filter(
                                                                o -> o.id() == nuevoId)
                                                .findFirst()
                                                .ifPresent(
                                                                especieCombo::setValue);

                                cerrarModalEspecie();

                        } catch (Exception ex) {

                                Alert alert = new Alert(
                                                Alert.AlertType.WARNING);

                                alert.setTitle(
                                                "Tatú Carreta");

                                alert.setHeaderText(
                                                "No se pudo guardar la especie");

                                alert.setContentText(
                                                ex.getMessage());

                                alert.showAndWait();
                        }
                });

                especieModalLayer
                                .getChildren()
                                .setAll(content);

                StackPane.setAlignment(
                                content,
                                Pos.CENTER);

                double width = Math.min(
                                420,
                                Math.max(
                                                360,
                                                getWidth() - 80));

                double height = Math.min(
                                250,
                                Math.max(
                                                220,
                                                getHeight() - 80));

                content.setMinSize(0, 0);
                content.setPrefSize(
                                width,
                                height);
                content.setMaxSize(
                                width,
                                height);

                especieModalLayer.setManaged(false);
                especieModalLayer.setVisible(true);

                requestLayout();

                especieModalLayer.resizeRelocate(
                                0,
                                0,
                                getWidth(),
                                getHeight());

                especieModalLayer.toFront();

                applyCss();
                layout();

                especieModalLayer.applyCss();
                especieModalLayer.layout();

                nombre.requestFocus();
        }

        private void cerrarModalEspecie() {

                especieModalLayer
                                .getChildren()
                                .clear();

                especieModalLayer.setVisible(false);
                especieModalLayer.setManaged(false);

                if (animalModalLayer.isVisible()) {

                        animalModalLayer.toFront();

                } else if (modalLayer.isVisible()) {

                        modalLayer.toFront();
                }

                requestLayout();
        }

        private void cerrarAnimalModal() {

                animalModalLayer
                                .getChildren()
                                .clear();

                animalModalLayer.setVisible(false);
                animalModalLayer.setManaged(false);

                if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                requestLayout();
        }

        // ============================================================
        // CABECERA DE MODAL
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

                List<SistemaDAO.Fila> filas =
                                new java.util.ArrayList<>(
                                                SistemaDAO.ingresos(q));

                /*
                 * El Acta General del Plantel Permanente es un acta lógica:
                 * no se guarda como un ingreso INDIVIDUAL porque su contenido
                 * se obtiene directamente del Plantel vivo.
                 *
                 * La fila con ID -1 es solamente una representación visual
                 * dentro de Ingresos. No altera la base de datos ni el stock.
                 *
                 * Se agrega siempre al principio para que vuelva a ser visible
                 * aunque el buscador tenga texto.
                 */
                filas.add(0,
                                new SistemaDAO.Fila(
                                                -1,
                                                java.util.List.of(
                                                                "ACTA GENERAL PLANTEL",
                                                                "Plantel Permanente",
                                                                "",
                                                                "Reserva",
                                                                "",
                                                                "",
                                                                "Plantel Permanente",
                                                                "Existencia viva actual",
                                                                "Activo")));

                return filas;
        }

        @Override
        protected List<SistemaDAO.Fila> consultarInactivos(
                        String q) {

                return SistemaDAO.ingresosInactivos(q);
        }

        @Override
        protected void activarRegistro(
                        int id) {

                SistemaDAO.activar(
                                "ingresos",
                                "id_ingreso",
                                id);
        }

        @Override
        protected void agregarNuevo() {

                limpiarActaPendiente();

                abrirWizardIngreso(
                                0,
                                null);
        }

        @Override
        protected void editarSeleccionado() {

                SistemaDAO.Fila f = tabla.getSelectionModel()
                                .getSelectedItem();

                if (f == null) {
                        return;
                }

                /*
                 * Acta General del Plantel.
                 */
                if (f.id() == -1) {

                        mostrarActaGeneralPlantel();

                        return;
                }

                /*
                 * Acta individual normal.
                 */
                mostrarActa(
                                f.id(),
                                f);
        }

        @Override
        protected void desactivar(
                        int id) {

                if (confirmarOK(
                                "Enviar acta a inactivos",
                                "¿Estás segura de que querés cambiar el estado de esta acta a Inactivo?")) {

                        SistemaDAO.baja(
                                        "ingresos",
                                        "id_ingreso",
                                        id);

                        cargar(
                                        buscador.getText());
                }
        }

        // ============================================================
        // ACTA GENERAL · PLANTEL PERMANENTE
        // ============================================================

        private void mostrarActaGeneralPlantel() {

                List<SistemaDAO.Fila> filas = SistemaDAO.actaGeneralPlantel("");

                VBox content = new VBox(14);

                content.setPadding(
                                new Insets(
                                                24,
                                                28,
                                                24,
                                                28));

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:14;");

                Label titulo = new Label(
                                "ACTA DE INGRESO GENERAL · PLANTEL PERMANENTE");

                titulo.setStyle(
                                "-fx-font-size:21px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.VERDE_OSCURO +
                                                ";");

                Label descripcion = new Label(
                                "Esta acta representa la existencia viva actual " +
                                                "del Plantel Permanente. Incluye ejemplares " +
                                                "ingresados directamente al Plantel, provenientes " +
                                                "de actas individuales y movimientos internos " +
                                                "dentro de la Reserva.");

                descripcion.setWrapText(true);

                descripcion.setStyle(
                                "-fx-font-size:13px;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                TableView<SistemaDAO.Fila> tablaGeneral = new TableView<>();

                tablaGeneral.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                tablaGeneral.setPlaceholder(
                                new Label(
                                                "No hay ejemplares activos actualmente en el Plantel."));

                TableColumn<SistemaDAO.Fila, String> animal = new TableColumn<>("Animal");

                animal.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 1
                                                                ? d.getValue().c().get(1)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> identificacion = new TableColumn<>("Identificación");

                identificacion.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 2
                                                                ? d.getValue().c().get(2)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> especie = new TableColumn<>("Especie");

                especie.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 3
                                                                ? d.getValue().c().get(3)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> cantidad = new TableColumn<>("Cantidad");

                cantidad.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 4
                                                                ? d.getValue().c().get(4)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> ubicacion = new TableColumn<>("Ubicación");

                ubicacion.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 5
                                                                ? d.getValue().c().get(5)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> habitaculo = new TableColumn<>("Habitáculo");

                habitaculo.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 6
                                                                ? d.getValue().c().get(6)
                                                                : ""));

                TableColumn<SistemaDAO.Fila, String> ingreso = new TableColumn<>("Ingreso");

                ingreso.setCellValueFactory(
                                d -> new SimpleStringProperty(
                                                d.getValue().c().size() > 7
                                                                ? d.getValue().c().get(7)
                                                                : ""));

                tablaGeneral.getColumns().addAll(
                                animal,
                                identificacion,
                                especie,
                                cantidad,
                                ubicacion,
                                habitaculo,
                                ingreso);

                tablaGeneral.getItems().setAll(filas);

                int total = 0;

                for (SistemaDAO.Fila f : filas) {

                        try {

                                if (f.c().size() > 4) {

                                        total += Integer.parseInt(
                                                        f.c().get(4));
                                }

                        } catch (Exception ignored) {
                        }
                }

                Label resumen = new Label(
                                "Ejemplares vivos actualmente en Plantel: "
                                                + total);

                resumen.setStyle(
                                "-fx-font-size:14px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.VERDE_OSCURO +
                                                ";");

                Button cerrar = Estilos.botonSec(
                                "CERRAR");

                cerrar.setOnAction(
                                e -> cerrarModal());

                HBox botones = new HBox(
                                cerrar);

                botones.setAlignment(
                                Pos.CENTER_LEFT);

                VBox.setVgrow(
                                tablaGeneral,
                                Priority.ALWAYS);

                content.getChildren().addAll(
                                titulo,
                                descripcion,
                                resumen,
                                tablaGeneral,
                                botones);

                mostrarModal(
                                content,
                                1180,
                                680);
        }

        // ============================================================
        // DETALLE DEL ACTA
        // ============================================================

        private void mostrarActa(
                        int ingreso,
                        SistemaDAO.Fila fila) {

                String numero = fila != null
                                ? fila.c().get(1)
                                : scalarIngreso(
                                                ingreso,
                                                "numero_acta");

                String fecha = fila != null
                                ? fila.c().get(2)
                                : scalarIngreso(
                                                ingreso,
                                                "fecha_ingreso");

                String organismo = fila != null
                                ? fila.c().get(3)
                                : scalarIngreso(
                                                ingreso,
                                                "COALESCE(organismo_procedencia,'')");

                String responsable = fila != null
                                ? fila.c().get(4)
                                : scalarIngreso(
                                                ingreso,
                                                "COALESCE(responsable_entrega,'')");

                String recibe = fila != null
                                ? fila.c().get(5)
                                : scalarIngreso(
                                                ingreso,
                                                "COALESCE(recibe_reserva,'')");

                String procedencia = fila != null
                                ? fila.c().get(6)
                                : scalarIngreso(
                                                ingreso,
                                                "COALESCE(procedencia,'')");

                String motivo = fila != null
                                ? fila.c().get(7)
                                : scalarIngreso(
                                                ingreso,
                                                "COALESCE(motivo_ingreso,'')");

                String observaciones = scalarIngreso(
                                ingreso,
                                "COALESCE(observaciones,'')");

                String estado = scalarIngreso(
                                ingreso,
                                "COALESCE(estado,'')");

                VBox content = new VBox(12);

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

                Label title = tituloModal(
                                "ACTA N° " + numero);

                Label subtitle = subtituloModal(
                                "Detalle completo del acta · "
                                                + formatearFecha(fecha));

                Button modificar = Estilos.boton(
                                "✎ MODIFICAR ACTA");

                Button cerrar = Estilos.botonSec(
                                "CERRAR");

                VBox tituloBox = new VBox(
                                2,
                                title,
                                subtitle);

                StackPane header = cabeceraModal(
                                tituloBox,
                                this::cerrarModal);

                modificar.setOnAction(e -> {

                        cerrarModal();

                        abrirWizardIngresoDesdeId(
                                        ingreso);
                });

                cerrar.setOnAction(
                                e -> cerrarModal());

                GridPane datos = new GridPane();

                datos.setHgap(14);
                datos.setVgap(10);

                VBox columna1 = new VBox(
                                10,
                                campoLectura(
                                                "Fecha",
                                                formatearFecha(fecha)),
                                campoLectura(
                                                "Organismo",
                                                organismo),
                                campoLectura(
                                                "Recibe en la Reserva",
                                                recibe));

                columna1.setFillWidth(true);

                VBox columna2 = new VBox(
                                10,
                                campoLectura(
                                                "Estado",
                                                estado),
                                campoLectura(
                                                "Responsable de entrega",
                                                responsable),
                                campoLectura(
                                                "Procedencia",
                                                procedencia),
                                campoLectura(
                                                "Motivo",
                                                motivo));

                columna2.setFillWidth(true);

                Label observacionesTitulo = etiqueta(
                                "OBSERVACIONES / ACTUALIZACIONES");

                String textoObservaciones = observaciones == null ||
                                observaciones.isBlank()
                                                ? "Sin observaciones"
                                                : observaciones;

                Label observacionesTexto = new Label(
                                textoObservaciones);

                observacionesTexto.setWrapText(true);

                observacionesTexto.setMaxWidth(
                                Double.MAX_VALUE);

                observacionesTexto.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-text-fill:" + Estilos.TEXTO + ";" +
                                                "-fx-padding:10;" +
                                                "-fx-background-color:" + Estilos.VERDE_SUAVE + ";" +
                                                "-fx-background-radius:8;");

                VBox observacionesBox = new VBox(
                                5,
                                observacionesTitulo,
                                observacionesTexto);

                observacionesBox.setFillWidth(true);

                ScrollPane observacionesScroll = new ScrollPane(
                                observacionesBox);

                observacionesScroll.setFitToWidth(true);
                observacionesScroll.setFitToHeight(false);

                observacionesScroll.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);

                observacionesScroll.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);

                observacionesScroll.setPrefViewportHeight(
                                220);

                observacionesScroll.setMinViewportHeight(
                                180);

                observacionesScroll.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;");

                datos.add(
                                columna1,
                                0,
                                0);

                datos.add(
                                columna2,
                                1,
                                0);

                datos.add(
                                observacionesScroll,
                                2,
                                0);

                GridPane.setHgrow(
                                columna1,
                                Priority.ALWAYS);

                GridPane.setHgrow(
                                columna2,
                                Priority.ALWAYS);

                GridPane.setHgrow(
                                observacionesScroll,
                                Priority.ALWAYS);

                GridPane.setVgrow(
                                observacionesScroll,
                                Priority.ALWAYS);

                ColumnConstraints col1 = new ColumnConstraints();

                ColumnConstraints col2 = new ColumnConstraints();

                ColumnConstraints col3 = new ColumnConstraints();

                col1.setPercentWidth(28);
                col2.setPercentWidth(28);
                col3.setPercentWidth(44);

                col1.setHgrow(
                                Priority.ALWAYS);

                col2.setHgrow(
                                Priority.ALWAYS);

                col3.setHgrow(
                                Priority.ALWAYS);

                datos.getColumnConstraints()
                                .setAll(
                                                col1,
                                                col2,
                                                col3);

                Label animalesTitulo = seccion(
                                "ANIMALES ASOCIADOS");

                TableView<SistemaDAO.Fila> animales = tablaAnimalesActa(ingreso);

                animales.setPrefHeight(152);
                animales.setMinHeight(152);
                animales.setMaxHeight(152);

                // ========================================================
                // RESUMEN DE MOVIMIENTOS
                // ========================================================

                HBox resumenMovimientos = resumenMovimientos(
                                ingreso);

                VBox cuerpo = new VBox(
                                12,
                                datos,
                                resumenMovimientos,
                                animalesTitulo,
                                animales);

                ScrollPane scrollPrincipal = new ScrollPane(
                                cuerpo);

                scrollPrincipal.setFitToWidth(true);
                scrollPrincipal.setFitToHeight(false);

                scrollPrincipal.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);

                scrollPrincipal.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);

                scrollPrincipal.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;");

                Region espacioAcciones = new Region();
                HBox.setHgrow(
                                espacioAcciones,
                                Priority.ALWAYS);

                HBox accionesFooter = new HBox(
                                10,
                                cerrar,
                                espacioAcciones,
                                modificar);
                accionesFooter.setAlignment(Pos.CENTER_LEFT);

                VBox.setVgrow(
                                scrollPrincipal,
                                Priority.ALWAYS);

                content.getChildren().addAll(
                                header,
                                scrollPrincipal,
                                accionesFooter);

                mostrarModal(
                                content,
                                1120,
                                760);
        }

        // ============================================================
        // CAMPO DE LECTURA
        // ============================================================

        private VBox campoLectura(
                        String titulo,
                        String valor) {

                Label t = etiqueta(
                                titulo.toUpperCase());

                Label v = new Label(
                                valor == null ||
                                                valor.isBlank()
                                                                ? "—"
                                                                : valor);

                v.setWrapText(true);

                v.setMaxWidth(
                                Double.MAX_VALUE);

                v.setMinHeight(
                                Region.USE_PREF_SIZE);

                v.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-text-fill:" +
                                                Estilos.TEXTO +
                                                ";" +
                                                "-fx-padding:8 10;" +
                                                "-fx-background-color:" +
                                                Estilos.VERDE_SUAVE +
                                                ";" +
                                                "-fx-background-radius:8;");

                VBox box = new VBox(
                                3,
                                t,
                                v);

                box.setFillWidth(true);

                VBox.setVgrow(
                                v,
                                Priority.NEVER);

                return box;
        }

        // ============================================================
        // TABLA DE ANIMALES DE ACTA EXISTENTE
        // ============================================================

        private TableView<SistemaDAO.Fila> tablaAnimalesActa(
                        int ingreso) {

                TableView<SistemaDAO.Fila> table = new TableView<>();

                Estilos.tablaCompacta(table);

                table.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                String[] columns = {
                                "Animal",
                                "Cantidad",
                                "Sexo",
                                "Edad",
                                "Peso",
                                "Estado sanitario",
                                "Identificación"
                };

                for (int i = 0; i < columns.length; i++) {

                        final int index = i;

                        TableColumn<SistemaDAO.Fila, String> col = new TableColumn<>(
                                        columns[i]);

                        col.setCellValueFactory(
                                        x -> new SimpleStringProperty(
                                                        x.getValue()
                                                                        .c()
                                                                        .size() > index + 1
                                                                                        ? x.getValue()
                                                                                                        .c()
                                                                                                        .get(index + 1)
                                                                                        : ""));

                        table.getColumns()
                                        .add(col);
                }

                List<SistemaDAO.Fila> data = SistemaDAO.detallesIngreso(
                                ingreso);

                table.setItems(
                                FXCollections.observableArrayList(
                                                data));

                table.setRowFactory(tv -> {

                        TableRow<SistemaDAO.Fila> row = new TableRow<>();

                        row.setOnMouseClicked(e -> {

                                if (e.getClickCount() == 2 &&
                                                !row.isEmpty()) {

                                        editarDetalleDesdeActa(
                                                        ingreso,
                                                        row.getItem());
                                }
                        });

                        return row;
                });

                return table;
        }

        // ============================================================
        // TABLA TEMPORAL DE ANIMALES
        // ============================================================

        private TableView<DetallePendiente> tablaAnimalesPendientes() {

                TableView<DetallePendiente> table = new TableView<>();

                Estilos.tablaCompacta(table);

                table.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                TableColumn<DetallePendiente, String> animal = new TableColumn<>("Animal");

                animal.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                nombreAnimal(
                                                                x.getValue().idAnimal())));

                TableColumn<DetallePendiente, String> cantidad = new TableColumn<>("Cantidad");

                cantidad.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                String.valueOf(
                                                                x.getValue().cantidad())));

                TableColumn<DetallePendiente, String> sexo = new TableColumn<>("Sexo");

                sexo.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                valorSeguro(
                                                                x.getValue().sexo())));

                TableColumn<DetallePendiente, String> edad = new TableColumn<>("Edad");

                edad.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                valorSeguro(
                                                                x.getValue().edad())));

                TableColumn<DetallePendiente, String> peso = new TableColumn<>("Peso");

                peso.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                valorSeguro(
                                                                x.getValue().peso())));

                TableColumn<DetallePendiente, String> estado = new TableColumn<>("Estado sanitario");

                estado.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                valorSeguro(
                                                                x.getValue().estadoSanitario())));

                TableColumn<DetallePendiente, String> identificacion = new TableColumn<>("Identificación");

                identificacion.setCellValueFactory(
                                x -> new SimpleStringProperty(
                                                identificacionTexto(
                                                                x.getValue().tipoIdentificacion(),
                                                                x.getValue().numeroIdentificacion())));

                table.getColumns().addAll(
                                animal,
                                cantidad,
                                sexo,
                                edad,
                                peso,
                                estado,
                                identificacion);

                table.setItems(
                                FXCollections.observableArrayList(
                                                detallesPendientes));

                table.setRowFactory(tv -> {

                        TableRow<DetallePendiente> row = new TableRow<>();

                        row.setOnMouseClicked(e -> {

                                if (e.getClickCount() == 2 &&
                                                !row.isEmpty()) {

                                        int index = detallesPendientes.indexOf(
                                                        row.getItem());

                                        if (index >= 0) {

                                                editarDetallePendiente(
                                                                index);
                                        }
                                }
                        });

                        return row;
                });

                return table;
        }

        // ============================================================
        // EDITAR ANIMAL DE ACTA EXISTENTE
        // ============================================================

        private void editarDetalleDesdeActa(
                        int ingreso,
                        SistemaDAO.Fila fila) {

                cerrarModal();

                mostrarPasoAnimales(
                                ingreso,
                                false,
                                fila);
        }

        // ============================================================
        // EDITAR ANIMAL TEMPORAL
        // ============================================================

        private void editarDetallePendiente(
                        int indice) {

                if (indice < 0 ||
                                indice >= detallesPendientes.size()) {

                        return;
                }

                indiceDetallePendienteEditando = indice;

                mostrarPasoAnimales(
                                0,
                                true,
                                null);
        }

        // ============================================================
        // PASO 1
        // ============================================================

        private void abrirWizardIngreso(
                        int id,
                        SistemaDAO.Fila fila) {

                boolean nuevo = id == 0;

                TextField acta = Estilos.campo(
                                "Número de acta");

                DatePicker fecha = Estilos.fecha();

                TextField organismo = Estilos.campo(
                                "Organismo");

                TextField responsable = Estilos.campo(
                                "Responsable de entrega");

                TextField recibe = Estilos.campo(
                                "Quién recibe en la Reserva");

                TextField procedencia = Estilos.campo(
                                "Procedencia");

                TextField motivo = Estilos.campo(
                                "Motivo");

                TextArea observaciones = Estilos.area(
                                "Actualizaciones del acta");

                observaciones.setPrefRowCount(4);

                if (fila != null) {

                        acta.setText(
                                        fila.c().get(1));

                        fecha.setValue(
                                        parseFecha(
                                                        fila.c().get(2)));

                        organismo.setText(
                                        fila.c().get(3));

                        responsable.setText(
                                        fila.c().get(4));

                        recibe.setText(
                                        fila.c().get(5));

                        procedencia.setText(
                                        fila.c().get(6));

                        motivo.setText(
                                        fila.c().get(7));

                        observaciones.setText(
                                        scalarIngreso(
                                                        id,
                                                        "COALESCE(observaciones,'')"));

                } else if (nuevo &&
                                actaPendiente != null) {

                        acta.setText(
                                        actaPendiente);

                        fecha.setValue(
                                        fechaPendiente);

                        organismo.setText(
                                        organismoPendiente);

                        responsable.setText(
                                        responsablePendiente);

                        recibe.setText(
                                        recibePendiente);

                        procedencia.setText(
                                        procedenciaPendiente);

                        motivo.setText(
                                        motivoPendiente);

                        observaciones.setText(
                                        observacionesPendientes);

                } else {

                        fecha.setValue(
                                        LocalDate.now());

                        observaciones.setText(
                                        ""
                                                        + fechaFormato.format(
                                                                        LocalDate.now())
                                                        + " : Acta creada.");

                        observaciones.positionCaret(
                                        observaciones.getText().length());
                }

                GridPane form = new GridPane();

                form.setHgap(14);
                form.setVgap(9);

                form.setPadding(
                                new Insets(
                                                8,
                                                0,
                                                0,
                                                0));

                addForm(
                                form,
                                0,
                                "N° Acta",
                                acta,
                                true);

                addForm(
                                form,
                                1,
                                "Fecha",
                                fecha,
                                true);

                addForm(
                                form,
                                2,
                                "Organismo",
                                organismo,
                                true);

                addForm(
                                form,
                                3,
                                "Responsable de entrega",
                                responsable,
                                true);

                addForm(
                                form,
                                4,
                                "Recibe en la Reserva",
                                recibe,
                                true);

                addForm(
                                form,
                                5,
                                "Procedencia",
                                procedencia,
                                true);

                addForm(
                                form,
                                6,
                                "Motivo",
                                motivo,
                                true);

                addForm(
                                form,
                                7,
                                "Observaciones",
                                observaciones,
                                false);

                Label step = paso(
                                "PASO 1 DE 2",
                                "Datos generales del acta");

                Label ayuda = subtituloModal(
                                "Completá los datos del ingreso. "
                                                + "Las observaciones funcionan como historial de actualizaciones.");

                Button cancelar = Estilos.botonSec(
                                "CANCELAR");

                Button siguiente = Estilos.boton(
                                "ASOCIAR ANIMALES →");

                cancelar.setOnAction(e -> {

                        if (nuevo) {

                                cancelarActaNueva();

                        } else {

                                cerrarModal();
                        }
                });

                HBox actions = new HBox(
                                10,
                                cancelar,
                                new Region(),
                                siguiente);

                HBox.setHgrow(
                                actions.getChildren().get(1),
                                Priority.ALWAYS);

                actions.setAlignment(
                                Pos.CENTER_RIGHT);

                VBox content = modalBase(
                                step,
                                ayuda,
                                form,
                                actions);

                siguiente.setOnAction(e -> {

                        try {

                                validarActa(
                                                acta,
                                                fecha,
                                                organismo,
                                                responsable,
                                                recibe,
                                                procedencia,
                                                motivo);

                                if (nuevo) {

                                        actaPendiente = acta.getText().trim();

                                        fechaPendiente = fecha.getValue();

                                        organismoPendiente = organismo.getText().trim();

                                        responsablePendiente = responsable.getText().trim();

                                        recibePendiente = recibe.getText().trim();

                                        procedenciaPendiente = procedencia.getText().trim();

                                        motivoPendiente = motivo.getText().trim();

                                        observacionesPendientes = observaciones.getText().trim();

                                        if (detallesPendientes.isEmpty()) {

                                                indiceDetallePendienteEditando = -1;
                                        }

                                        mostrarPasoAnimales(
                                                        0,
                                                        true,
                                                        null);

                                        return;
                                }

                                int ingreso = SistemaDAO.guardarIngreso(
                                                id,
                                                acta.getText().trim(),
                                                fecha.getValue().toString(),
                                                organismo.getText().trim(),
                                                responsable.getText().trim(),
                                                recibe.getText().trim(),
                                                procedencia.getText().trim(),
                                                motivo.getText().trim(),
                                                observaciones.getText().trim());

                                cargar(
                                                buscador.getText());

                                mostrarPasoAnimales(
                                                ingreso,
                                                false,
                                                null);

                        } catch (Exception ex) {

                                error(
                                                "No se puede continuar",
                                                ex.getMessage());
                        }
                });

                mostrarModal(
                                content,
                                980,
                                650);
        }

        // ============================================================
        // BOTÓN AGREGAR
        // ============================================================

        private Button botonAgregar() {

                Button boton = new Button("+");

                boton.setPrefWidth(42);
                boton.setMinWidth(42);
                boton.setMaxWidth(42);

                boton.setPrefHeight(38);
                boton.setMinHeight(38);
                boton.setMaxHeight(38);

                String normal = "-fx-background-color:" + Estilos.VERDE_OSCURO + ";" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:20px;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:8;" +
                                "-fx-padding:0;" +
                                "-fx-cursor:hand;";

                String hover = "-fx-background-color:" + Estilos.VERDE_MEDIO + ";" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:20px;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:8;" +
                                "-fx-padding:0;" +
                                "-fx-cursor:hand;";

                boton.setStyle(normal);

                boton.setOnMouseEntered(e -> boton.setStyle(hover));

                boton.setOnMouseExited(e -> boton.setStyle(normal));

                return boton;
        }

        // ============================================================
        // PASO 2
        // ============================================================

        private void mostrarPasoAnimales(
                        int ingreso,
                        boolean actaNueva,
                        SistemaDAO.Fila filaEditar) {

                VBox content = new VBox(10);

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

                Label step = paso(
                                "PASO 2 DE 2",
                                "Animales asociados al acta");

                Label ayuda = subtituloModal(
                                "Animal y cantidad son obligatorios. "
                                                + "Los datos individuales se habilitan solamente cuando la cantidad es 1.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                step,
                                                ayuda),
                                () -> {

                                        if (actaNueva) {

                                                cancelarActaNueva();

                                        } else {

                                                cerrarModal();
                                        }
                                });

                TableView<DetallePendiente> tablaPendiente = null;

                TableView<SistemaDAO.Fila> tablaExistente = null;

                if (actaNueva) {

                        tablaPendiente = tablaAnimalesPendientes();

                        tablaPendiente.setPrefHeight(152);
                        tablaPendiente.setMinHeight(152);
                        tablaPendiente.setMaxHeight(152);

                } else {

                        tablaExistente = tablaAnimalesActa(
                                        ingreso);

                        tablaExistente.setPrefHeight(152);
                        tablaExistente.setMinHeight(152);
                        tablaExistente.setMaxHeight(152);
                }

                // ========================================================
                // CAMPOS PRINCIPALES
                // ========================================================

                ComboBox<SistemaDAO.Opcion> animal = Estilos.combo();

                animal.getItems().setAll(
                                SistemaDAO.animalesOpciones());

                Button agregarAnimal = botonAgregar();

                agregarAnimal.setTooltip(
                                new Tooltip(
                                                "Agregar nuevo animal al catálogo"));

                agregarAnimal.setOnAction(
                                e -> mostrarModalNuevoAnimal(
                                                animal));

                TextField cantidad = Estilos.entero(
                                "Cantidad",
                                6);

                HBox animalRow = new HBox(
                                8,
                                animal,
                                agregarAnimal,
                                cantidad);

                aplicarValidacionObligatoria(animal, () -> true);
                aplicarValidacionObligatoria(cantidad, () -> true);

                HBox.setHgrow(
                                animal,
                                Priority.ALWAYS);

                cantidad.setPrefWidth(
                                145);

                animal.setMaxWidth(
                                Double.MAX_VALUE);

                // ========================================================
                // DATOS INDIVIDUALES
                // ========================================================

                ComboBox<String> sexo = Estilos.combo();

                sexo.getItems().setAll(
                                "Macho",
                                "Hembra",
                                "Indeterminado");

                TextField edad = Estilos.campo(
                                "Edad");

                TextField peso = Estilos.campo(
                                "Peso");

                ComboBox<String> estado = Estilos.combo();

                estado.getItems().setAll(
                                "Bueno",
                                "Regular",
                                "Delicado",
                                "Crítico",
                                "Sin especificar");

                ComboBox<String> tipoIdentificacion = Estilos.combo();

                tipoIdentificacion.getItems().setAll(
                                "Chip",
                                "Caravana",
                                "Anillo",
                                "Otro");

                tipoIdentificacion.setPromptText(
                                "Tipo de identificación");

                TextField otroTipoIdentificacion = Estilos.campo(
                                "Especificar tipo");

                otroTipoIdentificacion.setVisible(false);
                otroTipoIdentificacion.setManaged(false);

                TextField numeroIdentificacion = Estilos.campo(
                                "Número de identificación");

                TextArea observaciones = Estilos.area(
                                "Observaciones del animal");

                observaciones.setPrefRowCount(2);

                GridPane individual = new GridPane();

                individual.setHgap(10);
                individual.setVgap(8);

                individual.add(
                                etiqueta("Sexo"),
                                0,
                                0);

                individual.add(
                                sexo,
                                1,
                                0);

                individual.add(
                                etiqueta("Edad"),
                                2,
                                0);

                individual.add(
                                edad,
                                3,
                                0);

                individual.add(
                                etiqueta("Peso"),
                                0,
                                1);

                individual.add(
                                peso,
                                1,
                                1);

                individual.add(
                                etiqueta("Estado sanitario"),
                                2,
                                1);

                individual.add(
                                estado,
                                3,
                                1);

                individual.add(
                                etiqueta("Tipo identificación"),
                                0,
                                2);

                individual.add(
                                tipoIdentificacion,
                                1,
                                2);

                individual.add(
                                etiqueta("Número"),
                                2,
                                2);

                individual.add(
                                numeroIdentificacion,
                                3,
                                2);

                individual.add(
                                otroTipoIdentificacion,
                                1,
                                3,
                                3,
                                1);

                individual.add(
                                etiqueta("Observaciones"),
                                0,
                                4);

                individual.add(
                                observaciones,
                                1,
                                4,
                                3,
                                1);

                for (int i = 0; i < 4; i++) {

                        ColumnConstraints cc = new ColumnConstraints();

                        cc.setPercentWidth(
                                        i % 2 == 0
                                                        ? 17
                                                        : 33);

                        individual
                                        .getColumnConstraints()
                                        .add(cc);
                }

                tipoIdentificacion.valueProperty().addListener(
                                (obs, old, value) -> {

                                        boolean otro = "Otro".equals(value);

                                        otroTipoIdentificacion.setVisible(otro);
                                        otroTipoIdentificacion.setManaged(otro);

                                        if (!otro) {
                                                otroTipoIdentificacion.clear();
                                        }
                                });

                Label obligatorio = new Label(
                                "* Animal y cantidad son obligatorios");

                obligatorio.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                // ========================================================
                // BOTONES
                // ========================================================

                boolean editandoPendiente = actaNueva
                                && indiceDetallePendienteEditando >= 0;

                Button guardarAnimal = Estilos.boton(
                                filaEditar != null || editandoPendiente
                                                ? "GUARDAR EDICIÓN"
                                                : "＋ AGREGAR ANIMAL");

                Button finalizar = Estilos.botonSec(
                                "FINALIZAR ACTA");

                Button volver = Estilos.botonSec(
                                "← VOLVER A DATOS");

                guardarAnimal.setDisable(true);

                setIndividualEnabled(
                                individual,
                                false);

                // ========================================================
                // CARGAR EDICIÓN
                // ========================================================

                if (filaEditar != null) {

                        cargarDetalleDesdeFila(
                                        filaEditar,
                                        animal,
                                        cantidad,
                                        sexo,
                                        edad,
                                        peso,
                                        estado,
                                        tipoIdentificacion,
                                        otroTipoIdentificacion,
                                        numeroIdentificacion,
                                        observaciones);

                } else if (editandoPendiente) {

                        DetallePendiente detalle = detallesPendientes.get(
                                        indiceDetallePendienteEditando);

                        cargarDetallePendiente(
                                        detalle,
                                        animal,
                                        cantidad,
                                        sexo,
                                        edad,
                                        peso,
                                        estado,
                                        tipoIdentificacion,
                                        otroTipoIdentificacion,
                                        numeroIdentificacion,
                                        observaciones);
                }

                // ========================================================
                // ESTADO DE LOS CAMPOS
                // ========================================================

                Runnable actualizarEstado = () -> {

                        int n = parseCantidad(
                                        cantidad.getText());

                        boolean basicosOk = animal.getValue() != null
                                        && n > 0;

                        guardarAnimal.setDisable(
                                        !basicosOk);

                        boolean individualOk = basicosOk && n == 1;

                        setIndividualEnabled(
                                        individual,
                                        individualOk);

                        if (!individualOk) {

                                sexo.setValue(null);
                                edad.clear();
                                peso.clear();
                                estado.setValue(null);
                                tipoIdentificacion.setValue(null);
                                otroTipoIdentificacion.clear();
                                numeroIdentificacion.clear();
                                observaciones.clear();
                        }
                };

                animal.valueProperty().addListener(
                                (obs, old, value) -> actualizarEstado.run());

                cantidad.textProperty().addListener(
                                (obs, old, value) -> actualizarEstado.run());

                actualizarEstado.run();

                // ========================================================
                // GUARDAR ANIMAL
                // ========================================================

                guardarAnimal.setOnAction(e -> {

                        try {

                                if (animal.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Elegí un animal.");
                                }

                                int n = parseCantidad(
                                                cantidad.getText());

                                if (n <= 0) {

                                        throw new IllegalArgumentException(
                                                        "La cantidad debe ser mayor que cero.");
                                }

                                // ------------------------------------------------
                                // DATOS INDIVIDUALES
                                // ------------------------------------------------

                                String sexoValue = n == 1
                                                ? valorSeguro(sexo.getValue())
                                                : null;

                                String edadValue = n == 1
                                                ? valorSeguro(edad.getText()).trim()
                                                : "";

                                String pesoValue = n == 1
                                                ? valorSeguro(peso.getText()).trim()
                                                : "";

                                String estadoValue = n == 1
                                                ? valorSeguro(estado.getValue())
                                                : null;

                                String tipoValue = null;
                                String numeroValue = "";

                                if (n == 1) {

                                        String tipoSeleccionado = tipoIdentificacion.getValue();

                                        if (tipoSeleccionado != null
                                                        && !tipoSeleccionado.isBlank()) {

                                                if ("Otro".equalsIgnoreCase(
                                                                tipoSeleccionado)) {

                                                        tipoValue = valorSeguro(
                                                                        otroTipoIdentificacion
                                                                                        .getText())
                                                                        .trim();

                                                        if (tipoValue.isBlank()) {

                                                                throw new IllegalArgumentException(
                                                                                "Especificá el tipo de identificación.");
                                                        }

                                                } else {

                                                        tipoValue = tipoSeleccionado.trim();
                                                }
                                        }

                                        numeroValue = valorSeguro(
                                                        numeroIdentificacion
                                                                        .getText())
                                                        .trim();

                                        if (tipoValue != null
                                                        && numeroValue.isBlank()) {

                                                throw new IllegalArgumentException(
                                                                "Ingresá el número de identificación.");
                                        }

                                        if (tipoValue == null
                                                        && !numeroValue.isBlank()) {

                                                throw new IllegalArgumentException(
                                                                "Seleccioná el tipo de identificación.");
                                        }
                                }

                                String obsValue = n == 1
                                                ? valorSeguro(
                                                                observaciones.getText())
                                                                .trim()
                                                : "";

                                // =================================================
                                // NUEVA ACTA
                                // =================================================

                                if (actaNueva) {

                                        DetallePendiente nuevoDetalle = new DetallePendiente(
                                                        animal.getValue().id(),
                                                        n,
                                                        sexoValue,
                                                        edadValue,
                                                        pesoValue,
                                                        estadoValue,
                                                        tipoValue,
                                                        numeroValue,
                                                        obsValue);

                                        if (indiceDetallePendienteEditando >= 0
                                                        && indiceDetallePendienteEditando < detallesPendientes
                                                                        .size()) {

                                                detallesPendientes.set(
                                                                indiceDetallePendienteEditando,
                                                                nuevoDetalle);

                                        } else {

                                                detallesPendientes.add(
                                                                nuevoDetalle);
                                        }

                                        indiceDetallePendienteEditando = -1;

                                        mostrarPasoAnimales(
                                                        0,
                                                        true,
                                                        null);

                                        return;
                                }

                                // =================================================
                                // ACTA EXISTENTE
                                // =================================================

                                if (filaEditar == null) {

                                        throw new IllegalStateException(
                                                        "No se encontró el detalle que se está editando.");
                                }

                                Integer idIdentificacion = obtenerIdIdentificacion(
                                                filaEditar);

                                if (n != 1) {

                                        /*
                                         * Si antes existía una identificación y ahora
                                         * la cantidad deja de ser individual, no se
                                         * crea ninguna nueva identificación.
                                         *
                                         * El detalle queda sin identificación.
                                         */
                                        idIdentificacion = null;

                                } else if (tipoValue != null) {

                                        idIdentificacion = SistemaDAO.guardarIdentificacion(
                                                        idIdentificacion == null
                                                                        ? 0
                                                                        : idIdentificacion,
                                                        animal.getValue().id(),
                                                        tipoValue,
                                                        numeroValue,
                                                        LocalDate.now()
                                                                        .toString(),
                                                        "");

                                } else {

                                        idIdentificacion = null;
                                }

                                SistemaDAO.guardarDetalle(
                                                filaEditar.id(),
                                                ingreso,
                                                animal.getValue().id(),
                                                n,
                                                sexoValue,
                                                edadValue,
                                                pesoValue,
                                                estadoValue,
                                                obsValue,
                                                idIdentificacion);

                                cargar(
                                                buscador.getText());

                                mostrarPasoAnimales(
                                                ingreso,
                                                false,
                                                null);

                        } catch (Exception ex) {

                                ex.printStackTrace();

                                Alert alert = new Alert(
                                                Alert.AlertType.ERROR);

                                alert.setTitle(
                                                "Tatú Carreta");

                                alert.setHeaderText(
                                                "No se pudo guardar el animal");

                                String mensaje = ex.getMessage();

                                if (mensaje == null || mensaje.isBlank()) {

                                        mensaje = ex.toString();
                                }

                                TextArea detalle = new TextArea(
                                                mensaje);

                                detalle.setEditable(false);
                                detalle.setWrapText(true);
                                detalle.setPrefRowCount(10);
                                detalle.setPrefColumnCount(70);

                                alert.getDialogPane()
                                                .setContent(detalle);

                                alert.setResizable(true);

                                alert.showAndWait();
                        }
                });

                // ========================================================
                // VOLVER
                // ========================================================

                volver.setOnAction(e -> {

                        cerrarModal();

                        if (actaNueva) {

                                abrirWizardIngreso(
                                                0,
                                                null);

                        } else {

                                abrirWizardIngresoDesdeId(
                                                ingreso);
                        }
                });

                // ========================================================
                // FINALIZAR
                // ========================================================

                finalizar.setOnAction(e -> {

                        if (actaNueva) {

                                if (detallesPendientes.isEmpty()) {

                                        mostrarAdvertenciaSinAnimales();

                                        return;
                                }

                                confirmarFinalizacion(
                                                "¿Estás seguro de que querés finalizar el acta?\n\n"
                                                                + "Al confirmar se guardará el acta y todos los animales asociados.",
                                                respuesta -> {

                                                        if (respuesta != ButtonType.OK) {
                                                                return;
                                                        }

                                                        try {

                                                                if (actaPendiente == null
                                                                                || actaPendiente
                                                                                                .isBlank()) {

                                                                        throw new IllegalArgumentException(
                                                                                        "El número de acta es obligatorio.");
                                                                }

                                                                if (fechaPendiente == null) {

                                                                        throw new IllegalArgumentException(
                                                                                        "La fecha del acta es obligatoria.");
                                                                }

                                                                int nuevoIngreso = SistemaDAO.guardarIngreso(
                                                                                0,
                                                                                actaPendiente,
                                                                                fechaPendiente
                                                                                                .toString(),
                                                                                valorSeguro(
                                                                                                organismoPendiente),
                                                                                valorSeguro(
                                                                                                responsablePendiente),
                                                                                valorSeguro(
                                                                                                recibePendiente),
                                                                                valorSeguro(
                                                                                                procedenciaPendiente),
                                                                                valorSeguro(
                                                                                                motivoPendiente),
                                                                                valorSeguro(
                                                                                                observacionesPendientes));

                                                                for (DetallePendiente detalle : detallesPendientes) {

                                                                        Integer idIdentificacion = null;

                                                                        if (detalle.cantidad() == 1
                                                                                        && detalle.tipoIdentificacion() != null
                                                                                        && !detalle.tipoIdentificacion()
                                                                                                        .isBlank()
                                                                                        && detalle.numeroIdentificacion() != null
                                                                                        && !detalle.numeroIdentificacion()
                                                                                                        .isBlank()) {

                                                                                idIdentificacion = SistemaDAO
                                                                                                .guardarIdentificacion(
                                                                                                                0,
                                                                                                                detalle.idAnimal(),
                                                                                                                detalle.tipoIdentificacion(),
                                                                                                                detalle.numeroIdentificacion(),
                                                                                                                fechaPendiente
                                                                                                                                .toString(),
                                                                                                                "");
                                                                        }

                                                                        SistemaDAO.guardarDetalle(
                                                                                        0,
                                                                                        nuevoIngreso,
                                                                                        detalle.idAnimal(),
                                                                                        detalle.cantidad(),
                                                                                        detalle.sexo(),
                                                                                        detalle.edad(),
                                                                                        detalle.peso(),
                                                                                        detalle.estadoSanitario(),
                                                                                        detalle.observaciones(),
                                                                                        idIdentificacion);
                                                                }

                                                                limpiarActaPendiente();

                                                                cargar(
                                                                                buscador.getText());

                                                                cerrarModal();

                                                                mostrarActa(
                                                                                nuevoIngreso,
                                                                                null);

                                                        } catch (Exception ex) {

                                                                ex.printStackTrace();

                                                                error(
                                                                                "No se pudo finalizar el acta",
                                                                                ex.getMessage());
                                                        }
                                                });

                                return;
                        }

                        // =================================================
                        // ACTA EXISTENTE
                        // =================================================

                        List<SistemaDAO.Fila> animalesAsociados = SistemaDAO.detallesIngreso(
                                        ingreso);

                        if (animalesAsociados.isEmpty()) {

                                mostrarAdvertenciaSinAnimales();

                                return;
                        }

                        confirmarFinalizacion(
                                        "¿Estás seguro de que querés finalizar el acta?\n\n"
                                                        + "Se mostrará el detalle completo del acta.",
                                        respuesta -> {

                                                if (respuesta == ButtonType.OK) {

                                                        cargar(
                                                                        buscador.getText());

                                                        cerrarModal();

                                                        mostrarActa(
                                                                        ingreso,
                                                                        null);
                                                }
                                        });
                });

                // ========================================================
                // BOTONES INFERIORES
                // ========================================================

                Region espacioBotones = new Region();

                HBox.setHgrow(
                                espacioBotones,
                                Priority.ALWAYS);

                HBox buttons = new HBox(
                                10,
                                volver,
                                espacioBotones,
                                guardarAnimal,
                                finalizar);

                buttons.setAlignment(
                                Pos.CENTER_RIGHT);

                // ========================================================
                // AGREGAR CONTENIDO
                // ========================================================

                content.getChildren().add(
                                header);

                content.getChildren().add(
                                seccion(
                                                "ANIMALES CARGADOS"));

                if (actaNueva) {

                        content.getChildren().add(
                                        tablaPendiente);

                } else {

                        content.getChildren().add(
                                        tablaExistente);
                }

                content.getChildren().addAll(
                                seccion(
                                                "AGREGAR / EDITAR ANIMAL"),
                                animalRow,
                                individual,
                                obligatorio,
                                buttons);

                mostrarModal(
                                content,
                                1040,
                                720);
        }

        // ============================================================
        // CARGAR DETALLE EXISTENTE
        // ============================================================

        private void cargarDetalleDesdeFila(
                        SistemaDAO.Fila filaEditar,
                        ComboBox<SistemaDAO.Opcion> animal,
                        TextField cantidad,
                        ComboBox<String> sexo,
                        TextField edad,
                        TextField peso,
                        ComboBox<String> estado,
                        ComboBox<String> tipoIdentificacion,
                        TextField otroTipoIdentificacion,
                        TextField numeroIdentificacion,
                        TextArea observaciones) {

                if (filaEditar == null) {
                        return;
                }

                List<String> datos = filaEditar.c();

                if (datos == null || datos.size() < 3) {
                        return;
                }

                // ------------------------------------------------
                // ANIMAL
                // ------------------------------------------------

                if (datos.size() > 1) {

                        String textoAnimal = valorSeguro(
                                        datos.get(1));

                        SistemaDAO.Opcion selectedAnimal = animal.getItems()
                                        .stream()
                                        .filter(o -> o != null
                                                        && textoAnimal.equals(
                                                                        o.texto()))
                                        .findFirst()
                                        .orElse(null);

                        animal.setValue(
                                        selectedAnimal);
                }

                // ------------------------------------------------
                // CANTIDAD
                // ------------------------------------------------

                if (datos.size() > 2) {

                        cantidad.setText(
                                        valorSeguro(
                                                        datos.get(2)));
                }

                // ------------------------------------------------
                // SEXO
                // ------------------------------------------------

                if (datos.size() > 3) {

                        String valorSexo = valorSeguro(datos.get(3));

                        sexo.setValue(
                                        valorSexo.isBlank()
                                                        ? null
                                                        : valorSexo);
                }

                // ------------------------------------------------
                // EDAD
                // ------------------------------------------------

                if (datos.size() > 4) {

                        edad.setText(
                                        valorSeguro(
                                                        datos.get(4)));
                }

                // ------------------------------------------------
                // PESO
                // ------------------------------------------------

                if (datos.size() > 5) {

                        peso.setText(
                                        valorSeguro(
                                                        datos.get(5)));
                }

                // ------------------------------------------------
                // ESTADO SANITARIO
                // ------------------------------------------------

                if (datos.size() > 6) {

                        String valorEstado = valorSeguro(datos.get(6));

                        estado.setValue(
                                        valorEstado.isBlank()
                                                        ? null
                                                        : valorEstado);
                }

                // ------------------------------------------------
                // OBSERVACIONES
                // ------------------------------------------------

                if (datos.size() > 8) {

                        observaciones.setText(
                                        valorSeguro(
                                                        datos.get(8)));
                } else {

                        observaciones.clear();
                }

                // ------------------------------------------------
                // IDENTIFICACIÓN
                // ------------------------------------------------

                Integer idIdentificacion = obtenerIdIdentificacion(
                                filaEditar);

                cargarIdentificacionExistente(
                                idIdentificacion,
                                tipoIdentificacion,
                                otroTipoIdentificacion,
                                numeroIdentificacion);
        }

        // ============================================================
        // CARGAR DETALLE TEMPORAL
        // ============================================================

        private void cargarDetallePendiente(
                        DetallePendiente detalle,
                        ComboBox<SistemaDAO.Opcion> animal,
                        TextField cantidad,
                        ComboBox<String> sexo,
                        TextField edad,
                        TextField peso,
                        ComboBox<String> estado,
                        ComboBox<String> tipoIdentificacion,
                        TextField otroTipoIdentificacion,
                        TextField numeroIdentificacion,
                        TextArea observaciones) {

                if (detalle == null) {
                        return;
                }

                SistemaDAO.Opcion selectedAnimal = animal.getItems()
                                .stream()
                                .filter(o -> o != null
                                                && o.id() == detalle.idAnimal())
                                .findFirst()
                                .orElse(null);

                animal.setValue(
                                selectedAnimal);

                cantidad.setText(
                                String.valueOf(
                                                detalle.cantidad()));

                if (detalle.cantidad() == 1) {

                        sexo.setValue(
                                        detalle.sexo());

                        edad.setText(
                                        valorSeguro(
                                                        detalle.edad()));

                        peso.setText(
                                        valorSeguro(
                                                        detalle.peso()));

                        estado.setValue(
                                        detalle.estadoSanitario());

                        observaciones.setText(
                                        valorSeguro(
                                                        detalle.observaciones()));

                        cargarIdentificacionTemporal(
                                        detalle.tipoIdentificacion(),
                                        detalle.numeroIdentificacion(),
                                        tipoIdentificacion,
                                        otroTipoIdentificacion,
                                        numeroIdentificacion);

                } else {

                        sexo.setValue(null);
                        edad.clear();
                        peso.clear();
                        estado.setValue(null);
                        tipoIdentificacion.setValue(null);
                        otroTipoIdentificacion.clear();
                        numeroIdentificacion.clear();
                        observaciones.clear();
                }
        }

        // ============================================================
        // IDENTIFICACIONES
        // ============================================================

        private Integer obtenerIdIdentificacion(
                        SistemaDAO.Fila fila) {

                if (fila == null) {
                        return null;
                }

                try {

                        return SistemaDAO.obtenerIdIdentificacionDetalle(
                                        fila.id());

                } catch (Exception e) {

                        e.printStackTrace();

                        return null;
                }
        }

        private void cargarIdentificacionExistente(
                        Integer idIdentificacion,
                        ComboBox<String> tipoIdentificacion,
                        TextField otroTipoIdentificacion,
                        TextField numeroIdentificacion) {

                tipoIdentificacion.setValue(null);

                otroTipoIdentificacion.clear();

                numeroIdentificacion.clear();

                if (idIdentificacion == null) {
                        return;
                }

                SistemaDAO.Fila identificacion = SistemaDAO.identificacionPorId(
                                idIdentificacion);

                if (identificacion == null
                                || identificacion.c() == null
                                || identificacion.c().size() < 3) {

                        return;
                }

                String tipo = valorSeguro(
                                identificacion.c().get(1))
                                .trim();

                String numero = valorSeguro(
                                identificacion.c().get(2))
                                .trim();

                if (tipo.isBlank()) {
                        return;
                }

                if (tipo.equalsIgnoreCase("Chip")
                                || tipo.equalsIgnoreCase("Caravana")
                                || tipo.equalsIgnoreCase("Anillo")) {

                        tipoIdentificacion.setValue(
                                        normalizarTipoIdentificacion(
                                                        tipo));

                } else {

                        tipoIdentificacion.setValue(
                                        "Otro");

                        otroTipoIdentificacion.setText(
                                        tipo);
                }

                numeroIdentificacion.setText(
                                numero);
        }

        private void cargarIdentificacionTemporal(
                        String tipo,
                        String numero,
                        ComboBox<String> tipoIdentificacion,
                        TextField otroTipoIdentificacion,
                        TextField numeroIdentificacion) {

                tipoIdentificacion.setValue(null);

                otroTipoIdentificacion.clear();

                numeroIdentificacion.clear();

                if (tipo == null
                                || tipo.isBlank()) {

                        return;
                }

                String tipoLimpio = tipo.trim();

                if (tipoLimpio.equalsIgnoreCase("Chip")
                                || tipoLimpio.equalsIgnoreCase("Caravana")
                                || tipoLimpio.equalsIgnoreCase("Anillo")) {

                        tipoIdentificacion.setValue(
                                        normalizarTipoIdentificacion(
                                                        tipoLimpio));

                } else {

                        tipoIdentificacion.setValue(
                                        "Otro");

                        otroTipoIdentificacion.setText(
                                        tipoLimpio);
                }

                if (numero != null) {

                        numeroIdentificacion.setText(
                                        numero.trim());
                }
        }

        private String normalizarTipoIdentificacion(
                        String tipo) {

                if (tipo == null) {
                        return null;
                }

                if (tipo.equalsIgnoreCase("Chip")) {
                        return "Chip";
                }

                if (tipo.equalsIgnoreCase("Caravana")) {
                        return "Caravana";
                }

                if (tipo.equalsIgnoreCase("Anillo")) {
                        return "Anillo";
                }

                return tipo;
        }

        // ============================================================
        // RESUMEN DE MOVIMIENTOS DEL ACTA
        // ============================================================

        private HBox resumenMovimientos(
                        int ingreso) {

                /*
                 * IMPORTANTE:
                 *
                 * El resumen NO se calcula contando líneas de observaciones.
                 * Las observaciones son historial y pueden contener movimientos
                 * que posteriormente fueron revertidos.
                 *
                 * El DAO devuelve solamente movimientos actualmente ACTIVOS
                 * y suma sus cantidades reales.
                 *
                 * [0] Traslados
                 * [1] Fallecimientos
                 * [2] Nacimientos
                 * [3] Liberaciones
                 */
                int[] resumenDatos =
                                SistemaDAO.resumenMovimientosActa(ingreso);

                int trasladados = resumenDatos.length > 0
                                ? resumenDatos[0]
                                : 0;

                int fallecimientos = resumenDatos.length > 1
                                ? resumenDatos[1]
                                : 0;

                int nacimientos = resumenDatos.length > 2
                                ? resumenDatos[2]
                                : 0;

                int liberados = resumenDatos.length > 3
                                ? resumenDatos[3]
                                : 0;

                HBox resumen = new HBox(10);

                resumen.setFillHeight(true);

                resumen.getChildren().addAll(
                                tarjetaResumenMovimiento(
                                                "TRASLADADOS",
                                                trasladados),
                                tarjetaResumenMovimiento(
                                                "FALLECIMIENTOS",
                                                fallecimientos),
                                tarjetaResumenMovimiento(
                                                "NACIMIENTOS",
                                                nacimientos),
                                tarjetaResumenMovimiento(
                                                "LIBERADOS",
                                                liberados));

                return resumen;
        }

        private VBox tarjetaResumenMovimiento(
                        String titulo,
                        int cantidad) {

                Label tituloLabel = new Label(
                                titulo);

                tituloLabel.setStyle(
                                "-fx-font-size:10px;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:" + Estilos.GRIS + ";");

                Label cantidadLabel = new Label(
                                String.valueOf(cantidad));

                cantidadLabel.setStyle(
                                "-fx-font-size:24px;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

                VBox tarjeta = new VBox(
                                3,
                                tituloLabel,
                                cantidadLabel);

                tarjeta.setAlignment(
                                Pos.CENTER_LEFT);

                tarjeta.setPadding(
                                new Insets(
                                                10,
                                                14,
                                                10,
                                                14));

                tarjeta.setMinWidth(145);
                tarjeta.setPrefWidth(160);
                tarjeta.setMaxWidth(Double.MAX_VALUE);

                tarjeta.setStyle(
                                "-fx-background-color:" + Estilos.VERDE_SUAVE + ";" +
                                "-fx-background-radius:10;" +
                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                "-fx-border-radius:10;");

                HBox.setHgrow(
                                tarjeta,
                                Priority.ALWAYS);

                return tarjeta;
        }

        // ============================================================
        // VOLVER AL WIZARD EXISTENTE
        // ============================================================

        private void abrirWizardIngresoDesdeId(
                        int ingreso) {

                String numero = scalarIngreso(
                                ingreso,
                                "numero_acta");

                if (numero == null
                                || numero.isBlank()) {

                        error(
                                        "No se pudo abrir el acta",
                                        "No se encontró el número de acta.");

                        return;
                }

                List<SistemaDAO.Fila> filas = SistemaDAO.ingresos(
                                numero);

                SistemaDAO.Fila fila = filas.stream()
                                .filter(x -> x.id() == ingreso)
                                .findFirst()
                                .orElse(null);

                if (fila == null) {

                        error(
                                        "No se pudo abrir el acta",
                                        "No se encontró el registro seleccionado.");

                        return;
                }

                abrirWizardIngreso(
                                ingreso,
                                fila);
        }

        // ============================================================
        // MODAL BASE
        // ============================================================

        private VBox modalBase(
                        Node step,
                        Node ayuda,
                        Node form,
                        Node actions) {

                VBox content = new VBox(12);

                content.setPadding(
                                new Insets(20));

                content.setFillWidth(
                                true);

                content.setStyle(
                                "-fx-background-color:white;"
                                                + "-fx-background-radius:18;"
                                                + "-fx-border-color:"
                                                + Estilos.BORDE
                                                + ";"
                                                + "-fx-border-radius:18;"
                                                + "-fx-effect:dropshadow("
                                                + "gaussian,rgba(0,0,0,.22),24,0,0,8"
                                                + ");");

                StackPane header = cabeceraModal(
                                step,
                                this::cerrarModal);

                content.getChildren().addAll(
                                header,
                                ayuda,
                                form,
                                actions);

                return content;
        }

        // ============================================================
        // CONFIRMACIÓN
        // ============================================================

        private void confirmarFinalizacion(
                        String mensaje,
                        java.util.function.Consumer<ButtonType> respuesta) {

                Alert confirmacion = new Alert(
                                Alert.AlertType.CONFIRMATION);

                confirmacion.setTitle(
                                "Tatú Carreta");

                confirmacion.setHeaderText(
                                "Finalizar acta");

                confirmacion.setContentText(
                                mensaje);

                confirmacion.showAndWait()
                                .ifPresent(
                                                respuesta);
        }

        private void mostrarAdvertenciaSinAnimales() {

                Alert alert = new Alert(
                                Alert.AlertType.WARNING);

                alert.setTitle(
                                "Tatú Carreta");

                alert.setHeaderText(
                                "No se puede finalizar el acta");

                alert.setContentText(
                                "Debés agregar al menos un animal antes de finalizar el acta.");

                alert.showAndWait();
        }

        // ============================================================
        // UTILIDADES DE ANIMALES
        // ============================================================

        private String nombreAnimal(
                        int idAnimal) {

                return SistemaDAO.animalesOpciones()
                                .stream()
                                .filter(o -> o != null
                                                && o.id() == idAnimal)
                                .map(
                                                SistemaDAO.Opcion::texto)
                                .findFirst()
                                .orElse(
                                                "Animal #" + idAnimal);
        }

        private String identificacionTexto(
                        String tipo,
                        String numero) {

                if (tipo == null
                                || tipo.isBlank()) {

                        return "";
                }

                if (numero == null
                                || numero.isBlank()) {

                        return tipo;
                }

                return tipo
                                + " · "
                                + numero;
        }

        private String valorSeguro(
                        String value) {

                return value == null
                                ? ""
                                : value;
        }

        // ============================================================
        // UTILIDADES VISUALES
        // ============================================================

        private Label tituloModal(
                        String text) {

                Label l = new Label(
                                valorSeguro(text));

                l.setStyle(
                                "-fx-font-size:21px;"
                                                + "-fx-font-weight:bold;"
                                                + "-fx-text-fill:"
                                                + Estilos.VERDE_OSCURO
                                                + ";");

                return l;
        }

        private Label subtituloModal(
                        String text) {

                Label l = new Label(
                                valorSeguro(text));

                l.setWrapText(
                                true);

                l.setStyle(
                                "-fx-font-size:12px;"
                                                + "-fx-text-fill:"
                                                + Estilos.GRIS
                                                + ";");

                return l;
        }

        private Label paso(
                        String numero,
                        String titulo) {

                Label l = new Label(
                                valorSeguro(numero)
                                                + "  ·  "
                                                + valorSeguro(titulo));

                l.setStyle(
                                "-fx-font-size:11px;"
                                                + "-fx-font-weight:bold;"
                                                + "-fx-text-fill:"
                                                + Estilos.VERDE_MEDIO
                                                + ";");

                return l;
        }

        private Label seccion(
                        String text) {

                Label l = new Label(
                                valorSeguro(text));

                l.setStyle(
                                "-fx-font-size:11px;"
                                                + "-fx-font-weight:bold;"
                                                + "-fx-text-fill:"
                                                + Estilos.VERDE_OSCURO
                                                + ";");

                return l;
        }

        private Label etiqueta(
                        String text) {

                Label l = new Label(
                                valorSeguro(text));

                l.setStyle(
                                "-fx-font-size:11px;"
                                                + "-fx-font-weight:bold;"
                                                + "-fx-text-fill:"
                                                + Estilos.GRIS
                                                + ";");

                return l;
        }

        private void addForm(
                        GridPane g,
                        int row,
                        String label,
                        Region node,
                        boolean required) {

                if (g == null || node == null) {
                        return;
                }

                Label l = new Label(
                                (required
                                                ? "* "
                                                : "")
                                                + valorSeguro(label));

                l.setStyle(
                                "-fx-font-weight:bold;"
                                                + "-fx-text-fill:"
                                                + Estilos.TEXTO
                                                + ";"
                                                + "-fx-font-size:12px;");

                g.add(
                                l,
                                0,
                                row);

                g.add(
                                node,
                                1,
                                row);

                GridPane.setHgrow(
                                node,
                                Priority.ALWAYS);

                if (node instanceof Control control) {

                        control.setMaxWidth(
                                        Double.MAX_VALUE);
                }

                if (required) {
                        aplicarValidacionObligatoria(node, () -> true);
                }
        }

        private void aplicarValidacionObligatoria(Region campo,
                        java.util.function.BooleanSupplier requerido) {
                Runnable actualizar = () -> actualizarBordeObligatorio(campo, requerido.getAsBoolean());
                escucharCambiosObligatorios(campo, actualizar);
                actualizar.run();
        }

        private void actualizarBordeObligatorio(Node nodo, boolean marcar) {
                if (nodo instanceof TextInputControl || nodo instanceof ComboBoxBase<?>) {
                        Region entrada = (Region) nodo;
                        String estiloBase = entrada.getProperties().computeIfAbsent(
                                        "estiloBaseObligatorio", k -> entrada.getStyle() == null ? "" : entrada.getStyle()).toString();
                        entrada.setStyle(marcar
                                        ? estiloBase + ";-fx-border-color:#C62828;-fx-border-width:2;-fx-border-radius:8;"
                                        : estiloBase);
                } else if (nodo instanceof Parent padre) {
                        padre.getChildrenUnmodifiable().forEach(hijo -> actualizarBordeObligatorio(hijo, marcar));
                }
        }

        private void escucharCambiosObligatorios(Node nodo, Runnable actualizar) {
                if (nodo instanceof TextInputControl texto) {
                        texto.textProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof ComboBoxBase<?> combo) {
                        combo.valueProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof Parent padre) {
                        padre.getChildrenUnmodifiable().forEach(hijo -> escucharCambiosObligatorios(hijo, actualizar));
                }
        }

        // ============================================================
        // VALIDACIONES
        // ============================================================

        private void validarActa(
                        TextField acta,
                        DatePicker fecha,
                        TextField organismo,
                        TextField responsable,
                        TextField recibe,
                        TextField procedencia,
                        TextField motivo) {

                if (acta == null
                                || fecha == null
                                || organismo == null
                                || responsable == null
                                || recibe == null
                                || procedencia == null
                                || motivo == null) {

                        throw new IllegalArgumentException(
                                        "No se pudieron validar los datos del acta.");
                }

                if (fecha.getValue() == null) {

                        throw new IllegalArgumentException(
                                        "Seleccioná la fecha del acta.");
                }

                if (!validar(
                                acta.getText(),
                                organismo.getText(),
                                responsable.getText(),
                                recibe.getText(),
                                procedencia.getText(),
                                motivo.getText())) {

                        throw new IllegalArgumentException(
                                        "Completá todos los campos obligatorios.");
                }
        }

        private void setIndividualEnabled(
                        Node node,
                        boolean enabled) {

                if (node == null) {
                        return;
                }

                if (node instanceof Control control) {

                        control.setDisable(
                                        !enabled);

                        return;
                }

                if (node instanceof Parent parent) {

                        for (Node child : parent.getChildrenUnmodifiable()) {

                                setIndividualEnabled(
                                                child,
                                                enabled);
                        }
                }
        }

        // ============================================================
        // UTILIDADES
        // ============================================================

        private int parseCantidad(
                        String value) {

                if (value == null
                                || value.isBlank()) {

                        return 0;
                }

                try {

                        return Integer.parseInt(
                                        value.trim());

                } catch (NumberFormatException e) {

                        return 0;
                }
        }

        private LocalDate parseFecha(
                        String value) {

                if (value == null
                                || value.isBlank()) {

                        return LocalDate.now();
                }

                try {

                        return LocalDate.parse(
                                        value.trim());

                } catch (Exception e) {

                        return LocalDate.now();
                }
        }

        private String formatearFecha(
                        String value) {

                if (value == null
                                || value.isBlank()) {

                        return "";
                }

                try {

                        return LocalDate
                                        .parse(
                                                        value.trim())
                                        .format(
                                                        fechaFormato);

                } catch (Exception e) {

                        return value;
                }
        }

        private String scalarIngreso(
                        int id,
                        String expression) {

                if (id <= 0
                                || expression == null
                                || expression.isBlank()) {

                        return "";
                }

                return SistemaDAO.scalar(
                                "SELECT "
                                                + expression
                                                + " FROM ingresos WHERE id_ingreso="
                                                + id);
        }
}
