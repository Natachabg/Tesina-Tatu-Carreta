package com.tatucarreta;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class VentanaMovimientos {

    private final MovimientoDAO movimientoDAO =
            new MovimientoDAO();

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final IngresoDAO ingresoDAO =
            new IngresoDAO();

    private TableView<Movimiento> tabla;

    private ComboBox<Animal> comboAnimal;

    private ComboBox<Ingreso> comboIngreso;

    private TextField txtFecha;

    private ComboBox<String> comboTipo;

    private TextField txtCantidad;

    private TextField txtDestino;

    private TextArea txtObservaciones;


    // =========================================================
    // COLORES
    // =========================================================

    private final String VERDE =
            "#23452C";

    private final String VERDE_TITULO =
            "#30463A";

    private final String FONDO =
            "#F2F0E6";

    private final String BORDE =
            "#C7D0C8";

    private final String ROJO =
            "#A94442";


    // =========================================================
    // MOSTRAR VENTANA
    // =========================================================

    public void mostrar(Stage escenario) {

        /*
         * IMPORTANTE:
         *
         * Ya no utilizamos "escenario" como la ventana
         * de Movimientos.
         *
         * Creamos una ventana nueva para que el Panel Principal
         * quede abierto detrás.
         */

        Stage ventana =
                new Stage();


        // =====================================================
        // ENCABEZADO
        // =====================================================

        Label ruta =
                new Label(
                        "Inicio / Gestión de Movimientos"
                );


        ruta.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A827B;"
        );


        Label titulo =
                new Label(
                        "Movimientos de Animales"
                );


        titulo.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                        VERDE_TITULO +
                        ";"
        );


        Label subtitulo =
                new Label(
                        "Registre y consulte los movimientos de los animales de la reserva"
                );


        subtitulo.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #68746B;"
        );


        VBox encabezado =
                new VBox(
                        5,
                        ruta,
                        titulo,
                        subtitulo
                );


        // =====================================================
        // CAMPOS
        // =====================================================

        comboAnimal =
                new ComboBox<>();


        comboAnimal.setMaxWidth(
                Double.MAX_VALUE
        );


        comboAnimal.setPromptText(
                "Seleccione un animal"
        );


        cargarAnimales();


        configurarComboAnimal();


        comboAnimal.setOnAction(
                e ->
                        cargarIngresosPorAnimal()
        );


        // -----------------------------------------------------
        // INGRESO / ACTA
        // -----------------------------------------------------

        comboIngreso =
                new ComboBox<>();


        comboIngreso.setMaxWidth(
                Double.MAX_VALUE
        );


        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );


        configurarComboIngreso();


        // -----------------------------------------------------
        // FECHA
        // -----------------------------------------------------

        txtFecha =
                new TextField();


        txtFecha.setPromptText(
                "Ej: 25/08/2026"
        );


        // -----------------------------------------------------
        // TIPO
        // -----------------------------------------------------

        comboTipo =
                new ComboBox<>();


        comboTipo.setItems(
                FXCollections.observableArrayList(
                        "LIBERACIÓN",
                        "TRASLADO",
                        "FALLECIMIENTO"
                )
        );


        comboTipo.setMaxWidth(
                Double.MAX_VALUE
        );


        comboTipo.setPromptText(
                "Seleccione el tipo"
        );


        // -----------------------------------------------------
        // CANTIDAD
        // -----------------------------------------------------

        txtCantidad =
                new TextField();


        txtCantidad.setPromptText(
                "Ingrese la cantidad"
        );


        // -----------------------------------------------------
        // DESTINO
        // -----------------------------------------------------

        txtDestino =
                new TextField();


        txtDestino.setPromptText(
                "Solo si corresponde"
        );


        // -----------------------------------------------------
        // OBSERVACIONES
        // -----------------------------------------------------

        txtObservaciones =
                new TextArea();


        txtObservaciones.setPromptText(
                "Ingrese observaciones adicionales"
        );


        txtObservaciones.setPrefRowCount(
                2
        );


        txtObservaciones.setWrapText(
                true
        );


        // =====================================================
        // ESTILO CAMPOS
        // =====================================================

        String estiloCampo =
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-font-size: 13px;";


        comboAnimal.setStyle(
                estiloCampo
        );


        comboIngreso.setStyle(
                estiloCampo
        );


        txtFecha.setStyle(
                estiloCampo
        );


        comboTipo.setStyle(
                estiloCampo
        );


        txtCantidad.setStyle(
                estiloCampo
        );


        txtDestino.setStyle(
                estiloCampo
        );


        txtObservaciones.setStyle(
                estiloCampo
        );


        // =====================================================
        // FORMULARIO
        // =====================================================

        GridPane formulario =
                new GridPane();


        formulario.setHgap(
                15
        );


        formulario.setVgap(
                10
        );


        formulario.setAlignment(
                Pos.CENTER
        );


        ColumnConstraints columnaLabel =
                new ColumnConstraints();


        columnaLabel.setPercentWidth(
                28
        );


        ColumnConstraints columnaCampo =
                new ColumnConstraints();


        columnaCampo.setPercentWidth(
                72
        );


        formulario
                .getColumnConstraints()
                .addAll(
                        columnaLabel,
                        columnaCampo
                );


        // =====================================================
        // ANIMAL
        // =====================================================

        formulario.add(
                crearLabelObligatorio(
                        "Animal"
                ),
                0,
                0
        );


        formulario.add(
                comboAnimal,
                1,
                0
        );


        // =====================================================
        // ACTA
        // =====================================================

        formulario.add(
                crearLabelObligatorio(
                        "N° de Acta"
                ),
                0,
                1
        );


        formulario.add(
                comboIngreso,
                1,
                1
        );


        // =====================================================
        // FECHA
        // =====================================================

        formulario.add(
                crearLabelObligatorio(
                        "Fecha"
                ),
                0,
                2
        );


        formulario.add(
                txtFecha,
                1,
                2
        );


        // =====================================================
        // TIPO
        // =====================================================

        formulario.add(
                crearLabelObligatorio(
                        "Tipo de movimiento"
                ),
                0,
                3
        );


        formulario.add(
                comboTipo,
                1,
                3
        );


        // =====================================================
        // CANTIDAD
        // =====================================================

        formulario.add(
                crearLabelObligatorio(
                        "Cantidad"
                ),
                0,
                4
        );


        formulario.add(
                txtCantidad,
                1,
                4
        );


        // =====================================================
        // DESTINO
        // =====================================================

        formulario.add(
                crearLabelCampo(
                        "Destino"
                ),
                0,
                5
        );


        formulario.add(
                txtDestino,
                1,
                5
        );


        // =====================================================
        // OBSERVACIONES
        // =====================================================

        formulario.add(
                crearLabelCampo(
                        "Observaciones"
                ),
                0,
                6
        );


        formulario.add(
                txtObservaciones,
                1,
                6
        );


        // =====================================================
        // TARJETA FORMULARIO
        // =====================================================

        Label tituloFormulario =
                new Label(
                        "Datos del movimiento"
                );


        tituloFormulario.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                        VERDE_TITULO +
                        ";"
        );


        VBox tarjetaFormulario =
                new VBox(
                        18,
                        tituloFormulario,
                        formulario
                );


        tarjetaFormulario.setPadding(
                new Insets(22)
        );


        tarjetaFormulario.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D5DBD5;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );


        // =====================================================
        // BOTÓN REGISTRAR
        // =====================================================

        Button btnRegistrar =
                new Button(
                        "REGISTRAR MOVIMIENTO"
                );


        btnRegistrar.setPrefHeight(
                38
        );


        btnRegistrar.setStyle(
                "-fx-background-color: " +
                        VERDE +
                        ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 20 0 20;"
        );


        btnRegistrar.setOnAction(
                e ->
                        guardarMovimiento()
        );


        // =====================================================
        // BOTÓN LIMPIAR
        // =====================================================

        Button btnLimpiar =
                new Button(
                        "LIMPIAR"
                );


        btnLimpiar.setPrefHeight(
                38
        );


        btnLimpiar.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: " +
                        VERDE_TITULO +
                        ";" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #C7D0C8;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 20 0 20;"
        );


        btnLimpiar.setOnAction(
                e ->
                        limpiarCampos()
        );


        // =====================================================
        // BOTÓN CERRAR
        // =====================================================

        Button btnCerrar =
                new Button(
                        "←  VOLVER"
                );


        btnCerrar.setPrefHeight(
                38
        );


        btnCerrar.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: " +
                        VERDE_TITULO +
                        ";" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #B8C7B8;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 20 0 20;"
        );


        btnCerrar.setOnAction(
                e ->
                        ventana.close()
        );


        // =====================================================
        // BOTONES
        // =====================================================

        HBox botones =
                new HBox(
                        10,
                        btnRegistrar,
                        btnLimpiar
                );


        botones.setAlignment(
                Pos.CENTER_RIGHT
        );


        // =====================================================
        // TABLA
        // =====================================================

        tabla =
                new TableView<>();


        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        configurarTabla();


        cargarMovimientos();


        // =====================================================
        // TARJETA TABLA
        // =====================================================

        Label tituloTabla =
                new Label(
                        "Historial de movimientos"
                );


        tituloTabla.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                        VERDE_TITULO +
                        ";"
        );


        Label ayuda =
                new Label(
                        "Registro histórico de los movimientos realizados."
                );


        ayuda.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7A8580;"
        );


        VBox tarjetaTabla =
                new VBox(
                        10,
                        tituloTabla,
                        ayuda,
                        tabla
                );


        tarjetaTabla.setPadding(
                new Insets(18)
        );


        tarjetaTabla.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D5DBD5;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );


        // =====================================================
        // CONTENIDO
        // =====================================================

        VBox contenido =
                new VBox(
                        16,
                        encabezado,
                        tarjetaFormulario,
                        botones,
                        tarjetaTabla,
                        btnCerrar
                );


        contenido.setPadding(
                new Insets(
                        25,
                        30,
                        25,
                        30
                )
        );


        contenido.setStyle(
                "-fx-background-color: " +
                        FONDO +
                        ";"
        );


        contenido.setFillWidth(
                true
        );


        // =====================================================
        // SCROLL
        // =====================================================

        ScrollPane scroll =
                new ScrollPane(
                        contenido
                );


        scroll.setFitToWidth(
                true
        );


        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );


        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );


        scroll.setPannable(
                true
        );


        scroll.setStyle(
                "-fx-background: " +
                        FONDO +
                        ";" +
                "-fx-background-color: " +
                        FONDO +
                        ";"
        );


        // =====================================================
        // ESCENA
        // =====================================================

        Scene escena =
                new Scene(
                        scroll,
                        950,
                        750
                );


        ventana.setTitle(
                "Tatú Carreta - Movimientos"
        );


        ventana.setScene(
                escena
        );


        ventana.setMinWidth(
                850
        );


        ventana.setMinHeight(
                650
        );


        ventana.setWidth(
                950
        );


        ventana.setHeight(
                750
        );


        /*
         * Si conocemos la posición de la ventana principal,
         * ubicamos Movimientos al costado.
         */

        if (
                escenario != null
        ) {

            ventana.setX(
                    escenario.getX()
                            +
                    escenario.getWidth()
                            -
                    950
            );


            ventana.setY(
                    escenario.getY()
                            +
                    50
            );
        }


        // =====================================================
        // MOSTRAR
        // =====================================================

        ventana.show();
    }


    // =========================================================
    // CONFIGURAR COMBO ANIMAL
    // =========================================================

    private void configurarComboAnimal() {

        comboAnimal.setCellFactory(
                lista ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Animal animal,
                                    boolean vacio
                            ) {

                                super.updateItem(
                                        animal,
                                        vacio
                                );


                                if (
                                        vacio
                                                ||
                                        animal == null
                                ) {

                                    setText(
                                            null
                                    );

                                } else {

                                    setText(
                                            animal.getNombreVulgar()
                                    );
                                }
                            }
                        }
        );


        comboAnimal.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Animal animal,
                            boolean vacio
                    ) {

                        super.updateItem(
                                animal,
                                vacio
                        );


                        if (
                                vacio
                                        ||
                                animal == null
                        ) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    animal.getNombreVulgar()
                            );
                        }
                    }
                }
        );
    }


    // =========================================================
    // CONFIGURAR COMBO INGRESO
    // =========================================================

    private void configurarComboIngreso() {

        comboIngreso.setCellFactory(
                lista ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Ingreso ingreso,
                                    boolean vacio
                            ) {

                                super.updateItem(
                                        ingreso,
                                        vacio
                                );


                                if (
                                        vacio
                                                ||
                                        ingreso == null
                                ) {

                                    setText(
                                            null
                                    );

                                } else {

                                    setText(
                                            ingreso.getNumeroActa()
                                    );
                                }
                            }
                        }
        );


        comboIngreso.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Ingreso ingreso,
                            boolean vacio
                    ) {

                        super.updateItem(
                                ingreso,
                                vacio
                        );


                        if (
                                vacio
                                        ||
                                ingreso == null
                        ) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    ingreso.getNumeroActa()
                            );
                        }
                    }
                }
        );
    }


    // =========================================================
    // CONFIGURAR TABLA
    // =========================================================

    private void configurarTabla() {

        tabla.getColumns().clear();


        // -----------------------------------------------------
        // ANIMAL
        // -----------------------------------------------------

        TableColumn<Movimiento, String>
                columnaAnimal =
                new TableColumn<>(
                        "Animal"
                );


        columnaAnimal.setCellValueFactory(
                celda -> {

                    int idAnimal =
                            celda.getValue()
                                    .getIdAnimal();


                    return new SimpleStringProperty(
                            obtenerNombreAnimal(
                                    idAnimal
                            )
                    );
                }
        );


        // -----------------------------------------------------
        // FECHA
        // -----------------------------------------------------

        TableColumn<Movimiento, String>
                columnaFecha =
                new TableColumn<>(
                        "Fecha"
                );


        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fechaMovimiento"
                )
        );


        // -----------------------------------------------------
        // TIPO
        // -----------------------------------------------------

        TableColumn<Movimiento, String>
                columnaTipo =
                new TableColumn<>(
                        "Tipo"
                );


        columnaTipo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "tipoMovimiento"
                )
        );


        // -----------------------------------------------------
        // CANTIDAD
        // -----------------------------------------------------

        TableColumn<Movimiento, Integer>
                columnaCantidad =
                new TableColumn<>(
                        "Cantidad"
                );


        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "cantidad"
                )
        );


        // -----------------------------------------------------
        // DESTINO
        // -----------------------------------------------------

        TableColumn<Movimiento, String>
                columnaDestino =
                new TableColumn<>(
                        "Destino"
                );


        columnaDestino.setCellValueFactory(
                new PropertyValueFactory<>(
                        "destino"
                )
        );


        // -----------------------------------------------------
        // AGREGAR COLUMNAS
        // -----------------------------------------------------

        tabla.getColumns().addAll(
                columnaAnimal,
                columnaFecha,
                columnaTipo,
                columnaCantidad,
                columnaDestino
        );


        tabla.setPrefHeight(
                250
        );


        tabla.setMinHeight(
                200
        );
    }


    // =========================================================
    // CARGAR ANIMALES
    // =========================================================

    private void cargarAnimales() {

        ObservableList<Animal>
                animales =
                FXCollections.observableArrayList(
                        animalDAO.listar()
                );


        comboAnimal.setItems(
                animales
        );
    }


    // =========================================================
    // CARGAR INGRESOS POR ANIMAL
    // =========================================================

    private void cargarIngresosPorAnimal() {

        Animal animalSeleccionado =
                comboAnimal.getValue();


        comboIngreso
                .getItems()
                .clear();


        comboIngreso.setValue(
                null
        );


        if (
                animalSeleccionado == null
        ) {

            comboIngreso.setPromptText(
                    "Seleccione un animal primero"
            );

            return;
        }


        ObservableList<Ingreso>
                ingresos =
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
                ingresos.isEmpty()
                        ? "Sin actas asociadas"
                        : "Seleccione el N° de acta"
        );
    }


    // =========================================================
    // GUARDAR MOVIMIENTO
    // =========================================================

    private void guardarMovimiento() {

        try {

            // -------------------------------------------------
            // VALIDACIONES
            // -------------------------------------------------

            if (
                    comboAnimal.getValue()
                            == null
            ) {

                mostrarMensaje(
                        "Seleccione un animal."
                );

                return;
            }


            if (
                    comboIngreso.getValue()
                            == null
            ) {

                mostrarMensaje(
                        "Seleccione el N° de acta."
                );

                return;
            }


            if (
                    txtFecha.getText()
                            .isBlank()
            ) {

                mostrarMensaje(
                        "Ingrese la fecha del movimiento."
                );

                return;
            }


            if (
                    comboTipo.getValue()
                            == null
            ) {

                mostrarMensaje(
                        "Seleccione el tipo de movimiento."
                );

                return;
            }


            if (
                    txtCantidad.getText()
                            .isBlank()
            ) {

                mostrarMensaje(
                        "Ingrese la cantidad."
                );

                return;
            }


            // -------------------------------------------------
            // CANTIDAD
            // -------------------------------------------------

            int cantidad =
                    Integer.parseInt(
                            txtCantidad
                                    .getText()
                                    .trim()
                    );


            if (
                    cantidad <= 0
            ) {

                mostrarMensaje(
                        "La cantidad debe ser mayor que 0."
                );

                return;
            }


            // -------------------------------------------------
            // DESTINO OBLIGATORIO PARA TRASLADO
            // -------------------------------------------------

            if (
                    comboTipo.getValue()
                            .equals("TRASLADO")
                            &&
                    txtDestino.getText()
                            .isBlank()
            ) {

                mostrarMensaje(
                        "Ingrese el destino del traslado."
                );

                return;
            }


            // -------------------------------------------------
            // CREAR MOVIMIENTO
            // -------------------------------------------------

            Movimiento movimiento =
                    new Movimiento();


            movimiento.setIdAnimal(
                    comboAnimal
                            .getValue()
                            .getIdAnimal()
            );


            movimiento.setIdIngreso(
                    comboIngreso
                            .getValue()
                            .getIdIngreso()
            );


            movimiento.setFechaMovimiento(
                    txtFecha
                            .getText()
                            .trim()
            );


            movimiento.setTipoMovimiento(
                    comboTipo
                            .getValue()
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


            // -------------------------------------------------
            // GUARDAR
            // -------------------------------------------------

            movimientoDAO.agregar(
                    movimiento
            );


            mostrarInformacion(
                    "Movimiento registrado correctamente."
            );


            limpiarCampos();


            cargarMovimientos();


        } catch (
                NumberFormatException e
        ) {

            mostrarMensaje(
                    "La cantidad debe ser un número entero."
            );
        }
    }


    // =========================================================
    // CARGAR TABLA
    // =========================================================

    private void cargarMovimientos() {

        ObservableList<Movimiento>
                movimientos =
                FXCollections.observableArrayList(
                        movimientoDAO.listar()
                );


        tabla.setItems(
                movimientos
        );
    }


    // =========================================================
    // OBTENER NOMBRE DEL ANIMAL
    // =========================================================

    private String obtenerNombreAnimal(
            int idAnimal
    ) {

        for (
                Animal animal :
                animalDAO.listar()
        ) {

            if (
                    animal.getIdAnimal()
                            ==
                    idAnimal
            ) {

                return animal.getNombreVulgar();
            }
        }


        return "Animal no encontrado";
    }


    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiarCampos() {

        comboAnimal.setValue(
                null
        );


        comboIngreso
                .getItems()
                .clear();


        comboIngreso.setValue(
                null
        );


        comboIngreso.setPromptText(
                "Seleccione un animal primero"
        );


        txtFecha.clear();


        comboTipo.setValue(
                null
        );


        txtCantidad.clear();


        txtDestino.clear();


        txtObservaciones.clear();
    }


    // =========================================================
    // LABEL OBLIGATORIO
    // =========================================================

    private Label crearLabelObligatorio(
            String texto
    ) {

        Label label =
                new Label(
                        texto + " *"
                );


        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                        ROJO +
                        ";"
        );


        return label;
    }


    // =========================================================
    // LABEL NORMAL
    // =========================================================

    private Label crearLabelCampo(
            String texto
    ) {

        Label label =
                new Label(
                        texto
                );


        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #34463A;"
        );


        return label;
    }


    // =========================================================
    // MENSAJE DE ADVERTENCIA
    // =========================================================

    private void mostrarMensaje(
            String mensaje
    ) {

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
    // MENSAJE DE INFORMACIÓN
    // =========================================================

    private void mostrarInformacion(
            String mensaje
    ) {

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
}