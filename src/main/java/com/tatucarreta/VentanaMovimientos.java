package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VentanaMovimientos {
private VentanaPrincipal ventanaPrincipal;
public VentanaMovimientos(VentanaPrincipal ventanaPrincipal) {
    this.ventanaPrincipal = ventanaPrincipal;
}
    private final MovimientoDAO movimientoDAO =
            new MovimientoDAO();

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final IngresoDAO ingresoDAO =
            new IngresoDAO();

    private final DetalleIngresoDAO detalleIngresoDAO =
            new DetalleIngresoDAO();

    private final PlantelPermanenteDAO plantelDAO =
            new PlantelPermanenteDAO();

    private final HabitaculoDAO habitaculoDAO =
            new HabitaculoDAO();

    private TableView<Movimiento> tabla;
    private FilteredList<Movimiento> movimientosFiltrados;
    private TextField txtBuscarMovimiento;
    private ComboBox<String> comboFiltroTipo;
    private DatePicker datePickerFiltro;

    private ComboBox<Animal> comboAnimal;
    private ComboBox<Ingreso> comboIngreso;

    private DatePicker datePickerFecha;
    private ComboBox<String> comboTipo;
    private TextField txtCantidad;
    private TextField txtDestino;
    private TextArea txtObservaciones;

    private ComboBox<String> comboUbicacion;
    private ComboBox<Habitaculo> comboHabitaculo;

    private Label lblDisponible;
    private Label lblUbicacion;
    private Label lblHabitaculo;


    // =========================================================
    // CREAR CONTENIDO
    // =========================================================

    public ScrollPane crearContenido() {

        Label titulo =
                new Label(
                        "MOVIMIENTOS DE ANIMALES"
                );

        titulo.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );


        // =====================================================
        // ANIMAL
        // =====================================================

        comboAnimal =
                new ComboBox<>();

        comboAnimal.setPrefWidth(300);

        comboAnimal.setPromptText(
                "Seleccione un animal"
        );

        comboAnimal.setCellFactory(lista -> new javafx.scene.control.ListCell<Animal>() {
            @Override
            protected void updateItem(Animal animal, boolean vacio) {
                super.updateItem(animal, vacio);
                setText(vacio || animal == null ? null : animal.getNombreVulgar());
            }
        });

        comboAnimal.setButtonCell(new javafx.scene.control.ListCell<Animal>() {
            @Override
            protected void updateItem(Animal animal, boolean vacio) {
                super.updateItem(animal, vacio);
                setText(vacio || animal == null ? null : animal.getNombreVulgar());
            }
        });

        cargarAnimales();

        comboAnimal.setOnAction(e ->
                cargarIngresosPorAnimal()
        );


        // =====================================================
        // INGRESO / ACTA
        // =====================================================

        comboIngreso =
                new ComboBox<>();

        comboIngreso.setPrefWidth(300);

        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );

        comboIngreso.setCellFactory(lista -> new javafx.scene.control.ListCell<Ingreso>() {
            @Override
            protected void updateItem(Ingreso ingreso, boolean vacio) {
                super.updateItem(ingreso, vacio);
                setText(vacio || ingreso == null
                        ? null
                        : "Acta " + ingreso.getNumeroActa()
                          + " — " + ingreso.getFechaIngreso());
            }
        });

        comboIngreso.setButtonCell(new javafx.scene.control.ListCell<Ingreso>() {
            @Override
            protected void updateItem(Ingreso ingreso, boolean vacio) {
                super.updateItem(ingreso, vacio);
                setText(vacio || ingreso == null
                        ? null
                        : "Acta " + ingreso.getNumeroActa()
                          + " — " + ingreso.getFechaIngreso());
            }
        });

        comboIngreso.setOnAction(e ->
                actualizarCantidadDisponible()
        );


        // =====================================================
        // DISPONIBLE
        // =====================================================

        lblDisponible =
                new Label(
                        "Disponible en cuarentena: -"
                );

        lblDisponible.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );


        // =====================================================
        // FECHA
        // =====================================================

        datePickerFecha =
                new DatePicker();

        datePickerFecha.setPromptText(
                "dd/MM/yyyy"
        );

        DateTimeFormatter formatoFecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        datePickerFecha.setConverter(
                new javafx.util.StringConverter<LocalDate>() {
                    @Override
                    public String toString(LocalDate fecha) {
                        return fecha == null ? "" : formatoFecha.format(fecha);
                    }

                    @Override
                    public LocalDate fromString(String texto) {
                        if (texto == null || texto.isBlank()) {
                            return null;
                        }
                        try {
                            return LocalDate.parse(texto.trim(), formatoFecha);
                        } catch (DateTimeParseException e) {
                            return null;
                        }
                    }
                }
        );


        // =====================================================
        // TIPO
        // =====================================================

        comboTipo =
                new ComboBox<>();

        comboTipo.setItems(
                FXCollections.observableArrayList(
                        "LIBERACION",
                        "TRASLADO",
                        "FALLECIMIENTO",
                        "PLANTEL PERMANENTE"
                )
        );

        comboTipo.setPrefWidth(300);

        comboTipo.setPromptText(
                "Seleccione el tipo"
        );


        // =====================================================
        // CANTIDAD
        // =====================================================

        txtCantidad =
                new TextField();

        txtCantidad.setPromptText(
                "Cantidad de animales"
        );


        // =====================================================
        // DESTINO
        // =====================================================

        txtDestino =
                new TextField();

        txtDestino.setPromptText(
                "Solo si corresponde"
        );


        // =====================================================
        // UBICACIÓN PLANTEL
        // =====================================================

        comboUbicacion =
                new ComboBox<>();

        comboUbicacion.setPrefWidth(300);

        comboUbicacion.setPromptText(
                "Seleccione la ubicación"
        );

        comboUbicacion.getItems().addAll(
                "Habitáculo",
                "Recinto",
                "Campo abierto",
                "Otro"
        );


        // =====================================================
        // HABITÁCULO
        // =====================================================

        comboHabitaculo =
                new ComboBox<>();

        comboHabitaculo.setPrefWidth(300);

        comboHabitaculo.setPromptText(
                "Seleccione un habitáculo"
        );

        cargarHabitaculos();


        // =====================================================
        // OBSERVACIONES
        // =====================================================

        txtObservaciones =
                new TextArea();

        txtObservaciones.setPromptText(
                "Observaciones"
        );

        txtObservaciones.setPrefRowCount(3);


        // =====================================================
        // FORMULARIO
        // =====================================================

        GridPane formulario =
                new GridPane();

        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.setPadding(
                new Insets(20)
        );


        formulario.add(
                crearLabelCampo("Animal", true),
                0,
                0
        );

        formulario.add(
                comboAnimal,
                1,
                0
        );


        formulario.add(
                crearLabelCampo("N° de Acta", true),
                0,
                1
        );

        formulario.add(
                comboIngreso,
                1,
                1
        );


        formulario.add(
                new Label("Disponibilidad:"),
                0,
                2
        );

        formulario.add(
                lblDisponible,
                1,
                2
        );


        formulario.add(
                crearLabelCampo("Fecha", true),
                0,
                3
        );

        formulario.add(
                datePickerFecha,
                1,
                3
        );


        formulario.add(
                crearLabelCampo("Tipo de movimiento", true),
                0,
                4
        );

        formulario.add(
                comboTipo,
                1,
                4
        );


        formulario.add(
                crearLabelCampo("Cantidad", true),
                0,
                5
        );

        formulario.add(
                txtCantidad,
                1,
                5
        );


        formulario.add(
                crearLabelCampo("Destino"),
                0,
                6
        );

        formulario.add(
                txtDestino,
                1,
                6
        );


        // =====================================================
        // UBICACIÓN
        // =====================================================

        lblUbicacion =
                crearLabelCampo("Ubicación", true);

        formulario.add(
                lblUbicacion,
                0,
                7
        );

        formulario.add(
                comboUbicacion,
                1,
                7
        );


        // =====================================================
        // HABITÁCULO
        // =====================================================

        lblHabitaculo =
                crearLabelCampo("Habitáculo", true);

        formulario.add(
                lblHabitaculo,
                0,
                8
        );

        formulario.add(
                comboHabitaculo,
                1,
                8
        );


        // =====================================================
        // OBSERVACIONES
        // =====================================================

        formulario.add(
                crearLabelCampo("Observaciones"),
                0,
                9
        );

        formulario.add(
                txtObservaciones,
                1,
                9
        );


        // =====================================================
        // VISIBILIDAD PLANTEL
        // =====================================================

        comboTipo.setOnAction(e -> {

            actualizarCamposPlantel();

            actualizarCantidadDisponible();
        });


        lblUbicacion.setVisible(false);
        lblUbicacion.setManaged(false);

        comboUbicacion.setVisible(false);
        comboUbicacion.setManaged(false);

        lblHabitaculo.setVisible(false);
        lblHabitaculo.setManaged(false);

        comboHabitaculo.setVisible(false);
        comboHabitaculo.setManaged(false);


        // =====================================================
        // VISIBILIDAD HABITÁCULO
        // =====================================================

        comboUbicacion.setOnAction(e ->
                actualizarHabitaculo()
        );

        comboAnimal.valueProperty().addListener(
                (obs, viejo, nuevo) -> marcarCampoError(comboAnimal, nuevo == null)
        );

        comboIngreso.valueProperty().addListener(
                (obs, viejo, nuevo) -> marcarCampoError(comboIngreso, nuevo == null)
        );

        datePickerFecha.valueProperty().addListener(
                (obs, viejo, nuevo) -> marcarCampoError(datePickerFecha, nuevo == null)
        );

        comboTipo.valueProperty().addListener(
                (obs, viejo, nuevo) -> marcarCampoError(comboTipo, nuevo == null)
        );

        txtCantidad.textProperty().addListener(
                (obs, viejo, nuevo) -> marcarCampoError(
                        txtCantidad, nuevo == null || nuevo.isBlank())
        );


        // =====================================================
        // BOTÓN REGISTRAR
        // =====================================================

        Button btnGuardar =
                new Button(
                        "REGISTRAR MOVIMIENTO"
                );

        btnGuardar.setStyle(
                "-fx-background-color: #23452C;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        btnGuardar.setOnAction(e ->
                guardarMovimiento()
        );


        // =====================================================
        // BOTÓN LIMPIAR
        // =====================================================

        Button btnLimpiar =
                new Button(
                        "LIMPIAR"
                );

        btnLimpiar.setOnAction(e ->
                limpiarCampos()
        );


        HBox botones =
                new HBox(
                        10,
                        btnGuardar,
                        btnLimpiar
                );

        botones.setAlignment(
                Pos.CENTER
        );


        // =====================================================
        // FILTROS DEL HISTORIAL
        // =====================================================

        Label lblBuscar = new Label("Buscar:");
        lblBuscar.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        txtBuscarMovimiento = new TextField();
        txtBuscarMovimiento.setPromptText(
                "Animal, especie, acta o destino..."
        );
        txtBuscarMovimiento.setPrefWidth(280);

        Label lblFiltroTipo = new Label("Tipo:");
        lblFiltroTipo.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        comboFiltroTipo = new ComboBox<>();
        comboFiltroTipo.getItems().addAll(
                "TODOS",
                "LIBERACION",
                "TRASLADO",
                "FALLECIMIENTO",
                "PLANTEL PERMANENTE"
        );
        comboFiltroTipo.setValue("TODOS");
        comboFiltroTipo.setPrefWidth(190);

        Label lblFiltroFecha = new Label("Fecha:");
        lblFiltroFecha.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        datePickerFiltro = new DatePicker();
        datePickerFiltro.setPromptText("dd/MM/yyyy");
        datePickerFiltro.setPrefWidth(140);

        DateTimeFormatter formatoFiltro =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        datePickerFiltro.setConverter(
                new javafx.util.StringConverter<LocalDate>() {
                    @Override
                    public String toString(LocalDate fecha) {
                        return fecha == null
                                ? ""
                                : formatoFiltro.format(fecha);
                    }

                    @Override
                    public LocalDate fromString(String texto) {
                        if (texto == null || texto.isBlank()) {
                            return null;
                        }

                        try {
                            return LocalDate.parse(
                                    texto.trim(),
                                    formatoFiltro
                            );
                        } catch (DateTimeParseException e) {
                            return null;
                        }
                    }
                }
        );

        Button btnLimpiarFiltros =
                new Button("LIMPIAR FILTROS");

        btnLimpiarFiltros.setStyle(
                "-fx-background-color: #E8ECE8;" +
                "-fx-text-fill: #23452C;" +
                "-fx-font-weight: bold;"
        );

        HBox filtros =
                new HBox(
                        10,
                        lblBuscar,
                        txtBuscarMovimiento,
                        lblFiltroTipo,
                        comboFiltroTipo,
                        lblFiltroFecha,
                        datePickerFiltro,
                        btnLimpiarFiltros
                );

        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(
                new Insets(10, 20, 10, 20)
        );


        // =====================================================
        // TABLA - HISTORIAL DE MOVIMIENTOS
        // =====================================================

        tabla = new TableView<>();

        TableColumn<Movimiento, String> columnaAnimal =
                new TableColumn<>("Animal");

        columnaAnimal.setCellValueFactory(
                new PropertyValueFactory<>("nombreAnimal")
        );
        columnaAnimal.setPrefWidth(150);


        TableColumn<Movimiento, String> columnaEspecie =
                new TableColumn<>("Especie");

        columnaEspecie.setCellValueFactory(
                new PropertyValueFactory<>("especie")
        );
        columnaEspecie.setPrefWidth(150);


        TableColumn<Movimiento, String> columnaActa =
                new TableColumn<>("N° de Acta");

        columnaActa.setCellValueFactory(
                new PropertyValueFactory<>("numeroActa")
        );
        columnaActa.setPrefWidth(110);


        TableColumn<Movimiento, String> columnaFecha =
                new TableColumn<>("Fecha");

        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaMovimiento")
        );
        columnaFecha.setPrefWidth(100);


        TableColumn<Movimiento, String> columnaTipo =
                new TableColumn<>("Movimiento");

        columnaTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipoMovimiento")
        );
        columnaTipo.setPrefWidth(160);


        TableColumn<Movimiento, Integer> columnaCantidad =
                new TableColumn<>("Cantidad");

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );
        columnaCantidad.setPrefWidth(80);


        TableColumn<Movimiento, String> columnaDestino =
                new TableColumn<>("Destino");

        columnaDestino.setCellValueFactory(
                new PropertyValueFactory<>("destino")
        );
        columnaDestino.setPrefWidth(150);


        TableColumn<Movimiento, String> columnaObservaciones =
                new TableColumn<>("Observaciones");

        columnaObservaciones.setCellValueFactory(
                new PropertyValueFactory<>("observaciones")
        );
        columnaObservaciones.setPrefWidth(200);


        tabla.getColumns().addAll(
                columnaAnimal,
                columnaEspecie,
                columnaActa,
                columnaFecha,
                columnaTipo,
                columnaCantidad,
                columnaDestino,
                columnaObservaciones
        );

        tabla.setPrefHeight(380);
        tabla.setMinHeight(300);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tabla.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Movimiento seleccionado =
                        tabla.getSelectionModel().getSelectedItem();

                if (seleccionado != null && ventanaPrincipal != null) {

                    int idAnimal =
                            obtenerIdAnimalPorDetalle(
                                    seleccionado.getIdDetalle()
                            );

                    if (idAnimal > 0) {
                        ventanaPrincipal.mostrarFichaAnimalDesdeMovimientos(
                                idAnimal
                        );
                    }
                }
            }
        });


        // =====================================================
        // APLICAR FILTROS
        // =====================================================

        Runnable aplicarFiltros = () -> {

            if (movimientosFiltrados == null) {
                return;
            }

            String texto =
                    txtBuscarMovimiento.getText()
                            .trim()
                            .toLowerCase();

            String tipo =
                    comboFiltroTipo.getValue();

            LocalDate fechaSeleccionada =
                    datePickerFiltro.getValue();

            String fechaTexto =
                    fechaSeleccionada == null
                            ? null
                            : formatoFiltro.format(
                                    fechaSeleccionada
                            );

            movimientosFiltrados.setPredicate(movimiento -> {

                if (movimiento == null) {
                    return false;
                }

                // Buscar por animal, especie, acta o destino
                if (!texto.isEmpty()) {

                    String animal =
                            movimiento.getNombreAnimal() == null
                                    ? ""
                                    : movimiento.getNombreAnimal()
                                            .toLowerCase();

                    String especie =
                            movimiento.getEspecie() == null
                                    ? ""
                                    : movimiento.getEspecie()
                                            .toLowerCase();

                    String acta =
                            movimiento.getNumeroActa() == null
                                    ? ""
                                    : movimiento.getNumeroActa()
                                            .toLowerCase();

                    String destino =
                            movimiento.getDestino() == null
                                    ? ""
                                    : movimiento.getDestino()
                                            .toLowerCase();

                    if (!animal.contains(texto)
                            && !especie.contains(texto)
                            && !acta.contains(texto)
                            && !destino.contains(texto)) {

                        return false;
                    }
                }

                // Filtrar por tipo
                if (tipo != null
                        && !"TODOS".equals(tipo)
                        && !tipo.equals(
                                movimiento.getTipoMovimiento()
                        )) {

                    return false;
                }

                // Filtrar por fecha
                if (fechaTexto != null
                        && !fechaTexto.equals(
                                movimiento.getFechaMovimiento()
                        )) {

                    return false;
                }

                return true;
            });
        };


        txtBuscarMovimiento.textProperty()
                .addListener(
                        (obs, viejo, nuevo) ->
                                aplicarFiltros.run()
                );

        comboFiltroTipo.valueProperty()
                .addListener(
                        (obs, viejo, nuevo) ->
                                aplicarFiltros.run()
                );

        datePickerFiltro.valueProperty()
                .addListener(
                        (obs, viejo, nuevo) ->
                                aplicarFiltros.run()
                );

        btnLimpiarFiltros.setOnAction(e -> {

            txtBuscarMovimiento.clear();
            comboFiltroTipo.setValue("TODOS");
            datePickerFiltro.setValue(null);

            aplicarFiltros.run();
        });


        cargarMovimientos();


        // =====================================================
        // CONTENEDOR SUPERIOR
        // =====================================================

        VBox parteSuperior =
                new VBox(
                        15,
                        titulo,
                        formulario,
                        botones,
                        filtros
                );

        parteSuperior.setAlignment(
                Pos.CENTER
        );


        // =====================================================
        // CONTENEDOR PRINCIPAL
        // =====================================================

        BorderPane contenedor =
                new BorderPane();

        contenedor.setTop(
                parteSuperior
        );

        contenedor.setCenter(
                tabla
        );


        BorderPane.setMargin(
                tabla,
                new Insets(20)
        );


        // =====================================================
        // SCROLL
        // =====================================================

        ScrollPane scroll =
                new ScrollPane(
                        contenedor
                );

        scroll.setFitToWidth(true);
        scroll.setVvalue(0);

        scroll.setFitToHeight(false);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setStyle(
                "-fx-background: #F4F2EA;" +
                "-fx-background-color: #F4F2EA;"
        );


        return scroll;
    }


    // =========================================================
    // CARGAR ANIMALES
    // =========================================================

    private void cargarAnimales() {

        ObservableList<Animal> animales =
                FXCollections.observableArrayList(
                        animalDAO.listar()
                );

        comboAnimal.setItems(
                animales
        );
    }


    // =========================================================
    // CARGAR HABITÁCULOS
    // =========================================================

    private void cargarHabitaculos() {

        ObservableList<Habitaculo> habitaculos =
                FXCollections.observableArrayList(
                        habitaculoDAO.listar()
                );

        comboHabitaculo.setItems(
                habitaculos
        );
    }


    // =========================================================
    // CARGAR ACTAS SEGÚN ANIMAL
    // =========================================================

    private void cargarIngresosPorAnimal() {

        Animal animalSeleccionado =
                comboAnimal.getValue();


        comboIngreso.getItems().clear();


        lblDisponible.setText(
                "Disponible en cuarentena: -"
        );


        if (animalSeleccionado == null) {
            return;
        }


        ObservableList<Ingreso> ingresos =
                FXCollections.observableArrayList(
                        ingresoDAO.listarPorAnimal(
                                animalSeleccionado
                                        .getIdAnimal()
                        )
                );


        comboIngreso.setItems(
                ingresos
        );


        comboIngreso.setPromptText(
                "Seleccione el N° de acta"
        );
    }


    // =========================================================
    // ACTUALIZAR DISPONIBILIDAD
    // =========================================================

    private void actualizarCantidadDisponible() {

        Animal animal =
                comboAnimal.getValue();

        Ingreso ingreso =
                comboIngreso.getValue();


        if (animal == null
                || ingreso == null) {

            lblDisponible.setText(
                    "Disponible en cuarentena: -"
            );

            return;
        }


        DetalleIngreso detalle =
                detalleIngresoDAO
                        .buscarPorAnimalEIngreso(
                                animal.getIdAnimal(),
                                ingreso.getIdIngreso()
                        );


        if (detalle == null) {

            lblDisponible.setText(
                    "Disponible en cuarentena: 0"
            );

            return;
        }


        int disponible =
                calcularDisponible(
                        detalle
                );


        lblDisponible.setText(
                "Disponible en cuarentena: "
                        + disponible
        );
    }


    // =========================================================
    // CALCULAR DISPONIBLE
    // =========================================================

    private int calcularDisponible(
            DetalleIngreso detalle) {

        int cantidadInicial =
                detalle.getCantidad();


        int utilizada = 0;


        for (Movimiento movimiento :
                movimientoDAO.listar()) {

            if (movimiento.getIdDetalle()
                    == detalle.getIdDetalle()) {

                utilizada +=
                        movimiento.getCantidad();
            }
        }


        int disponible =
                cantidadInicial - utilizada;


        if (disponible < 0) {
            disponible = 0;
        }


        return disponible;
    }


    // =========================================================
    // MOSTRAR / OCULTAR CAMPOS DEL PLANTEL
    // =========================================================

    private void actualizarCamposPlantel() {

        boolean esPlantel =
                "PLANTEL PERMANENTE".equals(
                        comboTipo.getValue()
                );


        // ---------------------------------------------
        // UBICACIÓN
        // ---------------------------------------------

        comboUbicacion.setVisible(
                esPlantel
        );

        comboUbicacion.setManaged(
                esPlantel
        );


        // Buscar la etiqueta dentro del formulario
        // se controla mediante el estado del ComboBox.
        //
        // La etiqueta se mantiene visible únicamente
        // cuando corresponde.


        // ---------------------------------------------
        // HABITÁCULO
        // ---------------------------------------------

        boolean mostrarHabitaculo =
                esPlantel
                        && (
                        "Habitáculo".equals(
                                comboUbicacion.getValue()
                        )
                        ||
                        "Recinto".equals(
                                comboUbicacion.getValue()
                        )
                );


        comboHabitaculo.setVisible(
                mostrarHabitaculo
        );

        comboHabitaculo.setManaged(
                mostrarHabitaculo
        );


        if (!esPlantel) {

            comboUbicacion.setValue(
                    null
            );

            comboHabitaculo.setValue(
                    null
            );
        }
    }


    // =========================================================
    // ACTUALIZAR HABITÁCULO
    // =========================================================

    private void actualizarHabitaculo() {

        boolean mostrar =
                "PLANTEL PERMANENTE".equals(
                        comboTipo.getValue()
                )
                &&
                (
                        "Habitáculo".equals(
                                comboUbicacion.getValue()
                        )
                        ||
                        "Recinto".equals(
                                comboUbicacion.getValue()
                        )
                );


        comboHabitaculo.setVisible(
                mostrar
        );

        comboHabitaculo.setManaged(
                mostrar
        );


        if (!mostrar) {

            comboHabitaculo.setValue(
                    null
            );
        }
    }


    // =========================================================
    // ESTILOS Y VALIDACIÓN
    // =========================================================

    private Label crearLabelCampo(String texto) {
        return crearLabelCampo(texto, false);
    }

    private Label crearLabelCampo(String texto, boolean obligatorio) {
        Label label = new Label(obligatorio ? texto + " *" : texto);
        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                (obligatorio
                        ? "-fx-text-fill: #B43C3C;"
                        : "-fx-text-fill: #445149;")
        );
        return label;
    }

    private void marcarCampoError(javafx.scene.Node campo, boolean error) {
        campo.setStyle(error
                ? "-fx-border-color: #C94A4A;" +
                  "-fx-border-width: 2;" +
                  "-fx-border-radius: 8;" +
                  "-fx-background-radius: 8;"
                : "-fx-border-color: #C9D2CB;" +
                  "-fx-border-radius: 8;" +
                  "-fx-background-radius: 8;");
    }


    // =========================================================
    // GUARDAR MOVIMIENTO
    // =========================================================

    private void guardarMovimiento() {

        try {

            // =================================================
            // VALIDACIONES
            // =================================================

            if (comboAnimal.getValue() == null) {
                marcarCampoError(comboAnimal, true);

                mostrarMensaje(
                        "Seleccione un animal."
                );

                return;
            }


            if (comboIngreso.getValue() == null) {
                marcarCampoError(comboIngreso, true);

                mostrarMensaje(
                        "Seleccione el número de acta."
                );

                return;
            }


            if (datePickerFecha.getValue() == null) {
                marcarCampoError(datePickerFecha, true);

                mostrarMensaje(
                        "Ingrese la fecha del movimiento."
                );

                return;
            }


            if (comboTipo.getValue() == null) {
                marcarCampoError(comboTipo, true);

                mostrarMensaje(
                        "Seleccione el tipo de movimiento."
                );

                return;
            }


            if (txtCantidad.getText().isBlank()) {
                marcarCampoError(txtCantidad, true);

                mostrarMensaje(
                        "Ingrese la cantidad."
                );

                return;
            }


            // =================================================
            // OBTENER DATOS
            // =================================================

            Animal animal =
                    comboAnimal.getValue();

            Ingreso ingreso =
                    comboIngreso.getValue();


            DetalleIngreso detalle =
                    detalleIngresoDAO
                            .buscarPorAnimalEIngreso(
                                    animal.getIdAnimal(),
                                    ingreso.getIdIngreso()
                            );


            if (detalle == null) {

                mostrarMensaje(
                        "No se encontró el detalle correspondiente al animal y al acta seleccionados."
                );

                return;
            }


            // =================================================
            // CANTIDAD
            // =================================================

            int cantidad =
                    Integer.parseInt(
                            txtCantidad
                                    .getText()
                                    .trim()
                    );


            if (cantidad <= 0) {

                mostrarMensaje(
                        "La cantidad debe ser mayor que cero."
                );

                return;
            }


            // =================================================
            // DISPONIBILIDAD
            // =================================================

            int disponible =
                    calcularDisponible(
                            detalle
                    );


            if (cantidad > disponible) {

                mostrarMensaje(
                        "La cantidad ingresada supera la cantidad disponible en cuarentena."
                                + "\n\n"
                                + "Disponible: "
                                + disponible
                );

                return;
            }


            // =================================================
            // TRASLADO
            // =================================================

            if ("TRASLADO".equals(
                    comboTipo.getValue()
            )) {

                if (txtDestino.getText()
                        .isBlank()) {

                    mostrarMensaje(
                            "Ingrese el destino del traslado."
                    );

                    return;
                }
            }


            // =================================================
            // PLANTEL PERMANENTE
            // =================================================

            if ("PLANTEL PERMANENTE".equals(
                    comboTipo.getValue()
            )) {

                if (comboUbicacion.getValue()
                        == null) {

                    mostrarMensaje(
                            "Seleccione el tipo de ubicación del Plantel Permanente."
                    );

                    return;
                }


                boolean necesitaHabitaculo =
                        "Habitáculo".equals(
                                comboUbicacion
                                        .getValue()
                        )
                        ||
                        "Recinto".equals(
                                comboUbicacion
                                        .getValue()
                        );


                if (necesitaHabitaculo
                        && comboHabitaculo
                        .getValue() == null) {

                    mostrarMensaje(
                            "Seleccione el habitáculo correspondiente."
                    );

                    return;
                }
            }


            // =================================================
            // CREAR MOVIMIENTO
            // =================================================

            Movimiento movimiento =
                    new Movimiento();


            movimiento.setIdDetalle(
                    detalle.getIdDetalle()
            );


            movimiento.setFechaMovimiento(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            .format(datePickerFecha.getValue())
            );


            movimiento.setTipoMovimiento(
                    comboTipo.getValue()
            );


            movimiento.setCantidad(
                    cantidad
            );


            movimiento.setDestino(
                    txtDestino
                            .getText()
                            .trim()
            );


            movimiento.setObservaciones(
                    txtObservaciones
                            .getText()
                            .trim()
            );


            // =================================================
            // CONFIRMACIÓN
            // =================================================

            Alert confirmacion =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );


            confirmacion.setTitle(
                    "Confirmar movimiento"
            );


            confirmacion.setHeaderText(
                    "¿Desea registrar este movimiento?"
            );


            confirmacion.setContentText(
                    "Animal: "
                            + animal.getNombreVulgar()
                            + "\nTipo: "
                            + movimiento
                            .getTipoMovimiento()
                            + "\nCantidad: "
                            + cantidad
            );


            var resultado =
                    confirmacion.showAndWait();


            if (resultado.isEmpty()) {
                return;
            }


            if (resultado.get()
                    != javafx.scene.control.ButtonType.OK) {

                return;
            }


            // =================================================
            // GUARDAR MOVIMIENTO
            // =================================================

            movimientoDAO.agregar(
                    movimiento
            );


            // =================================================
            // SI ES PLANTEL PERMANENTE,
            // CREAR REGISTRO EN PLANTEL
            // =================================================

            if ("PLANTEL PERMANENTE".equals(
                    movimiento.getTipoMovimiento()
            )) {

                PlantelPermanente plantel =
                        new PlantelPermanente();


                plantel.setIdAnimal(
                        detalle.getIdAnimal()
                );


                plantel.setCantidad(
                        cantidad
                );


                plantel.setTipoUbicacion(
                        comboUbicacion.getValue()
                );


                if (comboHabitaculo
                        .getValue() != null) {

                    plantel.setIdHabitaculo(
                            comboHabitaculo
                                    .getValue()
                                    .getIdHabitaculo()
                    );

                } else {

                    plantel.setIdHabitaculo(
                            null
                    );
                }


                plantel.setFechaIngresoPlantel(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(datePickerFecha.getValue())
                );


                plantel.setEstado(
                        "Activo"
                );


                plantel.setObservaciones(
                        txtObservaciones
                                .getText()
                                .trim()
                );


                plantelDAO.agregar(
                        plantel
                );
            }


            // =================================================
            // ACTUALIZAR
            // =================================================

            mostrarInformacion(
                    "Movimiento registrado correctamente."
            );


            limpiarCampos();

            cargarMovimientos();
        }


        catch (NumberFormatException e) {

            mostrarMensaje(
                    "La cantidad debe ser un número entero."
            );
        }


        catch (Exception e) {

            mostrarMensaje(
                    "Ocurrió un error al registrar el movimiento."
                            + "\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // OBTENER ANIMAL DESDE EL DETALLE DEL MOVIMIENTO
    // =========================================================

    private int obtenerIdAnimalPorDetalle(int idDetalle) {

        String sql = """
                SELECT id_animal
                FROM detalle_ingreso
                WHERE id_detalle = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idDetalle);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt("id_animal");
                }
            }

        } catch (Exception e) {
            System.out.println(
                    "Error al obtener el animal del movimiento."
            );
            System.out.println(e.getMessage());
        }

        return 0;
    }


    // =========================================================
    // CARGAR TABLA
    // =========================================================

    private void cargarMovimientos() {

        ObservableList<Movimiento> movimientos =
                FXCollections.observableArrayList(
                        movimientoDAO.listar()
                );

        movimientosFiltrados =
                new FilteredList<>(
                        movimientos,
                        movimiento -> true
                );

        tabla.setItems(
                movimientosFiltrados
        );
    }


    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiarCampos() {

        comboAnimal.setValue(
                null
        );


        comboIngreso.getItems().clear();


        comboIngreso.setValue(
                null
        );


        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );

        comboIngreso.setCellFactory(lista -> new javafx.scene.control.ListCell<Ingreso>() {
            @Override
            protected void updateItem(Ingreso ingreso, boolean vacio) {
                super.updateItem(ingreso, vacio);
                setText(vacio || ingreso == null
                        ? null
                        : "Acta " + ingreso.getNumeroActa()
                          + " — " + ingreso.getFechaIngreso());
            }
        });

        comboIngreso.setButtonCell(new javafx.scene.control.ListCell<Ingreso>() {
            @Override
            protected void updateItem(Ingreso ingreso, boolean vacio) {
                super.updateItem(ingreso, vacio);
                setText(vacio || ingreso == null
                        ? null
                        : "Acta " + ingreso.getNumeroActa()
                          + " — " + ingreso.getFechaIngreso());
            }
        });


        lblDisponible.setText(
                "Disponible en cuarentena: -"
        );


        datePickerFecha.setValue(null);


        comboTipo.setValue(
                null
        );


        txtCantidad.clear();


        txtDestino.clear();


        comboUbicacion.setValue(
                null
        );


        comboHabitaculo.setValue(
                null
        );


        txtObservaciones.clear();


        actualizarCamposPlantel();
    }


    // =========================================================
    // MENSAJE DE ADVERTENCIA
    // =========================================================

    private void mostrarMensaje(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alerta.setTitle(
                "Tatú Carreta"
        );


        alerta.setHeaderText(
                null
        );


        alerta.setContentText(
                mensaje
        );


        alerta.showAndWait();
    }


    // =========================================================
    // MENSAJE INFORMACIÓN
    // =========================================================

    private void mostrarInformacion(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alerta.setTitle(
                "Tatú Carreta"
        );


        alerta.setHeaderText(
                null
        );


        alerta.setContentText(
                mensaje
        );


        alerta.showAndWait();
    }


    // =========================================================
    // MOSTRAR VENTANA
    // =========================================================

    public void mostrar(
            javafx.stage.Stage escenario) {

        javafx.scene.Scene escena =
                new javafx.scene.Scene(
                        crearContenido(),
                        1000,
                        750
                );


        escenario.setTitle(
                "Tatú Carreta - Movimientos"
        );


        escenario.setScene(
                escena
        );


        escenario.show();
    }
}