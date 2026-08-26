package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class VentanaIngresos {

    private final IngresoDAO ingresoDAO =
            new IngresoDAO();

    private final DetalleIngresoDAO detalleDAO =
            new DetalleIngresoDAO();

    private final EspecieDAO especieDAO =
            new EspecieDAO();

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final TableView<Ingreso> tablaIngresos =
            new TableView<>();

    private final TableView<DetalleIngreso> tablaDetalles =
            new TableView<>();

    // =========================================
    // DATOS DEL INGRESO
    // =========================================

    private final TextField txtNumeroActa =
            new TextField();

    private final TextField txtFechaIngreso =
            new TextField();

    private final TextField txtOrganismoProcedencia =
            new TextField();

    private final TextField txtResponsableEntrega =
            new TextField();

    private final TextField txtProcedencia =
            new TextField();

    private final TextField txtMotivoIngreso =
            new TextField();

    private final TextArea txtDocumentacion =
            new TextArea();

    private final TextArea txtObservaciones =
            new TextArea();

    // =========================================
    // DATOS DEL ANIMAL
    // =========================================

    private final ComboBox<Especie> comboEspecie =
            new ComboBox<>();

    private final ComboBox<Animal> comboAnimal =
            new ComboBox<>();

    private final TextField txtCantidad =
            new TextField();

    private final TextField txtSexo =
            new TextField();

    private final TextField txtEdad =
            new TextField();

    private final TextField txtPeso =
            new TextField();

    private final TextField txtEstadoIngreso =
            new TextField();

    private final TextArea txtObservacionesDetalle =
            new TextArea();

    private final List<DetalleIngreso>
            detallesPendientes =
            new ArrayList<>();


    public void mostrar() {

        Stage ventana = new Stage();

        // =========================================
        // ENCABEZADO
        // =========================================

        Label breadcrumb =
                new Label(
                        "Inicio / Gestión de Ingresos"
                );

        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A8580;"
        );

        Label titulo =
                new Label("Gestión de Ingresos");

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        Label subtitulo =
                new Label(
                        "Registre y administre los ingresos de animales a la reserva"
                );

        subtitulo.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6B756F;"
        );

        VBox encabezado =
                new VBox(
                        6,
                        breadcrumb,
                        titulo,
                        subtitulo
                );


        // =========================================
        // CAMPOS DEL INGRESO
        // =========================================

        configurarCampos();

        txtNumeroActa.setPromptText(
                "Ej: 001/2026"
        );

        txtFechaIngreso.setPromptText(
                "Ej: 25/08/2026"
        );

        txtOrganismoProcedencia.setPromptText(
                "Ej: Policía Ambiental"
        );

        txtResponsableEntrega.setPromptText(
                "Responsable de la entrega"
        );

        txtProcedencia.setPromptText(
                "Lugar de procedencia"
        );

        txtMotivoIngreso.setPromptText(
                "Motivo del ingreso"
        );

        txtDocumentacion.setPromptText(
                "Documentación presentada..."
        );

        txtObservaciones.setPromptText(
                "Observaciones generales..."
        );


        // =========================================
        // FORMULARIO INGRESO
        // =========================================

        GridPane formularioIngreso =
                crearFormulario();

        formularioIngreso.add(
                crearLabelCampo("N° de Acta"),
                0,
                0
        );

        formularioIngreso.add(
                crearLabelCampo("Fecha de Ingreso"),
                1,
                0
        );

        formularioIngreso.add(
                txtNumeroActa,
                0,
                1
        );

        formularioIngreso.add(
                txtFechaIngreso,
                1,
                1
        );


        formularioIngreso.add(
                crearLabelCampo(
                        "Organismo de Procedencia"
                ),
                0,
                2
        );

        formularioIngreso.add(
                crearLabelCampo(
                        "Responsable de Entrega"
                ),
                1,
                2
        );

        formularioIngreso.add(
                txtOrganismoProcedencia,
                0,
                3
        );

        formularioIngreso.add(
                txtResponsableEntrega,
                1,
                3
        );


        formularioIngreso.add(
                crearLabelCampo("Procedencia"),
                0,
                4
        );

        formularioIngreso.add(
                crearLabelCampo("Motivo de Ingreso"),
                1,
                4
        );

        formularioIngreso.add(
                txtProcedencia,
                0,
                5
        );

        formularioIngreso.add(
                txtMotivoIngreso,
                1,
                5
        );


        VBox documentacionBox =
                new VBox(
                        5,
                        crearLabelCampo("Documentación"),
                        txtDocumentacion
                );

        VBox observacionesBox =
                new VBox(
                        5,
                        crearLabelCampo(
                                "Observaciones"
                        ),
                        txtObservaciones
                );

        GridPane.setColumnSpan(
                documentacionBox,
                2
        );

        GridPane.setColumnSpan(
                observacionesBox,
                2
        );


        // =========================================
        // TARJETA INGRESO
        // =========================================

        Label tituloIngreso =
                crearTituloTarjeta(
                        "Datos del Ingreso"
                );

        VBox tarjetaIngreso =
                crearTarjeta(
                        tituloIngreso,
                        formularioIngreso,
                        documentacionBox,
                        observacionesBox
                );


        // =========================================
        // DATOS DEL ANIMAL
        // =========================================

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

        txtEstadoIngreso.setPromptText(
                "Estado al ingresar"
        );

        txtObservacionesDetalle.setPromptText(
                "Observaciones del animal..."
        );


        cargarEspecies();


        comboEspecie.setOnAction(e -> {

            Especie especie =
                    comboEspecie.getValue();

            cargarAnimalesPorEspecie(
                    especie
            );
        });


        // =========================================
        // BOTÓN NUEVO ANIMAL
        // =========================================

        Button btnNuevoAnimal =
                new Button("NUEVO ANIMAL");

        aplicarEstiloSecundario(
                btnNuevoAnimal
        );

        btnNuevoAnimal.setOnAction(
                e -> mostrarVentanaNuevoAnimal()
        );


        HBox filaAnimal =
                new HBox(
                        10,
                        comboAnimal,
                        btnNuevoAnimal
                );

        HBox.setHgrow(
                comboAnimal,
                Priority.ALWAYS
        );


        // =========================================
        // FORMULARIO ANIMAL
        // =========================================

        GridPane formularioAnimal =
                crearFormulario();

        formularioAnimal.add(
                crearLabelCampo("Especie"),
                0,
                0
        );

        formularioAnimal.add(
                crearLabelCampo("Animal"),
                1,
                0
        );

        formularioAnimal.add(
                comboEspecie,
                0,
                1
        );

        formularioAnimal.add(
                filaAnimal,
                1,
                1
        );


        formularioAnimal.add(
                crearLabelCampo("Cantidad"),
                0,
                2
        );

        formularioAnimal.add(
                crearLabelCampo("Sexo"),
                1,
                2
        );

        formularioAnimal.add(
                txtCantidad,
                0,
                3
        );

        formularioAnimal.add(
                txtSexo,
                1,
                3
        );


        formularioAnimal.add(
                crearLabelCampo("Edad"),
                0,
                4
        );

        formularioAnimal.add(
                crearLabelCampo("Peso"),
                1,
                4
        );

        formularioAnimal.add(
                txtEdad,
                0,
                5
        );

        formularioAnimal.add(
                txtPeso,
                1,
                5
        );


        formularioAnimal.add(
                crearLabelCampo(
                        "Estado al Ingreso"
                ),
                0,
                6
        );

        formularioAnimal.add(
                txtEstadoIngreso,
                0,
                7
        );

        VBox observacionesDetalleBox =
                new VBox(
                        5,
                        crearLabelCampo(
                                "Observaciones del Animal"
                        ),
                        txtObservacionesDetalle
                );


        Button btnAgregarAnimal =
                new Button(
                        "AGREGAR ANIMAL AL INGRESO"
                );

        aplicarEstiloPrincipal(
                btnAgregarAnimal
        );

        btnAgregarAnimal.setOnAction(
                e -> agregarDetallePendiente()
        );


        Label tituloAnimal =
                crearTituloTarjeta(
                        "Datos del Animal"
                );

        VBox tarjetaAnimal =
                crearTarjeta(
                        tituloAnimal,
                        formularioAnimal,
                        observacionesDetalleBox,
                        btnAgregarAnimal
                );


        // =========================================
        // TABLA DETALLES
        // =========================================

        configurarTablaDetalles();

        tablaDetalles.setPrefHeight(220);

        Label tituloDetalles =
                crearTituloTarjeta(
                        "Animales Agregados al Ingreso"
                );

        VBox tarjetaDetalles =
                crearTarjeta(
                        tituloDetalles,
                        tablaDetalles
                );


        // =========================================
        // BOTONES PRINCIPALES
        // =========================================

        Button btnLimpiar =
                new Button("LIMPIAR");

        Button btnEliminar =
                new Button("ELIMINAR");

        Button btnModificar =
                new Button("MODIFICAR");

        Button btnGuardar =
                new Button("GUARDAR INGRESO");

        Button btnVolver =
                new Button("VOLVER");


        aplicarEstiloSecundario(
                btnLimpiar
        );

        aplicarEstiloEliminar(
                btnEliminar
        );

        aplicarEstiloSecundario(
                btnModificar
        );

        aplicarEstiloPrincipal(
                btnGuardar
        );

        aplicarEstiloSecundario(
                btnVolver
        );


        btnLimpiar.setOnAction(
                e -> limpiarTodo()
        );


        // =========================================
        // CONFIGURAR TABLA INGRESOS
        // =========================================

        configurarTablaIngresos();

        cargarIngresos();


        tablaIngresos.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                anterior,
                                seleccionado
                        ) -> {

                            if (seleccionado != null) {

                                cargarDatosIngreso(
                                        seleccionado
                                );

                                cargarDetalles(
                                        seleccionado
                                                .getIdIngreso()
                                );
                            }
                        }
                );


        // =========================================
        // GUARDAR
        // =========================================

        btnGuardar.setOnAction(e -> {

            if (!datosIngresoValidos()) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Complete los datos obligatorios del ingreso."
                );

                return;
            }


            if (detallesPendientes.isEmpty()) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Debe agregar al menos un animal."
                );

                return;
            }


            Ingreso ingreso =
                    crearIngresoDesdeFormulario();

            ingresoDAO.agregar(
                    ingreso
            );


            List<Ingreso> ingresos =
                    ingresoDAO.listar();


            if (!ingresos.isEmpty()) {

                Ingreso ultimo =
                        ingresos.get(0);


                for (
                        DetalleIngreso detalle
                        : detallesPendientes
                ) {

                    detalle.setIdIngreso(
                            ultimo.getIdIngreso()
                    );

                    detalleDAO.agregar(
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
        });


        // =========================================
        // MODIFICAR
        // =========================================

        btnModificar.setOnAction(e -> {

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
        });


        // =========================================
        // ELIMINAR
        // =========================================

        btnEliminar.setOnAction(e -> {

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
        });


        // =========================================
        // BOTONES
        // =========================================

        HBox botones =
                new HBox(
                        10,
                        btnLimpiar,
                        btnEliminar,
                        btnModificar,
                        btnGuardar
                );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );


        // =========================================
        // TARJETA INGRESOS REGISTRADOS
        // =========================================

        Label tituloRegistrados =
                crearTituloTarjeta(
                        "Ingresos Registrados"
                );

        VBox tarjetaIngresos =
                crearTarjeta(
                        tituloRegistrados,
                        tablaIngresos
                );

        tablaIngresos.setPrefHeight(280);


        // =========================================
        // VOLVER
        // =========================================

        btnVolver.setOnAction(
                e -> ventana.close()
        );

        HBox contenedorVolver =
                new HBox(btnVolver);

        contenedorVolver.setAlignment(
                Pos.CENTER
        );


        // =========================================
        // CONTENIDO
        // =========================================

        VBox contenido =
                new VBox(
                        25,
                        encabezado,
                        tarjetaIngreso,
                        tarjetaAnimal,
                        tarjetaDetalles,
                        botones,
                        tarjetaIngresos,
                        contenedorVolver
                );

        contenido.setAlignment(
                Pos.TOP_CENTER
        );

        contenido.setPadding(
                new Insets(
                        25,
                        35,
                        35,
                        35
                )
        );

        contenido.setStyle(
                "-fx-background-color: #F4F1E8;"
        );


        // =========================================
        // SCROLL
        // =========================================

        ScrollPane scroll =
                new ScrollPane(contenido);

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background: #F4F1E8;" +
                "-fx-background-color: #F4F1E8;"
        );


        // =========================================
        // ESCENA
        // =========================================

        Scene escena =
                new Scene(
                        scroll,
                        1100,
                        750
                );

        ventana.setTitle(
                "Tatú Carreta - Gestión de Ingresos"
        );

        ventana.setMinWidth(900);

        ventana.setMinHeight(650);

        ventana.setScene(
                escena
        );

        ventana.setMaximized(true);

        ventana.show();
    }


    // =========================================
    // CONFIGURAR CAMPOS
    // =========================================

    private void configurarCampos() {

        String estiloCampo =
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #D1D8D2;" +
                "-fx-padding: 8;";

        txtNumeroActa.setStyle(
                estiloCampo
        );

        txtFechaIngreso.setStyle(
                estiloCampo
        );

        txtOrganismoProcedencia.setStyle(
                estiloCampo
        );

        txtResponsableEntrega.setStyle(
                estiloCampo
        );

        txtProcedencia.setStyle(
                estiloCampo
        );

        txtMotivoIngreso.setStyle(
                estiloCampo
        );

        txtDocumentacion.setStyle(
                estiloCampo
        );

        txtObservaciones.setStyle(
                estiloCampo
        );

        comboEspecie.setStyle(
                estiloCampo
        );

        comboAnimal.setStyle(
                estiloCampo
        );

        txtCantidad.setStyle(
                estiloCampo
        );

        txtSexo.setStyle(
                estiloCampo
        );

        txtEdad.setStyle(
                estiloCampo
        );

        txtPeso.setStyle(
                estiloCampo
        );

        txtEstadoIngreso.setStyle(
                estiloCampo
        );

        txtObservacionesDetalle.setStyle(
                estiloCampo
        );


        txtNumeroActa.setPrefHeight(38);
        txtFechaIngreso.setPrefHeight(38);
        txtOrganismoProcedencia.setPrefHeight(38);
        txtResponsableEntrega.setPrefHeight(38);
        txtProcedencia.setPrefHeight(38);
        txtMotivoIngreso.setPrefHeight(38);

        comboEspecie.setPrefHeight(38);
        comboAnimal.setPrefHeight(38);

        txtCantidad.setPrefHeight(38);
        txtSexo.setPrefHeight(38);
        txtEdad.setPrefHeight(38);
        txtPeso.setPrefHeight(38);
        txtEstadoIngreso.setPrefHeight(38);

        txtDocumentacion.setPrefRowCount(3);
        txtDocumentacion.setWrapText(true);

        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setWrapText(true);

        txtObservacionesDetalle.setPrefRowCount(3);
        txtObservacionesDetalle.setWrapText(true);
    }


    // =========================================
    // CREAR FORMULARIO
    // =========================================

    private GridPane crearFormulario() {

        GridPane formulario =
                new GridPane();

        formulario.setHgap(20);

        formulario.setVgap(12);

        return formulario;
    }


    // =========================================
    // LABEL DE CAMPO
    // =========================================

    private Label crearLabelCampo(
            String texto
    ) {

        Label label =
                new Label(texto);

        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #445149;"
        );

        return label;
    }


    // =========================================
    // TÍTULO TARJETA
    // =========================================

    private Label crearTituloTarjeta(
            String texto
    ) {

        Label titulo =
                new Label(texto);

        titulo.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );

        return titulo;
    }


    // =========================================
    // CREAR TARJETA
    // =========================================

    private VBox crearTarjeta(
            javafx.scene.Node... nodos
    ) {

        VBox tarjeta =
                new VBox(
                        18,
                        nodos
                );

        tarjeta.setPadding(
                new Insets(25)
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D8DED9;" +
                "-fx-border-radius: 16;"
        );

        return tarjeta;
    }


    // =========================================
    // ESTILO PRINCIPAL
    // =========================================

    private void aplicarEstiloPrincipal(
            Button boton
    ) {

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: #254D3D;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );
    }


    // =========================================
    // ESTILO SECUNDARIO
    // =========================================

    private void aplicarEstiloSecundario(
            Button boton
    ) {

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #405047;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C9D2CB;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
    }


    // =========================================
    // ESTILO ELIMINAR
    // =========================================

    private void aplicarEstiloEliminar(
            Button boton
    ) {

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #A34A4A;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #E2C5C5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
    }


    // =========================================
    // CONFIGURAR TABLA DETALLES
    // =========================================

    private void configurarTablaDetalles() {

        tablaDetalles.getColumns().clear();


        TableColumn<DetalleIngreso, Integer>
                columnaCantidad =
                new TableColumn<>("Cantidad");

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "cantidad"
                )
        );


        TableColumn<DetalleIngreso, String>
                columnaSexo =
                new TableColumn<>("Sexo");

        columnaSexo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "sexo"
                )
        );


        TableColumn<DetalleIngreso, String>
                columnaEdad =
                new TableColumn<>("Edad");

        columnaEdad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "edad"
                )
        );


        TableColumn<DetalleIngreso, Double>
                columnaPeso =
                new TableColumn<>("Peso");

        columnaPeso.setCellValueFactory(
                new PropertyValueFactory<>(
                        "peso"
                )
        );


        TableColumn<DetalleIngreso, String>
                columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>(
                        "estadoIngreso"
                )
        );


        tablaDetalles.getColumns().addAll(
                columnaCantidad,
                columnaSexo,
                columnaEdad,
                columnaPeso,
                columnaEstado
        );

        tablaDetalles.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }


    // =========================================
    // CONFIGURAR TABLA INGRESOS
    // =========================================

    private void configurarTablaIngresos() {

        tablaIngresos.getColumns().clear();


        TableColumn<Ingreso, Integer>
                columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idIngreso"
                )
        );


        TableColumn<Ingreso, String>
                columnaActa =
                new TableColumn<>("N° Acta");

        columnaActa.setCellValueFactory(
                new PropertyValueFactory<>(
                        "numeroActa"
                )
        );


        TableColumn<Ingreso, String>
                columnaFecha =
                new TableColumn<>("Fecha");

        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fechaIngreso"
                )
        );


        TableColumn<Ingreso, String>
                columnaOrganismo =
                new TableColumn<>("Organismo");

        columnaOrganismo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "organismoProcedencia"
                )
        );


        TableColumn<Ingreso, String>
                columnaProcedencia =
                new TableColumn<>("Procedencia");

        columnaProcedencia.setCellValueFactory(
                new PropertyValueFactory<>(
                        "procedencia"
                )
        );


        TableColumn<Ingreso, String>
                columnaMotivo =
                new TableColumn<>("Motivo");

        columnaMotivo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "motivoIngreso"
                )
        );


        tablaIngresos.getColumns().addAll(
                columnaId,
                columnaActa,
                columnaFecha,
                columnaOrganismo,
                columnaProcedencia,
                columnaMotivo
        );

        tablaIngresos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }


    // =========================================
    // NUEVO ANIMAL
    // =========================================

    private void mostrarVentanaNuevoAnimal() {

        Especie especie =
                comboEspecie.getValue();


        if (especie == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Primero seleccione una especie."
            );

            return;
        }


        Stage ventanaAnimal =
                new Stage();

        Label titulo =
                new Label("Nuevo Animal");

        titulo.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2E4138;"
        );


        Label lblNombre =
                crearLabelCampo(
                        "Nombre vulgar"
                );

        TextField txtNombre =
                new TextField();

        txtNombre.setPromptText(
                "Ej: Carancho"
        );


        Button btnGuardar =
                new Button("GUARDAR");

        aplicarEstiloPrincipal(
                btnGuardar
        );


        btnGuardar.setOnAction(e -> {

            String nombre =
                    txtNombre.getText().trim();


            if (nombre.isBlank()) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Ingrese el nombre del animal."
                );

                return;
            }


            Animal animal =
                    new Animal();

            animal.setIdEspecie(
                    especie.getIdEspecie()
            );

            animal.setNombreVulgar(
                    nombre
            );

            animal.setNombreCientifico(
                    ""
            );

            animal.setCantidadActual(0);

            animal.setOrigen("");

            animal.setEstado("Activo");


            animalDAO.agregar(
                    animal
            );


            Animal creado =
                    buscarAnimalCreado(
                            especie.getIdEspecie(),
                            nombre
                    );


            cargarAnimalesPorEspecie(
                    especie
            );


            if (creado != null) {

                comboAnimal.setValue(
                        creado
                );
            }


            ventanaAnimal.close();
        });


        VBox contenido =
                new VBox(
                        15,
                        titulo,
                        lblNombre,
                        txtNombre,
                        btnGuardar
                );

        contenido.setPadding(
                new Insets(25)
        );

        contenido.setAlignment(
                Pos.CENTER
        );

        contenido.setStyle(
                "-fx-background-color: #F4F1E8;"
        );


        Scene escena =
                new Scene(
                        contenido,
                        400,
                        250
                );

        ventanaAnimal.setTitle(
                "Tatú Carreta - Nuevo Animal"
        );

        ventanaAnimal.setScene(
                escena
        );

        ventanaAnimal.show();
    }


    // =========================================
    // BUSCAR ANIMAL CREADO
    // =========================================

    private Animal buscarAnimalCreado(
            int idEspecie,
            String nombreVulgar
    ) {

        for (
                Animal animal
                : animalDAO.listar()
        ) {

            if (
                    animal.getIdEspecie()
                            == idEspecie
                    &&
                    animal.getNombreVulgar()
                            .equalsIgnoreCase(
                                    nombreVulgar
                            )
            ) {

                return animal;
            }
        }

        return null;
    }


    // =========================================
    // CARGAR ESPECIES
    // =========================================

    private void cargarEspecies() {

        comboEspecie.setItems(
                FXCollections.observableArrayList(
                        especieDAO.listar()
                )
        );
    }


    // =========================================
    // CARGAR ANIMALES POR ESPECIE
    // =========================================

    private void cargarAnimalesPorEspecie(
            Especie especie
    ) {

        comboAnimal.getItems().clear();

        comboAnimal.setValue(null);


        if (especie == null) {

            return;
        }


        ObservableList<Animal>
                animales =
                FXCollections.observableArrayList();


        for (
                Animal animal
                : animalDAO.listar()
        ) {

            if (
                    animal.getIdEspecie()
                            == especie.getIdEspecie()
            ) {

                animales.add(
                        animal
                );
            }
        }


        comboAnimal.setItems(
                animales
        );
    }


    // =========================================
    // AGREGAR DETALLE PENDIENTE
    // =========================================

    private void agregarDetallePendiente() {

        Animal animal =
                comboAnimal.getValue();


        if (animal == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Seleccione o cree un animal."
            );

            return;
        }


        if (
                txtCantidad
                        .getText()
                        .isBlank()
        ) {

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


            double peso = 0;


            if (
                    !txtPeso
                            .getText()
                            .isBlank()
            ) {

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


            detalle.setCantidad(
                    cantidad
            );


            detalle.setSexo(
                    txtSexo.getText()
            );


            detalle.setEdad(
                    txtEdad.getText()
            );


            detalle.setPeso(
                    peso
            );


            detalle.setEstadoIngreso(
                    txtEstadoIngreso.getText()
            );


            detalle.setObservaciones(
                    txtObservacionesDetalle
                            .getText()
            );


            detallesPendientes.add(
                    detalle
            );


            tablaDetalles.setItems(
                    FXCollections.observableArrayList(
                            detallesPendientes
                    )
            );


            limpiarDetalle();


        } catch (
                NumberFormatException e
        ) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Cantidad y peso deben ser números."
            );
        }
    }


    // =========================================
    // CARGAR INGRESOS
    // =========================================

    private void cargarIngresos() {

        tablaIngresos.setItems(
                FXCollections.observableArrayList(
                        ingresoDAO.listar()
                )
        );
    }


    // =========================================
    // CARGAR DETALLES
    // =========================================

    private void cargarDetalles(
            int idIngreso
    ) {

        tablaDetalles.setItems(
                FXCollections.observableArrayList(
                        detalleDAO.listarPorIngreso(
                                idIngreso
                        )
                )
        );
    }


    // =========================================
    // CARGAR DATOS INGRESO
    // =========================================

    private void cargarDatosIngreso(
            Ingreso ingreso
    ) {

        txtNumeroActa.setText(
                ingreso.getNumeroActa()
        );

        txtFechaIngreso.setText(
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


    // =========================================
    // CREAR INGRESO
    // =========================================

    private Ingreso crearIngresoDesdeFormulario() {

        Ingreso ingreso =
                new Ingreso();


        ingreso.setNumeroActa(
                txtNumeroActa.getText()
        );

        ingreso.setFechaIngreso(
                txtFechaIngreso.getText()
        );

        ingreso.setOrganismoProcedencia(
                txtOrganismoProcedencia.getText()
        );

        ingreso.setResponsableEntrega(
                txtResponsableEntrega.getText()
        );

        ingreso.setProcedencia(
                txtProcedencia.getText()
        );

        ingreso.setMotivoIngreso(
                txtMotivoIngreso.getText()
        );

        ingreso.setDocumentacion(
                txtDocumentacion.getText()
        );

        ingreso.setObservaciones(
                txtObservaciones.getText()
        );


        return ingreso;
    }


    // =========================================
    // ACTUALIZAR INGRESO
    // =========================================

    private void actualizarIngresoDesdeFormulario(
            Ingreso ingreso
    ) {

        ingreso.setNumeroActa(
                txtNumeroActa.getText()
        );

        ingreso.setFechaIngreso(
                txtFechaIngreso.getText()
        );

        ingreso.setOrganismoProcedencia(
                txtOrganismoProcedencia.getText()
        );

        ingreso.setResponsableEntrega(
                txtResponsableEntrega.getText()
        );

        ingreso.setProcedencia(
                txtProcedencia.getText()
        );

        ingreso.setMotivoIngreso(
                txtMotivoIngreso.getText()
        );

        ingreso.setDocumentacion(
                txtDocumentacion.getText()
        );

        ingreso.setObservaciones(
                txtObservaciones.getText()
        );
    }


    // =========================================
    // VALIDAR DATOS
    // =========================================

    private boolean datosIngresoValidos() {

        return !txtNumeroActa
                .getText()
                .isBlank()

                && !txtFechaIngreso
                .getText()
                .isBlank()

                && !txtOrganismoProcedencia
                .getText()
                .isBlank()

                && !txtProcedencia
                .getText()
                .isBlank()

                && !txtMotivoIngreso
                .getText()
                .isBlank();
    }


    // =========================================
    // LIMPIAR DETALLE
    // =========================================

    private void limpiarDetalle() {

        comboEspecie.setValue(null);

        comboAnimal.getItems().clear();

        comboAnimal.setValue(null);

        txtCantidad.clear();

        txtSexo.clear();

        txtEdad.clear();

        txtPeso.clear();

        txtEstadoIngreso.clear();

        txtObservacionesDetalle.clear();
    }


    // =========================================
    // LIMPIAR TODO
    // =========================================

    private void limpiarTodo() {

        txtNumeroActa.clear();

        txtFechaIngreso.clear();

        txtOrganismoProcedencia.clear();

        txtResponsableEntrega.clear();

        txtProcedencia.clear();

        txtMotivoIngreso.clear();

        txtDocumentacion.clear();

        txtObservaciones.clear();


        detallesPendientes.clear();

        tablaDetalles.getItems().clear();


        limpiarDetalle();


        tablaIngresos
                .getSelectionModel()
                .clearSelection();
    }


    // =========================================
    // MENSAJES
    // =========================================

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
}