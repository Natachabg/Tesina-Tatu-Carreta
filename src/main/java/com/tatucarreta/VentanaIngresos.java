package com.tatucarreta;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VentanaIngresos {

    private VentanaPrincipal ventanaPrincipal;

    public VentanaIngresos() {
    }

    public VentanaIngresos(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    // =========================================================
    // DAO
    // =========================================================

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final DetalleIngresoDAO detalleDAO = new DetalleIngresoDAO();
    private final EspecieDAO especieDAO = new EspecieDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();

    // =========================================================
    // TABLAS
    // =========================================================

    private final TableView<Ingreso> tablaIngresos = new TableView<>();
    private final TableView<DetalleIngreso> tablaDetalles = new TableView<>();

    // =========================================================
    // DATOS DEL INGRESO
    // =========================================================

    private final TextField txtNumeroActa = new TextField();
    private final DatePicker datePickerFechaIngreso = new DatePicker();
    private final TextField txtOrganismoProcedencia = new TextField();
    private final TextField txtResponsableEntrega = new TextField();
    private final TextField txtProcedencia = new TextField();
    private final TextField txtMotivoIngreso = new TextField();
    private final TextArea txtDocumentacion = new TextArea();
    private final TextArea txtObservaciones = new TextArea();

    // =========================================================
    // DATOS DEL ANIMAL
    // =========================================================

    private final ComboBox<Especie> comboEspecie = new ComboBox<>();
    private final ComboBox<Animal> comboAnimal = new ComboBox<>();
    private final TextField txtCantidad = new TextField();
    private final TextField txtSexo = new TextField();
    private final TextField txtEdad = new TextField();
    private final TextField txtPeso = new TextField();
    private final TextField txtEstadoIngreso = new TextField();
    private final TextArea txtObservacionesDetalle = new TextArea();

    // Identificación individual opcional
    private final ComboBox<String> comboTipoIdentificacion =
            new ComboBox<>();

    private final TextField txtNumeroIdentificacion =
            new TextField();

    // =========================================================
    // NUEVO ANIMAL - INTEGRADO
    // =========================================================

    private final VBox panelNuevoAnimal = new VBox(10);
    private boolean formularioNuevoAnimalVisible = false;

    private final TextField txtNuevoNombre = new TextField();
    private final TextField txtNuevoNombreCientifico = new TextField();
    private final TextField txtNuevoOrigen = new TextField();
    private final ComboBox<String> comboNuevoEstado = new ComboBox<>();

    // =========================================================
    // FORMULARIO DE CARGA DE ANIMAL
    // =========================================================

    private final VBox panelCargaAnimal = new VBox(12);
    private boolean formularioAnimalVisible = false;

    private Button btnMostrarFormularioAnimal;

    // =========================================================
    // PENDIENTES
    // =========================================================

    private final List<DetalleIngreso> detallesPendientes = new ArrayList<>();

    // Identificación opcional asociada a cada detalle pendiente.
    // Se guarda al confirmar el ingreso, cuando ya existe el id_detalle.
    private final Map<DetalleIngreso, String[]> identificacionesPendientes =
            new HashMap<>();

    private final Map<Integer, String[]> identificacionesGuardadas =
            new HashMap<>();

    // =========================================================
    // MAPAS
    // =========================================================

    private final Map<Integer, Animal> mapaAnimales = new HashMap<>();
    private final Map<Integer, Especie> mapaEspecies = new HashMap<>();

    private final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================
    // CREAR CONTENIDO
    // =========================================================

    public ScrollPane crearContenido(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        return crearContenido();
    }

    public ScrollPane crearContenido() {

        cargarDatosParaMostrar();
        configurarCamposIngreso();
        configurarCamposAnimal();

        // -----------------------------------------------------
        // ENCABEZADO
        // -----------------------------------------------------

        Label breadcrumb = new Label("Inicio / Gestión de Ingresos");
        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #6B8174;"
        );

        Label titulo = new Label("Gestión de Ingresos");
        titulo.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1E513B;"
        );

        Label subtitulo = new Label(
                "Registre cada acta y todos los animales que ingresan en ella."
        );
        subtitulo.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #60746A;"
        );

        VBox encabezado = new VBox(
                4,
                breadcrumb,
                titulo,
                subtitulo
        );

        // -----------------------------------------------------
        // DATOS DEL INGRESO
        // -----------------------------------------------------

        GridPane formularioIngreso = crearFormulario();

        agregarCampo(
                formularioIngreso,
                "N° de Acta",
                txtNumeroActa,
                true,
                0,
                0
        );

        agregarCampo(
                formularioIngreso,
                "Fecha de Ingreso",
                datePickerFechaIngreso,
                true,
                1,
                0
        );

        agregarCampo(
                formularioIngreso,
                "Organismo de Procedencia",
                txtOrganismoProcedencia,
                true,
                2,
                0
        );

        agregarCampo(
                formularioIngreso,
                "Responsable de Entrega",
                txtResponsableEntrega,
                false,
                3,
                0
        );

        agregarCampo(
                formularioIngreso,
                "Procedencia",
                txtProcedencia,
                true,
                0,
                2
        );

        agregarCampo(
                formularioIngreso,
                "Motivo de Ingreso",
                txtMotivoIngreso,
                true,
                1,
                2
        );

        agregarCampo(
                formularioIngreso,
                "Documentación",
                txtDocumentacion,
                false,
                2,
                2
        );

        agregarCampo(
                formularioIngreso,
                "Observaciones",
                txtObservaciones,
                false,
                3,
                2
        );

        txtDocumentacion.setPrefHeight(52);
        txtObservaciones.setPrefHeight(52);

        VBox tarjetaIngreso = crearTarjeta();

        tarjetaIngreso.getChildren().add(
                crearTituloConAyuda(
                        "Datos del ingreso",
                        "Información general del acta."
                )
        );

        tarjetaIngreso.getChildren().add(
                formularioIngreso
        );

        // -----------------------------------------------------
        // ANIMALES DEL ACTA
        // -----------------------------------------------------

        HBox cabeceraAnimales = new HBox(12);
        cabeceraAnimales.setAlignment(Pos.CENTER_LEFT);

        VBox textosAnimales = new VBox(3);

        textosAnimales.getChildren().add(
                crearTituloTarjeta("Animales del acta")
        );

        Label ayudaAnimales = new Label(
                "Puede agregar varios animales o especies dentro de la misma acta."
        );
        ayudaAnimales.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #708078;"
        );

        textosAnimales.getChildren().add(
                ayudaAnimales
        );

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        btnMostrarFormularioAnimal = new Button(
                "+ AGREGAR ANIMAL"
        );
        aplicarEstiloPrincipal(btnMostrarFormularioAnimal);

        cabeceraAnimales.getChildren().addAll(
                textosAnimales,
                espacio,
                btnMostrarFormularioAnimal
        );

        // -----------------------------------------------------
        // PANEL DE CARGA DEL ANIMAL
        // -----------------------------------------------------

        construirPanelCargaAnimal();

        panelCargaAnimal.setVisible(false);
        panelCargaAnimal.setManaged(false);

        btnMostrarFormularioAnimal.setOnAction(e -> {

            if (formularioAnimalVisible) {
                ocultarFormularioCargaAnimal();
            } else {
                mostrarFormularioCargaAnimal();
            }
        });

        VBox tarjetaAnimales = crearTarjeta();

        tarjetaAnimales.getChildren().add(
                cabeceraAnimales
        );

        tarjetaAnimales.getChildren().add(
                panelCargaAnimal
        );

        Label tituloTabla = crearTituloTarjeta(
                "Animales cargados en el acta"
        );

        tarjetaAnimales.getChildren().add(
                tituloTabla
        );

        configurarTablaDetalles();
        tablaDetalles.setPrefHeight(180);

        tarjetaAnimales.getChildren().add(
                tablaDetalles
        );

        // -----------------------------------------------------
        // INGRESOS REGISTRADOS
        // -----------------------------------------------------

        configurarTablaIngresos();
        cargarIngresos();

        VBox tarjetaIngresos = crearTarjeta();

        tarjetaIngresos.getChildren().add(
                crearTituloConAyuda(
                        "Ingresos registrados",
                        "Doble clic sobre un acta para consultar su ficha completa."
                )
        );

        tablaIngresos.setPrefHeight(230);

        tarjetaIngresos.getChildren().add(
                tablaIngresos
        );

        // -----------------------------------------------------
        // BOTONES
        // -----------------------------------------------------

        Button btnLimpiar = new Button("LIMPIAR");
        Button btnEliminar = new Button("ELIMINAR");
        Button btnModificar = new Button("MODIFICAR");
        Button btnGuardar = new Button("GUARDAR INGRESO");

        aplicarEstiloSecundario(btnLimpiar);
        aplicarEstiloEliminar(btnEliminar);
        aplicarEstiloSecundario(btnModificar);
        aplicarEstiloPrincipal(btnGuardar);

        btnLimpiar.setOnAction(e -> limpiarTodo());
        btnGuardar.setOnAction(e -> guardarIngreso());
        btnModificar.setOnAction(e -> modificarIngreso());
        btnEliminar.setOnAction(e -> eliminarIngreso());

        HBox botones = new HBox(
                10,
                btnLimpiar,
                btnEliminar,
                btnModificar,
                btnGuardar
        );

        botones.setAlignment(Pos.CENTER_RIGHT);

        // -----------------------------------------------------
        // SELECCIÓN DE INGRESO
        // -----------------------------------------------------

        tablaIngresos.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {

                            if (seleccionado != null) {

                                cargarDatosIngreso(
                                        seleccionado
                                );

                                cargarDetalles(
                                        seleccionado.getIdIngreso()
                                );
                            }
                        }
                );

        tablaIngresos.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Ingreso seleccionado =
                        tablaIngresos
                                .getSelectionModel()
                                .getSelectedItem();

                if (
                        seleccionado != null
                                && ventanaPrincipal != null
                ) {

                    ventanaPrincipal.mostrarFichaIngreso(
                            seleccionado.getIdIngreso()
                    );
                }
            }
        });

        // -----------------------------------------------------
        // CONTENIDO
        // -----------------------------------------------------

        VBox contenido = new VBox(
                16,
                encabezado,
                tarjetaIngreso,
                tarjetaAnimales,
                botones,
                tarjetaIngresos
        );

        contenido.setPadding(
                new Insets(20, 28, 30, 28)
        );

        contenido.setStyle(
                "-fx-background-color: #E7F1EA;"
        );

        ScrollPane scroll = new ScrollPane(
                contenido
        );

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background: #E7F1EA;" +
                "-fx-background-color: #E7F1EA;"
        );

        return scroll;
    }

    // =========================================================
    // PANEL CARGA ANIMAL
    // =========================================================

    private void construirPanelCargaAnimal() {

        GridPane formulario = crearFormulario();

        agregarCampo(
                formulario,
                "Especie",
                comboEspecie,
                true,
                0,
                0
        );

        agregarCampo(
                formulario,
                "Animal",
                comboAnimal,
                true,
                1,
                0
        );

        agregarCampo(
                formulario,
                "Cantidad",
                txtCantidad,
                true,
                2,
                0
        );

        agregarCampo(
                formulario,
                "Sexo",
                txtSexo,
                false,
                3,
                0
        );

        agregarCampo(
                formulario,
                "Edad",
                txtEdad,
                false,
                0,
                2
        );

        agregarCampo(
                formulario,
                "Peso",
                txtPeso,
                false,
                1,
                2
        );

        agregarCampo(
                formulario,
                "Estado al ingreso",
                txtEstadoIngreso,
                false,
                2,
                2
        );

        comboTipoIdentificacion.getItems().setAll(
                "Sin identificación",
                "Chip",
                "Caravana",
                "Anillo",
                "Otro"
        );
        comboTipoIdentificacion.setValue(
                "Sin identificación"
        );

        txtNumeroIdentificacion.setPromptText(
                "Número de identificación"
        );

        agregarCampo(
                formulario,
                "Tipo de identificación",
                comboTipoIdentificacion,
                false,
                3,
                2
        );

        agregarCampo(
                formulario,
                "N° de identificación",
                txtNumeroIdentificacion,
                false,
                0,
                4
        );

        VBox observaciones = new VBox(4);

        observaciones.getChildren().add(
                crearLabelCampo(
                        "Observaciones del animal"
                )
        );

        txtObservacionesDetalle.setPrefHeight(55);
        observaciones.getChildren().add(
                txtObservacionesDetalle
        );

        Button btnNuevoAnimal = new Button(
                "+ CREAR NUEVO ANIMAL"
        );
        aplicarEstiloSecundario(btnNuevoAnimal);

        btnNuevoAnimal.setOnAction(
                e -> mostrarFormularioNuevoAnimal()
        );

        Button btnAgregar = new Button(
                "AGREGAR ESTE ANIMAL"
        );
        aplicarEstiloPrincipal(btnAgregar);

        btnAgregar.setOnAction(
                e -> agregarDetallePendiente()
        );

        Button btnCancelar = new Button(
                "CANCELAR"
        );
        aplicarEstiloSecundario(btnCancelar);

        btnCancelar.setOnAction(
                e -> ocultarFormularioCargaAnimal()
        );

        HBox botones = new HBox(
                10,
                btnNuevoAnimal,
                new Region(),
                btnCancelar,
                btnAgregar
        );

        HBox.setHgrow(
                botones.getChildren().get(1),
                Priority.ALWAYS
        );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        panelCargaAnimal.getChildren().clear();

        panelCargaAnimal.getChildren().addAll(
                formulario,
                observaciones,
                botones,
                panelNuevoAnimal
        );

        panelCargaAnimal.setPadding(
                new Insets(15)
        );

        panelCargaAnimal.setStyle(
                "-fx-background-color: #EFF7F1;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #C2D8C9;" +
                "-fx-border-radius: 12;"
        );

        panelNuevoAnimal.setVisible(false);
        panelNuevoAnimal.setManaged(false);
    }

    // =========================================================
    // MOSTRAR CARGA ANIMAL
    // =========================================================

    private void mostrarFormularioCargaAnimal() {

        formularioAnimalVisible = true;

        panelCargaAnimal.setVisible(true);
        panelCargaAnimal.setManaged(true);

        if (btnMostrarFormularioAnimal != null) {
            btnMostrarFormularioAnimal.setText(
                    "− CERRAR CARGA"
            );
        }
    }

    private void ocultarFormularioCargaAnimal() {

        formularioAnimalVisible = false;

        panelCargaAnimal.setVisible(false);
        panelCargaAnimal.setManaged(false);

        if (btnMostrarFormularioAnimal != null) {
            btnMostrarFormularioAnimal.setText(
                    "+ AGREGAR ANIMAL"
            );
        }

        ocultarFormularioNuevoAnimal();
        limpiarDetalle();
    }

    // =========================================================
    // NUEVO ANIMAL INTEGRADO
    // =========================================================

    private void mostrarFormularioNuevoAnimal() {

        Especie especie = comboEspecie.getValue();

        if (especie == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Primero seleccione una especie."
            );

            return;
        }

        formularioNuevoAnimalVisible = true;

        panelNuevoAnimal.getChildren().clear();

        Label titulo = new Label(
                "Crear nuevo animal"
        );

        titulo.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #20513D;"
        );

        Label ayuda = new Label(
                "La cantidad se carga abajo, como parte del acta."
        );

        ayuda.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #718078;"
        );

        txtNuevoNombre.setPromptText(
                "Nombre vulgar *"
        );

        txtNuevoNombreCientifico.setPromptText(
                "Nombre científico"
        );

        txtNuevoOrigen.setPromptText(
                "Origen"
        );

        comboNuevoEstado.getItems().clear();
        comboNuevoEstado.getItems().addAll(
                "Activo",
                "Inactivo"
        );
        comboNuevoEstado.setValue("Activo");

        GridPane formulario = crearFormulario();

        agregarCampo(
                formulario,
                "Nombre vulgar",
                txtNuevoNombre,
                true,
                0,
                0
        );

        agregarCampo(
                formulario,
                "Nombre científico",
                txtNuevoNombreCientifico,
                false,
                1,
                0
        );

        agregarCampo(
                formulario,
                "Origen",
                txtNuevoOrigen,
                false,
                2,
                0
        );

        agregarCampo(
                formulario,
                "Estado",
                comboNuevoEstado,
                false,
                3,
                0
        );

        Button btnCancelar = new Button(
                "CANCELAR"
        );

        Button btnGuardar = new Button(
                "GUARDAR ANIMAL"
        );

        aplicarEstiloSecundario(btnCancelar);
        aplicarEstiloPrincipal(btnGuardar);

        btnCancelar.setOnAction(
                e -> ocultarFormularioNuevoAnimal()
        );

        btnGuardar.setOnAction(
                e -> guardarNuevoAnimal()
        );

        HBox botones = new HBox(
                10,
                btnCancelar,
                btnGuardar
        );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        panelNuevoAnimal.getChildren().addAll(
                titulo,
                ayuda,
                formulario,
                botones
        );

        panelNuevoAnimal.setPadding(
                new Insets(12)
        );

        panelNuevoAnimal.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #BBD2C2;" +
                "-fx-border-radius: 10;"
        );

        panelNuevoAnimal.setVisible(true);
        panelNuevoAnimal.setManaged(true);
    }

    private void ocultarFormularioNuevoAnimal() {

        formularioNuevoAnimalVisible = false;

        panelNuevoAnimal.setVisible(false);
        panelNuevoAnimal.setManaged(false);

        txtNuevoNombre.clear();
        txtNuevoNombreCientifico.clear();
        txtNuevoOrigen.clear();
        comboNuevoEstado.setValue("Activo");
    }

    private void guardarNuevoAnimal() {

        Especie especie = comboEspecie.getValue();

        if (especie == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione una especie."
            );

            return;
        }

        String nombre =
                txtNuevoNombre.getText().trim();

        if (nombre.isBlank()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Ingrese el nombre vulgar del animal."
            );

            return;
        }

        Animal animal = new Animal();

        animal.setIdEspecie(
                especie.getIdEspecie()
        );

        animal.setNombreVulgar(
                nombre
        );

        animal.setNombreCientifico(
                txtNuevoNombreCientifico
                        .getText()
                        .trim()
        );

        animal.setCantidadActual(0);

        animal.setOrigen(
                txtNuevoOrigen
                        .getText()
                        .trim()
        );

        animal.setEstado(
                comboNuevoEstado.getValue()
        );

        animalDAO.agregar(animal);

        cargarDatosParaMostrar();
        cargarAnimalesPorEspecie(especie);

        Animal creado =
                buscarAnimalCreado(
                        especie.getIdEspecie(),
                        nombre
                );

        if (creado != null) {
            comboAnimal.setValue(creado);
        }

        ocultarFormularioNuevoAnimal();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Animal creado correctamente."
        );
    }

    // =========================================================
    // AGREGAR DETALLE
    // =========================================================

    private void agregarDetallePendiente() {

        // El animal forma parte de un acta, por eso primero
        // se deben completar los datos obligatorios del ingreso.
        if (!datosIngresoValidos()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Complete primero los datos obligatorios del acta."
            );

            return;
        }

        Animal animal =
                comboAnimal.getValue();

        if (animal == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione un animal."
            );

            return;
        }

        if (txtCantidad.getText().trim().isBlank()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Ingrese la cantidad."
            );

            return;
        }

        try {

            int cantidad =
                    Integer.parseInt(
                            txtCantidad
                                    .getText()
                                    .trim()
                    );

            if (cantidad <= 0) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "La cantidad debe ser mayor que cero."
                );

                return;
            }

            Double peso = null;

            if (!txtPeso.getText().trim().isBlank()) {

                peso =
                        Double.parseDouble(
                                txtPeso
                                        .getText()
                                        .trim()
                        );
            }

            DetalleIngreso detalle =
                    new DetalleIngreso();

            detalle.setIdAnimal(
                    animal.getIdAnimal()
            );

            detalle.setCantidad(cantidad);

            detalle.setSexo(
                    txtSexo.getText().trim()
            );

            detalle.setEdad(
                    txtEdad.getText().trim()
            );

            detalle.setPeso(peso);

            detalle.setEstadoIngreso(
                    "CUARENTENA"
            );

            detalle.setObservaciones(
                    txtObservacionesDetalle
                            .getText()
                            .trim()
            );

            String tipoIdentificacion =
                    comboTipoIdentificacion.getValue();

            String numeroIdentificacion =
                    txtNumeroIdentificacion
                            .getText()
                            .trim();

            boolean sinIdentificacion =
                    tipoIdentificacion == null
                            || tipoIdentificacion.equals(
                                    "Sin identificación"
                            );

            if (
                    sinIdentificacion
                            && numeroIdentificacion.isBlank()
            ) {
                // Correcto: el animal no tiene identificación.
            } else if (
                    !sinIdentificacion
                            && !numeroIdentificacion.isBlank()
            ) {
                // Correcto: tipo y número están completos.
            } else {
                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Si el animal tiene identificación, complete el tipo y el número."
                );
                return;
            }

            // Confirmación antes de agregar el animal al acta.
            Alert confirmacion = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmacion.setTitle("Tatú Carreta");
            confirmacion.setHeaderText("Confirmar carga");
            confirmacion.setContentText(
                    "¿Está seguro de que desea agregar este animal al acta?"
            );

            java.util.Optional<javafx.scene.control.ButtonType> respuesta =
                    confirmacion.showAndWait();

            if (
                    respuesta.isEmpty()
                            || respuesta.get()
                                    != javafx.scene.control.ButtonType.OK
            ) {
                return;
            }

            detallesPendientes.add(
                    detalle
            );

            if (
                    !sinIdentificacion
                            && !numeroIdentificacion.isBlank()
            ) {
                identificacionesPendientes.put(
                        detalle,
                        new String[] {
                                tipoIdentificacion,
                                numeroIdentificacion
                        }
                );
            }

            actualizarTablaDetallesPendientes();
            limpiarDetalle();

            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "Animal agregado al acta."
            );

        } catch (
                NumberFormatException e
        ) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Cantidad y peso deben ser números válidos."
            );
        }
    }

    private void actualizarTablaDetallesPendientes() {

        tablaDetalles.setItems(
                FXCollections.observableArrayList(
                        detallesPendientes
                )
        );
    }

    // =========================================================
    // TABLA DETALLES
    // =========================================================

    private void configurarTablaDetalles() {

        tablaDetalles.getColumns().clear();

        TableColumn<DetalleIngreso, String> especie =
                new TableColumn<>("Especie");

        especie.setCellValueFactory(
                dato -> {

                    Animal animal =
                            mapaAnimales.get(
                                    dato.getValue()
                                            .getIdAnimal()
                            );

                    if (animal == null) {
                        return new SimpleStringProperty("-");
                    }

                    Especie esp =
                            mapaEspecies.get(
                                    animal.getIdEspecie()
                            );

                    return new SimpleStringProperty(
                            esp == null
                                    ? "-"
                                    : esp.getNombre()
                    );
                }
        );

        TableColumn<DetalleIngreso, String> animal =
                new TableColumn<>("Animal");

        animal.setCellValueFactory(
                dato -> {

                    Animal a =
                            mapaAnimales.get(
                                    dato.getValue()
                                            .getIdAnimal()
                            );

                    return new SimpleStringProperty(
                            a == null
                                    ? "-"
                                    : a.getNombreVulgar()
                    );
                }
        );

        TableColumn<DetalleIngreso, Integer> cantidad =
                new TableColumn<>("Cantidad");

        cantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );

        TableColumn<DetalleIngreso, String> sexo =
                new TableColumn<>("Sexo");

        sexo.setCellValueFactory(
                new PropertyValueFactory<>("sexo")
        );

        TableColumn<DetalleIngreso, String> edad =
                new TableColumn<>("Edad");

        edad.setCellValueFactory(
                new PropertyValueFactory<>("edad")
        );

        TableColumn<DetalleIngreso, Double> peso =
                new TableColumn<>("Peso");

        peso.setCellValueFactory(
                new PropertyValueFactory<>("peso")
        );

        TableColumn<DetalleIngreso, String> estado =
                new TableColumn<>("Estado");

        estado.setCellValueFactory(
                new PropertyValueFactory<>("estadoIngreso")
        );

        TableColumn<DetalleIngreso, String> tipoId =
                new TableColumn<>("Tipo ID");

        tipoId.setCellValueFactory(
                dato -> new SimpleStringProperty(
                        obtenerTipoIdentificacion(
                                dato.getValue()
                        )
                )
        );

        TableColumn<DetalleIngreso, String> numeroId =
                new TableColumn<>("N° Identificación");

        numeroId.setCellValueFactory(
                dato -> new SimpleStringProperty(
                        obtenerNumeroIdentificacion(
                                dato.getValue()
                        )
                )
        );

        tablaDetalles.getColumns().addAll(
                especie,
                animal,
                cantidad,
                sexo,
                edad,
                peso,
                estado,
                tipoId,
                numeroId
        );

        tablaDetalles.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    // =========================================================
    // TABLA INGRESOS
    // =========================================================

    private void configurarTablaIngresos() {

        tablaIngresos.getColumns().clear();

        TableColumn<Ingreso, String> acta =
                new TableColumn<>("N° Acta");

        acta.setCellValueFactory(
                new PropertyValueFactory<>("numeroActa")
        );

        TableColumn<Ingreso, String> fecha =
                new TableColumn<>("Fecha");

        fecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaIngreso")
        );

        TableColumn<Ingreso, String> organismo =
                new TableColumn<>("Organismo");

        organismo.setCellValueFactory(
                new PropertyValueFactory<>("organismoProcedencia")
        );

        TableColumn<Ingreso, String> procedencia =
                new TableColumn<>("Procedencia");

        procedencia.setCellValueFactory(
                new PropertyValueFactory<>("procedencia")
        );

        TableColumn<Ingreso, String> motivo =
                new TableColumn<>("Motivo");

        motivo.setCellValueFactory(
                new PropertyValueFactory<>("motivoIngreso")
        );

        tablaIngresos.getColumns().addAll(
                acta,
                fecha,
                organismo,
                procedencia,
                motivo
        );

        tablaIngresos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    // =========================================================
    // CARGAR DATOS
    // =========================================================

    private void cargarIngresos() {

        tablaIngresos.setItems(
                FXCollections.observableArrayList(
                        ingresoDAO.listar()
                )
        );
    }

    private void cargarDetalles(int idIngreso) {

        List<DetalleIngreso> detalles =
                detalleDAO.listarPorIngreso(
                        idIngreso
                );

        identificacionesGuardadas.clear();

        for (DetalleIngreso detalle : detalles) {
            cargarIdentificacionGuardada(
                    detalle.getIdDetalle()
            );
        }

        tablaDetalles.setItems(
                FXCollections.observableArrayList(
                        detalles
                )
        );
    }

    private void cargarEspecies() {

        List<Especie> especies =
                especieDAO.listar();

        comboEspecie.setItems(
                FXCollections.observableArrayList(
                        especies
                )
        );

        mapaEspecies.clear();

        for (Especie especie : especies) {

            mapaEspecies.put(
                    especie.getIdEspecie(),
                    especie
            );
        }
    }

    private void cargarAnimalesPorEspecie(
            Especie especie
    ) {

        comboAnimal.getItems().clear();
        comboAnimal.setValue(null);

        if (especie == null) {
            return;
        }

        List<Animal> animales =
                new ArrayList<>();

        for (Animal animal :
                animalDAO.listar()) {

            if (
                    animal.getIdEspecie()
                            == especie.getIdEspecie()
            ) {

                animales.add(animal);
            }
        }

        comboAnimal.setItems(
                FXCollections.observableArrayList(
                        animales
                )
        );
    }

    private void cargarDatosParaMostrar() {

        mapaEspecies.clear();
        mapaAnimales.clear();

        for (Especie especie :
                especieDAO.listar()) {

            mapaEspecies.put(
                    especie.getIdEspecie(),
                    especie
            );
        }

        for (Animal animal :
                animalDAO.listar()) {

            mapaAnimales.put(
                    animal.getIdAnimal(),
                    animal
            );
        }

        comboEspecie.setItems(
                FXCollections.observableArrayList(
                        mapaEspecies.values()
                )
        );
    }

    private Animal buscarAnimalCreado(
            int idEspecie,
            String nombre
    ) {

        for (Animal animal :
                animalDAO.listar()) {

            if (
                    animal.getIdEspecie()
                            == idEspecie
                            &&
                    animal.getNombreVulgar()
                            .equalsIgnoreCase(nombre)
            ) {

                return animal;
            }
        }

        return null;
    }

    // =========================================================
    // IDENTIFICACIONES
    // =========================================================

    private void guardarIdentificacionDelDetalle(
            int idIngreso,
            DetalleIngreso detalle
    ) {

        String[] identificacion =
                identificacionesPendientes.get(
                        detalle
                );

        if (identificacion == null) {
            return;
        }

        String sqlIdDetalle = """
                SELECT id_detalle
                FROM detalle_ingreso
                WHERE id_ingreso = ?
                  AND id_animal = ?
                ORDER BY id_detalle DESC
                LIMIT 1
                """;

        String sqlIdentificacion = """
                INSERT INTO identificaciones (
                    id_detalle,
                    tipo_identificacion,
                    numero_identificacion,
                    fecha_identificacion,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion =
                        ConexionSQLite.conectar();
                PreparedStatement buscar =
                        conexion.prepareStatement(
                                sqlIdDetalle
                        )
        ) {

            buscar.setInt(
                    1,
                    idIngreso
            );

            buscar.setInt(
                    2,
                    detalle.getIdAnimal()
            );

            int idDetalle = -1;

            try (
                    ResultSet resultado =
                            buscar.executeQuery()
            ) {

                if (resultado.next()) {
                    idDetalle =
                            resultado.getInt(
                                    "id_detalle"
                            );
                }
            }

            if (idDetalle == -1) {
                return;
            }

            try (
                    PreparedStatement insertar =
                            conexion.prepareStatement(
                                    sqlIdentificacion
                            )
            ) {

                insertar.setInt(
                        1,
                        idDetalle
                );

                insertar.setString(
                        2,
                        identificacion[0]
                );

                insertar.setString(
                        3,
                        identificacion[1]
                );

                insertar.setString(
                        4,
                        null
                );

                insertar.setString(
                        5,
                        null
                );

                insertar.executeUpdate();
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al guardar identificación."
            );

            System.out.println(
                    e.getMessage()
            );
        }
    }

    private void cargarIdentificacionGuardada(
            int idDetalle
    ) {

        String sql = """
                SELECT
                    tipo_identificacion,
                    numero_identificacion
                FROM identificaciones
                WHERE id_detalle = ?
                ORDER BY id_identificacion DESC
                LIMIT 1
                """;

        try (
                Connection conexion =
                        ConexionSQLite.conectar();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idDetalle
            );

            try (
                    ResultSet resultado =
                            sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    identificacionesGuardadas.put(
                            idDetalle,
                            new String[] {
                                    resultado.getString(
                                            "tipo_identificacion"
                                    ),
                                    resultado.getString(
                                            "numero_identificacion"
                                    )
                            }
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al cargar identificación."
            );

            System.out.println(
                    e.getMessage()
            );
        }
    }

    private String obtenerTipoIdentificacion(
            DetalleIngreso detalle
    ) {

        String[] pendiente =
                identificacionesPendientes.get(
                        detalle
                );

        if (pendiente != null) {
            return pendiente[0];
        }

        String[] guardada =
                identificacionesGuardadas.get(
                        detalle.getIdDetalle()
                );

        return guardada == null
                ? "Sin identificación"
                : guardada[0];
    }

    private String obtenerNumeroIdentificacion(
            DetalleIngreso detalle
    ) {

        String[] pendiente =
                identificacionesPendientes.get(
                        detalle
                );

        if (pendiente != null) {
            return pendiente[1];
        }

        String[] guardada =
                identificacionesGuardadas.get(
                        detalle.getIdDetalle()
                );

        return guardada == null
                ? "-"
                : guardada[1];
    }

    // =========================================================
    // CARGAR INGRESO
    // =========================================================

    private void cargarDatosIngreso(
            Ingreso ingreso
    ) {

        txtNumeroActa.setText(
                ingreso.getNumeroActa()
        );

        cargarFechaEnDatePicker(
                ingreso.getFechaIngreso()
        );

        txtOrganismoProcedencia.setText(
                ingreso.getOrganismoProcedencia()
        );

        txtResponsableEntrega.setText(
                ingreso.getResponsableEntrega()
        );

        txtProcedencia.setText(
                ingreso.getProcedencia()
        );

        txtMotivoIngreso.setText(
                ingreso.getMotivoIngreso()
        );

        txtDocumentacion.setText(
                ingreso.getDocumentacion()
        );

        txtObservaciones.setText(
                ingreso.getObservaciones()
        );
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    private void guardarIngreso() {

        if (!datosIngresoValidos()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Complete los campos obligatorios."
            );

            return;
        }

        if (detallesPendientes.isEmpty()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Debe agregar al menos un animal al acta."
            );

            return;
        }

        Ingreso ingreso =
                crearIngresoDesdeFormulario();

        ingresoDAO.agregar(ingreso);

        List<Ingreso> ingresos =
                ingresoDAO.listar();

        if (!ingresos.isEmpty()) {

            Ingreso ultimo =
                    ingresos.get(0);

            for (DetalleIngreso detalle :
                    detallesPendientes) {

                detalle.setIdIngreso(
                        ultimo.getIdIngreso()
                );

                detalleDAO.agregar(
                        detalle
                );

                guardarIdentificacionDelDetalle(
                        ultimo.getIdIngreso(),
                        detalle
                );
            }
        }

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Ingreso guardado correctamente."
        );

        limpiarTodo();
        cargarIngresos();
    }

    // =========================================================
    // MODIFICAR
    // =========================================================

    private void modificarIngreso() {

        Ingreso seleccionado =
                tablaIngresos
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione un ingreso para modificar."
            );

            return;
        }

        if (!datosIngresoValidos()) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Complete los campos obligatorios."
            );

            return;
        }

        actualizarIngresoDesdeFormulario(
                seleccionado
        );

        ingresoDAO.modificar(
                seleccionado
        );

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Ingreso modificado correctamente."
        );

        limpiarTodo();
        cargarIngresos();
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    private void eliminarIngreso() {

        Ingreso seleccionado =
                tablaIngresos
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione un ingreso para eliminar."
            );

            return;
        }

        ingresoDAO.eliminar(
                seleccionado.getIdIngreso()
        );

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Ingreso eliminado correctamente."
        );

        limpiarTodo();
        cargarIngresos();
    }

    // =========================================================
    // OBJETO INGRESO
    // =========================================================

    private Ingreso crearIngresoDesdeFormulario() {

        Ingreso ingreso = new Ingreso();

        ingreso.setNumeroActa(
                txtNumeroActa.getText().trim()
        );

        ingreso.setFechaIngreso(
                obtenerFechaComoTexto()
        );

        ingreso.setOrganismoProcedencia(
                txtOrganismoProcedencia
                        .getText()
                        .trim()
        );

        ingreso.setResponsableEntrega(
                txtResponsableEntrega
                        .getText()
                        .trim()
        );

        ingreso.setProcedencia(
                txtProcedencia
                        .getText()
                        .trim()
        );

        ingreso.setMotivoIngreso(
                txtMotivoIngreso
                        .getText()
                        .trim()
        );

        ingreso.setDocumentacion(
                txtDocumentacion
                        .getText()
                        .trim()
        );

        ingreso.setObservaciones(
                txtObservaciones
                        .getText()
                        .trim()
        );

        return ingreso;
    }

    private void actualizarIngresoDesdeFormulario(
            Ingreso ingreso
    ) {

        ingreso.setNumeroActa(
                txtNumeroActa.getText().trim()
        );

        ingreso.setFechaIngreso(
                obtenerFechaComoTexto()
        );

        ingreso.setOrganismoProcedencia(
                txtOrganismoProcedencia
                        .getText()
                        .trim()
        );

        ingreso.setResponsableEntrega(
                txtResponsableEntrega
                        .getText()
                        .trim()
        );

        ingreso.setProcedencia(
                txtProcedencia
                        .getText()
                        .trim()
        );

        ingreso.setMotivoIngreso(
                txtMotivoIngreso
                        .getText()
                        .trim()
        );

        ingreso.setDocumentacion(
                txtDocumentacion
                        .getText()
                        .trim()
        );

        ingreso.setObservaciones(
                txtObservaciones
                        .getText()
                        .trim()
        );
    }

    // =========================================================
    // VALIDACIÓN
    // =========================================================

    private boolean datosIngresoValidos() {

        return !txtNumeroActa.getText().isBlank()
                && datePickerFechaIngreso.getValue() != null
                && !txtOrganismoProcedencia.getText().isBlank()
                && !txtProcedencia.getText().isBlank()
                && !txtMotivoIngreso.getText().isBlank();
    }

    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiarDetalle() {

        comboEspecie.setValue(null);
        comboAnimal.getItems().clear();
        comboAnimal.setValue(null);

        txtCantidad.clear();
        txtSexo.clear();
        txtEdad.clear();
        txtPeso.clear();

        txtEstadoIngreso.setText(
                "CUARENTENA"
        );

        comboTipoIdentificacion.setValue(
                "Sin identificación"
        );
        txtNumeroIdentificacion.clear();

        txtObservacionesDetalle.clear();
    }

    private void limpiarTodo() {

        txtNumeroActa.clear();
        datePickerFechaIngreso.setValue(null);
        txtOrganismoProcedencia.clear();
        txtResponsableEntrega.clear();
        txtProcedencia.clear();
        txtMotivoIngreso.clear();
        txtDocumentacion.clear();
        txtObservaciones.clear();

        detallesPendientes.clear();
        identificacionesPendientes.clear();
        identificacionesGuardadas.clear();

        tablaDetalles.getItems().clear();

        ocultarFormularioCargaAnimal();

        tablaIngresos
                .getSelectionModel()
                .clearSelection();
    }

    // =========================================================
    // CAMPOS
    // =========================================================

    private void configurarCamposIngreso() {

        txtNumeroActa.setPromptText(
                "Ej: AB547"
        );

        datePickerFechaIngreso.setPromptText(
                "dd/MM/yyyy"
        );

        datePickerFechaIngreso.setConverter(
                new javafx.util.StringConverter<LocalDate>() {

                    @Override
                    public String toString(
                            LocalDate fecha
                    ) {
                        return fecha == null
                                ? ""
                                : FORMATO_FECHA.format(fecha);
                    }

                    @Override
                    public LocalDate fromString(
                            String texto
                    ) {

                        if (
                                texto == null
                                        || texto.isBlank()
                        ) {
                            return null;
                        }

                        try {

                            return LocalDate.parse(
                                    texto.trim(),
                                    FORMATO_FECHA
                            );

                        } catch (
                                DateTimeParseException e
                        ) {

                            return null;
                        }
                    }
                }
        );

        txtOrganismoProcedencia.setPromptText(
                "Ej: Policía Ambiental"
        );

        txtResponsableEntrega.setPromptText(
                "Responsable"
        );

        txtProcedencia.setPromptText(
                "Lugar de procedencia"
        );

        txtMotivoIngreso.setPromptText(
                "Ej: Rescate"
        );

        txtDocumentacion.setPromptText(
                "Documentación..."
        );

        txtObservaciones.setPromptText(
                "Observaciones..."
        );

        txtDocumentacion.setWrapText(true);
        txtObservaciones.setWrapText(true);

        configurarValidacionObligatoria(
                txtNumeroActa,
                datePickerFechaIngreso,
                txtOrganismoProcedencia,
                txtProcedencia,
                txtMotivoIngreso
        );
    }

    private void configurarCamposAnimal() {

        comboEspecie.setPromptText(
                "Seleccione una especie"
        );

        comboAnimal.setPromptText(
                "Seleccione un animal"
        );

        txtCantidad.setPromptText(
                "Ej: 2"
        );

        txtSexo.setPromptText(
                "Macho / Hembra / Mixto"
        );

        txtEdad.setPromptText(
                "Ej: Adulto"
        );

        txtPeso.setPromptText(
                "Ej: 2.5"
        );

        txtEstadoIngreso.setText(
                "CUARENTENA"
        );

        txtEstadoIngreso.setEditable(false);

        txtObservacionesDetalle.setPromptText(
                "Observaciones..."
        );

        comboEspecie.setOnAction(
                e -> cargarAnimalesPorEspecie(
                        comboEspecie.getValue()
                )
        );
    }

    // =========================================================
    // FORMULARIO
    // =========================================================

    private GridPane crearFormulario() {

        GridPane formulario = new GridPane();

        formulario.setHgap(12);
        formulario.setVgap(6);

        formulario.setMaxWidth(
                Double.MAX_VALUE
        );

        for (int i = 0; i < 4; i++) {

            javafx.scene.layout.ColumnConstraints columna =
                    new javafx.scene.layout.ColumnConstraints();

            columna.setHgrow(
                    Priority.ALWAYS
            );

            columna.setPercentWidth(25);

            formulario.getColumnConstraints()
                    .add(columna);
        }

        return formulario;
    }

    private void agregarCampo(
            GridPane grid,
            String texto,
            Node campo,
            boolean obligatorio,
            int columna,
            int fila
    ) {

        grid.add(
                crearLabelCampo(
                        texto,
                        obligatorio
                ),
                columna,
                fila
        );

        grid.add(
                campo,
                columna,
                fila + 1
        );

        GridPane.setHgrow(
                campo,
                Priority.ALWAYS
        );

        if (campo instanceof TextField tf) {
            tf.setMaxWidth(
                    Double.MAX_VALUE
            );
        }

        if (campo instanceof DatePicker dp) {
            dp.setMaxWidth(
                    Double.MAX_VALUE
            );
        }

        if (campo instanceof ComboBox<?> cb) {
            cb.setMaxWidth(
                    Double.MAX_VALUE
            );
        }

        if (campo instanceof TextArea ta) {
            ta.setMaxWidth(
                    Double.MAX_VALUE
            );
        }
    }

    // =========================================================
    // TARJETAS
    // =========================================================

    private VBox crearTarjeta(
            Node... nodos
    ) {

        VBox tarjeta =
                new VBox(
                        12,
                        nodos
                );

        tarjeta.setPadding(
                new Insets(17)
        );

        tarjeta.setMaxWidth(
                Double.MAX_VALUE
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #C9DCCF;" +
                "-fx-border-radius: 14;"
        );

        return tarjeta;
    }

    private VBox crearTituloConAyuda(
            String tituloTexto,
            String ayudaTexto
    ) {

        Label titulo =
                crearTituloTarjeta(
                        tituloTexto
                );

        Label ayuda =
                new Label(
                        ayudaTexto
                );

        ayuda.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #708078;"
        );

        return new VBox(
                3,
                titulo,
                ayuda
        );
    }

    private Label crearTituloTarjeta(
            String texto
    ) {

        Label titulo =
                new Label(texto);

        titulo.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #20513D;"
        );

        return titulo;
    }

    private Label crearLabelCampo(
            String texto
    ) {
        return crearLabelCampo(
                texto,
                false
        );
    }

    private Label crearLabelCampo(
            String texto,
            boolean obligatorio
    ) {

        Label label =
                new Label(
                        obligatorio
                                ? texto + " *"
                                : texto
                );

        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                (
                        obligatorio
                                ? "#B64040;"
                                : "#42564B;"
                )
        );

        return label;
    }

    // =========================================================
    // BOTONES
    // =========================================================

    private void aplicarEstiloPrincipal(
            Button boton
    ) {

        boton.setPrefHeight(36);

        boton.setStyle(
                "-fx-background-color: #286247;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 15 0 15;"
        );
    }

    private void aplicarEstiloSecundario(
            Button boton
    ) {

        boton.setPrefHeight(36);

        boton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #286247;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #B9CEC0;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 15 0 15;"
        );
    }

    private void aplicarEstiloEliminar(
            Button boton
    ) {

        boton.setPrefHeight(36);

        boton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #A04444;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #DABABA;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 15 0 15;"
        );
    }

    // =========================================================
    // VALIDACIÓN VISUAL
    // =========================================================

    private void configurarValidacionObligatoria(
            Node... campos
    ) {

        for (Node campo : campos) {

            if (campo instanceof TextField tf) {

                tf.textProperty().addListener(
                        (obs, viejo, nuevo) ->
                                actualizarEstadoCampo(
                                        tf,
                                        nuevo == null
                                                || nuevo.isBlank()
                                )
                );

                actualizarEstadoCampo(
                        tf,
                        tf.getText() == null
                                || tf.getText().isBlank()
                );

            } else if (campo instanceof DatePicker dp) {

                dp.valueProperty().addListener(
                        (obs, viejo, nuevo) ->
                                actualizarEstadoCampo(
                                        dp,
                                        nuevo == null
                                )
                );

                actualizarEstadoCampo(
                        dp,
                        dp.getValue() == null
                );
            }
        }
    }

    private void actualizarEstadoCampo(
            Node campo,
            boolean vacio
    ) {

        campo.setStyle(
                (vacio
                        ? "-fx-border-color: #C94A4A; -fx-border-width: 1.5;"
                        : "-fx-border-color: #BFD2C6; -fx-border-width: 1;")
                        +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-background-color: white;"
        );
    }

    // =========================================================
    // FECHAS
    // =========================================================

    private String obtenerFechaComoTexto() {

        LocalDate fecha =
                datePickerFechaIngreso.getValue();

        return fecha == null
                ? ""
                : FORMATO_FECHA.format(fecha);
    }

    private void cargarFechaEnDatePicker(
            String texto
    ) {

        if (
                texto == null
                        || texto.isBlank()
        ) {

            datePickerFechaIngreso
                    .setValue(null);

            return;
        }

        try {

            datePickerFechaIngreso.setValue(
                    LocalDate.parse(
                            texto.trim(),
                            FORMATO_FECHA
                    )
            );

        } catch (
                DateTimeParseException e
        ) {

            datePickerFechaIngreso
                    .setValue(null);
        }
    }

    // =========================================================
    // MENSAJES
    // =========================================================

    private void mostrarMensaje(
            Alert.AlertType tipo,
            String mensaje
    ) {

        Alert alerta =
                new Alert(tipo);

        alerta.setTitle(
                "Tatú Carreta"
        );

        alerta.setHeaderText(null);

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    // =========================================================
    // MOSTRAR - COMPATIBILIDAD
    // =========================================================

    public void mostrar() {

        if (ventanaPrincipal != null) {
            return;
        }

        ScrollPane contenido =
                crearContenido();

        javafx.scene.Scene escena =
                new javafx.scene.Scene(
                        contenido,
                        1200,
                        850
                );

        javafx.stage.Stage ventana =
                new javafx.stage.Stage();

        ventana.setTitle(
                "Tatú Carreta - Gestión de Ingresos"
        );

        ventana.setScene(escena);
        ventana.setMaximized(true);
        ventana.show();
    }
}
