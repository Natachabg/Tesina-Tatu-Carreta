package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaAnimales {

    private VentanaPrincipal ventanaPrincipal;

    public VentanaAnimales() {
    }

    public VentanaAnimales(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final TableView<Animal> tabla =
            new TableView<>();

    private final ComboBox<Especie> comboEspecie =
            new ComboBox<>();

    private final TextField txtNombreVulgar =
            new TextField();

    private final TextField txtNombreCientifico =
            new TextField();

    private final ComboBox<String> comboEstado =
            new ComboBox<>();

    private final String COLOR_FONDO =
            "#E7F1EA";

    private final String COLOR_VERDE =
            "#23452C";

    private final String COLOR_VERDE_CLARO =
            "#DCEBE1";


    public ScrollPane crearContenido() {


        // =====================================
        // ENCABEZADO
        // =====================================

        Label ruta = new Label(
                "Inicio / Gestión de Animales"
        );

        ruta.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #8A918E;"
        );


        Label titulo = new Label(
                "Gestión de Animales"
        );

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_VERDE + ";"
        );


        Label descripcion = new Label(
                "Administre los animales registrados en la reserva."
        );

        descripcion.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #727A76;"
        );


        VBox encabezado = new VBox(6);

        encabezado.getChildren().addAll(
                ruta,
                titulo,
                descripcion
        );


        // =====================================
        // CONFIGURAR CAMPOS
        // =====================================

        comboEspecie.setPromptText(
                "Seleccionar especie"
        );

        txtNombreVulgar.setPromptText(
                "Ej: Loro hablador"
        );

        txtNombreCientifico.setPromptText(
                "Ej: Amazona aestiva"
        );
comboEstado.getItems().addAll(
                "Activo",
                "Inactivo",
                "Trasladado",
                "Fallecido"
        );

        comboEstado.setValue(
                "Activo"
        );


        cargarEspecies();


        // =====================================
        // FORMULARIO
        // =====================================

        Label tituloFormulario =
                new Label("Datos del Animal");

        tituloFormulario.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_VERDE + ";"
        );


        GridPane formulario =
                new GridPane();

        formulario.setHgap(20);
        formulario.setVgap(15);

        formulario.setPadding(
                new Insets(20, 0, 0, 0)
        );


        ColumnConstraints columna1 =
                new ColumnConstraints();

        columna1.setPercentWidth(50);


        ColumnConstraints columna2 =
                new ColumnConstraints();

        columna2.setPercentWidth(50);


        formulario.getColumnConstraints().addAll(
                columna1,
                columna2
        );


        // ESPECIE

        formulario.add(
                crearLabelCampo("Especie *"),
                0,
                0
        );

        Button btnNuevaEspecie =
                crearBotonSecundario("＋ NUEVA ESPECIE");

        btnNuevaEspecie.setOnAction(
                e -> mostrarNuevaEspecie()
        );

        HBox filaEspecie =
                new HBox(
                        10,
                        comboEspecie,
                        btnNuevaEspecie
                );

        filaEspecie.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(
                comboEspecie,
                Priority.ALWAYS
        );

        formulario.add(
                filaEspecie,
                0,
                1
        );


        // NOMBRE VULGAR

        formulario.add(
                crearLabelCampo("Nombre vulgar *"),
                1,
                0
        );

        formulario.add(
                txtNombreVulgar,
                1,
                1
        );


        // NOMBRE CIENTÍFICO

        formulario.add(
                crearLabelCampo("Nombre científico *"),
                0,
                2
        );

        formulario.add(
                txtNombreCientifico,
                0,
                3
        );
// ESTADO

        formulario.add(
                crearLabelCampo("Estado *"),
                1,
                2
        );

        formulario.add(
                comboEstado,
                1,
                3
        );


        configurarCampo(comboEspecie);
        configurarCampo(txtNombreVulgar);
        configurarCampo(txtNombreCientifico);
        configurarCampo(comboEstado);


        // =====================================
        // BOTONES
        // =====================================

        Button btnAgregar =
                crearBotonPrincipal("＋ AGREGAR");

        Button btnModificar =
                crearBotonSecundario("✎ MODIFICAR");

        Button btnEliminar =
                crearBotonEliminar("🗑 ELIMINAR");

        Button btnLimpiar =
                crearBotonSecundario("↺ LIMPIAR");


        HBox botones =
                new HBox(12);

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        botones.setPadding(
                new Insets(20, 0, 0, 0)
        );

        botones.getChildren().addAll(
                btnLimpiar,
                btnEliminar,
                btnModificar,
                btnAgregar
        );


        // =====================================
        // TARJETA FORMULARIO
        // =====================================

        VBox tarjetaFormulario =
                new VBox();

        tarjetaFormulario.setPadding(
                new Insets(25)
        );

        tarjetaFormulario.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #DDDAD1;" +
                "-fx-border-radius: 14;"
        );

        tarjetaFormulario.getChildren().addAll(
                tituloFormulario,
                formulario,
                botones
        );


        // =====================================
        // TABLA
        // =====================================

        Label tituloTabla =
                new Label(
                        "Animales Registrados"
                );

        tituloTabla.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_VERDE + ";"
        );

        Label ayudaTabla =
                new Label(
                        "Seleccione un animal para modificar sus datos o haga doble clic para consultar su ficha."
                );

        ayudaTabla.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #66756A;"
        );


        // NOMBRE VULGAR

        TableColumn<Animal, String> columnaVulgar =
                new TableColumn<>(
                        "Nombre vulgar"
                );

        columnaVulgar.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombreVulgar"
                )
        );


        // NOMBRE CIENTÍFICO

        TableColumn<Animal, String> columnaCientifico =
                new TableColumn<>(
                        "Nombre científico"
                );

        columnaCientifico.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombreCientifico"
                )
        );


        // ESTADO

        TableColumn<Animal, String> columnaEstado =
                new TableColumn<>(
                        "Estado"
                );

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>(
                        "estado"
                )
        );

        tabla.getColumns().addAll(
                columnaVulgar,
                columnaCientifico,
                columnaEstado
        );


        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(330);

        tabla.setMinHeight(300);
        tabla.setMaxHeight(330);

        tabla.setPlaceholder(
                new Label("No hay animales registrados.")
        );

        tabla.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #E1E1DC;"
        );


        VBox seccionTabla =
                new VBox(18);

        seccionTabla.setPadding(
                new Insets(25)
        );

        seccionTabla.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #DDDAD1;" +
                "-fx-border-radius: 14;"
        );

        seccionTabla.getChildren().addAll(
                tituloTabla,
                ayudaTabla,
                tabla
        );


        cargarAnimales();


        // =====================================
        // DOBLE CLIC - FICHA COMPLETA
        // =====================================

        tabla.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Animal seleccionado =
                        tabla.getSelectionModel()
                                .getSelectedItem();

                if (seleccionado != null
                        && ventanaPrincipal != null) {

                    ventanaPrincipal.mostrarFichaAnimal(
                            seleccionado.getIdAnimal()
                    );
                }
            }
        });


        // =====================================
        // SELECCIONAR ANIMAL
        // =====================================

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                anterior,
                                seleccionado
                        ) -> {

                            if (seleccionado != null) {

                                txtNombreVulgar.setText(
                                        seleccionado
                                                .getNombreVulgar()
                                );

                                txtNombreCientifico.setText(
                                        seleccionado
                                                .getNombreCientifico()
                                );
comboEstado.setValue(
                                        seleccionado
                                                .getEstado()
                                );


                                for (
                                        Especie especie
                                        : comboEspecie.getItems()
                                ) {

                                    if (
                                            especie.getIdEspecie()
                                                    == seleccionado
                                                    .getIdEspecie()
                                    ) {

                                        comboEspecie.setValue(
                                                especie
                                        );

                                        break;
                                    }
                                }
                            }
                        }
                );


        // =====================================
        // AGREGAR
        // =====================================

        btnAgregar.setOnAction(e -> {

            if (!validarCampos()) {
                return;
            }

            if (!confirmar(
                    "Confirmar registro",
                    "¿Está seguro de que desea registrar este animal?"
            )) {
                return;
            }

            Animal animal = new Animal();
            animal.setIdEspecie(comboEspecie.getValue().getIdEspecie());
            animal.setNombreVulgar(txtNombreVulgar.getText().trim());
            animal.setNombreCientifico(txtNombreCientifico.getText().trim());
            animal.setCantidadActual(1);
            animal.setEstado(comboEstado.getValue());

            animalDAO.agregar(animal);
            limpiarCampos();
            cargarAnimales();

            mostrarMensaje(
                    "Animal registrado",
                    "El animal fue registrado correctamente."
            );
        });


        // =====================================
        // MODIFICAR
        // =====================================

        btnModificar.setOnAction(e -> {

            Animal seleccionado =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                mostrarMensaje(
                        "Modificar animal",
                        "Seleccione un animal de la tabla."
                );
                return;
            }

            if (!validarCampos()) {
                return;
            }

            if (!confirmar(
                    "Confirmar modificación",
                    "¿Está seguro de que desea modificar los datos de este animal?"
            )) {
                return;
            }

            seleccionado.setIdEspecie(comboEspecie.getValue().getIdEspecie());
            seleccionado.setNombreVulgar(txtNombreVulgar.getText().trim());
            seleccionado.setNombreCientifico(txtNombreCientifico.getText().trim());
            seleccionado.setEstado(comboEstado.getValue());

            animalDAO.modificar(seleccionado);
            limpiarCampos();
            cargarAnimales();

            mostrarMensaje(
                    "Animal modificado",
                    "Los datos del animal fueron modificados correctamente."
            );
        });


        // =====================================
        // ELIMINAR
        // =====================================

        btnEliminar.setOnAction(e -> {

            Animal seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {
                mostrarMensaje(
                        "Selección requerida",
                        "Seleccione un animal de la tabla."
                );
                return;
            }

            Alert confirmacion =
                    new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "¿Está seguro de que desea eliminar este animal?",
                            ButtonType.OK,
                            ButtonType.CANCEL
                    );

            confirmacion.setTitle("Tatú Carreta");
            confirmacion.setHeaderText("Confirmar eliminación");

            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                    != ButtonType.OK) {
                return;
            }

            animalDAO.eliminar(
                    seleccionado.getIdAnimal()
            );

            limpiarCampos();
            cargarAnimales();

            mostrarMensaje(
                    "Animal eliminado",
                    "El animal fue eliminado correctamente."
            );
        });


        // =====================================
        // LIMPIAR
        // =====================================

        btnLimpiar.setOnAction(e -> {

            limpiarCampos();
        });


        // =====================================
        // CONTENIDO PRINCIPAL
        // =====================================

        VBox contenido =
                new VBox(20);

        contenido.setPadding(
                new Insets(25, 30, 30, 30)
        );

        contenido.setFillWidth(true);

        contenido.setStyle(
                "-fx-background-color: "
                        + COLOR_FONDO
                        + ";"
        );

        contenido.getChildren().addAll(
                encabezado,
                tarjetaFormulario,
                seccionTabla
        );


        // =====================================
        // SCROLL PRINCIPAL
        // =====================================

        ScrollPane scroll =
                new ScrollPane();

        scroll.setContent(contenido);

        scroll.setFitToWidth(true);

        scroll.setFitToHeight(false);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scroll.setStyle(
                "-fx-background: " + COLOR_FONDO + ";" +
                "-fx-background-color: " + COLOR_FONDO + ";"
        );


        return scroll;
    }

    // Mantiene el método anterior por compatibilidad.
    // La VentanaPrincipal utilizará crearContenido() para mostrar
    // Animales dentro del panel central, sin abrir otra ventana.
    public void mostrar() {
        Stage ventana = new Stage();
        Scene escena = new Scene(crearContenido(), 1200, 850);

        ventana.setTitle(
                "Tatú Carreta - Gestión de Animales"
        );
        ventana.setScene(escena);
        ventana.setMinWidth(1000);
        ventana.setMinHeight(700);
        ventana.setMaximized(true);
        ventana.show();
    }


    // =====================================
    // LABEL DE CAMPO
    // =====================================

    private Label crearLabelCampo(
            String texto) {

        Label label =
                new Label(texto);

        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #4B5752;"
        );

        return label;
    }


    // =====================================
    // CONFIGURAR CAMPOS
    // =====================================

    private void configurarCampo(
            Control campo) {

        campo.setMaxWidth(
                Double.MAX_VALUE
        );

        campo.setPrefHeight(38);

        campo.setStyle(
                "-fx-background-color: #FAFAF8;" +
                "-fx-border-color: #D7D9D2;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;"
        );
    }


    // =====================================
    // BOTÓN PRINCIPAL
    // =====================================

    private Button crearBotonPrincipal(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: "
                        + COLOR_VERDE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 12px;"
        );

        return boton;
    }


    // =====================================
    // BOTÓN SECUNDARIO
    // =====================================

    private Button crearBotonSecundario(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: " + COLOR_VERDE + ";" +
                "-fx-border-color: #B8C7B8;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;"
        );

        return boton;
    }


    // =====================================
    // BOTÓN ELIMINAR
    // =====================================

    private Button crearBotonEliminar(
            String texto) {

        Button boton =
                new Button(texto);

        boton.setPrefHeight(38);

        boton.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: #A94442;" +
                "-fx-border-color: #D8B3B3;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;"
        );

        return boton;
    }


    // =====================================
    // NUEVA ESPECIE
    // =====================================

    private void mostrarNuevaEspecie() {

        Stage ventanaEspecie =
                new Stage();

        ventanaEspecie.setTitle(
                "Tatú Carreta - Nueva Especie"
        );

        Label titulo =
                new Label("Nueva Especie");

        titulo.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_VERDE + ";"
        );

        Label ayuda =
                new Label(
                        "Registre una especie para poder utilizarla en el alta de animales."
                );

        ayuda.setWrapText(true);
        ayuda.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #66756A;"
        );

        Label lblNombre =
                crearLabelCampo("Nombre de la especie *");

        TextField txtNombre =
                new TextField();

        txtNombre.setPromptText(
                "Ej: Mamíferos"
        );

        configurarCampo(txtNombre);

        Button btnCancelar =
                crearBotonSecundario("CANCELAR");

        Button btnGuardar =
                crearBotonPrincipal("GUARDAR ESPECIE");

        HBox botones =
                new HBox(
                        10,
                        btnCancelar,
                        btnGuardar
                );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        btnCancelar.setOnAction(
                e -> ventanaEspecie.close()
        );

        btnGuardar.setOnAction(e -> {

            String nombre =
                    txtNombre.getText().trim();

            if (nombre.isBlank()) {

                marcarError(txtNombre);

                mostrarMensaje(
                        "Datos incompletos",
                        "Complete el campo obligatorio marcado con *."
                );

                return;
            }

            for (Especie existente :
                    new EspecieDAO().listar()) {

                if (existente.getNombre()
                        .equalsIgnoreCase(nombre)) {

                    marcarError(txtNombre);

                    mostrarMensaje(
                            "Especie existente",
                            "La especie ya está registrada."
                    );

                    return;
                }
            }

            Alert confirmacion =
                    new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "¿Está seguro de que desea registrar esta especie?",
                            ButtonType.OK,
                            ButtonType.CANCEL
                    );

            confirmacion.setTitle("Tatú Carreta");
            confirmacion.setHeaderText("Confirmar registro");

            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                    != ButtonType.OK) {
                return;
            }

            Especie nueva =
                    new Especie();

            nueva.setNombre(nombre);

            new EspecieDAO().agregar(nueva);

            cargarEspecies();

            // Selecciona automáticamente la especie recién creada.
            for (Especie especie :
                    comboEspecie.getItems()) {

                if (especie.getNombre()
                        .equalsIgnoreCase(nombre)) {

                    comboEspecie.setValue(especie);
                    break;
                }
            }

            ventanaEspecie.close();

            mostrarMensaje(
                    "Especie registrada",
                    "La especie fue registrada correctamente."
            );
        });

        VBox contenido =
                new VBox(
                        12,
                        titulo,
                        ayuda,
                        lblNombre,
                        txtNombre,
                        botones
                );

        contenido.setPadding(
                new Insets(25)
        );

        contenido.setStyle(
                "-fx-background-color: #E7F1EA;"
        );

        Scene escena =
                new Scene(
                        contenido,
                        500,
                        300
                );

        ventanaEspecie.setScene(
                escena
        );

        ventanaEspecie.showAndWait();
    }


    // =====================================
    // CARGAR ESPECIES
    // =====================================

    private void cargarEspecies() {

        ObservableList<Especie> especies =
                FXCollections.observableArrayList(
                        new EspecieDAO().listar()
                );

        comboEspecie.setItems(
                especies
        );
    }


    // =====================================
    // CARGAR ANIMALES
    // =====================================

    private void cargarAnimales() {

        ObservableList<Animal> lista =
                FXCollections.observableArrayList(
                        animalDAO.listar()
                );

        tabla.setItems(
                lista
        );
    }


    // =====================================
    // LIMPIAR CAMPOS
    // =====================================

    private void limpiarCampos() {

        comboEspecie.setValue(null);

        txtNombreVulgar.clear();

        txtNombreCientifico.clear();

        comboEstado.setValue(
                "Activo"
        );

        tabla.getSelectionModel()
                .clearSelection();
    }
    // =====================================
    // VALIDACIONES Y MENSAJES
    // =====================================

    private boolean validarCampos() {
        boolean valido = true;
        limpiarEstadoError();

        if (comboEspecie.getValue() == null) {
            marcarError(comboEspecie);
            valido = false;
        }
        if (txtNombreVulgar.getText().isBlank()) {
            marcarError(txtNombreVulgar);
            valido = false;
        }
        if (txtNombreCientifico.getText().isBlank()) {
            marcarError(txtNombreCientifico);
            valido = false;
        }
        if (comboEstado.getValue() == null) {
            marcarError(comboEstado);
            valido = false;
        }

        if (!valido) {
            mostrarMensaje(
                    "Datos incompletos",
                    "Complete todos los campos obligatorios marcados con *."
            );
        }
        return valido;
    }

    private void marcarError(Control campo) {
        campo.setStyle(
                "-fx-background-color: #FFF5F5;" +
                "-fx-border-color: #C62828;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;"
        );
    }

    private void limpiarEstadoError() {
        configurarCampo(comboEspecie);
        configurarCampo(txtNombreVulgar);
        configurarCampo(txtNombreCientifico);
        configurarCampo(comboEstado);
    }

    private boolean confirmar(String titulo, String mensaje) {
        javafx.scene.control.Alert alerta =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION
                );
        alerta.setTitle("Tatú Carreta");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        java.util.Optional<javafx.scene.control.ButtonType> respuesta =
                alerta.showAndWait();
        return respuesta.isPresent()
                && respuesta.get() == javafx.scene.control.ButtonType.OK;
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        javafx.scene.control.Alert alerta =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION
                );
        alerta.setTitle("Tatú Carreta");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}