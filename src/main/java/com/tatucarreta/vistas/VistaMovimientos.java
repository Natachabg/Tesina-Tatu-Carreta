package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.Estilos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class VistaMovimientos extends VistaCRUD {

        private final StackPane modalLayer = new StackPane();
        private final StackPane subModalLayer = new StackPane();

        public VistaMovimientos() {

                super(
                                "Movimientos",
                                "Historial de ingresos, traslados, liberaciones y fallecimientos.",
                                new String[] {
                                                "Animal",
                                                "Acta",
                                                "Fecha",
                                                "Tipo",
                                                "Cantidad",
                                                "Sexo",
                                                "Identificación",
                                                "Destino",
                                                "Estado"
                                });

                mostrarBotonEliminar(false);
                mostrarBotonInactivos(false);

                agregar.setText("＋ AGREGAR MOVIMIENTO");

                prepararOverlay();

                // ------------------------------------------------------------
                // DOBLE CLICK → DETALLE DEL MOVIMIENTO
                // ------------------------------------------------------------
                // El botón CRUD MODIFICAR sigue usando editarSeleccionado()
                // y abre directamente el formulario de modificación.
                // El doble click, en cambio, muestra primero la información.
                tabla.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

                        if (e.getButton() == MouseButton.PRIMARY &&
                                        e.getClickCount() == 2) {

                                SistemaDAO.Fila fila = tabla.getSelectionModel().getSelectedItem();

                                if (fila != null) {
                                        e.consume();
                                        mostrarDetalle(fila);
                                }
                        }
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

                if (subModalLayer.isVisible()) {
                        subModalLayer.resizeRelocate(
                                        0,
                                        0,
                                        getWidth(),
                                        getHeight());
                        subModalLayer.toFront();
                } else if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }
        }

        private void prepararOverlay() {

                modalLayer.setManaged(false);
                modalLayer.setVisible(false);
                modalLayer.setPickOnBounds(true);

                modalLayer.setStyle(
                                "-fx-background-color: rgba(0, 0, 0, 0.60);");

                getChildren().addAll(
                                modalLayer,
                                subModalLayer);

                subModalLayer.setManaged(false);
                subModalLayer.setVisible(false);
                subModalLayer.setPickOnBounds(true);
                subModalLayer.setStyle(
                                "-fx-background-color: rgba(0, 0, 0, 0.35);");
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
                                Math.max(280, availableHeight - 80));

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

        private void cerrarModal() {

                modalLayer.getChildren().clear();

                modalLayer.setVisible(false);
                modalLayer.setManaged(false);

                requestLayout();
        }

        private void mostrarSubModal(
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
                                Math.max(360, availableWidth - 100));

                double height = Math.min(
                                preferredHeight,
                                Math.max(280, availableHeight - 100));

                content.setMinSize(0, 0);
                content.setPrefSize(width, height);
                content.setMaxSize(width, height);

                subModalLayer.getChildren().setAll(content);
                StackPane.setAlignment(content, Pos.CENTER);

                subModalLayer.setManaged(false);
                subModalLayer.setVisible(true);
                subModalLayer.resizeRelocate(
                                0, 0, getWidth(), getHeight());
                subModalLayer.toFront();

                applyCss();
                layout();
                subModalLayer.applyCss();
                subModalLayer.layout();
        }

        private void cerrarSubModal() {

                subModalLayer.getChildren().clear();
                subModalLayer.setVisible(false);
                subModalLayer.setManaged(false);

                if (modalLayer.isVisible()) {
                        modalLayer.toFront();
                }

                requestLayout();
        }

        // ============================================================
        // CRUD
        // ============================================================

        @Override
        protected List<SistemaDAO.Fila> consultar(String q) {
                return SistemaDAO.movimientos(q);
        }

        @Override
        protected List<SistemaDAO.Fila> consultarInactivos(String q) {
                return SistemaDAO.movimientosInactivos(q);
        }

        @Override
        protected void activarRegistro(int id) {

                SistemaDAO.activar(
                                "movimientos",
                                "id_movimiento",
                                id);
        }

        @Override
        protected void agregarNuevo() {
                mostrarFormularioMovimiento(null, false);
        }

        @Override
        protected void editarSeleccionado() {

                SistemaDAO.Fila fila = tabla.getSelectionModel().getSelectedItem();

                if (fila == null) {
                        return;
                }

                mostrarFormularioMovimiento(fila, true);
        }

        @Override
        protected void desactivar(int id) {

                if (confirmarOK(
                                "Enviar movimiento a inactivos",
                                "¿Estás segura de que querés quitar este movimiento del listado activo? El historial se conserva.")) {

                        SistemaDAO.baja(
                                        "movimientos",
                                        "id_movimiento",
                                        id);

                        cargar(buscador.getText());
                }
        }

        // ============================================================
        // FORMULARIO
        // ============================================================

        private void mostrarFormularioMovimiento(
                        SistemaDAO.Fila movimiento,
                        boolean modoEdicion) {

                final int movimientoId = movimiento == null
                                ? 0
                                : movimiento.id();

                final int detalleInicial = modoEdicion
                                ? obtenerDetalleMovimiento(movimientoId)
                                : 0;

                final int plantelInicial = modoEdicion
                                ? obtenerPlantelMovimiento(movimientoId)
                                : 0;

                final int habitaculoInicial = modoEdicion
                                ? obtenerHabitaculoMovimiento(movimientoId)
                                : 0;

                // --------------------------------------------------------
                // DATOS INICIALES
                // --------------------------------------------------------

                String fechaInicial = "";
                String tipoInicial = "";
                String sexoInicial = "";
                String destinoInicial = "";
                String observacionesIniciales = "";

                final int cantidadInicial;
                int ingresoInicial;

                if (movimiento != null) {

                        fechaInicial = valorSeguro(
                                        movimiento.c().size() > 3
                                                        ? movimiento.c().get(3)
                                                        : "");

                        tipoInicial = valorSeguro(
                                        movimiento.c().size() > 4
                                                        ? movimiento.c().get(4)
                                                        : "");

                        cantidadInicial = parseCantidad(
                                        movimiento.c().size() > 5
                                                        ? movimiento.c().get(5)
                                                        : "");

                        sexoInicial = valorSeguro(
                                        movimiento.c().size() > 6
                                                        ? movimiento.c().get(6)
                                                        : "");

                        destinoInicial = valorSeguro(
                                        movimiento.c().size() > 8
                                                        ? movimiento.c().get(8)
                                                        : "");

                        try {
                                SistemaDAO.MovimientoDetalle detalleMovimiento = SistemaDAO
                                                .detalleMovimiento(movimiento.id());

                                observacionesIniciales = detalleMovimiento.observaciones() == null
                                                ? ""
                                                : detalleMovimiento.observaciones();
                        } catch (Exception ignored) {
                                observacionesIniciales = "";
                        }

                        ingresoInicial = SistemaDAO.ingresoDeMovimiento(
                                        movimiento.id());

                        // Respaldo: si el movimiento tiene un detalle de acta,
                        // recuperar el ingreso directamente desde ese detalle.
                        // Esto evita tratarlo como movimiento directo de plantel
                        // cuando la consulta principal no devuelve el acta.
                        if (ingresoInicial <= 0 && detalleInicial > 0) {
                                try {
                                        ingresoInicial = Integer.parseInt(
                                                        SistemaDAO.scalar(
                                                                        "SELECT COALESCE(id_ingreso,0) " +
                                                                        "FROM detalle_ingreso " +
                                                                        "WHERE id_detalle=" + detalleInicial));
                                } catch (Exception ignored) {
                                        ingresoInicial = 0;
                                }
                        }

                } else {

                        cantidadInicial = 0;
                        ingresoInicial = 0;
                }

                final boolean movimientoSoloPlantel = modoEdicion &&
                                plantelInicial > 0 &&
                                ingresoInicial <= 0;

                // --------------------------------------------------------
                // ACTA
                // --------------------------------------------------------

                ComboBox<SistemaDAO.Opcion> acta = Estilos.combo();

                acta.setPromptText(
                                "Seleccioná un acta");

                acta.getItems().setAll(
                                SistemaDAO.actasConDisponibilidad());

                if (modoEdicion && ingresoInicial > 0) {

                        boolean existeActa = false;
                        for (SistemaDAO.Opcion opcion : acta.getItems()) {
                                if (opcion.id() == ingresoInicial) {
                                        existeActa = true;
                                        break;
                                }
                        }

                        if (!existeActa) {

                                List<SistemaDAO.Opcion> todas = SistemaDAO.opciones(
                                                "SELECT id_ingreso," +
                                                                "numero_acta || ' · ' || fecha_ingreso " +
                                                                "FROM ingresos " +
                                                                "WHERE id_ingreso=" +
                                                                ingresoInicial);

                                if (!todas.isEmpty()) {
                                        acta.getItems().add(todas.get(0));
                                }
                        }

                        for (SistemaDAO.Opcion opcion : acta.getItems()) {

                                if (opcion.id() == ingresoInicial) {

                                        acta.setValue(opcion);
                                        break;
                                }
                        }
                }

                // --------------------------------------------------------
                // ANIMAL
                // --------------------------------------------------------

                ComboBox<SistemaDAO.DetalleMovimiento> animal = Estilos.combo();

                animal.setPromptText(
                                movimientoSoloPlantel
                                                ? "Plantel Permanente"
                                                : "Primero seleccioná un acta");

                if (movimientoSoloPlantel) {

                        acta.getItems().setAll(
                                        new SistemaDAO.Opcion(
                                                        -1,
                                                        "Plantel Permanente · Movimiento directo"));

                        acta.setValue(
                                        acta.getItems().get(0));

                        acta.setDisable(true);
                }

                // --------------------------------------------------------
                // FECHA
                // --------------------------------------------------------

                DatePicker fecha = Estilos.fecha();

                if (!fechaInicial.isBlank()) {

                        try {

                                fecha.setValue(
                                                LocalDate.parse(fechaInicial));

                        } catch (Exception ignored) {

                                fecha.setValue(LocalDate.now());
                        }

                } else {

                        fecha.setValue(LocalDate.now());
                }

                // --------------------------------------------------------
                // TIPO
                // --------------------------------------------------------

                ComboBox<String> tipo = Estilos.combo();

                tipo.getItems().setAll(
                                "Traslado",
                                "Liberación",
                                "Fallecimiento",
                                "Otro");

                if (!tipoInicial.isBlank()) {

                        tipo.setValue(tipoInicial);

                } else {

                        tipo.setValue("Traslado");
                }

                // --------------------------------------------------------
                // CANTIDAD
                // --------------------------------------------------------

                TextField cantidad = Estilos.entero(
                                "Cantidad",
                                6);

                limitarCaracteres(cantidad, "Cantidad");

                if (cantidadInicial > 0) {

                        cantidad.setText(
                                        String.valueOf(cantidadInicial));
                }

                // --------------------------------------------------------
                // DATOS INDIVIDUALES
                // --------------------------------------------------------

                ComboBox<String> sexo = Estilos.combo();

                sexo.getItems().setAll(
                                "Macho",
                                "Hembra",
                                "Indeterminado");

                TextField edad = Estilos.campo("Edad");

                TextField peso = Estilos.campo("Peso");

                sexo.setDisable(true);
                edad.setEditable(false);
                peso.setEditable(false);

                GridPane individuales = new GridPane();

                individuales.setHgap(12);
                individuales.setVgap(8);

                individuales.add(
                                etiqueta("Sexo"),
                                0,
                                0);

                individuales.add(
                                sexo,
                                1,
                                0);

                individuales.add(
                                etiqueta("Edad"),
                                2,
                                0);

                individuales.add(
                                edad,
                                3,
                                0);

                individuales.add(
                                etiqueta("Peso"),
                                0,
                                1);

                individuales.add(
                                peso,
                                1,
                                1);

                for (int i = 0; i < 4; i++) {

                        ColumnConstraints cc = new ColumnConstraints();

                        cc.setPercentWidth(
                                        i % 2 == 0
                                                        ? 18
                                                        : 32);

                        individuales
                                        .getColumnConstraints()
                                        .add(cc);
                }

                individuales.setVisible(false);
                individuales.setManaged(false);

                // --------------------------------------------------------
                // DESTINO
                // --------------------------------------------------------

                ComboBox<String> destinoTipo = Estilos.combo();

                destinoTipo.getItems().setAll(
                                "Plantel Permanente",
                                "Otro");

                ComboBox<String> ubicacionPlantel = Estilos.combo();

                ubicacionPlantel.getItems().setAll(
                                "Habitáculo",
                                "Campo abierto");

                ubicacionPlantel.setPromptText(
                                "Ubicación dentro del Plantel");

                destinoTipo.setPromptText(
                                "Seleccioná un destino");

                TextField destinoOtro = Estilos.campo("¿Dónde?");

                limitarCaracteres(destinoOtro, "Destino");

                TextField destinoLiberacion = Estilos.campo("Destino de la liberación");

                limitarCaracteres(destinoLiberacion, "Destino de la liberación");

                VBox destinoBox = new VBox(
                                6,
                                destinoTipo,
                                destinoOtro,
                                destinoLiberacion);

                ubicacionPlantel.setVisible(false);
                ubicacionPlantel.setManaged(false);

                destinoBox.setVisible(false);
                destinoBox.setManaged(false);

                destinoOtro.setVisible(false);
                destinoOtro.setManaged(false);

                destinoLiberacion.setVisible(false);
                destinoLiberacion.setManaged(false);

                // --------------------------------------------------------
                // HABITÁCULO
                // --------------------------------------------------------

                ComboBox<SistemaDAO.Opcion> habitaculo = Estilos.combo();

                habitaculo.setPromptText(
                                "Seleccioná un habitáculo");

                habitaculo.getItems().setAll(
                                SistemaDAO.habitaculoOpciones());

                Button nuevoHabitaculo = Estilos.boton("＋");

                nuevoHabitaculo.setMinWidth(52);
                nuevoHabitaculo.setPrefWidth(52);
                nuevoHabitaculo.setMaxWidth(52);

                nuevoHabitaculo.setTooltip(
                                new Tooltip("Agregar un nuevo habitáculo"));

                Label estadoHabitaculos = new Label();

                estadoHabitaculos.setWrapText(true);

                estadoHabitaculos.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                HBox habitaculoSelector = new HBox(
                                8,
                                habitaculo,
                                nuevoHabitaculo);

                HBox.setHgrow(
                                habitaculo,
                                Priority.ALWAYS);

                VBox habitaculoBox = new VBox(
                                5,
                                habitaculoSelector,
                                estadoHabitaculos);

                habitaculoBox.setVisible(false);
                habitaculoBox.setManaged(false);

                nuevoHabitaculo.setOnAction(
                                e -> mostrarNuevoHabitaculo(
                                                habitaculo,
                                                estadoHabitaculos,
                                                plantelInicial));

                Label etiquetaHabitaculo = etiqueta("Habitáculo");

                etiquetaHabitaculo.setVisible(false);
                etiquetaHabitaculo.setManaged(false);

                // --------------------------------------------------------
                // OBSERVACIONES
                // --------------------------------------------------------

                TextArea observaciones = Estilos.area("Observaciones");

                observaciones.setPrefRowCount(3);
                observaciones.setWrapText(true);
                observaciones.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-insets:0;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-width:1;" +
                                                "-fx-border-radius:10;" +
                                                "-fx-background-radius:10;" +
                                                "-fx-padding:0;");

                if (!observacionesIniciales.isBlank()) {
                        observaciones.setText(observacionesIniciales);
                        observaciones.positionCaret(observaciones.getText().length());
                }

                // --------------------------------------------------------
                // DISPONIBILIDAD
                // --------------------------------------------------------

                Label disponibilidad = new Label(
                                "Seleccioná un animal para ver su disponibilidad.");

                disponibilidad.setWrapText(true);

                disponibilidad.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                // --------------------------------------------------------
                // FORMULARIO EN 2 COLUMNAS
                // --------------------------------------------------------

                Label etiquetaDestino = etiqueta("Destino");
                etiquetaDestino.setVisible(false);
                etiquetaDestino.setManaged(false);

                Label etiquetaUbicacion = etiqueta("Ubicación");
                etiquetaUbicacion.setVisible(false);
                etiquetaUbicacion.setManaged(false);

                GridPane form = new GridPane();

                form.setHgap(14);
                form.setVgap(10);

                ColumnConstraints etiquetaIzquierda = new ColumnConstraints();
                etiquetaIzquierda.setMinWidth(92);
                etiquetaIzquierda.setPrefWidth(105);
                etiquetaIzquierda.setMaxWidth(125);

                ColumnConstraints campoIzquierdo = new ColumnConstraints();
                campoIzquierdo.setMinWidth(0);
                campoIzquierdo.setPrefWidth(0);
                campoIzquierdo.setHgrow(Priority.ALWAYS);
                campoIzquierdo.setFillWidth(true);

                ColumnConstraints etiquetaDerecha = new ColumnConstraints();
                etiquetaDerecha.setMinWidth(92);
                etiquetaDerecha.setPrefWidth(105);
                etiquetaDerecha.setMaxWidth(125);

                ColumnConstraints campoDerecho = new ColumnConstraints();
                campoDerecho.setMinWidth(0);
                campoDerecho.setPrefWidth(0);
                campoDerecho.setHgrow(Priority.ALWAYS);
                campoDerecho.setFillWidth(true);

                form.getColumnConstraints().setAll(
                                etiquetaIzquierda,
                                campoIzquierdo,
                                etiquetaDerecha,
                                campoDerecho);

                form.setPadding(
                                new Insets(8, 0, 0, 0));

                // Orden visual solicitado:
                // IZQUIERDA: Fecha → Acta → Animal
                // DERECHA: Tipo → Cantidad → Destino
                agregarCampoDosColumnas(
                                form, 0,
                                "Fecha", fecha, true,
                                "Tipo", tipo, true);

                agregarCampoDosColumnas(
                                form, 1,
                                "Acta", acta, true,
                                "Cantidad", cantidad, true);

                agregarCampoDosColumnas(
                                form, 2,
                                "Animal", animal, true,
                                "Destino", destinoBox, false);

                // La etiqueta de destino se controla junto con su contenido.
                // Se reemplaza la etiqueta genérica agregada por el helper.
                Node etiquetaDestinoGenerada = form.getChildren().stream()
                                .filter(n -> GridPane.getColumnIndex(n) != null
                                                && GridPane.getColumnIndex(n) == 2
                                                && GridPane.getRowIndex(n) != null
                                                && GridPane.getRowIndex(n) == 2)
                                .findFirst()
                                .orElse(null);

                if (etiquetaDestinoGenerada != null) {
                        form.getChildren().remove(etiquetaDestinoGenerada);
                }

                form.add(etiquetaDestino, 2, 2);

                // Ubicación dentro del Plantel ocupa toda la fila.
                form.add(
                                etiquetaUbicacion,
                                0, 3);

                form.add(
                                ubicacionPlantel,
                                1, 3,
                                3, 1);

                GridPane.setHgrow(
                                ubicacionPlantel,
                                Priority.ALWAYS);

                // Habitáculo ocupa toda la fila para que el selector y el +
                // tengan espacio suficiente.
                form.add(
                                etiquetaHabitaculo,
                                0, 4);

                form.add(
                                habitaculoBox,
                                1, 4,
                                3, 1);

                GridPane.setHgrow(
                                habitaculoBox,
                                Priority.ALWAYS);

                Runnable validarFecha = aplicarValidacionObligatoria(fecha, () -> true);
                Runnable validarActa = aplicarValidacionObligatoria(acta, () -> true);
                Runnable validarAnimal = aplicarValidacionObligatoria(animal, () -> true);
                Runnable validarTipo = aplicarValidacionObligatoria(tipo, () -> true);
                Runnable validarCantidad = aplicarValidacionObligatoria(cantidad, () -> true);
                Runnable validarTipoDestino = aplicarValidacionObligatoria(destinoTipo,
                                () -> "Traslado".equals(tipo.getValue()));
                Runnable validarLiberacion = aplicarValidacionObligatoria(destinoLiberacion,
                                () -> "Liberación".equals(tipo.getValue()));
                Runnable validarDestinoOtro = aplicarValidacionObligatoria(destinoOtro,
                                () -> "Traslado".equals(tipo.getValue()) && "Otro".equals(destinoTipo.getValue()));
                Runnable validarUbicacion = aplicarValidacionObligatoria(ubicacionPlantel,
                                () -> "Traslado".equals(tipo.getValue()) && "Plantel Permanente".equals(destinoTipo.getValue()));
                Runnable validarHabitaculo = aplicarValidacionObligatoria(habitaculo,
                                () -> "Traslado".equals(tipo.getValue())
                                                && "Plantel Permanente".equals(destinoTipo.getValue())
                                                && "Habitáculo".equals(ubicacionPlantel.getValue()));
                Label disponibilidadHabitaculo = new Label();
                disponibilidadHabitaculo.setWrapText(true);
                disponibilidadHabitaculo.setStyle("-fx-font-size:10px;-fx-text-fill:" + Estilos.GRIS + ";");
                habitaculoBox.getChildren().add(disponibilidadHabitaculo);
                Runnable actualizarDisponibilidad = () -> {
                        SistemaDAO.Opcion seleccionado = habitaculo.getValue();
                        if (seleccionado == null) {
                                disponibilidadHabitaculo.setText(habitaculo.getItems().isEmpty()
                                                ? "No hay habitáculos disponibles con capacidad libre."
                                                : "Elegí un habitáculo para ver sus lugares disponibles.");
                                return;
                        }
                        int capacidad = SistemaDAO.capacidadHabitaculo(seleccionado.id());
                        int ocupados = SistemaDAO.ocupacionHabitaculo(seleccionado.id(), plantelInicial);
                        disponibilidadHabitaculo.setText("Capacidad: " + capacidad + " · Ocupados: " + ocupados
                                        + " · Disponibles: " + Math.max(0, capacidad - ocupados) + " lugar(es).");
                };
                habitaculo.valueProperty().addListener((o, anterior, nuevo) -> actualizarDisponibilidad.run());
                Runnable actualizarIndicadoresObligatorios = () -> {
                        boolean esTraslado = "Traslado".equals(tipo.getValue());
                        boolean esLiberacion = "Liberación".equals(tipo.getValue());
                        boolean esPlantel = esTraslado && "Plantel Permanente".equals(destinoTipo.getValue());
                        boolean esHabitaculo = esPlantel && "Habitáculo".equals(ubicacionPlantel.getValue());
                        etiquetaDestino.setText(esTraslado || esLiberacion ? "* Destino" : "Destino");
                        etiquetaUbicacion.setText(esPlantel ? "* Ubicación" : "Ubicación");
                        etiquetaHabitaculo.setText(esHabitaculo ? "* Habitáculo" : "Habitáculo");
                        validarFecha.run(); validarActa.run(); validarAnimal.run(); validarTipo.run(); validarCantidad.run();
                        validarTipoDestino.run(); validarLiberacion.run(); validarDestinoOtro.run();
                        validarUbicacion.run(); validarHabitaculo.run();
                        actualizarDisponibilidad.run();
                };
                tipo.valueProperty().addListener((o, anterior, nuevo) -> actualizarIndicadoresObligatorios.run());
                destinoTipo.valueProperty().addListener((o, anterior, nuevo) -> actualizarIndicadoresObligatorios.run());
                ubicacionPlantel.valueProperty().addListener((o, anterior, nuevo) -> actualizarIndicadoresObligatorios.run());
                actualizarIndicadoresObligatorios.run();

                // Observaciones SIEMPRE al final y ocupando las dos columnas.
                Label etiquetaObservaciones = etiqueta("Observaciones");

                form.add(
                                etiquetaObservaciones,
                                0, 5);

                form.add(
                                observaciones,
                                1, 5,
                                3, 1);

                GridPane.setHgrow(
                                observaciones,
                                Priority.ALWAYS);
                GridPane.setVgrow(
                                observaciones,
                                Priority.NEVER);

                // --------------------------------------------------------
                // TIPO → DESTINO
                // --------------------------------------------------------

                tipo.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {

                                        boolean esTraslado = "Traslado".equals(newValue);

                                        boolean esLiberacion = "Liberación".equals(newValue);

                                        boolean requiereDestino = esTraslado || esLiberacion;

                                        destinoBox.setVisible(requiereDestino);
                                        destinoBox.setManaged(requiereDestino);

                                        etiquetaDestino.setVisible(requiereDestino);
                                        etiquetaDestino.setManaged(requiereDestino);

                                        // Las liberaciones tienen destino libre:
                                        // no se selecciona Plantel Permanente/Otro.
                                        destinoTipo.setVisible(esTraslado);
                                        destinoTipo.setManaged(esTraslado);

                                        destinoLiberacion.setVisible(esLiberacion);
                                        destinoLiberacion.setManaged(esLiberacion);

                                        if (!esTraslado) {
                                                destinoTipo.setValue(null);
                                                ubicacionPlantel.setValue(null);
                                                destinoOtro.clear();

                                                habitaculo.setValue(null);
                                                habitaculoBox.setVisible(false);
                                                habitaculoBox.setManaged(false);
                                                etiquetaHabitaculo.setVisible(false);
                                                etiquetaHabitaculo.setManaged(false);
                                        }

                                        if (!esLiberacion) {
                                                destinoLiberacion.clear();
                                        }

                                        if (!requiereDestino) {
                                                destinoLiberacion.clear();
                                                return;
                                        }

                                        if (esTraslado && destinoTipo.getValue() == null) {
                                                destinoTipo.setValue("Plantel Permanente");
                                        }

                                        actualizarVisibilidadHabitaculo(
                                                        destinoTipo,
                                                        ubicacionPlantel,
                                                        habitaculo,
                                                        habitaculoBox,
                                                        etiquetaHabitaculo,
                                                        estadoHabitaculos,
                                                        nuevoHabitaculo,
                                                        plantelInicial);
                                });

                // --------------------------------------------------------
                // DESTINO → OTRO / HABITÁCULO
                // --------------------------------------------------------

                destinoTipo.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {

                                        boolean esOtro = "Otro".equals(newValue);

                                        boolean esPlantel = "Plantel Permanente".equals(newValue);

                                        destinoOtro.setVisible(esOtro);
                                        destinoOtro.setManaged(esOtro);

                                        ubicacionPlantel.setVisible(esPlantel);
                                        ubicacionPlantel.setManaged(esPlantel);

                                        if (!esOtro) {
                                                destinoOtro.clear();
                                        }

                                        if (!esPlantel) {
                                                ubicacionPlantel.setValue(null);
                                        }

                                        actualizarVisibilidadHabitaculo(
                                                        destinoTipo,
                                                        ubicacionPlantel,
                                                        habitaculo,
                                                        habitaculoBox,
                                                        etiquetaHabitaculo,
                                                        estadoHabitaculos,
                                                        nuevoHabitaculo,
                                                        plantelInicial);
                                });

                // --------------------------------------------------------
                // UBICACIÓN DENTRO DEL PLANTEL
                // --------------------------------------------------------

                ubicacionPlantel.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {

                                        boolean mostrarHabitaculo = "Plantel Permanente".equals(
                                                        destinoTipo.getValue())
                                                        &&
                                                        "Habitáculo".equals(newValue);

                                        habitaculoBox.setVisible(mostrarHabitaculo);
                                        habitaculoBox.setManaged(mostrarHabitaculo);

                                        etiquetaHabitaculo.setVisible(mostrarHabitaculo);
                                        etiquetaHabitaculo.setManaged(mostrarHabitaculo);

                                        etiquetaUbicacion.setVisible(
                                                        "Plantel Permanente".equals(destinoTipo.getValue()));
                                        etiquetaUbicacion.setManaged(
                                                        "Plantel Permanente".equals(destinoTipo.getValue()));

                                        if (!mostrarHabitaculo) {
                                                habitaculo.setValue(null);
                                        } else {
                                                actualizarVisibilidadHabitaculo(
                                                                destinoTipo,
                                                                ubicacionPlantel,
                                                                habitaculo,
                                                                habitaculoBox,
                                                                etiquetaHabitaculo,
                                                                estadoHabitaculos,
                                                                nuevoHabitaculo,
                                                                plantelInicial);
                                        }
                                });

                // --------------------------------------------------------
                // ACTA
                // --------------------------------------------------------

                acta.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {

                                        if (movimientoSoloPlantel) {
                                                return;
                                        }

                                        animal.getItems().clear();
                                        animal.setValue(null);

                                        disponibilidad.setText(
                                                        "Seleccioná un animal para ver su disponibilidad.");

                                        sexo.setValue(null);
                                        edad.clear();
                                        peso.clear();

                                        individuales.setVisible(false);
                                        individuales.setManaged(false);

                                        if (newValue == null) {
                                                return;
                                        }

                                        List<SistemaDAO.DetalleMovimiento> detalles = SistemaDAO.detallesMovimiento(
                                                        newValue.id());

                                        animal.getItems().setAll(detalles);

                                        configurarComboAnimales(animal);

                                        List<SistemaDAO.DetalleMovimiento> disponibles = detalles.stream()
                                                        .filter(
                                                                        d -> d.cantidadDisponible() > 0)
                                                        .toList();

                                        if (!modoEdicion &&
                                                        disponibles.size() == 1) {

                                                animal.setValue(
                                                                disponibles.get(0));

                                                return;
                                        }

                                        if (modoEdicion &&
                                                        detalleInicial > 0) {

                                                for (SistemaDAO.DetalleMovimiento d : detalles) {

                                                        if (d.idDetalle() == detalleInicial) {

                                                                animal.setValue(d);
                                                                break;
                                                        }
                                                }
                                        }
                                });

                if (movimientoSoloPlantel) {

                        int animalId = SistemaDAO.animalPorPlantel(
                                        plantelInicial);

                        String animalNombre = SistemaDAO.scalar(
                                        "SELECT COALESCE(nombre_vulgar,'') " +
                                                        "FROM animales WHERE id_animal=" +
                                                        animalId);

                        int actualPlantel = Math.max(
                                        SistemaDAO.cantidadPlantel(
                                                        plantelInicial),
                                        0);

                        int disponibleEdicion = actualPlantel + cantidadInicial;

                        SistemaDAO.DetalleMovimiento detallePlantel = new SistemaDAO.DetalleMovimiento(
                                        0,
                                        animalId,
                                        animalNombre,
                                        disponibleEdicion,
                                        disponibleEdicion,
                                        sexoInicial,
                                        "",
                                        "");

                        animal.getItems().setAll(
                                        detallePlantel);

                        // Mantiene la presentación del animal igual que en
                        // los movimientos asociados a un acta.
                        configurarComboAnimales(animal);

                        animal.setValue(
                                        detallePlantel);
                }

                // --------------------------------------------------------
                // ANIMAL
                // --------------------------------------------------------

                animal.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {

                                        if (newValue == null) {

                                                disponibilidad.setText(
                                                                "Seleccioná un animal para ver su disponibilidad.");

                                                individuales.setVisible(false);
                                                individuales.setManaged(false);

                                                return;
                                        }

                                        disponibilidad.setText(
                                                        "Disponible: " +
                                                                        newValue.cantidadDisponible() +
                                                                        " de " +
                                                                        newValue.cantidadOriginal() +
                                                                        " · " +
                                                                        newValue.animal());

                                        boolean individual = newValue.cantidadOriginal() == 1;

                                        individuales.setVisible(individual);
                                        individuales.setManaged(individual);

                                        if (individual) {

                                                sexo.setValue(
                                                                newValue.sexo() == null ||
                                                                                newValue.sexo().isBlank()
                                                                                                ? null
                                                                                                : newValue.sexo());

                                                edad.setText(
                                                                newValue.edad() == null
                                                                                ? ""
                                                                                : newValue.edad());

                                                peso.setText(
                                                                newValue.peso() == null
                                                                                ? ""
                                                                                : newValue.peso());
                                        }
                                });

                if (movimientoSoloPlantel && animal.getValue() != null) {

                        disponibilidad.setText(
                                        "Disponible para modificar: " +
                                                        animal.getValue().cantidadDisponible() +
                                                        " · Plantel Permanente");

                        sexo.setValue(
                                        sexoInicial == null || sexoInicial.isBlank()
                                                        ? null
                                                        : sexoInicial);
                }

                // --------------------------------------------------------
                // DESTINO INICIAL
                // --------------------------------------------------------

                if (!destinoInicial.isBlank()) {

                        if ("Liberación".equalsIgnoreCase(tipoInicial)) {

                                destinoLiberacion.setText(destinoInicial);

                        } else if ("Plantel Permanente".equalsIgnoreCase(
                                        destinoInicial)
                                        || destinoInicial.toLowerCase().startsWith(
                                                        "plantel permanente")) {

                                destinoTipo.setValue(
                                                "Plantel Permanente");

                                if (destinoInicial.toLowerCase().contains("campo abierto")
                                                || habitaculoInicial <= 0) {
                                        ubicacionPlantel.setValue("Campo abierto");
                                } else {
                                        ubicacionPlantel.setValue("Habitáculo");
                                }

                        } else if ("Campo Abierto".equalsIgnoreCase(
                                        destinoInicial)
                                        || "Campo abierto".equalsIgnoreCase(destinoInicial)
                                        || destinoInicial.toLowerCase().startsWith("habitáculo:")
                                        || destinoInicial.toLowerCase().startsWith("habitáculo ·")
                                        || destinoInicial.toLowerCase().startsWith("habitáculo -")) {

                                // Compatibilidad con movimientos internos históricos.
                                if ("Traslado".equalsIgnoreCase(tipoInicial)) {

                                        destinoTipo.setValue(
                                                        "Plantel Permanente");

                                        if (destinoInicial.toLowerCase().startsWith("habitáculo")) {
                                                ubicacionPlantel.setValue("Habitáculo");
                                        } else {
                                                ubicacionPlantel.setValue("Campo abierto");
                                        }

                                } else {

                                        destinoTipo.setValue("Otro");
                                        destinoOtro.setText(
                                                        destinoInicial);
                                }

                        } else {

                                destinoTipo.setValue("Otro");

                                destinoOtro.setText(
                                                destinoInicial);
                        }
                }

                // --------------------------------------------------------
                // HABITÁCULO INICIAL
                // --------------------------------------------------------

                if (modoEdicion &&
                                habitaculoInicial > 0) {

                        for (SistemaDAO.Opcion opcion : habitaculo.getItems()) {

                                if (opcion.id() == habitaculoInicial) {

                                        habitaculo.setValue(opcion);
                                        break;
                                }
                        }
                }

                // --------------------------------------------------------
                // HABITÁCULO → AVISAR SI YA TIENE ANIMALES
                // --------------------------------------------------------
                // Durante la inicialización del formulario no se muestra el
                // aviso. Sólo se dispara cuando el usuario selecciona un
                // habitáculo manualmente.

                final boolean[] habitaculoListoParaConfirmar = new boolean[] { false };

                habitaculo.valueProperty().addListener(
                                (obs, viejo, nuevo) -> {

                                        if (!habitaculoListoParaConfirmar[0] ||
                                                        nuevo == null) {
                                                return;
                                        }

                                        int ocupacion = SistemaDAO.ocupacionHabitaculo(
                                                        nuevo.id(),
                                                        plantelInicial);

                                        if (ocupacion <= 0) {
                                                return;
                                        }

                                        String ocupantes = detalleOcupantesHabitaculo(
                                                        nuevo.id(),
                                                        plantelInicial);

                                        int capacidad = SistemaDAO.capacidadHabitaculo(
                                                        nuevo.id());

                                        Alert alerta = new Alert(
                                                        Alert.AlertType.CONFIRMATION);

                                        alerta.setTitle(
                                                        "Habitáculo con animales");

                                        alerta.setHeaderText(
                                                        "El habitáculo seleccionado ya está ocupado.");

                                        alerta.setContentText(
                                                        ocupantes +
                                                                        "\n\n" +
                                                                        "Ocupación: " +
                                                                        ocupacion +
                                                                        " de " +
                                                                        capacidad +
                                                                        "\n\n" +
                                                                        "¿Querés agregar igualmente el animal seleccionado?");

                                        ButtonType aceptar = new ButtonType(
                                                        "AGREGAR ANIMAL",
                                                        ButtonBar.ButtonData.YES);

                                        ButtonType cancelarAlerta = new ButtonType(
                                                        "CANCELAR",
                                                        ButtonBar.ButtonData.NO);

                                        alerta.getButtonTypes().setAll(
                                                        aceptar,
                                                        cancelarAlerta);

                                        ButtonType respuesta = alerta.showAndWait().orElse(
                                                        cancelarAlerta);

                                        if (respuesta != aceptar) {
                                                habitaculo.setValue(null);
                                        }
                                });

                habitaculoListoParaConfirmar[0] = true;

                // --------------------------------------------------------
                // BOTONES
                // --------------------------------------------------------

                Button cancelar = Estilos.botonSec("CANCELAR");

                Button guardar = Estilos.boton(
                                modoEdicion
                                                ? "✓ GUARDAR CAMBIOS"
                                                : "＋ GUARDAR MOVIMIENTO");

                Region espacioBotones = new Region();

                HBox.setHgrow(
                                espacioBotones,
                                Priority.ALWAYS);

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                espacioBotones,
                                guardar);

                acciones.setAlignment(
                                Pos.CENTER_LEFT);

                // --------------------------------------------------------
                // HEADER
                // --------------------------------------------------------

                Label titulo = tituloModal(
                                modoEdicion
                                                ? "MODIFICAR MOVIMIENTO"
                                                : "NUEVO MOVIMIENTO");

                Label subtitulo = subtituloModal(
                                modoEdicion
                                                ? "Modificá los datos del movimiento registrado."
                                                : "Seleccioná primero el acta. La disponibilidad se calcula automáticamente según los movimientos registrados.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                subtitulo),
                                () -> cerrarModal());

                // --------------------------------------------------------
                // CONTENT
                // --------------------------------------------------------
                // BorderPane mantiene los botones siempre abajo. El centro
                // contiene el formulario dentro de un ScrollPane para que,
                // cuando aparezcan más datos al seleccionar un acta, ningún
                // campo desaparezca ni se superponga.

                BorderPane content = new BorderPane();

                content.setPadding(
                                new Insets(20));

                content.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" +
                                                Estilos.BORDE +
                                                ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian," +
                                                "rgba(0,0,0,.28)," +
                                                "24,0,0,8);");

                VBox cuerpo = new VBox(
                                12,
                                form,
                                disponibilidad,
                                seccion(
                                                "DATOS DEL ACTA · SOLO CUANDO EL DETALLE ES INDIVIDUAL"),
                                individuales);

                cuerpo.setFillWidth(true);

                ScrollPane scroll = new ScrollPane(cuerpo);

                scroll.setFitToWidth(true);
                scroll.setFitToHeight(false);
                scroll.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;" +
                                                "-fx-border-color:transparent;");
                scroll.setPadding(new Insets(0));

                content.setTop(header);
                content.setCenter(scroll);
                content.setBottom(acciones);

                BorderPane.setAlignment(
                                header,
                                Pos.TOP_LEFT);

                BorderPane.setAlignment(
                                acciones,
                                Pos.CENTER_RIGHT);

                BorderPane.setMargin(
                                header,
                                new Insets(0, 0, 10, 0));

                BorderPane.setMargin(
                                acciones,
                                new Insets(12, 0, 0, 0));

                // --------------------------------------------------------
                // CANCELAR
                // --------------------------------------------------------

                cancelar.setOnAction(
                                e -> cerrarModal());

                // --------------------------------------------------------
                // GUARDAR
                // --------------------------------------------------------

                guardar.setOnAction(e -> {

                        try {

                                if (!movimientoSoloPlantel &&
                                                acta.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Seleccioná un acta.");
                                }

                                if (animal.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Seleccioná un animal del acta.");
                                }

                                if (fecha.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Seleccioná la fecha.");
                                }

                                if (tipo.getValue() == null) {

                                        throw new IllegalArgumentException(
                                                        "Seleccioná el tipo de movimiento.");
                                }

                                int n = parseCantidad(
                                                cantidad.getText());

                                if (n <= 0) {

                                        throw new IllegalArgumentException(
                                                        "Ingresá una cantidad mayor que cero.");
                                }

                                int disponible = animal.getValue()
                                                .cantidadDisponible();

                                if (!modoEdicion &&
                                                n > disponible) {

                                        throw new IllegalArgumentException(
                                                        "No hay suficiente cantidad disponible.\n\n" +
                                                                        "Disponible: " +
                                                                        disponible +
                                                                        "\n" +
                                                                        "Solicitado: " +
                                                                        n);
                                }

                                int disponibleParaModificar = movimientoSoloPlantel
                                                ? disponible
                                                : disponible + cantidadInicial;

                                if (modoEdicion &&
                                                n > disponibleParaModificar) {

                                        throw new IllegalArgumentException(
                                                        "No hay suficiente cantidad disponible.\n\n" +
                                                                        "Disponible para modificar: " +
                                                                        disponibleParaModificar +
                                                                        "\n" +
                                                                        "Solicitado: " +
                                                                        n);
                                }

                                // ------------------------------------------------
                                // DESTINO
                                // ------------------------------------------------

                                String destino = "";
                                Integer idHabitaculo = null;

                                boolean esTraslado = "Traslado".equals(tipo.getValue());

                                boolean esLiberacion = "Liberación".equals(tipo.getValue());

                                boolean requiereDestino = esTraslado || esLiberacion;

                                if (esLiberacion) {

                                        destino = destinoLiberacion.getText().trim();

                                        if (destino.isBlank()) {
                                                throw new IllegalArgumentException(
                                                                "Indicá el destino de la liberación.");
                                        }

                                } else if (esTraslado) {

                                        if (destinoTipo.getValue() == null) {

                                                throw new IllegalArgumentException(
                                                                "Seleccioná un destino.");
                                        }

                                        if ("Otro".equals(destinoTipo.getValue())) {

                                                destino = destinoOtro.getText().trim();

                                                if (destino.isBlank()) {

                                                        throw new IllegalArgumentException(
                                                                        "Indicá dónde se realizará el movimiento.");
                                                }

                                        } else {

                                                destino = destinoTipo.getValue();
                                        }

                                        // --------------------------------------------
                                        // PLANTEL PERMANENTE
                                        // --------------------------------------------

                                        if ("Plantel Permanente".equals(destino)) {

                                                if (ubicacionPlantel.getValue() == null) {
                                                        throw new IllegalArgumentException(
                                                                        "Seleccioná si el animal irá a un habitáculo o a campo abierto.");
                                                }

                                                if ("Habitáculo".equals(ubicacionPlantel.getValue())) {

                                                        if (habitaculo.getValue() == null) {
                                                                throw new IllegalArgumentException(
                                                                                "Seleccioná el habitáculo donde se ubicará el animal.");
                                                        }

                                                        idHabitaculo = habitaculo.getValue().id();
                                                        destino = "Habitáculo · " + habitaculo.getValue().texto();
                                                } else {
                                                        // Guardar la ubicación física elegida, no solo el
                                                        // nombre genérico del Plantel. Así el DAO no puede
                                                        // inferir un habitáculo distinto al seleccionado.
                                                        destino = "Campo abierto";
                                                }

                                                if (idHabitaculo != null) {

                                                        String observacionHabitaculo = SistemaDAO.scalar(
                                                                        "SELECT COALESCE(observaciones,'') " +
                                                                                        "FROM habitaculos " +
                                                                                        "WHERE id_habitaculo=" +
                                                                                        idHabitaculo +
                                                                                        " AND estado='Activo'");

                                                        String obsHabitaculo = observacionHabitaculo == null
                                                                        ? ""
                                                                        : observacionHabitaculo
                                                                                        .trim()
                                                                                        .toLowerCase();

                                                        boolean esMismoHabitaculo = modoEdicion &&
                                                                        habitaculoInicial == idHabitaculo;

                                                        if (obsHabitaculo.contains(
                                                                        "mantenimiento")) {

                                                                throw new IllegalArgumentException(
                                                                                "El habitáculo seleccionado está en mantenimiento.");
                                                        }

                                                }

                                                int ocupacion = idHabitaculo == null
                                                                ? 0
                                                                : SistemaDAO.ocupacionHabitaculo(
                                                                                idHabitaculo,
                                                                                plantelInicial);

                                                int capacidad = idHabitaculo == null
                                                                ? 0
                                                                : SistemaDAO.capacidadHabitaculo(
                                                                                idHabitaculo);

                                                if (idHabitaculo != null && capacidad <= 0) {
                                                        throw new IllegalArgumentException(
                                                                        "El habitáculo seleccionado no tiene una capacidad válida.");
                                                }

                                                if (idHabitaculo != null && ocupacion + n > capacidad) {
                                                        throw new IllegalArgumentException(
                                                                        "El habitáculo seleccionado no tiene suficiente capacidad.\n\n"
                                                                                        +
                                                                                        "Capacidad: " +
                                                                                        capacidad +
                                                                                        "\nOcupación actual: " +
                                                                                        ocupacion +
                                                                                        "\nSolicitado: " +
                                                                                        n);
                                                }

                                                destino = idHabitaculo == null
                                                                ? "Plantel Permanente · Campo abierto"
                                                                : "Plantel Permanente · Habitáculo: " +
                                                                                habitaculo.getValue().texto();
                                        }
                                }
                                // ------------------------------------------------
                                // CONFIRMAR
                                // ------------------------------------------------

                                String accion = modoEdicion
                                                ? "guardar los cambios"
                                                : "registrar este movimiento";

                                if (!confirmarOK(
                                                modoEdicion
                                                                ? "Modificar movimiento"
                                                                : "Agregar movimiento",

                                                "¿Estás segura de que querés " +
                                                                accion +
                                                                "?\n\n" +
                                                                "Animal: " +
                                                                animal.getValue().animal() +
                                                                "\n" +
                                                                "Cantidad: " +
                                                                n +
                                                                "\n" +
                                                                "Tipo: " +
                                                                tipo.getValue() +
                                                                (idHabitaculo != null
                                                                                ? "\nHabitáculo: " +
                                                                                                habitaculo.getValue()
                                                                                                                .texto()
                                                                                : ""))) {

                                        return;
                                }

                                // ------------------------------------------------
                                // GUARDAR
                                // ------------------------------------------------

                                String sexoValue = movimientoSoloPlantel
                                                ? sexo.getValue()
                                                : null;
                                String edadValue = null;
                                String pesoValue = null;

                                if (movimientoSoloPlantel &&
                                                "Plantel Permanente".equals(destinoTipo.getValue())) {

                                        destino = idHabitaculo != null
                                                        ? "Habitáculo · " +
                                                                        habitaculo.getValue().texto()
                                                        : "Campo abierto";
                                }

                                if (modoEdicion) {

                                        if (movimientoSoloPlantel) {

                                                SistemaDAO.guardarMovimiento(
                                                                movimientoId,
                                                                null,
                                                                plantelInicial,
                                                                fecha.getValue().toString(),
                                                                tipo.getValue(),
                                                                n,
                                                                sexoValue,
                                                                destino,
                                                                observaciones.getText().trim());

                                        } else {

                                                SistemaDAO.actualizarMovimientoActa(
                                                                movimientoId,
                                                                acta.getValue().id(),
                                                                animal.getValue().idDetalle(),
                                                                fecha.getValue().toString(),
                                                                tipo.getValue(),
                                                                n,
                                                                sexoValue,
                                                                edadValue,
                                                                pesoValue,
                                                                destino,
                                                                idHabitaculo,
                                                                observaciones.getText().trim());
                                        }

                                } else {

                                        SistemaDAO.guardarMovimientoActa(
                                                        acta.getValue().id(),
                                                        animal.getValue().idDetalle(),
                                                        fecha.getValue().toString(),
                                                        tipo.getValue(),
                                                        n,
                                                        sexoValue,
                                                        edadValue,
                                                        pesoValue,
                                                        destino,
                                                        idHabitaculo,
                                                        observaciones.getText().trim());
                                }

                                cargar(
                                                buscador.getText());

                                cerrarModal();

                        } catch (Exception ex) {

                                error(
                                                modoEdicion
                                                                ? "No se pudo modificar el movimiento"
                                                                : "No se pudo guardar el movimiento",
                                                ex.getMessage());
                        }
                });

                // --------------------------------------------------------
                // MOSTRAR DESTINO INICIAL
                // --------------------------------------------------------

                boolean requiereDestinoInicial = "Traslado".equals(
                                tipo.getValue())
                                ||
                                "Liberación".equals(
                                                tipo.getValue());

                if (requiereDestinoInicial) {

                        destinoBox.setVisible(true);
                        destinoBox.setManaged(true);

                        etiquetaDestino.setVisible(true);
                        etiquetaDestino.setManaged(true);

                        boolean inicialLiberacion = "Liberación".equals(tipo.getValue());

                        destinoTipo.setVisible(!inicialLiberacion);
                        destinoTipo.setManaged(!inicialLiberacion);

                        destinoLiberacion.setVisible(inicialLiberacion);
                        destinoLiberacion.setManaged(inicialLiberacion);

                        actualizarVisibilidadHabitaculo(
                                        destinoTipo,
                                        ubicacionPlantel,
                                        habitaculo,
                                        habitaculoBox,
                                        etiquetaHabitaculo,
                                        estadoHabitaculos,
                                        nuevoHabitaculo,
                                        plantelInicial);
                }

                // --------------------------------------------------------
                // INICIALIZAR ACTA
                // --------------------------------------------------------

                if (modoEdicion && ingresoInicial > 0) {
                        // Seleccionar el acta una vez instalado el listener para
                        // que también se cargue automáticamente su detalle animal.
                        SistemaDAO.Opcion actaInicial = null;
                        for (SistemaDAO.Opcion opcion : acta.getItems()) {
                                if (opcion.id() == ingresoInicial) {
                                        actaInicial = opcion;
                                        break;
                                }
                        }

                        if (actaInicial != null) {
                                acta.setValue(null);
                                acta.getSelectionModel().select(actaInicial);
                        }
                } else if (acta.getValue() != null) {
                        SistemaDAO.Opcion actaInicial = acta.getValue();
                        acta.setValue(null);
                        acta.getSelectionModel().select(actaInicial);
                }

                mostrarModal(
                                content,
                                1280,
                                850);

                // Limpia la línea interna que algunos skins de JavaFX dibujan
                // dentro del TextArea de Observaciones.
                observaciones.applyCss();
                Node areaContent = observaciones.lookup(".content");
                if (areaContent != null) {
                        areaContent.setStyle(
                                        "-fx-background-color:white;" +
                                                        "-fx-border-color:transparent;" +
                                                        "-fx-background-insets:0;" +
                                                        "-fx-border-width:0;");
                }
        }

        // ============================================================
        // DETALLE
        // ============================================================

        private void mostrarDetalle(
                        SistemaDAO.Fila f) {

                String animal = valorSeguro(f.c().get(1));

                String acta = valorSeguro(f.c().get(2));

                String fecha = valorSeguro(f.c().get(3));

                String tipo = valorSeguro(f.c().get(4));

                String cantidad = valorSeguro(f.c().get(5));

                String sexo = valorSeguro(f.c().get(6));

                String identificacion = valorSeguro(f.c().get(7));

                String destino = valorSeguro(f.c().get(8));

                String estado = f.c().size() > 9
                                ? valorSeguro(f.c().get(9))
                                : "";

                Label titulo = tituloModal(
                                "DETALLE DEL MOVIMIENTO");

                Label subtitulo = subtituloModal(
                                "Información del movimiento registrado");

                VBox tituloBox = new VBox(
                                3,
                                titulo,
                                subtitulo);

                Button modificar = Estilos.boton(
                                "✎  MODIFICAR");

                Button revertir = Estilos.botonSec(
                                "↩  REVERTIR");

                Button cerrar = Estilos.botonSec(
                                "CERRAR");

                Region espacioBotones = new Region();

                HBox.setHgrow(
                                espacioBotones,
                                Priority.ALWAYS);

                HBox acciones = new HBox(
                                8,
                                cerrar,
                                espacioBotones,
                                revertir,
                                modificar);

                acciones.setAlignment(
                                Pos.CENTER_LEFT);

                StackPane header = cabeceraModal(
                                tituloBox,
                                () -> cerrarModal());

                Label tipoLabel = new Label(
                                tipo.isBlank()
                                                ? "MOVIMIENTO"
                                                : tipo.toUpperCase());

                tipoLabel.setStyle(
                                "-fx-background-color: #E7F1EA;" +
                                                "-fx-text-fill: " +
                                                Estilos.VERDE_OSCURO +
                                                ";" +
                                                "-fx-font-size: 12px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-padding: 6px 12px;" +
                                                "-fx-background-radius: 20px;");

                Label animalLabel = new Label(animal);

                animalLabel.setStyle(
                                "-fx-text-fill: #1F2924;" +
                                                "-fx-font-size: 22px;" +
                                                "-fx-font-weight: bold;");

                Label actaLabel = new Label(
                                acta.isBlank()
                                                ? "Sin acta asociada"
                                                : "Acta " + acta);

                actaLabel.setStyle(
                                "-fx-text-fill: #718078;" +
                                                "-fx-font-size: 13px;");

                VBox identidad = new VBox(
                                7,
                                tipoLabel,
                                animalLabel,
                                actaLabel);

                identidad.setPadding(
                                new Insets(
                                                2,
                                                0,
                                                4,
                                                0));

                VBox fechaBox = tarjetaDato(
                                "FECHA",
                                formatearFecha(fecha));

                VBox tipoBox = tarjetaDato(
                                "TIPO DE MOVIMIENTO",
                                tipo.isBlank()
                                                ? "Sin especificar"
                                                : tipo);

                VBox cantidadBox = tarjetaDato(
                                "CANTIDAD",
                                cantidad.isBlank()
                                                ? "0"
                                                : cantidad);

                VBox estadoBox = tarjetaDato(
                                "ESTADO",
                                estado.isBlank()
                                                ? "Sin especificar"
                                                : estado);

                GridPane informacionPrincipal = new GridPane();

                informacionPrincipal.setHgap(12);
                informacionPrincipal.setVgap(12);

                informacionPrincipal.add(
                                fechaBox,
                                0,
                                0);

                informacionPrincipal.add(
                                tipoBox,
                                1,
                                0);

                informacionPrincipal.add(
                                cantidadBox,
                                0,
                                1);

                informacionPrincipal.add(
                                estadoBox,
                                1,
                                1);

                ColumnConstraints col1 = new ColumnConstraints();

                ColumnConstraints col2 = new ColumnConstraints();

                col1.setPercentWidth(50);
                col2.setPercentWidth(50);

                informacionPrincipal
                                .getColumnConstraints()
                                .setAll(
                                                col1,
                                                col2);

                Label tituloAnimal = seccion(
                                "DATOS DEL ANIMAL");

                VBox sexoBox = tarjetaDato(
                                "SEXO",
                                sexo.isBlank()
                                                ? "Sin especificar"
                                                : sexo);

                VBox identificacionBox = tarjetaDato(
                                "IDENTIFICACIÓN",
                                identificacion.isBlank()
                                                ? "Sin identificación"
                                                : identificacion);

                GridPane datosAnimal = new GridPane();

                datosAnimal.setHgap(12);
                datosAnimal.setVgap(12);

                datosAnimal.add(
                                sexoBox,
                                0,
                                0);

                datosAnimal.add(
                                identificacionBox,
                                1,
                                0);

                datosAnimal
                                .getColumnConstraints()
                                .setAll(
                                                new ColumnConstraints(),
                                                new ColumnConstraints());

                datosAnimal
                                .getColumnConstraints()
                                .get(0)
                                .setPercentWidth(50);

                datosAnimal
                                .getColumnConstraints()
                                .get(1)
                                .setPercentWidth(50);

                Label tituloDestino = seccion("DESTINO");

                VBox destinoBox = tarjetaDato(
                                "DESTINO DEL MOVIMIENTO",
                                destino.isBlank()
                                                ? "No corresponde"
                                                : destino);

                BorderPane contenido = new BorderPane();

                contenido.setPadding(
                                new Insets(22));

                contenido.setStyle(
                                "-fx-background-color: white;" +
                                                "-fx-background-radius: 18;" +
                                                "-fx-border-color: " +
                                                Estilos.BORDE +
                                                ";" +
                                                "-fx-border-radius: 18;" +
                                                "-fx-effect: dropshadow(" +
                                                "gaussian," +
                                                "rgba(0,0,0,0.22)," +
                                                "22,0,0,6);");

                VBox cuerpoDetalle = new VBox(
                                16,
                                identidad,
                                separador(),
                                informacionPrincipal,
                                tituloAnimal,
                                datosAnimal,
                                tituloDestino,
                                destinoBox);

                ScrollPane scrollDetalle = new ScrollPane(cuerpoDetalle);

                scrollDetalle.setFitToWidth(true);
                scrollDetalle.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);
                scrollDetalle.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);

                scrollDetalle.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;" +
                                                "-fx-border-color:transparent;");

                contenido.setTop(header);
                contenido.setCenter(scrollDetalle);
                contenido.setBottom(acciones);

                BorderPane.setMargin(
                                header,
                                new Insets(0, 0, 12, 0));

                BorderPane.setMargin(
                                acciones,
                                new Insets(12, 0, 0, 0));

                BorderPane.setAlignment(
                                acciones,
                                Pos.CENTER_LEFT);

                cerrar.setOnAction(
                                e -> cerrarModal());

                modificar.setOnAction(e -> {

                        cerrarModal();

                        mostrarFormularioMovimiento(
                                        f,
                                        true);
                });

                boolean movimientoActivo = "Activo".equalsIgnoreCase(estado);

                revertir.setDisable(!movimientoActivo);

                revertir.setOnAction(e -> {

                        if (!movimientoActivo) {
                                return;
                        }

                        Alert confirmacion = new Alert(
                                        Alert.AlertType.CONFIRMATION,
                                        "¿Querés revertir el Movimiento #" + f.id() + "?\n\n" +
                                                        "Se desharán los efectos que produjo este movimiento, " +
                                                        "se restaurará el estado anterior y el movimiento quedará " +
                                                        "conservado en el historial como REVERTIDO.",
                                        ButtonType.CANCEL,
                                        ButtonType.OK);

                        confirmacion.setTitle("Revertir movimiento");
                        confirmacion.setHeaderText("Revertir Movimiento #" + f.id());

                        ButtonType respuesta = confirmacion.showAndWait().orElse(ButtonType.CANCEL);

                        if (respuesta != ButtonType.OK) {
                                return;
                        }

                        try {

                                SistemaDAO.revertirMovimiento(f.id());

                                cerrarModal();
                                cargar(buscador.getText());

                                mostrarInfo(
                                                "Movimiento revertido",
                                                "El Movimiento #" + f.id() +
                                                                " fue revertido correctamente y quedó conservado en el historial.");

                        } catch (Exception ex) {

                                mostrarError(
                                                "No se pudo revertir el movimiento",
                                                ex.getMessage());
                        }
                });

                mostrarModal(
                                contenido,
                                820,
                                680);
        }

        // ============================================================
        // AUXILIARES DE HABITÁCULO
        // ============================================================

        private void actualizarVisibilidadHabitaculo(
                        ComboBox<String> destinoTipo,
                        ComboBox<String> ubicacionPlantel,
                        ComboBox<SistemaDAO.Opcion> habitaculo,
                        VBox habitaculoBox,
                        Label etiquetaHabitaculo,
                        Label estadoHabitaculos,
                        Button nuevoHabitaculo,
                        int plantelInicial) {

                boolean mostrar = "Plantel Permanente".equals(
                                destinoTipo.getValue())
                                &&
                                "Habitáculo".equals(
                                                ubicacionPlantel.getValue());

                habitaculoBox.setVisible(mostrar);
                habitaculoBox.setManaged(mostrar);

                etiquetaHabitaculo.setVisible(mostrar);
                etiquetaHabitaculo.setManaged(mostrar);

                if (!mostrar) {
                        habitaculo.setValue(null);
                        estadoHabitaculos.setText("");
                        nuevoHabitaculo.setVisible(false);
                        nuevoHabitaculo.setManaged(false);
                        return;
                }

                nuevoHabitaculo.setVisible(
                                contarHabitaculosActivos() < 10);
                nuevoHabitaculo.setManaged(
                                contarHabitaculosActivos() < 10);

                cargarHabitaculosDisponibles(
                                habitaculo,
                                estadoHabitaculos,
                                plantelInicial);
        }

        private void cargarHabitaculosDisponibles(
                        ComboBox<SistemaDAO.Opcion> habitaculo,
                        Label estadoHabitaculos,
                        int plantelInicial) {

                Integer seleccionado = habitaculo.getValue() == null
                                ? null
                                : habitaculo.getValue().id();

                List<SistemaDAO.Opcion> opciones = SistemaDAO.habitaculoOpciones();

                List<SistemaDAO.Opcion> disponibles = opciones.stream()
                                .filter(op -> habitaculoDisponible(
                                                op.id(),
                                                plantelInicial))
                                .toList();

                habitaculo.getItems().setAll(disponibles);

                /*
                 * Al modificar un movimiento existente, el habitáculo actual
                 * puede figurar como ocupado por su propio plantel. Se conserva
                 * para poder editar el movimiento. Mantenimiento nunca se permite.
                 */
                if (seleccionado != null &&
                                habitaculoDisponibleParaEdicion(
                                                seleccionado,
                                                plantelInicial)
                                &&
                                disponibles.stream().noneMatch(
                                                op -> op.id() == seleccionado)) {

                        for (SistemaDAO.Opcion op : opciones) {
                                if (op.id() == seleccionado) {
                                        habitaculo.getItems().add(op);
                                        break;
                                }
                        }
                }

                if (seleccionado != null) {
                        for (SistemaDAO.Opcion op : habitaculo.getItems()) {
                                if (op.id() == seleccionado) {
                                        habitaculo.setValue(op);
                                        break;
                                }
                        }
                }

                if (disponibles.isEmpty()) {

                        if (contarHabitaculosActivos() >= 10) {
                                estadoHabitaculos.setText(
                                                "No hay habitáculos disponibles. " +
                                                                "Se alcanzó el máximo de 10 habitáculos.");
                        } else {
                                estadoHabitaculos.setText(
                                                "No hay habitáculos disponibles actualmente. " +
                                                                "Podés agregar uno con «＋».");
                        }

                } else {

                        estadoHabitaculos.setText(
                                        "Disponibles: " +
                                                        disponibles.size() +
                                                        " · Activos: " +
                                                        contarHabitaculosActivos() +
                                                        "/10.");
                }
        }

        /**
         * Devuelve el detalle de los animales que actualmente ocupan un
         * habitáculo. El movimiento que originó cada registro se muestra
         * como referencia para poder localizarlo y editarlo desde
         * Movimientos. Esta consulta NO modifica observaciones del
         * habitáculo.
         */
        private String detalleOcupantesHabitaculo(
                        int idHabitaculo,
                        int excluirPlantel) {

                try {

                        List<SistemaDAO.Fila> filas = SistemaDAO.filas(
                                        "SELECT " +
                                                        "p.id_plantel," +
                                                        "COALESCE(a.nombre_vulgar,'')," +
                                                        "p.cantidad," +
                                                        "COALESCE((" +
                                                        "SELECT MAX(m.id_movimiento) " +
                                                        "FROM movimientos m " +
                                                        "WHERE m.id_plantel=p.id_plantel " +
                                                        "AND m.estado='Activo'" +
                                                        "),0) " +
                                                        "FROM plantel_permanente p " +
                                                        "JOIN animales a " +
                                                        "ON a.id_animal=p.id_animal " +
                                                        "WHERE p.id_habitaculo=" +
                                                        idHabitaculo +
                                                        " AND p.estado='Activo' " +
                                                        " AND p.cantidad>0 " +
                                                        " AND p.id_plantel<>" +
                                                        excluirPlantel +
                                                        " ORDER BY a.nombre_vulgar");

                        if (filas.isEmpty()) {
                                return "No se encontraron registros de animales activos, aunque el habitáculo tiene ocupación.";
                        }

                        StringBuilder texto = new StringBuilder(
                                        "Actualmente está ocupado por:");

                        for (SistemaDAO.Fila fila : filas) {

                                List<String> c = fila.c();

                                String animal = c.size() > 1
                                                ? valorSeguro(c.get(1))
                                                : "Animal sin nombre";

                                String cantidad = c.size() > 2
                                                ? valorSeguro(c.get(2))
                                                : "0";

                                String movimiento = c.size() > 3
                                                ? valorSeguro(c.get(3))
                                                : "0";

                                texto.append("\n• ")
                                                .append(animal)
                                                .append(" · Cantidad: ")
                                                .append(cantidad);

                                if (!movimiento.isBlank() &&
                                                !"0".equals(movimiento)) {

                                        texto.append(
                                                        " · Movimiento #")
                                                        .append(movimiento);
                                }
                        }

                        return texto.toString();

                } catch (Exception e) {

                        return "El habitáculo ya tiene animales registrados. " +
                                        "Podés consultar sus movimientos desde el historial.";
                }
        }

        private boolean habitaculoDisponible(
                        int idHabitaculo,
                        int plantelInicial) {

                try {

                        String observacion = SistemaDAO.scalar(
                                        "SELECT COALESCE(observaciones,'') " +
                                                        "FROM habitaculos " +
                                                        "WHERE id_habitaculo=" +
                                                        idHabitaculo +
                                                        " AND estado='Activo'");

                        if (observacion == null) {
                                return false;
                        }

                        String obs = observacion.trim().toLowerCase();

                        if (obs.contains("mantenimiento")) {
                                return false;
                        }

                        int capacidad = SistemaDAO.capacidadHabitaculo(
                                        idHabitaculo);

                        if (capacidad <= 0) {
                                return false;
                        }

                        int ocupacion = SistemaDAO.ocupacionHabitaculo(
                                        idHabitaculo,
                                        plantelInicial);

                        return ocupacion < capacidad;

                } catch (Exception e) {

                        return false;
                }
        }

        private boolean habitaculoDisponibleParaEdicion(
                        int idHabitaculo,
                        int plantelInicial) {

                try {

                        String observacion = SistemaDAO.scalar(
                                        "SELECT COALESCE(observaciones,'') " +
                                                        "FROM habitaculos " +
                                                        "WHERE id_habitaculo=" +
                                                        idHabitaculo +
                                                        " AND estado='Activo'");

                        if (observacion == null ||
                                        observacion.toLowerCase().contains("mantenimiento")) {
                                return false;
                        }

                        int capacidad = SistemaDAO.capacidadHabitaculo(
                                        idHabitaculo);

                        int ocupacion = SistemaDAO.ocupacionHabitaculo(
                                        idHabitaculo,
                                        plantelInicial);

                        return capacidad > 0 &&
                                        ocupacion < capacidad;

                } catch (Exception e) {

                        return false;
                }
        }

        private int contarHabitaculosActivos() {

                try {

                        return Integer.parseInt(
                                        SistemaDAO.scalar(
                                                        "SELECT COUNT(*) " +
                                                                        "FROM habitaculos " +
                                                                        "WHERE estado='Activo'"));

                } catch (Exception e) {

                        return 0;
                }
        }

        private void mostrarNuevoHabitaculo(
                        ComboBox<SistemaDAO.Opcion> habitaculo,
                        Label estadoHabitaculos,
                        int plantelInicial) {

                if (contarHabitaculosActivos() >= 10) {

                        error(
                                        "Límite de habitáculos",
                                        "No se pueden agregar más habitáculos. " +
                                                        "El máximo permitido es de 10.");

                        return;
                }

                Label titulo = tituloModal("NUEVO HABITÁCULO");

                Label subtitulo = subtituloModal(
                                "Agregá un habitáculo sin salir del movimiento.");

                StackPane header = cabeceraModal(
                                new VBox(
                                                2,
                                                titulo,
                                                subtitulo),
                                () -> cerrarSubModal());

                TextField nombre = Estilos.campo("Nombre del habitáculo");

                limitarCaracteres(nombre, "Nombre del habitáculo");

                TextField sector = Estilos.campo("Sector");

                limitarCaracteres(sector, "Sector");

                TextField capacidad = Estilos.entero(
                                "Capacidad máxima",
                                4);

                limitarCaracteres(capacidad, "Capacidad máxima");

                TextArea observaciones = Estilos.area("Observaciones");

                observaciones.setPrefRowCount(4);
                observaciones.setWrapText(true);
                observaciones.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-insets:0;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-width:1;" +
                                                "-fx-border-radius:10;" +
                                                "-fx-background-radius:10;" +
                                                "-fx-padding:0;");

                GridPane form = new GridPane();

                form.setHgap(14);
                form.setVgap(10);
                form.setPadding(
                                new Insets(6, 0, 0, 0));

                ColumnConstraints etiqueta1 = new ColumnConstraints();
                etiqueta1.setMinWidth(105);
                etiqueta1.setPrefWidth(110);
                etiqueta1.setMaxWidth(125);

                ColumnConstraints campo1 = new ColumnConstraints();
                campo1.setHgrow(Priority.ALWAYS);
                campo1.setFillWidth(true);

                ColumnConstraints etiqueta2 = new ColumnConstraints();
                etiqueta2.setMinWidth(105);
                etiqueta2.setPrefWidth(110);
                etiqueta2.setMaxWidth(125);

                ColumnConstraints campo2 = new ColumnConstraints();
                campo2.setHgrow(Priority.ALWAYS);
                campo2.setFillWidth(true);

                form.getColumnConstraints().setAll(
                                etiqueta1,
                                campo1,
                                etiqueta2,
                                campo2);

                aplicarValidacionObligatoria(nombre, () -> true);
                aplicarValidacionObligatoria(sector, () -> true);
                aplicarValidacionObligatoria(capacidad, () -> true);

                form.add(
                                etiqueta("* Nombre"),
                                0,
                                0);
                form.add(
                                nombre,
                                1,
                                0);
                form.add(
                                etiqueta("* Sector"),
                                2,
                                0);
                form.add(
                                sector,
                                3,
                                0);

                form.add(
                                etiqueta("* Capacidad"),
                                0,
                                1);
                form.add(
                                capacidad,
                                1,
                                1);

                form.add(
                                etiqueta("Observaciones"),
                                0,
                                2);
                form.add(
                                observaciones,
                                1,
                                2,
                                3,
                                1);

                GridPane.setHgrow(
                                observaciones,
                                Priority.ALWAYS);

                Button cancelar = Estilos.botonSec("CANCELAR");

                Button guardar = Estilos.boton("GUARDAR");

                HBox acciones = new HBox(
                                10,
                                cancelar,
                                guardar);

                acciones.setAlignment(
                                Pos.CENTER_RIGHT);

                BorderPane contenido = new BorderPane();

                contenido.setPadding(
                                new Insets(22));

                contenido.setStyle(
                                "-fx-background-color:white;" +
                                                "-fx-background-radius:18;" +
                                                "-fx-border-color:" + Estilos.BORDE + ";" +
                                                "-fx-border-radius:18;" +
                                                "-fx-effect:dropshadow(" +
                                                "gaussian,rgba(0,0,0,.28),24,0,0,8);");

                VBox centro = new VBox(
                                12,
                                form);
                centro.setFillWidth(true);

                ScrollPane scroll = new ScrollPane(centro);
                scroll.setFitToWidth(true);
                scroll.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-background:transparent;" +
                                                "-fx-border-color:transparent;");

                contenido.setTop(header);
                contenido.setCenter(scroll);
                contenido.setBottom(acciones);

                BorderPane.setMargin(
                                header,
                                new Insets(0, 0, 14, 0));
                BorderPane.setMargin(
                                acciones,
                                new Insets(14, 0, 0, 0));
                BorderPane.setAlignment(
                                acciones,
                                Pos.CENTER_RIGHT);

                cancelar.setOnAction(
                                e -> cerrarSubModal());

                guardar.setOnAction(e -> {

                        try {

                                if (nombre.getText().trim().isBlank()) {
                                        throw new IllegalArgumentException(
                                                        "El nombre del habitáculo es obligatorio.");
                                }

                                if (sector.getText().trim().isBlank()) {
                                        throw new IllegalArgumentException(
                                                        "El sector es obligatorio.");
                                }

                                if (capacidad.getText().trim().isBlank()) {
                                        throw new IllegalArgumentException(
                                                        "Ingresá la capacidad máxima.");
                                }

                                int cap = Integer.parseInt(
                                                capacidad.getText().trim());

                                if (cap <= 0) {
                                        throw new IllegalArgumentException(
                                                        "La capacidad debe ser mayor que cero.");
                                }

                                if (contarHabitaculosActivos() >= 10) {
                                        throw new IllegalArgumentException(
                                                        "No se pueden agregar más habitáculos. " +
                                                                        "El máximo permitido es de 10.");
                                }

                                int nuevoId = SistemaDAO.guardarHabitaculo(
                                                0,
                                                nombre.getText().trim(),
                                                sector.getText().trim(),
                                                cap,
                                                observaciones.getText().trim());

                                cargarHabitaculosDisponibles(
                                                habitaculo,
                                                estadoHabitaculos,
                                                plantelInicial);

                                for (SistemaDAO.Opcion op : habitaculo.getItems()) {

                                        if (op.id() == nuevoId) {
                                                habitaculo.setValue(op);
                                                break;
                                        }
                                }

                                cerrarSubModal();

                        } catch (NumberFormatException ex) {

                                error(
                                                "Habitáculo",
                                                "La capacidad debe ser un número entero.");

                        } catch (Exception ex) {

                                error(
                                                "No se pudo crear el habitáculo",
                                                ex.getMessage());
                        }
                });

                mostrarSubModal(
                                contenido,
                                900,
                                560);
        }

        private int obtenerPlantelMovimiento(
                        int movimiento) {

                try {

                        String plantel = SistemaDAO.scalar(
                                        "SELECT COALESCE(id_plantel,0) " +
                                                        "FROM movimientos " +
                                                        "WHERE id_movimiento=" +
                                                        movimiento);

                        return Integer.parseInt(plantel);

                } catch (Exception e) {

                        return 0;
                }
        }

        private int obtenerHabitaculoMovimiento(
                        int movimiento) {

                try {

                        String habitaculo = SistemaDAO.scalar(
                                        "SELECT COALESCE((" +
                                                        "SELECT h.id_habitaculo FROM movimientos mx " +
                                                        "JOIN habitaculos h ON LOWER(TRIM(h.nombre))=LOWER(TRIM(SUBSTR(mx.destino,INSTR(mx.destino,'·')+1))) " +
                                                        "WHERE mx.id_movimiento=" + movimiento +
                                                        " AND LOWER(TRIM(COALESCE(mx.destino,''))) LIKE 'habitáculo%' LIMIT 1" +
                                                        "),(SELECT h.id_habitaculo FROM movimientos mx " +
                                                        "JOIN habitaculos h ON LOWER(mx.destino) LIKE '%' || LOWER(h.nombre) || '%' " +
                                                        "WHERE mx.id_movimiento=" + movimiento +
                                                        " AND LOWER(TRIM(COALESCE(mx.destino,''))) LIKE 'habitáculo%' LIMIT 1" +
                                                        "),(SELECT COALESCE(p.id_habitaculo,0) FROM movimientos m " +
                                                        "LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                        "WHERE m.id_movimiento=" + movimiento + "),0)");

                        return Integer.parseInt(habitaculo);

                } catch (Exception e) {

                        return 0;
                }
        }

        private int obtenerDetalleMovimiento(
                        int movimiento) {

                try {

                        String detalle = SistemaDAO.scalar(
                                        "SELECT COALESCE(id_detalle,0) " +
                                                        "FROM movimientos " +
                                                        "WHERE id_movimiento=" +
                                                        movimiento);

                        return Integer.parseInt(detalle);

                } catch (Exception e) {

                        return 0;
                }
        }

        // ============================================================
        // COMBO ANIMALES
        // ============================================================

        private void configurarComboAnimales(
                        ComboBox<SistemaDAO.DetalleMovimiento> combo) {

                combo.setCellFactory(
                                list -> new ListCell<>() {

                                        @Override
                                        protected void updateItem(
                                                        SistemaDAO.DetalleMovimiento item,
                                                        boolean empty) {

                                                super.updateItem(
                                                                item,
                                                                empty);

                                                if (empty || item == null) {

                                                        setText(null);
                                                        setDisable(false);
                                                        setOpacity(1.0);

                                                        return;
                                                }

                                                boolean disponible = item.cantidadDisponible() > 0;

                                                if (disponible) {

                                                        setText(
                                                                        item.animal() +
                                                                                        " · Disponible: " +
                                                                                        item.cantidadDisponible() +
                                                                                        " / " +
                                                                                        item.cantidadOriginal());

                                                } else {

                                                        setText(
                                                                        item.animal() +
                                                                                        " · SIN DISPONIBILIDAD");
                                                }

                                                setDisable(!disponible);

                                                setOpacity(
                                                                disponible
                                                                                ? 1.0
                                                                                : 0.55);
                                        }
                                });

                combo.setButtonCell(
                                new ListCell<>() {

                                        @Override
                                        protected void updateItem(
                                                        SistemaDAO.DetalleMovimiento item,
                                                        boolean empty) {

                                                super.updateItem(
                                                                item,
                                                                empty);

                                                if (empty || item == null) {

                                                        setText(null);
                                                        return;
                                                }

                                                if (item.cantidadDisponible() > 0) {

                                                        setText(
                                                                        item.animal() +
                                                                                        " · Disponible: " +
                                                                                        item.cantidadDisponible() +
                                                                                        " / " +
                                                                                        item.cantidadOriginal());

                                                } else {

                                                        setText(
                                                                        item.animal() +
                                                                                        " · SIN DISPONIBILIDAD");
                                                }
                                        }
                                });
        }

        // ============================================================
        // COMPONENTES
        // ============================================================

        private VBox tarjetaDato(
                        String titulo,
                        String valor) {

                Label tituloLabel = new Label(titulo);

                tituloLabel.setStyle(
                                "-fx-text-fill: #829088;" +
                                                "-fx-font-size: 10px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-letter-spacing: 0.8px;");

                Label valorLabel = new Label(
                                valor == null ||
                                                valor.isBlank()
                                                                ? "—"
                                                                : valor);

                valorLabel.setWrapText(true);

                valorLabel.setStyle(
                                "-fx-text-fill: #26332D;" +
                                                "-fx-font-size: 14px;" +
                                                "-fx-font-weight: bold;");

                VBox tarjeta = new VBox(
                                6,
                                tituloLabel,
                                valorLabel);

                tarjeta.setPadding(
                                new Insets(
                                                12,
                                                14,
                                                12,
                                                14));

                tarjeta.setMaxWidth(
                                Double.MAX_VALUE);

                tarjeta.setStyle(
                                "-fx-background-color: #F7F9F7;" +
                                                "-fx-background-radius: 10;" +
                                                "-fx-border-color: #E2E8E3;" +
                                                "-fx-border-radius: 10;");

                return tarjeta;
        }

        private Separator separador() {

                Separator separator = new Separator();

                separator.setStyle(
                                "-fx-background-color: #E7ECE8;");

                return separator;
        }

        private StackPane cabeceraModal(
                        Node titulo,
                        Runnable cerrar) {

                Label close = new Label("✕");

                close.setPrefSize(34, 34);
                close.setMinSize(34, 34);
                close.setMaxSize(34, 34);

                close.setAlignment(Pos.CENTER);

                close.setStyle(
                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";" +
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

        private void limitarCaracteres(
                        javafx.scene.control.TextInputControl campo,
                        String nombre) {

                campo.textProperty().addListener(
                                (obs, anterior, nuevo) -> {

                                        if (nuevo == null || nuevo.length() <= 50) {
                                                return;
                                        }

                                        campo.setText(
                                                        nuevo.substring(0, 50));
                                        campo.positionCaret(50);

                                        error(
                                                        "Límite de caracteres",
                                                        nombre +
                                                                        " no puede superar los 50 caracteres. " +
                                                                        "Se conservaron únicamente los primeros 50.");
                                });
        }

        private void agregarCampoDosColumnas(
                        GridPane form,
                        int fila,
                        String etiquetaIzq,
                        Region campoIzq,
                        boolean requeridoIzq,
                        String etiquetaDer,
                        Region campoDer,
                        boolean requeridoDer) {

                Label izquierda = etiqueta(
                                (requeridoIzq ? "* " : "") + etiquetaIzq);
                Label derecha = etiqueta(
                                (requeridoDer ? "* " : "") + etiquetaDer);

                izquierda.setWrapText(false);
                derecha.setWrapText(false);

                form.add(izquierda, 0, fila);
                form.add(campoIzq, 1, fila);
                form.add(derecha, 2, fila);
                form.add(campoDer, 3, fila);

                GridPane.setHgrow(campoIzq, Priority.ALWAYS);
                GridPane.setHgrow(campoDer, Priority.ALWAYS);

                campoIzq.setMinWidth(0);
                campoDer.setMinWidth(0);
                campoIzq.setPrefWidth(0);
                campoDer.setPrefWidth(0);
                campoIzq.setMaxWidth(Double.MAX_VALUE);
                campoDer.setMaxWidth(Double.MAX_VALUE);

                if (requeridoIzq) aplicarValidacionObligatoria(campoIzq, () -> true);
                if (requeridoDer) aplicarValidacionObligatoria(campoDer, () -> true);
        }

        private void addForm(
                        GridPane g,
                        int row,
                        String label,
                        Region node,
                        boolean required) {

                Label l = etiqueta(
                                (required ? "* " : "") +
                                                label);

                l.setMinWidth(125);
                l.setPrefWidth(125);
                l.setMaxWidth(145);
                l.setWrapText(false);

                g.add(l, 0, row);
                g.add(node, 1, row);

                GridPane.setHgrow(
                                node,
                                Priority.ALWAYS);

                if (node instanceof Control control) {
                        control.setMaxWidth(
                                        Double.MAX_VALUE);
                }

                if (required) aplicarValidacionObligatoria(node, () -> true);
        }

        private Runnable aplicarValidacionObligatoria(Region campo,
                        java.util.function.BooleanSupplier requerido) {
                Runnable actualizar = () -> actualizarBordeObligatorio(campo, requerido.getAsBoolean());
                escucharCambiosObligatorios(campo, actualizar);
                actualizar.run();
                return actualizar;
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

        private void escucharCambiosObligatorios(Node nodo, Runnable actualizar) {
                if (nodo instanceof TextInputControl texto) {
                        texto.textProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof ComboBoxBase<?> combo) {
                        combo.valueProperty().addListener((o, anterior, nuevo) -> actualizar.run());
                } else if (nodo instanceof Parent padre) {
                        padre.getChildrenUnmodifiable().forEach(hijo -> escucharCambiosObligatorios(hijo, actualizar));
                }
        }

        private Label tituloModal(
                        String text) {

                Label l = new Label(text);

                l.setStyle(
                                "-fx-font-size:21px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.VERDE_OSCURO +
                                                ";");

                return l;
        }

        private Label subtituloModal(
                        String text) {

                Label l = new Label(text);

                l.setWrapText(true);

                l.setStyle(
                                "-fx-font-size:12px;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                return l;
        }

        private Label seccion(
                        String text) {

                Label l = new Label(text);

                l.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.VERDE_OSCURO +
                                                ";");

                return l;
        }

        private Label etiqueta(
                        String text) {

                Label l = new Label(text);

                l.setStyle(
                                "-fx-font-size:11px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-text-fill:" +
                                                Estilos.GRIS +
                                                ";");

                return l;
        }

        // ============================================================
        // MENSAJES
        // ============================================================

        private void mostrarInfo(
                        String titulo,
                        String mensaje) {

                Alert alerta = new Alert(
                                Alert.AlertType.INFORMATION);

                alerta.setTitle(titulo);
                alerta.setHeaderText(titulo);
                alerta.setContentText(
                                mensaje == null || mensaje.isBlank()
                                                ? "La operación se realizó correctamente."
                                                : mensaje);

                alerta.showAndWait();
        }

        private void mostrarError(
                        String titulo,
                        String mensaje) {

                Alert alerta = new Alert(
                                Alert.AlertType.ERROR);

                alerta.setTitle(titulo);
                alerta.setHeaderText(titulo);
                alerta.setContentText(
                                mensaje == null || mensaje.isBlank()
                                                ? "Ocurrió un error inesperado."
                                                : mensaje);

                alerta.showAndWait();
        }

        // ============================================================
        // UTILIDADES
        // ============================================================

        private int parseCantidad(
                        String value) {

                try {

                        return Integer.parseInt(
                                        value == null ||
                                                        value.isBlank()
                                                                        ? "0"
                                                                        : value.trim());

                } catch (NumberFormatException e) {

                        return 0;
                }
        }

        private String valorSeguro(
                        String value) {

                return value == null
                                ? ""
                                : value;
        }

        private String formatearFecha(
                        String value) {

                try {

                        return LocalDate
                                        .parse(value)
                                        .format(
                                                        DateTimeFormatter.ofPattern(
                                                                        "dd/MM/yyyy"));

                } catch (Exception e) {

                        return value == null
                                        ? ""
                                        : value;
                }
        }
}
