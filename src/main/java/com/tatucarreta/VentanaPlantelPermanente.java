package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class VentanaPlantelPermanente {

    private final PlantelPermanenteDAO plantelDAO =
            new PlantelPermanenteDAO();

    private final AnimalDAO animalDAO =
            new AnimalDAO();

    private final HabitaculoDAO habitaculoDAO =
            new HabitaculoDAO();

    private final TableView<PlantelPermanente> tabla =
            new TableView<>();

    private final ComboBox<Animal> comboAnimal =
            new ComboBox<>();

    private final ComboBox<Habitaculo> comboHabitaculo =
            new ComboBox<>();

    private final TextField txtCantidad =
            new TextField();

    private final TextField txtFecha =
            new TextField();

    private final ComboBox<String> comboUbicacion =
            new ComboBox<>();

    private final ComboBox<String> comboEstado =
            new ComboBox<>();

    private final TextArea txtObservaciones =
            new TextArea();


    public void mostrar() {

        Stage ventana = new Stage();

        // =========================
        // TÍTULO
        // =========================

        Label titulo =
                new Label("PLANTEL PERMANENTE");

        titulo.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );


        // =========================
        // ETIQUETAS
        // =========================

        Label lblAnimal =
                new Label("Animal:");

        Label lblCantidad =
                new Label("Cantidad en esta ubicación:");

        Label lblUbicacion =
                new Label("Tipo de ubicación:");

        Label lblHabitaculo =
                new Label("Habitáculo:");

        Label lblFecha =
                new Label("Fecha ingreso:");

        Label lblEstado =
                new Label("Estado:");

        Label lblObservaciones =
                new Label("Observaciones:");


        // =========================
        // CARGAR DATOS
        // =========================

        cargarAnimales();
        cargarHabitaculos();

        comboAnimal.setPrefWidth(350);

        comboHabitaculo.setPrefWidth(350);


        // =========================
        // MOSTRAR NOMBRE Y SECTOR
        // EN EL DESPLEGABLE DE HABITÁCULOS
        // =========================

        comboHabitaculo.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(
                            Habitaculo habitaculo) {

                        if (habitaculo == null) {
                            return "";
                        }

                        return habitaculo.getNombre()
                                + " - "
                                + habitaculo.getSector();
                    }

                    @Override
                    public Habitaculo fromString(
                            String string) {

                        return null;
                    }
                }
        );


        // =========================
        // CONFIGURAR CAMPOS
        // =========================

        txtCantidad.setPromptText(
                "Ej: 1, 10, 200"
        );

        txtFecha.setPromptText(
                "Ej: 2026-08-24"
        );

        comboUbicacion.getItems().addAll(
                "Habitáculo",
                "Recinto",
                "Campo abierto",
                "Otro"
        );

        comboEstado.getItems().addAll(
                "Activo",
                "Inactivo"
        );

        comboEstado.setValue("Activo");

        txtObservaciones.setPrefRowCount(3);


        // =========================
        // FILAS
        // =========================

        HBox filaAnimal = new HBox(
                10,
                lblAnimal,
                comboAnimal
        );

        HBox filaCantidad = new HBox(
                10,
                lblCantidad,
                txtCantidad
        );

        HBox filaUbicacion = new HBox(
                10,
                lblUbicacion,
                comboUbicacion
        );

        HBox filaHabitaculo = new HBox(
                10,
                lblHabitaculo,
                comboHabitaculo
        );

        HBox filaFecha = new HBox(
                10,
                lblFecha,
                txtFecha
        );

        HBox filaEstado = new HBox(
                10,
                lblEstado,
                comboEstado
        );

        filaAnimal.setAlignment(Pos.CENTER_LEFT);
        filaCantidad.setAlignment(Pos.CENTER_LEFT);
        filaUbicacion.setAlignment(Pos.CENTER_LEFT);
        filaHabitaculo.setAlignment(Pos.CENTER_LEFT);
        filaFecha.setAlignment(Pos.CENTER_LEFT);
        filaEstado.setAlignment(Pos.CENTER_LEFT);


        // =========================
        // MOSTRAR HABITÁCULO
        // SOLO CUANDO CORRESPONDA
        // =========================

        comboUbicacion.setOnAction(e -> {

            actualizarVisibilidadHabitaculo(
                    filaHabitaculo
            );
        });

        filaHabitaculo.setVisible(false);
        filaHabitaculo.setManaged(false);


        // =========================
        // BOTONES
        // =========================

        Button btnAgregar =
                new Button("AGREGAR");

        Button btnModificar =
                new Button("MODIFICAR");

        Button btnEliminar =
                new Button("ELIMINAR");

        Button btnVolver =
                new Button("VOLVER");

        HBox botones = new HBox(
                10,
                btnAgregar,
                btnModificar,
                btnEliminar
        );

        botones.setAlignment(Pos.CENTER);


        // =========================
        // COLUMNAS DE LA TABLA
        // =========================

        TableColumn<PlantelPermanente, Integer> columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(
                new PropertyValueFactory<>("idPlantel")
        );


        // ANIMAL: AHORA MUESTRA EL NOMBRE

        TableColumn<PlantelPermanente, String> columnaAnimal =
                new TableColumn<>("Animal");

        columnaAnimal.setCellValueFactory(
                new PropertyValueFactory<>("nombreAnimal")
        );


        TableColumn<PlantelPermanente, Integer> columnaCantidad =
                new TableColumn<>("Cantidad");

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );


        TableColumn<PlantelPermanente, String> columnaUbicacion =
                new TableColumn<>("Ubicación");

        columnaUbicacion.setCellValueFactory(
                new PropertyValueFactory<>("tipoUbicacion")
        );


        // HABITÁCULO: AHORA MUESTRA EL NOMBRE

        TableColumn<PlantelPermanente, String> columnaHabitaculo =
                new TableColumn<>("Habitáculo");

        columnaHabitaculo.setCellValueFactory(
                new PropertyValueFactory<>("nombreHabitaculo")
        );


        TableColumn<PlantelPermanente, String> columnaFecha =
                new TableColumn<>("Fecha");

        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fechaIngresoPlantel"
                )
        );


        TableColumn<PlantelPermanente, String> columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );


        tabla.getColumns().addAll(
                columnaId,
                columnaAnimal,
                columnaCantidad,
                columnaUbicacion,
                columnaHabitaculo,
                columnaFecha,
                columnaEstado
        );

        tabla.setPrefHeight(250);

        cargarPlantel();


        // =========================
        // SELECCIONAR REGISTRO
        // =========================

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                anterior,
                                seleccionado
                        ) -> {

                            if (seleccionado != null) {

                                // BUSCAR ANIMAL

                                for (Animal animal :
                                        comboAnimal.getItems()) {

                                    if (animal.getIdAnimal()
                                            == seleccionado.getIdAnimal()) {

                                        comboAnimal.setValue(
                                                animal
                                        );

                                        break;
                                    }
                                }


                                // CANTIDAD

                                txtCantidad.setText(
                                        String.valueOf(
                                                seleccionado.getCantidad()
                                        )
                                );


                                // UBICACIÓN

                                comboUbicacion.setValue(
                                        seleccionado.getTipoUbicacion()
                                );

                                actualizarVisibilidadHabitaculo(
                                        filaHabitaculo
                                );


                                // BUSCAR HABITÁCULO

                                if (seleccionado.getIdHabitaculo()
                                        != null) {

                                    for (Habitaculo habitaculo :
                                            comboHabitaculo.getItems()) {

                                        if (habitaculo.getIdHabitaculo()
                                                == seleccionado
                                                .getIdHabitaculo()) {

                                            comboHabitaculo.setValue(
                                                    habitaculo
                                            );

                                            break;
                                        }
                                    }

                                } else {

                                    comboHabitaculo.setValue(null);
                                }


                                // FECHA

                                txtFecha.setText(
                                        seleccionado
                                                .getFechaIngresoPlantel()
                                );


                                // ESTADO

                                comboEstado.setValue(
                                        seleccionado.getEstado()
                                );


                                // OBSERVACIONES

                                txtObservaciones.setText(
                                        seleccionado.getObservaciones()
                                );
                            }
                        }
                );


        // =========================
        // AGREGAR
        // =========================

        btnAgregar.setOnAction(e -> {

            if (comboAnimal.getValue() == null
                    || txtCantidad.getText().isBlank()
                    || comboUbicacion.getValue() == null
                    || txtFecha.getText().isBlank()
                    || comboEstado.getValue() == null) {

                System.out.println(
                        "Complete todos los campos obligatorios."
                );

                return;
            }

            boolean necesitaHabitaculo =
                    necesitaHabitaculo();

            if (necesitaHabitaculo
                    && comboHabitaculo.getValue() == null) {

                System.out.println(
                        "Seleccione un habitáculo."
                );

                return;
            }

            try {

                int cantidad =
                        Integer.parseInt(
                                txtCantidad.getText()
                        );

                if (cantidad <= 0) {

                    System.out.println(
                            "La cantidad debe ser mayor a cero."
                    );

                    return;
                }

                PlantelPermanente plantel =
                        new PlantelPermanente();

                plantel.setIdAnimal(
                        comboAnimal
                                .getValue()
                                .getIdAnimal()
                );

                plantel.setCantidad(
                        cantidad
                );

                plantel.setTipoUbicacion(
                        comboUbicacion.getValue()
                );


                if (comboHabitaculo.getValue() != null) {

                    plantel.setIdHabitaculo(
                            comboHabitaculo
                                    .getValue()
                                    .getIdHabitaculo()
                    );

                } else {

                    plantel.setIdHabitaculo(null);
                }


                plantel.setFechaIngresoPlantel(
                        txtFecha.getText()
                );

                plantel.setEstado(
                        comboEstado.getValue()
                );

                plantel.setObservaciones(
                        txtObservaciones.getText()
                );

                plantelDAO.agregar(
                        plantel
                );

                limpiarCampos();

                cargarPlantel();

            } catch (NumberFormatException ex) {

                System.out.println(
                        "Ingrese una cantidad numérica válida."
                );
            }
        });


        // =========================
        // MODIFICAR
        // =========================

        btnModificar.setOnAction(e -> {

            PlantelPermanente seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null
                    || comboAnimal.getValue() == null) {

                System.out.println(
                        "Seleccione un registro y un animal."
                );

                return;
            }

            boolean necesitaHabitaculo =
                    necesitaHabitaculo();

            if (necesitaHabitaculo
                    && comboHabitaculo.getValue() == null) {

                System.out.println(
                        "Seleccione un habitáculo."
                );

                return;
            }

            try {

                int cantidad =
                        Integer.parseInt(
                                txtCantidad.getText()
                        );

                if (cantidad <= 0) {

                    System.out.println(
                            "La cantidad debe ser mayor a cero."
                    );

                    return;
                }


                seleccionado.setIdAnimal(
                        comboAnimal
                                .getValue()
                                .getIdAnimal()
                );

                seleccionado.setCantidad(
                        cantidad
                );

                seleccionado.setTipoUbicacion(
                        comboUbicacion.getValue()
                );


                if (comboHabitaculo.getValue() != null) {

                    seleccionado.setIdHabitaculo(
                            comboHabitaculo
                                    .getValue()
                                    .getIdHabitaculo()
                    );

                } else {

                    seleccionado.setIdHabitaculo(null);
                }


                seleccionado.setFechaIngresoPlantel(
                        txtFecha.getText()
                );

                seleccionado.setEstado(
                        comboEstado.getValue()
                );

                seleccionado.setObservaciones(
                        txtObservaciones.getText()
                );


                plantelDAO.modificar(
                        seleccionado
                );

                limpiarCampos();

                cargarPlantel();

            } catch (NumberFormatException ex) {

                System.out.println(
                        "Ingrese una cantidad numérica válida."
                );
            }
        });


        // =========================
        // ELIMINAR
        // =========================

        btnEliminar.setOnAction(e -> {

            PlantelPermanente seleccionado =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {

                System.out.println(
                        "Seleccione un registro."
                );

                return;
            }

            plantelDAO.eliminar(
                    seleccionado.getIdPlantel()
            );

            limpiarCampos();

            cargarPlantel();
        });


        // =========================
        // VOLVER
        // =========================

        btnVolver.setOnAction(
                e -> ventana.close()
        );


        // =========================
        // CONTENEDOR
        // =========================

        VBox contenedor =
                new VBox(12);

        contenedor.setAlignment(
                Pos.TOP_CENTER
        );

        contenedor.setPadding(
                new Insets(25)
        );

        contenedor.getChildren().addAll(
                titulo,
                filaAnimal,
                filaCantidad,
                filaUbicacion,
                filaHabitaculo,
                filaFecha,
                filaEstado,
                lblObservaciones,
                txtObservaciones,
                botones,
                tabla,
                btnVolver
        );

        contenedor.setStyle(
                "-fx-background-color: #F2F0E6;"
        );


        // =========================
        // ESCENA
        // =========================

        Scene escena = new Scene(
                contenedor,
                950,
                750
        );

        ventana.setTitle(
                "Tatú Carreta - Plantel Permanente"
        );

        ventana.setScene(
                escena
        );

        ventana.show();
    }


    // =========================
    // SABER SI NECESITA HABITÁCULO
    // =========================

    private boolean necesitaHabitaculo() {

        String ubicacion =
                comboUbicacion.getValue();

        return "Habitáculo".equals(ubicacion)
                || "Recinto".equals(ubicacion);
    }


    // =========================
    // MOSTRAR / OCULTAR HABITÁCULO
    // =========================

    private void actualizarVisibilidadHabitaculo(
            HBox filaHabitaculo) {

        boolean mostrar =
                necesitaHabitaculo();

        filaHabitaculo.setVisible(
                mostrar
        );

        filaHabitaculo.setManaged(
                mostrar
        );

        if (!mostrar) {

            comboHabitaculo.setValue(null);
        }
    }


    // =========================
    // CARGAR ANIMALES
    // =========================

    private void cargarAnimales() {

        ObservableList<Animal> lista =
                FXCollections.observableArrayList(
                        animalDAO.listar()
                );

        comboAnimal.setItems(
                lista
        );
    }


    // =========================
    // CARGAR HABITÁCULOS
    // =========================

    private void cargarHabitaculos() {

        ObservableList<Habitaculo> lista =
                FXCollections.observableArrayList(
                        habitaculoDAO.listar()
                );

        comboHabitaculo.setItems(
                lista
        );
    }


    // =========================
    // CARGAR PLANTEL
    // =========================

    private void cargarPlantel() {

        ObservableList<PlantelPermanente> lista =
                FXCollections.observableArrayList(
                        plantelDAO.listar()
                );

        tabla.setItems(
                lista
        );
    }


    // =========================
    // LIMPIAR CAMPOS
    // =========================

    private void limpiarCampos() {

        comboAnimal.setValue(null);

        comboHabitaculo.setValue(null);

        txtCantidad.clear();

        txtFecha.clear();

        comboUbicacion.setValue(null);

        comboEstado.setValue("Activo");

        txtObservaciones.clear();

        tabla.getSelectionModel()
                .clearSelection();
    }
}