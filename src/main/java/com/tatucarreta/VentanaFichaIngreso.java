package com.tatucarreta;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VentanaFichaIngreso {

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final DetalleIngresoDAO detalleDAO = new DetalleIngresoDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final EspecieDAO especieDAO = new EspecieDAO();

    public ScrollPane crearContenido(int idIngreso, Runnable volver) {

        Ingreso ingreso = buscarIngreso(idIngreso);

        if (ingreso == null) {

            Label mensaje = new Label("No se encontró el ingreso seleccionado.");
            mensaje.setStyle(
                    "-fx-font-size: 18px;" +
                    "-fx-text-fill: #355C48;" +
                    "-fx-font-weight: bold;"
            );

            VBox contenido = new VBox(mensaje);
            contenido.setAlignment(Pos.CENTER);
            contenido.setPadding(new Insets(40));

            ScrollPane scroll = new ScrollPane(contenido);
            scroll.setFitToWidth(true);

            return scroll;
        }

        // =========================================
        // ENCABEZADO
        // =========================================

        Label breadcrumb = new Label(
                "Inicio / Ingresos / Ficha del ingreso"
        );

        breadcrumb.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #718078;"
        );

        Label titulo = new Label(
                "Ficha del Ingreso"
        );

        titulo.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        Label subtitulo = new Label(
                "Información completa del acta y de los animales ingresados"
        );

        subtitulo.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #64736B;"
        );

        VBox encabezado = new VBox(
                5,
                breadcrumb,
                titulo,
                subtitulo
        );

        // =========================================
        // IDENTIFICACIÓN DEL INGRESO
        // =========================================

        VBox numeroActa = crearValorDestacado(
                "ACTA N°",
                valor(ingreso.getNumeroActa())
        );

        VBox fecha = crearValorDestacado(
                "FECHA DE INGRESO",
                valor(ingreso.getFechaIngreso())
        );

        HBox resumen = new HBox(
                15,
                numeroActa,
                fecha
        );

        // =========================================
        // DATOS GENERALES
        // =========================================

        GridPane datosGenerales = crearGrid();

        agregarDato(
                datosGenerales,
                "Organismo de procedencia",
                ingreso.getOrganismoProcedencia(),
                0,
                0
        );

        agregarDato(
                datosGenerales,
                "Responsable de entrega",
                ingreso.getResponsableEntrega(),
                1,
                0
        );

        agregarDato(
                datosGenerales,
                "Procedencia",
                ingreso.getProcedencia(),
                0,
                1
        );

        agregarDato(
                datosGenerales,
                "Motivo de ingreso",
                ingreso.getMotivoIngreso(),
                1,
                1
        );

        VBox tarjetaDatos = crearTarjeta(
                crearTitulo("Datos generales"),
                datosGenerales
        );

        // =========================================
        // DOCUMENTACIÓN
        // =========================================

        Label documentacion = crearTextoGrande(
                valor(ingreso.getDocumentacion())
        );

        Label observaciones = crearTextoGrande(
                valor(ingreso.getObservaciones())
        );

        VBox tarjetaDocumentacion = crearTarjeta(
                crearTitulo("Documentación"),
                documentacion
        );

        VBox tarjetaObservaciones = crearTarjeta(
                crearTitulo("Observaciones generales"),
                observaciones
        );

        // =========================================
        // ANIMALES DEL INGRESO
        // =========================================

        TableView<DetalleIngreso> tabla = new TableView<>();

        TableColumn<DetalleIngreso, String> columnaAnimal =
                new TableColumn<>("Animal");

        columnaAnimal.setCellValueFactory(
                celda -> {

                    Animal animal =
                            buscarAnimal(celda.getValue().getIdAnimal());

                    String nombre = animal != null
                            ? animal.getNombreVulgar()
                            : "Sin nombre";

                    return new javafx.beans.property.SimpleStringProperty(
                            nombre
                    );
                }
        );

        TableColumn<DetalleIngreso, String> columnaEspecie =
                new TableColumn<>("Especie");

        columnaEspecie.setCellValueFactory(
                celda -> {

                    Animal animal =
                            buscarAnimal(celda.getValue().getIdAnimal());

                    if (animal == null) {
                        return new javafx.beans.property.SimpleStringProperty(
                                "Sin especie"
                        );
                    }

                    Especie especie =
                            buscarEspecie(animal.getIdEspecie());

                    String nombre = especie != null
                            ? especie.getNombre()
                            : "Sin especie";

                    return new javafx.beans.property.SimpleStringProperty(
                            nombre
                    );
                }
        );

        TableColumn<DetalleIngreso, Integer> columnaCantidad =
                new TableColumn<>("Cantidad");

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );

        TableColumn<DetalleIngreso, String> columnaSexo =
                new TableColumn<>("Sexo");

        columnaSexo.setCellValueFactory(
                new PropertyValueFactory<>("sexo")
        );

        TableColumn<DetalleIngreso, String> columnaEdad =
                new TableColumn<>("Edad");

        columnaEdad.setCellValueFactory(
                new PropertyValueFactory<>("edad")
        );

        TableColumn<DetalleIngreso, Double> columnaPeso =
                new TableColumn<>("Peso");

        columnaPeso.setCellValueFactory(
                new PropertyValueFactory<>("peso")
        );

        TableColumn<DetalleIngreso, String> columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estadoIngreso")
        );

        tabla.getColumns().addAll(
                columnaAnimal,
                columnaEspecie,
                columnaCantidad,
                columnaSexo,
                columnaEdad,
                columnaPeso,
                columnaEstado
        );

        List<DetalleIngreso> detalles =
                detalleDAO.listarPorIngreso(idIngreso);

        tabla.getItems().setAll(detalles);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPrefHeight(260);

        VBox tarjetaAnimales = crearTarjeta(
                crearTitulo(
                        "Animales incluidos en el ingreso"
                ),
                tabla
        );

        // =========================================
        // OBSERVACIONES POR ANIMAL
        // =========================================

        VBox observacionesAnimales =
                new VBox(12);

        if (detalles.isEmpty()) {

            Label sinDatos =
                    new Label(
                            "No hay animales registrados para este ingreso."
                    );

            sinDatos.setStyle(
                    "-fx-text-fill: #718078;" +
                    "-fx-font-size: 14px;"
            );

            observacionesAnimales.getChildren().add(
                    sinDatos
            );

        } else {

            for (DetalleIngreso detalle : detalles) {

                Animal animal =
                        buscarAnimal(
                                detalle.getIdAnimal()
                        );

                String nombreAnimal =
                        animal != null
                                ? animal.getNombreVulgar()
                                : "Animal";

                Label tituloAnimal =
                        new Label(
                                nombreAnimal
                        );

                tituloAnimal.setStyle(
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #23452C;"
                );

                Label texto =
                        crearTextoGrande(
                                valor(
                                        detalle.getObservaciones()
                                )
                        );

                VBox bloque =
                        new VBox(
                                5,
                                tituloAnimal,
                                texto
                        );

                bloque.setPadding(
                        new Insets(12)
                );

                bloque.setStyle(
                        "-fx-background-color: #F4F7F3;" +
                        "-fx-background-radius: 10;"
                );

                observacionesAnimales.getChildren().add(
                        bloque
                );
            }
        }

        VBox tarjetaObservacionesAnimales =
                crearTarjeta(
                        crearTitulo(
                                "Observaciones de los animales"
                        ),
                        observacionesAnimales
                );

        // =========================================
        // BOTÓN VOLVER
        // =========================================

        Button btnVolver =
                new Button("← VOLVER A INGRESOS");

        btnVolver.setPrefHeight(42);

        btnVolver.setStyle(
                "-fx-background-color: #23452C;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9;" +
                "-fx-padding: 0 22 0 22;"
        );

        btnVolver.setOnAction(
                e -> {
                    if (volver != null) {
                        volver.run();
                    }
                }
        );

        HBox contenedorBoton =
                new HBox(btnVolver);

        contenedorBoton.setAlignment(
                Pos.CENTER_RIGHT
        );

        // =========================================
        // CONTENIDO
        // =========================================

        VBox contenido =
                new VBox(
                        22,
                        encabezado,
                        resumen,
                        tarjetaDatos,
                        tarjetaDocumentacion,
                        tarjetaObservaciones,
                        tarjetaAnimales,
                        tarjetaObservacionesAnimales,
                        contenedorBoton
                );

        contenido.setPadding(
                new Insets(
                        30,
                        40,
                        40,
                        40
                )
        );

        contenido.setStyle(
                "-fx-background-color: #EEF4EF;"
        );

        ScrollPane scroll =
                new ScrollPane(contenido);

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background-color: #EEF4EF;" +
                "-fx-background: #EEF4EF;"
        );

        return scroll;
    }

    // =========================================
    // BUSCAR INGRESO
    // =========================================

    private Ingreso buscarIngreso(int idIngreso) {

        for (Ingreso ingreso : ingresoDAO.listar()) {

            if (ingreso.getIdIngreso() == idIngreso) {
                return ingreso;
            }
        }

        return null;
    }

    // =========================================
    // BUSCAR ANIMAL
    // =========================================

    private Animal buscarAnimal(int idAnimal) {

        for (Animal animal : animalDAO.listar()) {

            if (animal.getIdAnimal() == idAnimal) {
                return animal;
            }
        }

        return null;
    }

    // =========================================
    // BUSCAR ESPECIE
    // =========================================

    private Especie buscarEspecie(int idEspecie) {

        for (Especie especie : especieDAO.listar()) {

            if (especie.getIdEspecie() == idEspecie) {
                return especie;
            }
        }

        return null;
    }

    // =========================================
    // GRID
    // =========================================

    private GridPane crearGrid() {

        GridPane grid =
                new GridPane();

        grid.setHgap(30);
        grid.setVgap(18);

        return grid;
    }

    // =========================================
    // AGREGAR DATO
    // =========================================

    private void agregarDato(
            GridPane grid,
            String titulo,
            String valor,
            int columna,
            int fila) {

        Label lblTitulo =
                new Label(titulo);

        lblTitulo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #718078;"
        );

        Label lblValor =
                new Label(valor(valor));

        lblValor.setWrapText(true);

        lblValor.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #263B31;"
        );

        VBox bloque =
                new VBox(
                        4,
                        lblTitulo,
                        lblValor
                );

        grid.add(
                bloque,
                columna,
                fila
        );
    }

    // =========================================
    // VALOR DESTACADO
    // =========================================

    private VBox crearValorDestacado(
            String titulo,
            String valor) {

        Label lblTitulo =
                new Label(titulo);

        lblTitulo.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #718078;"
        );

        Label lblValor =
                new Label(valor);

        lblValor.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        VBox tarjeta =
                new VBox(
                        5,
                        lblTitulo,
                        lblValor
                );

        tarjeta.setPadding(
                new Insets(15, 25, 15, 25)
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #D6E0D8;" +
                "-fx-border-radius: 12;"
        );

        return tarjeta;
    }

    // =========================================
    // TÍTULO
    // =========================================

    private Label crearTitulo(String texto) {

        Label titulo =
                new Label(texto);

        titulo.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #23452C;"
        );

        return titulo;
    }

    // =========================================
    // TARJETA
    // =========================================

    private VBox crearTarjeta(
            javafx.scene.Node... nodos) {

        VBox tarjeta =
                new VBox(
                        15,
                        nodos
                );

        tarjeta.setPadding(
                new Insets(22)
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #D6E0D8;" +
                "-fx-border-radius: 16;"
        );

        return tarjeta;
    }

    // =========================================
    // TEXTO
    // =========================================

    private Label crearTextoGrande(String texto) {

        Label label =
                new Label(texto);

        label.setWrapText(true);

        label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #46574E;"
        );

        return label;
    }

    // =========================================
    // VALOR
    // =========================================

    private String valor(String texto) {

        if (texto == null || texto.isBlank()) {
            return "Sin información";
        }

        return texto;
    }
}