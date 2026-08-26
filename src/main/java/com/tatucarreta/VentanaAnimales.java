package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaAnimales {

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

    private final TextField txtCantidad =
            new TextField();

    private final ComboBox<String> comboOrigen =
            new ComboBox<>();

    private final ComboBox<String> comboEstado =
            new ComboBox<>();

    private final String COLOR_FONDO =
            "#F4F2EA";

    private final String COLOR_VERDE =
            "#23452C";


    public void mostrar() {

        Stage ventana = new Stage();


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

        txtCantidad.setPromptText(
                "Ej: 1"
        );


        comboOrigen.getItems().addAll(
                "Ingreso",
                "Ya existente en la reserva"
        );

        comboOrigen.setValue(
                "Ingreso"
        );


        comboEstado.getItems().addAll(
                "Activo",
                "Inactivo"
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
                crearLabelCampo("Especie"),
                0,
                0
        );

        formulario.add(
                comboEspecie,
                0,
                1
        );


        // NOMBRE VULGAR

        formulario.add(
                crearLabelCampo("Nombre vulgar"),
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
                crearLabelCampo("Nombre científico"),
                0,
                2
        );

        formulario.add(
                txtNombreCientifico,
                0,
                3
        );


        // CANTIDAD

        formulario.add(
                crearLabelCampo(
                        "Cantidad total registrada"
                ),
                1,
                2
        );

        formulario.add(
                txtCantidad,
                1,
                3
        );


        // ORIGEN

        formulario.add(
                crearLabelCampo("Origen"),
                0,
                4
        );

        formulario.add(
                comboOrigen,
                0,
                5
        );


        // ESTADO

        formulario.add(
                crearLabelCampo("Estado"),
                1,
                4
        );

        formulario.add(
                comboEstado,
                1,
                5
        );


        configurarCampo(comboEspecie);
        configurarCampo(txtNombreVulgar);
        configurarCampo(txtNombreCientifico);
        configurarCampo(txtCantidad);
        configurarCampo(comboOrigen);
        configurarCampo(comboEstado);


        // =====================================
        // BOTONES
        // =====================================

        Button btnAgregar =
                crearBotonPrincipal("AGREGAR");

        Button btnModificar =
                crearBotonSecundario("MODIFICAR");

        Button btnEliminar =
                crearBotonEliminar("ELIMINAR");

        Button btnLimpiar =
                crearBotonSecundario("LIMPIAR");


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
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_VERDE + ";"
        );


        // ID

        TableColumn<Animal, Integer> columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idAnimal"
                )
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


        // CANTIDAD

        TableColumn<Animal, Integer> columnaCantidad =
                new TableColumn<>(
                        "Cantidad"
                );

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "cantidadActual"
                )
        );


        // ORIGEN

        TableColumn<Animal, String> columnaOrigen =
                new TableColumn<>(
                        "Origen"
                );

        columnaOrigen.setCellValueFactory(
                new PropertyValueFactory<>(
                        "origen"
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
                columnaId,
                columnaVulgar,
                columnaCientifico,
                columnaCantidad,
                columnaOrigen,
                columnaEstado
        );


        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(330);

        tabla.setMinHeight(330);

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
                tabla
        );


        cargarAnimales();


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

                                txtCantidad.setText(
                                        String.valueOf(
                                                seleccionado
                                                        .getCantidadActual()
                                        )
                                );

                                comboOrigen.setValue(
                                        seleccionado
                                                .getOrigen()
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

            if (
                    comboEspecie.getValue() == null
                            || txtNombreVulgar
                            .getText()
                            .isBlank()
                            || txtNombreCientifico
                            .getText()
                            .isBlank()
                            || txtCantidad
                            .getText()
                            .isBlank()
                            || comboOrigen
                            .getValue()
                            == null
                            || comboEstado
                            .getValue()
                            == null
            ) {

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

                    return;
                }


                Animal animal =
                        new Animal();

                animal.setIdEspecie(
                        comboEspecie
                                .getValue()
                                .getIdEspecie()
                );

                animal.setNombreVulgar(
                        txtNombreVulgar
                                .getText()
                                .trim()
                );

                animal.setNombreCientifico(
                        txtNombreCientifico
                                .getText()
                                .trim()
                );

                animal.setCantidadActual(
                        cantidad
                );

                animal.setOrigen(
                        comboOrigen.getValue()
                );

                animal.setEstado(
                        comboEstado.getValue()
                );


                animalDAO.agregar(animal);

                limpiarCampos();

                cargarAnimales();

            } catch (NumberFormatException error) {

                System.out.println(
                        "La cantidad debe ser un número."
                );
            }
        });


        // =====================================
        // MODIFICAR
        // =====================================

        btnModificar.setOnAction(e -> {

            Animal seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (
                    seleccionado == null
                            || comboEspecie
                            .getValue()
                            == null
                            || txtCantidad
                            .getText()
                            .isBlank()
                            || comboOrigen
                            .getValue()
                            == null
            ) {

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

                    return;
                }


                seleccionado.setIdEspecie(
                        comboEspecie
                                .getValue()
                                .getIdEspecie()
                );

                seleccionado.setNombreVulgar(
                        txtNombreVulgar
                                .getText()
                                .trim()
                );

                seleccionado.setNombreCientifico(
                        txtNombreCientifico
                                .getText()
                                .trim()
                );

                seleccionado.setCantidadActual(
                        cantidad
                );

                seleccionado.setOrigen(
                        comboOrigen.getValue()
                );

                seleccionado.setEstado(
                        comboEstado.getValue()
                );


                animalDAO.modificar(
                        seleccionado
                );

                limpiarCampos();

                cargarAnimales();

            } catch (NumberFormatException error) {

                System.out.println(
                        "La cantidad debe ser un número."
                );
            }
        });


        // =====================================
        // ELIMINAR
        // =====================================

        btnEliminar.setOnAction(e -> {

            Animal seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {

                return;
            }

            animalDAO.eliminar(
                    seleccionado.getIdAnimal()
            );

            limpiarCampos();

            cargarAnimales();
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
                new VBox(25);

        contenido.setPadding(
                new Insets(35, 40, 35, 40)
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


        // =====================================
        // ESCENA
        // =====================================

        Scene escena =
                new Scene(
                        scroll,
                        1200,
                        850
                );

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

        txtCantidad.clear();

        comboOrigen.setValue(
                "Ingreso"
        );

        comboEstado.setValue(
                "Activo"
        );

        tabla.getSelectionModel()
                .clearSelection();
    }
}